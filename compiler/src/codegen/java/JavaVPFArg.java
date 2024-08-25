package codegen.java;

import codegen.MIR;

interface JavaVPFArg {
  static String assignment(JavaSingleCodegen gen, MIR.VPFArg arg) {
    return switch (arg) {
      case MIR.VPFArg.Plain plain -> "var %s = %s;".formatted(argName(plain.i()), plain.e().accept(gen, true));
      case MIR.VPFArg.Spawn spawn -> "var %s = rt.VPF.spawn(()->%s);".formatted(argName(spawn.i()), gen.visitMCall(spawn.call(), true));
    };
  }
  static String argName(int i) {
    return i < 0 ? "recv" : "arg"+i;
  }
}
