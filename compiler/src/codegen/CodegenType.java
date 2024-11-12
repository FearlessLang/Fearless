package codegen;

public interface CodegenType<T> {
  boolean canHandle(MIR.MT t);
  void instantiate(MIR.MT t, T apply);
  void call(MIR.MT t, MIR.MCall call, T apply);
  void type(MIR.MT t, T apply);
  CodegenTarget<T> target();
}

