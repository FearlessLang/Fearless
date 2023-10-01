package visitors;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;

import java.util.Collection;

public interface CollectorVisitor<C extends Collection<?>> extends Visitor<Void> {
  C res();

  default Void visitMCall(E.MCall e) {
    e.receiver().accept(this);
    e.ts().forEach(this::visitT);
    e.es().forEach(ei->ei.accept(this));
    return null;
  }
  default Void visitX(E.X e) { return null; }
  default Void visitLambda(E.Lambda e) {
    visitMdf(e.mdf());
    e.its().forEach(this::visitIT);
    e.meths().forEach(this::visitMeth);
    return null;
  }
  default Void visitMeth(E.Meth m) {
    visitMethName(m.name());
    visitSig(m.sigs());
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
  default void visitMdf(Mdf mdf) {}
  default void visitT(T t) {
    t.match(this::visitGX, this::visitIT);
  }
  default Void visitIT(Id.IT<T> it) {
    visitDecId(it.name());
    it.ts().forEach(this::visitT);
    return null;
  }
  default Void visitGX(Id.GX<T> gx) { return null; }
  default Void visitMethName(Id.MethName m) { return null; }
  default Void visitDecId(Id.DecId d) { return null; }
}
