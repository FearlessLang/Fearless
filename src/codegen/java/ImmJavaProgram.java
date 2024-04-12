package codegen.java;

import main.CompilerFrontEnd;
import rt.ResolveResource;
import utils.Box;
import utils.Bug;
import utils.DeleteOnExit;
import utils.IoErr;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ImmJavaProgram extends JavaProgram {
  public ImmJavaProgram(String code) {
    super(code);
  }
  public ImmJavaProgram(String topLevelClassName, String code) {
    super(topLevelClassName, code);
  }

  public static Path compile(CompilerFrontEnd.Verbosity verbosity, JavaProgram... files) {
    assert Arrays.stream(files).anyMatch(f->f.isNameCompatible(MAIN_CLASS_NAME, Kind.SOURCE));
    var compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new RuntimeException("No Java compiler could be found. Please install a JDK >= 10.");
    }


    var workingDir = IoErr.of(()->Files.createTempDirectory("fearOut"));
    if (verbosity.printCodegen()) {
      System.err.println("Java codegen working dir: "+workingDir.toAbsolutePath());
    }
    var options = List.of(
      "-d",
      workingDir.toString(),
      "-Xdiags:verbose"
    );

    var errors = new Box<Diagnostic<?>>(null);

    var runtimeFiles = Stream.of(
      "Str",
      "ResolveResource",
      "NativeRuntime",
      "FearlessError"
    ).map(name -> new JavaProgram(name, ResolveResource.getAndRead("/rt/"+name+".java")));
    var userFiles = Arrays.stream(files);
    var codegenUnits = Stream.concat(userFiles, runtimeFiles);

    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      (Iterable<JavaProgram>) codegenUnits::iterator
    ).call();

    if (!success) {
      var diagnostic = errors.get();
      if (diagnostic == null) {
        throw Bug.of("ICE: Java compilation failed.");
      }
      throw Bug.of("ICE: Java compilation failed:\n"+ diagnostic);
    }

    copyRuntimeLibs(workingDir);
    if (!verbosity.printCodegen()) {
      DeleteOnExit.of(workingDir);
    }
    return workingDir.resolve("FProgram.class");
  }
}
