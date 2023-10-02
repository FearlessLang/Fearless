package visitors;

import astFull.E;
import id.Id;
import id.Mdf;
import program.inference.RefineTypes;

import java.util.Collection;

public interface FullCollectorVisitor<C extends Collection<?>> extends FullVisitor<Void> {
  C res();

  default Void visitMCall(E.MCall e) {
    e.receiver().accept(this);
    e.ts().ifPresent(tsi->tsi.forEach(this::visitT));
    e.es().forEach(ei->ei.accept(this));
    return null;
  }
  default Void visitX(E.X e) { return null; }
  default Void visitLambda(E.Lambda e) {
    e.mdf().ifPresent(this::visitMdf);
    e.its().forEach(this::visitIT);
    e.it().ifPresent(this::visitIT);
    e.meths().forEach(this::visitMeth);
    return null;
  }
  default Void visitMeth(E.Meth m) {
    m.name().ifPresent(this::visitMethName);
    m.sigs().ifPresent(sigs->sigs.forEach(this::visitSig));
    m.body().ifPresent(e->e.accept(this));
    return null;
  }
  default Void visitSig(E.Sig s) {
    visitMdf(s.mdf());
    s.gens().forEach(this::visitGX);
    s.ts().forEach(this::visitT);
    visitT(s.ret());
    return null;
  }
  default Void visitRefinedSig(RefineTypes.RefinedSig s) {
    visitMdf(s.mdf());
    s.gens().forEach(this::visitT);
    s.args().forEach(this::visitT);
    visitT(s.rt());
    return null;
  }
  default void visitMdf(Mdf mdf) {}
  default void visitT(astFull.T t) {
    if (t.isInfer()) { return; }
    t.match(this::visitGX, this::visitIT);
  }
  default Void visitIT(Id.IT<astFull.T> it) {
    visitDecId(it.name());
    it.ts().forEach(this::visitT);
    return null;
  }
  default Void visitGX(Id.GX<astFull.T> gx) { return null; }
  default Void visitMethName(Id.MethName m) { return null; }
  default Void visitDecId(Id.DecId d) { return null; }
}
