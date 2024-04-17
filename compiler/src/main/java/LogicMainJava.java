package main.java;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import ast.Program;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import codegen.java.JavaCompiler;
import codegen.java.JavaProgram;
import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import main.LogicMain;
import program.typesystem.EMethTypeSystem;
import utils.IoErr;

public interface LogicMainJava extends LogicMain<JavaProgram>{
  default void cachePackageTypes(MIR.Program program) {
    HDCache.cachePackageTypes(this, program);
  }

  @Override default MIR.Program lower(Program program, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls) {
    return new MIRInjectionVisitor(cachedPkg(),program, resolvedCalls).visitProgram();
  }
  default JavaProgram codeGeneration(
          MIR.Program mir
  ){
    var c= new JavaCompiler(verbosity(),io());
    var res= new JavaProgram(this,mir);

//    var tmp = IoErr.of(()->Files.createTempDirectory("fgen"));
//    res.writeJavaFiles(tmp);
//    System.out.println("saved to "+tmp);

    c.compile(res.files());
    return res;
  }
  default ProcessBuilder execution(
          MIR.Program program,
          JavaProgram exe,
          ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls
  ){
    return MakeJavaProcess.of(io());
  }
  static LogicMainJava of(
		  InputOutput io, Verbosity verbosity){
    var cachedPkg=new HashSet<String>();
    return new LogicMainJava(){
      public InputOutput io(){ return io; }
      public HashSet<String> cachedPkg(){ return cachedPkg; }
      public Verbosity verbosity(){ return verbosity; }
    };
  }
}
class MakeJavaProcess{
  static public ProcessBuilder of(InputOutput io) {
    var command= makeJavaCommand(io);
    //System.out.println(List.of(command));
    return new ProcessBuilder(command);
  }
  static private String[] makeJavaCommand(InputOutput io) {
    Path fearlessMainPath = io.cachedBase()
            .resolve("base/FearlessMain.class");
    var jrePath = Path.of(System.getProperty("java.home"), "bin", "java")
            .toAbsolutePath().toString();
    String entryPoint = "base."
            + fearlessMainPath.getFileName().toString().split("\\.class")[0];
    String classpath = io.output().toAbsolutePath()
            + File.pathSeparator
            + io.cachedBase().toAbsolutePath();
    var baseCommand = Stream.of(
            jrePath, "-cp", classpath, entryPoint, io.entry());
    return Stream.concat(baseCommand,
                    io.commandLineArguments().stream())
            .toArray(String[]::new);
  }
}