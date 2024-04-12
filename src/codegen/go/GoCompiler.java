package codegen.go;

import main.CompilerFrontEnd;
import org.apache.commons.lang3.SystemUtils;
import utils.Bug;
import rt.ResolveResource;
import utils.DeleteOnExit;
import utils.IoErr;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public record GoCompiler(Unit entry, List<? extends Unit> rt, List<? extends Unit> units, CompilerFrontEnd.Verbosity verbosity) {
  public static List<Unit.Runtime> IMM_RUNTIME_UNITS = List.of(
    new Unit.Runtime("go.mod"),
    new Unit.Runtime("base~rt.go")
  );

  public sealed interface Unit permits GoCodegen.MainFile, Unit.Runtime, PackageCodegen.GoPackage {
    String name();
    String src();
    default void write(Path workingDir) throws IOException {
      Files.writeString(workingDir.resolve(this.name()), this.src());
    }
    record Runtime(String name) implements Unit {
      @Override public String src() {
        return ResolveResource.getAndRead("/rt/" +name);
      }
    }
  }

  public Path compile() throws IOException {
    assert !units.isEmpty();

    var workingDir = IoErr.of(()->Files.createTempDirectory("fearOut"));
    if (verbosity.printCodegen()) {
      System.err.println("Go codegen working dir: "+workingDir.toAbsolutePath());
    }

    this.entry().write(workingDir);
    for (var unit : this.units()) { unit.write(workingDir); }
    for (var unit : this.rt()) { unit.write(workingDir); }

    var compilerPath = ResolveResource.of(GoVersion.path());
    var canExecute = compilerPath.toFile().setExecutable(true);
    if (!canExecute) {
      System.err.println("Warning: Could not make the Go compiler executable");
    }

    try {
      runGoCmd(workingDir, "build", "-o", "fear_out").join();
    } catch (CompletionException err) {
      var cause = err.getCause();
      if (cause instanceof Bug bug) { throw bug; }
      throw Bug.of(cause);
    }

    if (!verbosity.printCodegen()) {
      DeleteOnExit.of(workingDir);
    }

    // TODO: maybe fear_out.exe on windows?
    return workingDir.resolve("fear_out");
  }

  Process goProcess(Path workingDir, String[] args) {
    var compiler = ResolveResource.of(GoVersion.path());
    String[] command = Stream.concat(Stream.of(compiler.toString()), Arrays.stream(args)).toArray(String[]::new);
    var pb = new ProcessBuilder(command).directory(workingDir.toFile());
    var inheritIO = verbosity.progress() == CompilerFrontEnd.ProgressVerbosity.Full;
    try {
      return inheritIO ? pb.inheritIO().start() : pb.start();
    } catch (IOException e) {
      throw Bug.of(e);
    }
  }
  private CompletableFuture<Void> runGoCmd(Path workingDir, String... args) {
    Process proc= goProcess(workingDir,args);
    return proc.onExit().thenAccept(p->{
      var exitValue = proc.exitValue();
      if (exitValue != 0) {
        throw Bug.of("ICE: Go compilation failed");
      }
    });
  }

  interface GoVersion {
    static String path() {
      return "/go-compilers/%s/go/bin/go".formatted(goCompilerVersion());
    }
    private static String goCompilerVersion() {
      var arch = switch (SystemUtils.OS_ARCH) {
        case "x86_64", "amd64" -> "amd64";
        case "aarch64", "arm64" -> "arm64";
        default -> throw new IllegalStateException("Unsupported architecture: "+System.getProperty(SystemUtils.OS_ARCH));
      };
      return "go-"+osName()+"-"+arch;
    }
    private static String osName() {
      if (SystemUtils.IS_OS_MAC) { return "macos"; }
      if (SystemUtils.IS_OS_WINDOWS) { return "windows"; }
      if (SystemUtils.IS_OS_LINUX) { return "linux"; }
      throw new IllegalStateException("Unsupported OS: "+System.getProperty("os.name"));
    }
  }
}

