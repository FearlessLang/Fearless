package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;
import id.Id;
import id.Mdf;
import utils.Bug;

import java.util.List;

public class FearlessTrait extends FearlessNode {
  private final MIR.Trait t;
  public FearlessTrait(MIR.Trait t) {
    this.t = t;
  }

  @Override public MIR.Lambda executeLambda(VirtualFrame frame) {
    throw Bug.unreachable();
  }
}
