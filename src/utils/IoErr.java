package utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class IoErr {
//  public static Consumer<? super Path> of(Object o) {
//  }
  public interface RunVoid{void run() throws IOException;}
  public interface Run<T>{T run() throws IOException;}
  public static void of(RunVoid f) {
    try { f.run(); }
    catch(IOException io){ throw new UncheckedIOException(io); }
  }
  public static <T> T of(Run<T> f) {
    try { return f.run(); }
    catch(IOException io){ throw new UncheckedIOException(io); }
  }
}