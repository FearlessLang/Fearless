package program.typesystem;

import ast.T;
import failure.Fail;
import failure.FailOr;
import id.Id;
import id.Mdf;
import program.TypeTable;
import utils.Bug;
import utils.Streams;
import visitors.TypeVisitor;

import java.util.*;

/**
 * Kinded type judgements. Used for getting potential RCs from a type and checking if a type is valid for an expected
 * set of RCs.
 */
public record KindingJudgement(TypeTable p, XBs xbs, Set<Mdf> expected, boolean checkOnly) implements TypeVisitor<T, FailOr<List<Set<Mdf>>>> {
  static Set<Mdf> ALL_RCs = Set.of(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.mutH, Mdf.read, Mdf.readH);
  public KindingJudgement(TypeTable p, XBs xbs, boolean checkOnly) {
    this(p, xbs, ALL_RCs, checkOnly);
  }

  @Override public FailOr<List<Set<Mdf>>> visitLiteral(Mdf mdf_, Id.IT<T> it) {
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
      .map((t, gx)-> t.accept(new KindingJudgement(p, xbs, bounds.get(gx), checkOnly)))
      .filter(FailOr::isErr)
      .findFirst();
    return res.orElseGet(()->holds(Set.of(mdf), mdf+" "+it));
  }
  @Override public FailOr<List<Set<Mdf>>> visitRCX(Mdf mdf, Id.GX<T> gx) {
    return holds(Set.of(mdf), mdf+" "+gx);
  }

  @Override public FailOr<List<Set<Mdf>>> visitX(Id.GX<T> x) {
    return holds(xbs.get(x), x.toString());
  }

  @Override public FailOr<List<Set<Mdf>>> visitReadImm(Id.GX<T> x) {
    var res = new ArrayList<Set<Mdf>>();

    var case1 = holds(Set.of(Mdf.read, Mdf.imm), Mdf.readImm+" "+x);
    if (checkOnly && case1.isRes()) { return case1; }
    case1.ifRes(res::addAll);

    if (new KindingJudgement(p, xbs, Set.of(Mdf.iso, Mdf.imm), checkOnly).visitX(x).isRes()) {
      var caseImm = holds(Set.of(Mdf.imm), Mdf.readImm+" "+x);
      if (checkOnly && caseImm.isRes()) { return caseImm; }
      caseImm.ifRes(res::addAll);
    }

    if (new KindingJudgement(p, xbs, Set.of(Mdf.mut, Mdf.mutH, Mdf.read, Mdf.readH), checkOnly).visitX(x).isRes()) {
      var caseRead = holds(Set.of(Mdf.read), Mdf.readImm+" "+x);
      if (checkOnly && caseRead.isRes()) { return caseRead; }
      caseRead.ifRes(res::addAll);
    }
    if (checkOnly || res.isEmpty()) {
      return FailOr.err(()->Fail.invalidMdfBound(Mdf.readImm+" "+x, expected.stream().sorted()));
    }
    return FailOr.res(Collections.unmodifiableList(res));
  }

  private FailOr<List<Set<Mdf>>> holds(Set<Mdf> actual, String typeName) {
    // If we don't really care about the result, just check the hardest case.
    // This is a performance optimization, for validating bounds this has identical behaviour to
    // holdsAux.
    if (checkOnly) { return holdsSimple(actual, typeName); }

    return holdsAux(actual, typeName, new HashSet<>()).map(rcss->rcss.stream()
      .sorted(Comparator.comparingInt(Set::size))
      .toList()
    );
  }
  private FailOr<List<Set<Mdf>>> holdsSimple(Set<Mdf> actual, String typeName) {
    if (expected.containsAll(actual)) { return FailOr.res(List.of(expected)); }
    return FailOr.err(()->Fail.invalidMdfBound(typeName, expected.stream().sorted()));
  }
  private FailOr<Set<Set<Mdf>>> holdsAux(Set<Mdf> actual, String typeName, Set<Set<Mdf>> visited) {
    /* Subsumption means that
     * ∆ |- mut X : mut,imm,read
     * holds if
     * ∆ |- mut X : mut
     *
     * but importantly:
     * ∆ |- mut X : imm,read
     * does not hold
     */
    if (!visited.add(actual)) {
      return FailOr.res(Set.of());
    }

    if (!expected.containsAll(actual)) {
      return FailOr.err(()->Fail.invalidMdfBound(typeName, expected.stream().sorted()));
    }
    var holdsFor = new HashSet<Set<Mdf>>();
    holdsFor.add(actual);
    var untested = expected.stream().filter(e->!actual.contains(e)).toList();
    if (untested.isEmpty()) { return FailOr.res(Collections.unmodifiableSet(holdsFor)); }
    for (var rc : untested) {
      var expandedActual = new HashSet<>(actual);
      expandedActual.add(rc);
      holdsAux(Collections.unmodifiableSet(expandedActual), typeName, visited)
        .ifRes(holdsFor::addAll);
    }

    return FailOr.res(Collections.unmodifiableSet(holdsFor));
  }

  @Override public FailOr<List<Set<Mdf>>> visitInfer() { throw Bug.unreachable(); }
}
