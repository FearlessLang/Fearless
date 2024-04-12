package rt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public interface ThrowingConsumer<A> extends Consumer<A> {
  @Override default void accept(A a){
    try { this._accept(a); }
    catch (IOException e) { throw new UncheckedIOException(e); }
    catch(RuntimeException | java.lang.Error e){ throw e; }
    catch(Throwable t){ throw new RuntimeException("SneakyThrow",t); }
  }
  void  _accept(A a)throws Throwable;
  static <A> ThrowingConsumer<A> of(ThrowingConsumer<A> id){ return id; }
}
