package main;

import astFull.Package;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import failure.Fail;
import files.Pos;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.IoErr;
import utils.ResolveResource;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public interface LogicMain<Exe> {
  InputOutput io();
  HashSet<String> cachedPkg();
  CompilerFrontEnd.Verbosity verbosity();

  default astFull.Program parse() {
    var cache = load(io().cachedFiles());
    cachedPkg().addAll(cache.keySet());
    var app = load(io().inputFiles());
    var standardLibOverriden = app.entrySet().stream()
      .filter(s->s.getKey().startsWith("base.") || s.getKey().equals("base") || s.getKey().startsWith("rt.") || s.getKey().equals("rt"))
      .flatMap(s->s.getValue().stream()
        .flatMap(pkg->pkg.ps().stream())
        .map(path->new Pos(path.toUri(), 0, 0))
        .map(pos->new Fail.Conflict(pos, s.getKey()))
      ).toList();
    if (!standardLibOverriden.isEmpty()) {
      throw Fail.specialPackageConflict(standardLibOverriden);
    }
    var packages = new HashMap<>(app);
    if(!cachedPkg().contains("base")){
      packages.putAll(load(io().baseFiles()));
    }
    packages.putAll(cache);//Purposely overriding any app also in cache
    // TODO: ^ should we show a warning if we do this?
    return Parser.parseAll(packages, new TypeSystemFeatures());
  }
  default void wellFormednessFull(astFull.Program fullProgram){
    new WellFormednessFullShortCircuitVisitor()
      .visitProgram(fullProgram)
      .ifPresent(err->{ throw err; });
  }
  default ast.Program inference(astFull.Program fullProgram){
    return InferBodies.inferAll(fullProgram);
  }
  default void wellFormednessCore(ast.Program program){
    new WellFormednessShortCircuitVisitor(program)
            .visitProgram(program)
            .ifPresent(err->{ throw err; });
  }
  default ConcurrentHashMap<Long, EMethTypeSystem.TsT> typeSystem(ast.Program program){
    var acc= new ConcurrentHashMap<Long, EMethTypeSystem.TsT>();
    program.typeCheck(acc);
    return acc;
  }
  MIR.Program lower(ast.Program program, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls);
  void cachePackageTypes(MIR.Program program);
  Exe codeGeneration(MIR.Program program);
  ProcessBuilder execution(MIR.Program program, Exe exe, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls);
  default ProcessBuilder run(){
    var fullProgram= parse();
    wellFormednessFull(fullProgram);
    var program = inference(fullProgram);
    wellFormednessCore(program);
    var resolvedCalls = typeSystem(program);
    var mir = lower(program,resolvedCalls);
    var code = codeGeneration(mir);
    var process = execution(mir,code,resolvedCalls);
    cachePackageTypes(mir);
    return process;
  }

  default List<Parser> loadFiles(Path root) {
    return IoErr.of(()->{try(var fs = Files.walk(root)) {
      return fs
        .filter(Files::isRegularFile)
        .map(p->new Parser(p, ResolveResource.read(p)))
        .toList();
    }});
  }
  default Map<String, List<Package>> load(List<Parser> files) {
    return files.stream()
      .map(p->p.parseFile(CompileError::err))
      .collect(Collectors.groupingBy(Package::name));
  }
}