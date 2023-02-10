package visitors;

import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;

public interface MIRVisitor<R> {
  // Structure
  R visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry);
  R visitTrait(String pkg, MIR.Trait trait);
  R visitMeth(MIR.Meth meth, String selfName, boolean concrete);

  // Expressions/values
  R visitX(MIR.X x);
  R visitMCall(MIR.MCall mCall);
  R visitLambda(MIR.Lambda newL);
//  R visitNewDynLambda(MIR.NewDynLambda newL);
//  R visitNewStaticLambda(MIR.NewStaticLambda newL);
//  R visitShare(MIR.Share s);

  // TODO: Ref stuff
}
