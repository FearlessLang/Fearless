package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;

public class MCallNode extends FearlessNode {
  @Child private FearlessNode recv;
  public MCallNode(FearlessNode recv) {
    this.recv = recv;
  }

  @Override public MIR.Lambda executeLambda(VirtualFrame frame) {
    // TODO
    return recv.executeLambda(frame);
  }
}
