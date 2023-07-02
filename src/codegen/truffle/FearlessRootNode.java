package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

@SuppressWarnings("FieldMayBeFinal")
public class FearlessRootNode extends RootNode {
  @Child private FearlessNode entryExpr;
  public FearlessRootNode(FearlessNode entryExpr) {
    super(null);
    this.entryExpr = entryExpr;
  }

  @Override public MIR.Lambda execute(VirtualFrame frame) {
    return this.entryExpr.executeLambda(frame);
  }
}
