package rt;

import utils.ResolveResource;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
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
    var libName = System.mapLibraryName("native_compiler");
    var resourceLibPath = ResolveResource.artefact("/rt/libnative/"+arch+"-"+libName);
    // This is a little sad but Windows, Mac, and Linux all will refuse to dynamically link to a file that does not
    // exist in the "real" filesystem. So, we need to copy our library to load it into memory.
    try {
      var concreteLibPath = Files.createTempFile(null, libName);
      concreteLibPath.toFile().deleteOnExit();
      try (var libFileWriter = Files.newOutputStream(concreteLibPath);
           var libFileReader = Files.newInputStream(resourceLibPath, StandardOpenOption.READ)) {
        libFileReader.transferTo(libFileWriter);
      }
      System.load(concreteLibPath.toAbsolutePath().toString());
    } catch (IOException | UnsatisfiedLinkError err) {
      throw new RuntimeException("Internal Fearless runtime error: Could not link to "+resourceLibPath+".\n"+err.getMessage());
    }
  }

  private static final Cleaner cleaner = Cleaner.create();

  // Strings
  public static class StringEncodingError extends RuntimeException {
    public StringEncodingError(String message) {
      super(message);
    }
  }
  public static native void validateStringOrThrow(ByteBuffer utf8Str) throws StringEncodingError;
  public static native int[] indexString(ByteBuffer utf8Str);
  public static native void print(ByteBuffer utf8Str);
  public static native void println(ByteBuffer utf8Str);
  public static native void printlnErr(ByteBuffer utf8Str);
  public static native void printErr(ByteBuffer utf8Str);
  public static native byte[] normaliseString(ByteBuffer utf8Str);
  public static native long hashString(ByteBuffer utf8Str);
  public static native byte[] uniqueHashStr(String strToHash);

  // Regex
  public static final class Regex {
    record CleaningState(long pattern) implements Runnable {
      @Override public void run() {
        NativeRuntime.dropRegexPattern(pattern);
      }
    }

    private final long patternPtr;
    public Regex(ByteBuffer patternStr) {
      this.patternPtr = NativeRuntime.compileRegexPattern(patternStr);
      cleaner.register(this, new CleaningState(patternPtr));
    }
    public boolean doesRegexMatch(ByteBuffer utf8Str) {
      return NativeRuntime.doesRegexMatch(patternPtr, utf8Str);
    }
    public static class InvalidRegexError extends RuntimeException {
      public InvalidRegexError(String message) {
        super(message);
      }
    }
  }
  private static native long compileRegexPattern(ByteBuffer utf8Str);
  private static native void dropRegexPattern(long pattern);
  private static native boolean doesRegexMatch(long pattern, ByteBuffer utf8Str);
}
