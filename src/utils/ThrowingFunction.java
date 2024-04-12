package utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

public interface ThrowingFunction<A,R> extends Function<A,R>{
  @Override default R apply(A a){
    try { return this._apply(a); }
    catch (IOException e) { throw new UncheckedIOException(e); }
    catch(RuntimeException | java.lang.Error e){ throw e; }
    catch(Throwable t){ throw new RuntimeException("SneakyThrow",t); }
  }
  R _apply(A a)throws Throwable;
  static <A,R> ThrowingFunction<A,R> of(ThrowingFunction<A,R> id){ return id; }
}
