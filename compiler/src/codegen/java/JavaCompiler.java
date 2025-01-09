package codegen.java;

import main.CompilerFrontEnd.Verbosity;
import main.InputOutput;
import utils.Box;
import utils.Bug;
import utils.IoErr;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.util.List;

public record JavaCompiler(Verbosity verbosity, InputOutput io){
  public void compile(List<JavaFile> files) {
    assert !files.isEmpty();
    var compiler = ToolProvider.getSystemJavaCompiler();
    assert compiler != null
      :"No Java compiler could be found. Please use a JDK >= 10";
    IoErr.of(()->Files.createDirectories(io.output()));

    CopyRuntimeLibs.of(io.output().toAbsolutePath());
    
    var options = List.of(
      "-d", io.output().toAbsolutePath().toString(),
      "-cp", io.cachedBase().toAbsolutePath().toString(),
      "-Xdiags:verbose",
      "-Xlint:preview",
      "--enable-preview",
      "--release", "23"
      );
    var errors = new Box<Diagnostic<?>>(null);
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      files
      ).call();
  
    if (success){ return; }
    var diagnostic = errors.get();
    if (diagnostic == null) {
      throw Bug.of("Java compilation failed.");
    }
    throw Bug.of("Java compilation failed:\n"+ diagnostic);
  }
}