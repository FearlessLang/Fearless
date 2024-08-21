package codegen.java;

import codegen.MIR;
import visitors.MIRVisitor;

sealed interface JavaVPFArg {
  static JavaVPFArg of(int i, MIR.E e) {
    return e.accept(new MIRVisitor<JavaVPFArg>() {
      @Override public JavaVPFArg visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
        return new Plain(i, e);
      }
      @Override public JavaVPFArg visitX(MIR.X x, boolean checkMagic) {
        return new Plain(i, e);
      }
      @Override public JavaVPFArg visitMCall(MIR.MCall call, boolean checkMagic) {
        return new Spawn(i, call);
      }
    }, true);
  }

  String assignment(JavaSingleCodegen gen);
  String fetch(JavaSingleCodegen gen);
  record Spawn(int i, MIR.MCall call) implements JavaVPFArg {
    @Override public String assignment(JavaSingleCodegen gen) {
      return "var arg%s = rt.VPF.spawn(()->%s);".formatted(i, gen.visitMCall(call, true));
    }
    @Override public String fetch(JavaSingleCodegen gen) {
      return "rt.VPF.join(arg%s)".formatted(i);
    }
  }
  record Plain(int i, MIR.E e) implements JavaVPFArg {
    @Override public String assignment(JavaSingleCodegen gen) {
      return "var arg%s = %s;".formatted(i, e.accept(gen, true));
    }
    @Override public String fetch(JavaSingleCodegen gen) {
      return e.accept(gen, true);
    }
  }
}
