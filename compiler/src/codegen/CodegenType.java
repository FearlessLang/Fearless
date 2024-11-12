package codegen;

public interface CodegenType<T, Target extends CodegenTarget<T, Target>> {
  boolean canHandle(MIR.MT t);
  void instantiate(MIR.MT t, MIR.CreateObj k, T apply);
  default void instantiate(MIR.MT t, MIR.E e, T apply) {
    if (!(e instanceof MIR.CreateObj k)) {
      target().withState(apply, target->e.accept(target, true));
      return;
    }
    instantiate(t, k, apply);
  }
  void call(MIR.MT t, MIR.MCall call, T apply);
  void type(MIR.MT t, T apply);
  Target target();
}

