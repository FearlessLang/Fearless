package wellFormedness;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;
import visitors.ShortCircuitVisitor;

import java.util.List;
import java.util.Optional;

// TODO: _C/_m restrictions
public class WellFormednessShortCircuitVisitor implements ShortCircuitVisitor<CompileError> {
  private final Program p;
  private Optional<String> pkg = Optional.empty();
  public WellFormednessShortCircuitVisitor(Program p) { this.p = p; }

  @Override public Optional<CompileError> visitDec(T.Dec d) {
    pkg = Optional.of(d.name().pkg());
    return ShortCircuitVisitor.visitAll(
        d.lambda().its(),
        it->noRecMdfInImpls(it).map(err->err.pos(d.lambda().pos()))
        )
      .or(()->ShortCircuitVisitor.super.visitDec(d))
      .map(err->err.parentPos(d.pos()));
  }

  @Override public Optional<CompileError> visitLambda(E.Lambda e) {
    return noSealedOutsidePkg(e)
      .or(()->ShortCircuitVisitor.super.visitLambda(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e) {
    return ShortCircuitVisitor.super.visitMeth(e).map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitT(T t) {
    assert !(t.mdf().isMdf() && t.isIt());
    return noHygInMut(t).or(()->ShortCircuitVisitor.super.visitT(t));
  }

  private Optional<CompileError> noHygInMut(T t) {
    if (!(t.rt() instanceof Id.IT<T> it)) { return Optional.empty(); }
    if (t.mdf().isMut() && it.ts().stream().map(T::mdf).anyMatch(Mdf::isHyg)) {
      return Optional.of(Fail.mutCapturesHyg(t));
    }
    return Optional.empty();
  }

  private Optional<CompileError> noRecMdfInImpls(Id.IT<T> it) {
    return new ShortCircuitVisitor<CompileError>(){
      public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) { return Optional.of(Fail.recMdfInImpls(t)); }
        return ShortCircuitVisitor.super.visitT(t);
      }
    }.visitIT(it);
  }

  private Optional<CompileError> noSealedOutsidePkg(E.Lambda e) {
    if (e.meths().isEmpty()) { return Optional.empty(); }
    var pkg = this.pkg.orElseThrow();
    return getSealedDec(e.its()).filter(dec->!dec.pkg().equals(pkg)).map(dec->Fail.sealedCreation(dec, pkg).pos(e.pos()));
  }

  private Optional<Id.DecId> getSealedDec(List<Id.IT<T>> its) {
    if (its.isEmpty()) { return Optional.empty(); }
    return its.stream()
//      .map(Id.IT::name)
      .filter(it->p.itsOf(it).stream().anyMatch(it1->it1.name().equals(Magic.Sealed)))
      .map(Id.IT::name)
      .findFirst()
      .or(()->getSealedDec(its.stream().flatMap(it->p.itsOf(it).stream()).toList()));
  }
}
