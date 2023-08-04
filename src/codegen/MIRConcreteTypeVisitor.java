package codegen;

import ast.T;
import id.Id;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MIRConcreteTypeVisitor() implements MIRVisitor<T> {
//  private HashMap<>
  @Override public T visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) { throw Bug.unreachable(); }
  @Override public T visitTrait(String pkg, MIR.Trait trait) { throw Bug.unreachable(); }
  @Override public T visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    return meth.body().orElseThrow().accept(this);
  }

  @Override public T visitX(MIR.X x, boolean checkMagic) {
    return x.t();
  }

  @Override public T visitMCall(MIR.MCall mCall, boolean checkMagic) {
//    return mCall.;
    throw Bug.todo();
  }

  @Override public T visitLambda(MIR.Lambda newL, boolean checkMagic) {
    return newL.t();
  }
}
