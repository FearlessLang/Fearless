package codegen.java;

import codegen.MIR;

interface JavaVPFArg {
  static String assignment(JavaSingleCodegen gen, MIR.VPFArg arg) {
    return switch (arg) {
      case MIR.VPFArg.Plain plain -> "var arg%s = %s;".formatted(plain.i(), plain.e().accept(gen, true));
      case MIR.VPFArg.Spawn spawn -> "var arg%s = rt.VPF.spawn(()->%s);".formatted(spawn.i(), gen.visitMCall(spawn.call(), true));
    };
  }
}
