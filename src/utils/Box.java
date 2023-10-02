package utils;

import java.util.function.Function;
import java.util.function.Supplier;

public class Box<T> {
  private T inner;

  public static <T> Box<T> of(Supplier<T> f) { return new Box<>(f.get()); }

  public Box(T inner) { this.inner = inner; }

  public T get() { return inner; }
  public void set(T inner) { this.inner = inner; }
  public T update(Function<T, T> f) {
    this.inner = f.apply(this.inner);
    return this.inner;
  }
}
