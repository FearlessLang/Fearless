package rt;

import userCode.FProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class NativeRuntime {
  static {
    // This notably is defined as the architecture the JVM is running as (and not the architecture of the computer).
    // We want this because the code is going to be linked into the JVM so it *must* be the same architecture.
    var arch = System.getProperty("os.arch");
    arch = switch (arch) {
      case "x86_64", "amd64" -> "amd64";
      case "aarch64", "arm64" -> "arm64";
      default -> throw new IllegalStateException("Unsupported architecture: "+System.getProperty(arch));
    };
    var libName = System.mapLibraryName("native_rt");
    var resourceLibPath = ResolveResource.of("/rt/libnative/"+arch+"-"+libName);
    assert Files.exists(resourceLibPath):resourceLibPath;
    // This is a little sad but Windows, Mac, and Linux all will refuse to dynamically link to a file that does not
    // exist in the "real" filesystem. So, we need to copy our library to load it into memory.
    try {
      var concreteLibPath = Files.createTempFile(null, libName);
      concreteLibPath.toFile().deleteOnExit();
      try (var libFileWriter = Files.newOutputStream(concreteLibPath);
           var libFileReader = Files.newInputStream(resourceLibPath, StandardOpenOption.READ)) {
        libFileReader.transferTo(libFileWriter);
        libFileWriter.flush();
        System.load(concreteLibPath.toAbsolutePath().toString());
      }
    } catch (IOException | UnsatisfiedLinkError err) {
      throw new RuntimeException("Internal Fearless runtime error: Could not link to "+resourceLibPath+".\n"+err.getMessage());
    }
  }

  public static class StringEncodingError extends FearlessError {
    public StringEncodingError(String message) {
      super(FProgram.base.FInfo_0.base$FInfo_0$msg$imm$$noSelfCap(rt.Str.fromJavaStr(message)));
    }
  }

  public static native void validateStringOrThrow(byte[] utf8Str) throws StringEncodingError;
  public static native int[] indexString(byte[] utf8Str);
  public static native void print(byte[] utf8Str);
  public static native void println(byte[] utf8Str);
  public static native void printlnErr(byte[] utf8Str);
  public static native void printErr(byte[] utf8Str);
}
