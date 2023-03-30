package utils;

import java.util.function.Function;

public class Box<T> {
  private T inner;

  public Box(T inner) { this.inner = inner; }

  public T get() { return inner; }
  public void set(T inner) { this.inner = inner; }
  public T up(Function<T, T> update) {
    var old = this.inner;
    this.inner = update.apply(this.inner);
    return old;
  }
}
