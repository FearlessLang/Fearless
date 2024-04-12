package utils;

import failure.CompileError;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class OneOr{
  public static <T> T of(String err, Stream<T> ts){
    return ts
      .reduce((a,b)->{ throw new OneOrException(err); })
      .orElseThrow(()-> new OneOrException(err));
  }
  public static <T> T of(Supplier<? extends CompileError> err, Stream<T> ts){
    return ts
      .reduce( (a,b)->{throw new RuntimeException(err.get());})
      .orElseThrow(err);
  }

  public static class OneOrException extends RuntimeException {
    public OneOrException(String err) { super(err); }
  }
}
