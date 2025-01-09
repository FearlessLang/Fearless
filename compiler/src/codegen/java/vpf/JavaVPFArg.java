package codegen.java.vpf;

import codegen.MIR;
import codegen.java.JavaSingleCodegen;

public interface JavaVPFArg {
  static String assignment(JavaSingleCodegen gen, MIR.VPFCall.VPFArg arg) {
    return switch (arg) {
      case MIR.VPFCall.VPFArg.Plain plain ->
        "var %s = %s;".formatted(argName(plain.i()), plain.e().accept(gen, true));
      case MIR.VPFCall.VPFArg.Spawn spawn ->
        "var %s = rt.vpf.VPF.spawn(()->%s);".formatted(argName(spawn.i()), spawn.e().accept(gen, true));
    };
  }

  static String argName(int i) {
    return i < 0 ? "recv" : "arg" + i;
  }
}
