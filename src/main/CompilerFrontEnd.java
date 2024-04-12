package main;

import ast.Program;
import astFull.Package;
import codegen.MIRInjectionVisitor;
import codegen.html.HtmlDocgen;
import codegen.java.ImmJavaCodegen;
import codegen.java.ImmJavaProgram;
import codegen.java.JavaCodegen;
import codegen.java.JavaProgram;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.ThrowingFunction;
import utils.Box;
import utils.Bug;
import rt.ResolveResource;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: It might be good to ban any files from having a "package base*" that are not in the base directory.

public record CompilerFrontEnd(BaseVariant bv, Verbosity v, TypeSystemFeatures tsf) {
  public record Verbosity(boolean showInternalStackTraces, boolean printCodegen, ProgressVerbosity progress){
    Verbosity showInternalStackTraces(boolean showInternalStackTraces) { return new Verbosity(showInternalStackTraces, printCodegen, progress); }
    Verbosity printCodegen(boolean printCodegen) { return new Verbosity(showInternalStackTraces, printCodegen, progress); }
  }
  public enum ProgressVerbosity {
    None, Tasks, Full;
    void printTask(String msg) {
      if (this != Tasks && this != Full) { return; }
      System.err.println(msg);
    }
    void printStep(String msg) {
      if (this != Full) { return; }
      System.err.println(msg);
    }
  }
  enum BaseVariant { Std, Imm }
  static Box<Map<String, List<Package>>> immBaseLib = new Box<>(null);
  static Box<Map<String, List<Package>>> baseLib = new Box<>(null);

  void newPkg(String name) {
    if (!Id.validDecName(name+".Check")) {
      System.err.println("Error creating package structure: Invalid package name.");
      System.exit(1);
    }

    try {
      var dir = Path.of(name);
      Files.createDirectory(dir);
      Files.writeString(dir.resolve("pkg.fear"), "package " + name + "\n" + regenerateAliases() + "\n");
      Files.writeString(dir.resolve("lib.fear"), "package " + name + "\nGreeting:{ .get: Str -> \"Hello, World!\" }\n");
      Files.writeString(dir.resolve("main.fear"), "package " + name + "\n"+"""
        App:Main{ sys -> Block#
          .let io = {FIO#sys}
          .let[Str] greeting = {Greeting.get}
          .return {io.println(greeting)}
          }
        """.stripIndent());
    } catch (FileAlreadyExistsException err) {
      System.err.println("Error creating package structure: Files already exist at that path.");
      System.exit(1);
    } catch (IOException err) {
      System.err.println("Error creating package structure: "+ err);
      System.exit(1);
    }
  }

  void generateDocs(String[] files) throws IOException, URISyntaxException {
    if (files == null) { files = new String[0]; }
    var p = generateProgram(files, new ConcurrentHashMap<>());
    var docgen = new HtmlDocgen(p);
    var docs = docgen.visitProgram();
    Path root = Path.of("docs");
    try { Files.createDirectory(root); } catch (FileAlreadyExistsException ignored) {}
    Files.writeString(root.resolve(docs.fileName()), docs.index());
    var styleCss = ResolveResource.getAndRead("/style.css");
    Files.writeString(root.resolve("style.css"), styleCss);
    var highlightingJs = ResolveResource.getAndRead("/highlighting.js");
    Files.writeString(root.resolve("highlighting.js"), highlightingJs);
    for (var pkg : docs.docs()) {
      var links = pkg.links();
      Files.writeString(root.resolve(pkg.fileName()), pkg.index(links));
      for (var trait : pkg.traits()) {
        Files.writeString(root.resolve(trait.fileName()), trait.html(links));
      }
    }
  }

  void run(String entryPoint, String[] files, List<String> cliArgs) {
    var entry = new Id.DecId(entryPoint, 0);
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    var p = compile(files, resolvedCalls);

    var main = p.of(Magic.Main).toIT();
    var isEntryValid = p.isSubType(XBs.empty(), new ast.T(Mdf.mdf, p.of(entry).toIT()), new ast.T(Mdf.mdf, main));
    if (!isEntryValid) { throw Fail.invalidEntryPoint(entry, main); }

    var timer = new Timer();
    v.progress.printTask("Running code generation \uD83C\uDFED");
    var mainClass = toJava(entry, p, resolvedCalls);
    v.progress.printTask("Code generated \uD83E\uDD73 ("+timer.duration()+"ms)");
    v.progress.printStep("Executing backend compiler \uD83C\uDFED");
    var classFile = switch (bv) {
      case Std -> JavaProgram.compile(v, mainClass);
      case Imm -> ImmJavaProgram.compile(v, mainClass);
    };
    v.progress.printStep("Done executing backend compiler \uD83E\uDD73 ("+timer.duration()+"ms)");

    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath();
    String[] command = Stream.concat(
      Stream.of(jrePath.toString(), "userCode."+classFile.getFileName().toString().split("\\.class")[0]),
      cliArgs.stream()
    ).toArray(String[]::new);
    var pb = new ProcessBuilder(command);
    pb.directory(classFile.getParent().toFile());
    pb.inheritIO();
    Process proc; try { proc = pb.start();
    } catch (IOException e) {
      throw Bug.of(e);
    }

    proc.onExit().join();
    System.exit(proc.exitValue());
  }

  void check(String[] files) {
    compile(files, new ConcurrentHashMap<>());
  }

  Program generateProgram(String[] files, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls) {
    var base = parseBase();
    Map<String, List<Package>> ps = new HashMap<>(base);
    Arrays.stream(files)
      .map(Path::of)
      .map(path->{
        try { return CompilerFrontEnd.load(path); }
        catch (FileSystemException err) { throw Fail.fsError(err); }
        catch (IOException err) { throw Fail.ioError(err); }
      })
      .flatMap(pkgs->pkgs.entrySet().stream())
      .forEach(pkg->ps.compute(
        pkg.getKey(),
        (name, ps_)->Optional.ofNullable(ps_)
          .map(ps__->Stream.concat(ps__.stream(), pkg.getValue().stream()).distinct().toList())
          .orElse(pkg.getValue())
      ));

    var timer = new Timer();
    v.progress.printTask("Parsing \uD83D\uDC40");
    var p = Parser.parseAll(ps, tsf);
    v.progress.printTask("Parsing complete \uD83E\uDD73 ("+timer.duration()+"ms)");
    v.progress.printTask("Checking that the program is well formed \uD83D\uDD0E");
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    v.progress.printTask("Well formedness checks complete \uD83E\uDD73 ("+timer.duration()+"ms)");
    v.progress.printTask("Inferring types \uD83D\uDD75️");
    var inferred = InferBodies.inferAll(p);
    v.progress.printTask("Types inferred \uD83E\uDD73 ("+timer.duration()+"ms)");
    v.progress.printTask("Checking that the program is still well formed \uD83D\uDD0E");
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    v.progress.printTask("Well formedness checks complete \uD83E\uDD73 ("+timer.duration()+"ms)");
    return inferred;
  }

  Program compile(String[] files, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls) {
    var inferred = generateProgram(files, resolvedCalls);
    var timer = new Timer();
    v.progress.printTask("Checking types \uD83E\uDD14");
    inferred.typeCheck(resolvedCalls);
    v.progress.printTask("Types look all good \uD83E\uDD73 ("+timer.duration()+"ms)");
    return inferred;
  }
  private JavaProgram toJava(Id.DecId entry, Program p, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls) {
    var mir = new MIRInjectionVisitor(p, resolvedCalls).visitProgram();
    var codegen = switch (bv) {
      case Std -> new JavaCodegen(mir);
      case Imm -> new ImmJavaCodegen(mir);
    };
    var src = codegen.visitProgram(entry);
    if (v.printCodegen) {
      System.out.println(src);
    }
    return new JavaProgram(src);
  }

  String regenerateAliases() {
    var path = switch (bv) {
      case Std -> "/default-aliases.fear";
      case Imm -> "/default-imm-aliases.fear";
    };
    return ResolveResource.getAndRead(path);
  }


  Map<String, List<Package>> parseBase() {
    var load = ThrowingFunction.of(CompilerFrontEnd::load);
    Map<String, List<Package>> ps; try { ps =
      switch (bv) {
        case Std -> {
          var res = baseLib.get();
          if (res == null) { res = load(ResolveResource.of("/base")); baseLib.set(res); }
          yield res;
        }
        case Imm -> {
          var res = immBaseLib.get();
          if (res == null) { res = load(ResolveResource.of("/immBase")); immBaseLib.set(res); }
          yield res;
        }
      };
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return ps;
  }

  static Map<String, List<Package>> load(Path root) throws IOException{
    try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(path->new Parser(path, ResolveResource.read(path)).parseFile(CompileError::err))
        .collect(Collectors.groupingBy(Package::name));
    }
  }
}
