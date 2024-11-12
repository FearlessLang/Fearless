package codegen.java.types;

import codegen.CodegenType;
import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;

import java.util.Optional;

public interface JavaCodegenType extends CodegenType<JavaCodegenState, JavaTarget> {
  @Override JavaTarget target();
  @Override default boolean canHandle(MIR.MT exprType) {
    return Optional.ofNullable(this.magicDec())
      .map(magicDec->{
        if (exprType.name().isEmpty()) { return false; }
        return target().p().p().superDecIds(exprType.name().get()).contains(magicDec);
      })
      .orElse(false);
  }
  @Override default void instantiate(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    target().standardType().instantiate(t, k, state);
  }
  default void instantiateNoSingleton(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    this.instantiate(t, k, state);
  }
  @Override default void call(MIR.MT t, MIR.MCall call, JavaCodegenState state) {
    target().standardType().call(t, call, state);
  }

  @Override default void type(MIR.MT t, JavaCodegenState state) {
    target().standardType().type(t, state);
  }
  default void boxedType(MIR.MT t, JavaCodegenState state) {
    type(t, state);
  }
  default Id.DecId magicDec() {
    return null;
  }

  interface MethImpl<S> {
    void of(MIR.MT t, MIR.MCall call, JavaCodegenState state, S self);
  }
}
