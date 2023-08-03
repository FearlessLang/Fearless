package codegen.java;

import codegen.MIR;
import utils.Mapper;

import java.util.List;
import java.util.Map;

public class SimplifiedTraitMIRVisitor {
  public Map<String, List<MIR.Trait>> visitProgram(Map<String, List<MIR.Trait>> pkgs) {
    return Mapper.of(c -> pkgs.forEach((pkg, ts) -> c.put(pkg, ts.stream().map(t->visitTrait(pkg, t)).toList())));
  }

  public MIR.Trait visitTrait(String pkg, MIR.Trait trait) {
    return new MIR.Trait(
      trait.name(),
      trait.gens(),
      trait.its(),
      trait.meths().stream().map(this::visitMeth).toList(),
      trait.canSingleton()
    );
  }

  public MIR.Meth visitMeth(MIR.Meth meth) {
    return new MIR.Meth(
      meth.name(),
      meth.mdf(),
      meth.gens(),
      meth.xs(),
      meth.rt(),
      meth.body().map(e->e.accept(this))
    );
  }

  public MIR visitX(MIR.X x) {
    return x;
  }

  public MIR visitMCall(MIR.MCall mCall) {
    return new MIR.MCall(
      mCall.recv().accept(this),
      mCall.name(),
      mCall.args().stream().map(e->e.accept(this)).toList(),
      mCall.t()
    );
  }

  public MIR visitLambda(MIR.Lambda newL) {
    if (newL.its().size() == 1) {
      var it = newL.its().get(0);
      return new MIR.Lambda(
        newL.mdf(),
        it.name(),
        newL.selfName(),
        newL.its(),
        newL.captures(),
        newL.meths().stream().map(this::visitMeth).toList(),
        newL.canSingleton()
      );
    }

    return new MIR.Lambda(
      newL.mdf(),
      newL.freshName(),
      newL.selfName(),
      newL.its(),
      newL.captures(),
      newL.meths().stream().map(this::visitMeth).toList(),
      newL.canSingleton()
    );
  }
}
