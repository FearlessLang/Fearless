package rt;

public class Var implements base._MagicVarImpl_1 {
  private volatile Object value;
  public Var(Object value) {
    this.value = value;
  }
  @Override public Object get$read() {
    return this.value;
  }
  @Override public Object get$mut() {
    return this.value;
  }
  @Override public Object swap$mut(Object x_m$) {
    var old = this.value;
    this.value = x_m$;
    return old;
  }
}
