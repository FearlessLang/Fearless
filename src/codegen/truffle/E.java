package codegen.truffle;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import id.Mdf;
import utils.Bug;

import java.util.Arrays;

public abstract class E extends Node {
  public abstract Mdf mdf();
  public abstract Object execute(VirtualFrame frame);

  static class X extends E {
    @Child Mdf mdf;
    X(Mdf mdf) {
      this.mdf = mdf;
    }

    @Override public Mdf mdf() { return this.mdf; }

    @Override public Object execute(VirtualFrame frame) {
      throw Bug.todo();
    }
  }
  @NodeChildren({ @NodeChild("recv"), @NodeChild("m"), @NodeChild("args") })
  static class MCall extends E {
    @Child Mdf mdf;
//    @Child final E recv;
//    @Children final E[] args;
    MCall(Mdf mdf) {
      this.mdf = mdf;
    }

    @Specialization(guards = "isMagicRef(m)")
    Object callRef(RefLambda recv, String m, Object args) {
      throw Bug.todo();
    }
    static boolean isMagicRef(String m) {
      return m.equals("*") || m.equals(".swap");
    }

    @Specialization
    Object call(Object recv, String m, Object args) {
      throw Bug.todo();
    }

    @Override public Object execute(VirtualFrame frame) {
      throw Bug.todo();
    }
    @Override public Mdf mdf() { return this.mdf; }
  }
  static class Lambda extends E {
    @Child Mdf mdf;
    @Child Decl.Dec[] impls;
    Lambda(Mdf mdf, Decl.Dec[] impls) {
      this.mdf = mdf;
      this.impls = impls;
    }

    boolean is(String traitName) {
      return Arrays.stream(impls).anyMatch(d->d.name.equals(traitName));
    }

    @Override public Mdf mdf() { return this.mdf; }
    @Override public Object execute(VirtualFrame frame) {
      throw Bug.todo();
    }
  }
  static class RefLambda extends Lambda {
    @Child Lambda inner;
    RefLambda(Mdf mdf, Lambda inner) {
      super(mdf, new Decl.Dec[]{Decl.Dec.Ref});
      this.inner = inner;
    }

    @Override public Mdf mdf() { return this.mdf; }
    @Override public Object execute(VirtualFrame frame) {
      throw Bug.todo();
    }
  }
}
