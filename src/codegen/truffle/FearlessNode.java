package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class FearlessNode extends Node {
  public abstract MIR.Lambda executeLambda(VirtualFrame frame);
}
