package codegen.java;

import main.CompilerFrontEnd;
import utils.Box;
import utils.Bug;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ImmJavaProgram extends JavaFile {
  public ImmJavaProgram(String code) {
    super(JavaCompiler.MAIN_CLASS_NAME,code);
  }
  public ImmJavaProgram(String topLevelClassName, String code) {
    super(topLevelClassName, code);
  }

  public static Path compile(CompilerFrontEnd.Verbosity verbosity, JavaFile... files) {
    assert files.length > 0;
    assert Arrays.stream(files).anyMatch(f->f.isNameCompatible(JavaCompiler.MAIN_CLASS_NAME, Kind.SOURCE));
    var compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new RuntimeException("No Java compiler could be found. Please install a JDK >= 10.");
    }

    var workingDir = Paths.get(System.getProperty("java.io.tmpdir"), "fearOut"+System.currentTimeMillis());
    if (!workingDir.toFile().mkdir()) {
      throw Bug.of("Could not create a working directory for building the program in: " + System.getProperty("java.io.tmpdir"));
    }

    if (verbosity.printCodegen()) {
      System.err.println("Java codegen working dir: "+workingDir.toAbsolutePath());
    }

    var options = List.of(
      "-d",
      workingDir.toString(),
      "-Xdiags:verbose"
    );

    var errors = new Box<Diagnostic<?>>(null);

    var codegenUnits = Arrays.stream(files);
    boolean success = compiler.getTask(
      null,
      null,
      errors::set,
      options,
      null,
      (Iterable<JavaFile>) codegenUnits::iterator
    ).call();

    if (!success) {
      var diagnostic = errors.get();
      if (diagnostic == null) {
        throw Bug.of("ICE: Java compilation failed.");
      }
      throw Bug.of("ICE: Java compilation failed:\n"+ diagnostic);
    }

    return workingDir.resolve("FProgram.class");
  }
}
