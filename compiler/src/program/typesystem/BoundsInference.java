package program.typesystem;

import ast.T;
import failure.Fail;
import failure.FailOr;
import id.Id;
import id.Mdf;
import program.Program;
import utils.Bug;
import utils.Streams;
import visitors.TypeVisitor;

import java.util.Set;

public record BoundsInference(Program p, XBs xbs, Set<Mdf> expected) implements TypeVisitor<T, FailOr<Set<Mdf>>> {
  @Override public FailOr<Set<Mdf>> visitLiteral(Mdf mdf_, Id.IT<T> it) {
    Mdf mdf = mdf_.isMdf() ? Mdf.mut : mdf_;
    var dec = p.of(it.name());
    var gens = switch (dec) {
      case T.Dec dec_ -> dec_.lambda().id().gens();
      case astFull.T.Dec dec_ -> dec_.lambda().id().gens();
    };
    var bounds = switch (dec) {
      case T.Dec dec_ -> dec_.lambda().id().bounds();
      case astFull.T.Dec dec_ -> dec_.lambda().id().bounds();
    };
    var res = Streams.zip(it.ts(), gens)
      .map((t, gx)-> t.accept(new BoundsInference(p, xbs, bounds.get(gx))))
      .filter(FailOr::isErr)
      .findFirst();
    return res.orElseGet(()->holds(Set.of(mdf), mdf+" "+it));
  }
  @Override public FailOr<Set<Mdf>> visitRCX(Mdf mdf, Id.GX<T> gx) {
    return holds(Set.of(mdf), mdf+" "+gx);
  }

  @Override public FailOr<Set<Mdf>> visitX(Id.GX<T> x) {
    return holds(xbs.get(x), x.toString());
  }

  @Override public FailOr<Set<Mdf>> visitReadImm(Id.GX<T> x) {
    var case1 = holds(Set.of(Mdf.read, Mdf.imm), Mdf.readImm+" "+x);
    if (case1.isRes()) { return case1; }
    if (xbs.get(x).equals(Set.of(Mdf.imm))) {
      var caseImm = holds(Set.of(Mdf.imm), Mdf.readImm+" "+x);
      if (caseImm.isRes()) { return caseImm; }
    }
    var bs = xbs.get(x);
    if (!bs.contains(Mdf.imm)) {
      var caseRead = holds(Set.of(Mdf.read), Mdf.readImm+" "+x);
      if (caseRead.isRes()) { return caseRead; }
    }
    return FailOr.err(()->Fail.invalidMdfBound(Mdf.readImm+" "+x, expected.stream().sorted()));
  }

  private FailOr<Set<Mdf>> holds(Set<Mdf> inferred, String typeName) {
    /* Subsumption means that
     * ∆ |- mut X : mut,imm,read
     * holds if
     * ∆ |- mut X : mut
     *
     * but importantly:
     * ∆ |- mut X : imm,read
     * does not hold
     */
//    if (expected.stream().anyMatch(inferred::contains)) { return FailOr.res(expected); }
    if (expected.containsAll(inferred)) { return FailOr.res(expected); }
    return FailOr.err(()->Fail.invalidMdfBound(typeName, expected.stream().sorted()));
  }

  @Override public FailOr<Set<Mdf>> visitInfer() { throw Bug.unreachable(); }
}
