package codegen.java.types;

import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import magic.Magic;

public record Debug(JavaTarget target) implements JavaCodegenType {
  @Override public void instantiate(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("rt.Debug.$self");
  }

  @Override public void type(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("rt.Debug");
  }

  @Override public Id.DecId magicDec() {
    return Magic.Debug;
  }
}
