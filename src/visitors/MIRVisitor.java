package visitors;

import codegen.MIR;

import java.util.Map;

public interface MIRVisitor<R> {
  // Structure
  R visitPackage(String pkg, Map<String, MIR.Trait> ds);
  R visitTrait(String pkg, String name, MIR.Trait trait);
  R visitMeth(String name, MIR.Meth meth, String selfName, boolean concrete);

  // Expressions/values
  R visitX(MIR.X x);
  R visitMCall(MIR.MCall mCall);
  R visitLambda(MIR.Lambda newL);
//  R visitNewDynLambda(MIR.NewDynLambda newL);
//  R visitNewStaticLambda(MIR.NewStaticLambda newL);
//  R visitShare(MIR.Share s);

  // TODO: Ref stuff
}
