package codegen;

public interface MagicCodegenType<T, Target extends CodegenTarget<T, Target>> extends CodegenType<T, Target> {
  void instantiate(MIR.MT t, T apply);

  @Override default void instantiate(MIR.MT t, MIR.CreateObj k, T apply) {
    instantiate(t, apply);
  }
}

