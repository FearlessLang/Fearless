package codegen.java.types;

import codegen.MIR;
import codegen.MagicCodegenType;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;

public interface JavaMagicType extends JavaCodegenType, MagicCodegenType<JavaCodegenState, JavaTarget> {
  @Override default void instantiate(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    MagicCodegenType.super.instantiate(t, k, state);
  }
}
