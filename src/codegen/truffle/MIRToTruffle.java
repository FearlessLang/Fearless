package codegen.truffle;

import codegen.MIR;
import id.Id;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;

public class MIRToTruffle implements MIRVisitor<FearlessNode> {
  @Override public FearlessNode visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    throw Bug.unreachable();
  }
  public Map<Id.DecId, FearlessTrait> visitProgram(Map<String, List<MIR.Trait>> pkgs) {
    throw Bug.todo();
  }

  @Override public FearlessNode visitTrait(String pkg, MIR.Trait trait) {
    throw Bug.todo();
  }

  @Override public FearlessNode visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    throw Bug.todo();
  }

  @Override public FearlessNode visitX(MIR.X x, boolean checkMagic) {
    throw Bug.todo();
  }

  @Override public FearlessNode visitMCall(MIR.MCall mCall, boolean checkMagic) {
    return new MCallNode(
      mCall.recv().accept(this),
      mCall.args().stream().map(arg->arg.accept(this)).toArray(FearlessNode[]::new)
    );
  }

  @Override public FearlessNode visitLambda(MIR.Lambda newL, boolean checkMagic) {
    return new LambdaNode(newL);
  }
}
