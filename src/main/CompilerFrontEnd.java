package main;

import ast.Program;
import astFull.Package;
import codegen.MIRInjectionVisitor;
import codegen.java.ImmJavaCodegen;
import codegen.java.JavaCodegen;
import codegen.java.JavaProgram;
import codegen.md.MarkdownDocgen;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import parser.Parser;
import program.inference.InferBodies;
import program.typesystem.XBs;
import utils.Box;
import utils.Bug;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.zalando.fauxpas.FauxPas.throwingFunction;

public record CompilerFrontEnd(BaseVariant bv, Verbosity v) {
  record Verbosity(boolean showInternalStackTraces, boolean printCodegen, ProgressVerbosity progress){
    Verbosity showInternalStackTraces(boolean showInternalStackTraces) { return new Verbosity(showInternalStackTraces, printCodegen, progress); }
    Verbosity printCodegen(boolean printCodegen) { return new Verbosity(showInternalStackTraces, printCodegen, progress); }
  }
  enum ProgressVerbosity {
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
    // TODO: check valid package name
    try {
      var dir = Path.of(name);
      Files.createDirectory(dir);
      Files.writeString(dir.resolve("pkg.fear"), "package "+name+"\n"+regenerateAliases()+"\n");
      Files.writeString(dir.resolve("lib.fear"), "package "+name+"\nGreeting:{ .get: Str -> \"Hello, World!\" }\n");
      Files.writeString(dir.resolve("main.fear"), "package "+name+"\n"+"""
        App:Main{ s -> s
          .use[IO] io = FIO
          .block
          .var[Str] greeting = { Greeting.get }
          .return{ io.println(greeting) }
          }
        """.stripIndent());
    } catch (IOException err) {
      System.err.println("Error creating package structure: "+ err);
      System.exit(1);
    }
  }

  void generateDocs(String[] files) throws IOException {
    if (files == null) { files = new String[0]; }
    var p = compile(files);
    var docgen = new MarkdownDocgen(p);
    throw Bug.todo();
//    var docs = docgen.visitProgram();
//    Path root = Path.of("docs");
//    try { Files.createDirectory(root); } catch (FileAlreadyExistsException ignored) {}
//    for (var doc : docs) {
//      Files.writeString(root.resolve(doc.fileName()), doc.markdown());
//    }
  }

  void run(String entryPoint, String[] files, List<String> cliArgs) {
    var entry = new Id.DecId(entryPoint, 0);
    var p = compile(files);

    var main = p.of(Magic.Main).toIT();
    var isEntryValid = p.isSubType(XBs.empty(), new ast.T(Mdf.mdf, p.of(entry).toIT()), new ast.T(Mdf.mdf, main));
    if (!isEntryValid) { throw Fail.invalidEntryPoint(entry, main); }

    v.progress.printTask("Running code generation \uD83C\uDFED");
    var java = toJava(entry, p);
    var classFile = java.compile();
    v.progress.printTask("Code generated \uD83E\uDD73");

    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java").toAbsolutePath();
    String[] command = Stream.concat(
      Stream.of(jrePath.toString(), classFile.getFileName().toString().split("\\.class")[0]),
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
    compile(files);
  }

  Program compile(String[] files) {
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

    v.progress.printTask("Parsing \uD83D\uDC40");
    var p = Parser.parseAll(ps);
    v.progress.printTask("Parsing complete \uD83E\uDD73");
    v.progress.printTask("Checking that the program is well formed \uD83D\uDD0E");
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    v.progress.printTask("Well formedness checks complete \uD83E\uDD73");
    v.progress.printTask("Inferring method signatures \uD83D\uDD75️");
    var inferredSigs = p.inferSignaturesToCore();
    v.progress.printTask("Method signatures inferred \uD83E\uDD73");
    v.progress.printTask("Inferring method bodies \uD83D\uDD75️");
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    v.progress.printTask("Method bodies inferred \uD83E\uDD73");
    v.progress.printTask("Checking that the program is still well formed \uD83D\uDD0E");
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    v.progress.printTask("Well formedness checks complete \uD83E\uDD73");
    v.progress.printTask("Checking types \uD83E\uDD14");
    inferred.typeCheck();
    v.progress.printTask("Types look all good \uD83E\uDD73");
    return inferred;
  }
  private JavaProgram toJava(Id.DecId entry, Program p) {
    var mirVisitor = new MIRInjectionVisitor(p);
    var mir = mirVisitor.visitProgram();
    var codegen = switch (bv) {
      case Std -> new JavaCodegen(mirVisitor.getProgram());
      case Imm -> new ImmJavaCodegen(mirVisitor.getProgram());
    };
    var src = codegen.visitProgram(mir.pkgs(), entry);
    if (v.printCodegen) {
      System.out.println(src);
    }
    return new JavaProgram(src);
  }

  String regenerateAliases() {
    return parseBase().values().stream()
      .flatMap(Collection::stream)
      .flatMap(pkg->pkg.shallowParse().stream())
      .map(astFull.T.Dec::name)
      .filter(dec->!dec.shortName().startsWith("_"))
      .map(dec->"alias "+dec.name()+" as "+dec.shortName()+",")
      .distinct()
      .collect(Collectors.joining("\n"));
  }

  Map<String, List<Package>> parseBase() {
    var load = throwingFunction(CompilerFrontEnd::load);
    Map<String, List<Package>> ps; try { ps =
      switch (bv) {
        case Std -> {
          var res = baseLib.get();
          if (res == null) { res = resolveResource("/base", load); baseLib.set(res); }
          yield res;
        }
        case Imm -> {
          var res = immBaseLib.get();
          if (res == null) { res = resolveResource("/immBase", load); immBaseLib.set(res); }
          yield res;
        }
      };
    } catch (URISyntaxException | IOException e) {
      throw Bug.of(e);
    }
    return ps;
  }

  static <R> R resolveResource(String root, Function<Path, R> f) throws IOException, URISyntaxException {
    var top = requireNonNull(CompilerFrontEnd.class.getResource(root)).toURI();
    if (!top.getScheme().equals("jar") && !top.getScheme().equals("resource")) {
      return f.apply(Path.of(top));
    }
    try(var fs = FileSystems.newFileSystem(top, Map.of())) {
      return f.apply(fs.getPath(root));
    }
  }

  static Map<String, List<Package>> load(Path root) throws IOException {
    try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(throwingFunction(path->new Parser(path, read(path)).parseFile(CompileError::err)))
        .collect(Collectors.groupingBy(Package::name));
    }
  }
  static String read(Path p) throws IOException {
    try(var br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      return br.lines().collect(Collectors.joining("\n"));
    }
  }
}
