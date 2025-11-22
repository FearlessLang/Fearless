package utils;

import failure.CompileError;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OneOr{
  public static <T> Optional<T> opt(String err, Stream<T> ts){
    return ts.reduce((_,_)->{ throw new OneOrException(err); });
  }
  public static <T> T of(String err, Stream<T> ts){
    return ts
      .reduce((_,_)->{ throw new OneOrException(err); })
      .orElseThrow(()-> new OneOrException(err));
  }
  public static <T> T of(Supplier<CompileError> err, Stream<T> ts){
    return ts
      .reduce((a,b)->{throw err.get();})
      .orElseThrow(err);
  }

  public static class OneOrException extends RuntimeException {
    public OneOrException(String err) { super(err); }
  }
}
