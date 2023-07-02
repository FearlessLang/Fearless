package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;

@SuppressWarnings("FieldMayBeFinal")
public class LambdaNode extends FearlessNode {
  private final MIR.Lambda value;
  public LambdaNode(MIR.Lambda value) {
    this.value = value;
  }
  @Override public MIR.Lambda executeLambda(VirtualFrame frame) {
    return this.value;
  }
}
