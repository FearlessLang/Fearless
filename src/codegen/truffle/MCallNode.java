package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;
import utils.Bug;

@SuppressWarnings("FieldMayBeFinal")
public class MCallNode extends FearlessNode {
  @Child private FearlessNode recv;
  @Children private FearlessNode[] args;

  public MCallNode(FearlessNode recv, FearlessNode[] args) {
    this.recv = recv;
    this.args = args;
  }

  @Override public MIR.Lambda executeLambda(VirtualFrame frame) {
    var rec = recv.executeLambda(frame);
    MIR.Lambda[] resolvedArgs = new MIR.Lambda[args.length];
    for (int i = 0; i < args.length; ++i) { resolvedArgs[i] = args[i].executeLambda(frame); }
    throw Bug.todo();
  }
}
