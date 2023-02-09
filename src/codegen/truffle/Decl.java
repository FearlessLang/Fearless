package codegen.truffle;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;

public abstract class Decl extends Node {
  static class Dec extends Decl {
    static Dec Ref = new Dec("base.Ref/1");

    @Child final String name;
    Dec(String name) {
      this.name = name;
    }
  }
  static final class Meth extends Decl {
    private final String name;
    private final RootCallTarget callTarget;

    private Meth(String name, RootCallTarget target) {
      this.name = name;
      this.callTarget = target;
    }
  }
}
