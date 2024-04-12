package main.java;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import codegen.MIRInjectionVisitor;
import codegen.java.JavaCodegen;
import codegen.java.JavaProgram;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import main.LogicMain;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;

public interface LogicMainJava extends LogicMain<JavaProgram>{
  List<String> commandLineArguments();
  String entry();
  default String[] makeJavaCommand(Path pathToMain) {
    var jrePath= Path.of(System.getProperty("java.home"), "bin", "java")
      .toAbsolutePath();
    String entryPoint= "userCode." 
      + pathToMain.getFileName().toString().split("\\.class")[0];
    var baseCommand = Stream.of(jrePath.toString(),entryPoint);
    return Stream.concat(baseCommand, commandLineArguments().stream())
      .toArray(String[]::new);
  }
  default JavaProgram codeGeneration(
      ast.Program program,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    var mir = new MIRInjectionVisitor(program, resolvedCalls).visitProgram();
    var codegen= new JavaCodegen(mir);
    var main = program.of(Magic.Main).toIT();
    var entry= new Id.DecId(entry(),0);
    var isEntryValid = program
      .isSubType(XBs.empty(), new ast.T(Mdf.mdf, program.of(entry).toIT()), new ast.T(Mdf.mdf,main));
    if (!isEntryValid) { throw Fail.invalidEntryPoint(entry, main); }
    var src = codegen.visitProgram(entry);
    return new JavaProgram(src);
  }
  default JavaProgram mainCodeGeneration(
      ast.Program program,
      JavaProgram exe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls 
      ) {
    return exe;
    }

  default Process execution(
      ast.Program program,
      JavaProgram exe,
      JavaProgram mainExe,
      ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
      ){
    Path pathToMain= JavaProgram.compile(verbosity(), mainExe);
    var pb = new ProcessBuilder(makeJavaCommand(pathToMain));
    pb.directory(pathToMain.getParent().toFile());
    this.preStart(pb);
    Process proc; try { proc = pb.start();}
    catch (IOException e) { throw new UncheckedIOException(e); }
    return proc;
  }

  @Override default void preStart(ProcessBuilder pb) {
    pb.inheritIO();
  }
}
