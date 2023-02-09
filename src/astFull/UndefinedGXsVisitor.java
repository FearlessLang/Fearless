package astFull;

import id.Id;
import program.inference.RefineTypes;
import visitors.FullCollectorVisitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UndefinedGXsVisitor implements FullCollectorVisitor<Set<Id.GX<T>>> {
  private final HashSet<Id.GX<T>> undefined = new HashSet<>();
  private final HashSet<Id.GX<T>> defined;
  public UndefinedGXsVisitor(Collection<Id.GX<T>> topLevelGXs) {
    defined = new HashSet<>(topLevelGXs);
  }

  public Set<Id.GX<T>> res() {
    var intersection = new HashSet<>(undefined);
    intersection.retainAll(defined);
    assert intersection.isEmpty();
    return undefined;
  }

  public Void visitGX(Id.GX<T> gx) {
    if (!defined.contains(gx)) { undefined.add(gx); }
    return FullCollectorVisitor.super.visitGX(gx);
  }
  public Void visitSig(E.Sig s) {
    s.gens().forEach(gx->{
      defined.add(gx);
      undefined.remove(gx);
    });
    return FullCollectorVisitor.super.visitSig(s);
  }
  public Void visitRefinedSig(RefineTypes.RefinedSig s) {
    s.gens().forEach(t->{
      if (t.isInfer()) { return; }
      var gx = t.gxOrThrow();
      defined.add(gx);
      undefined.remove(gx);
    });
    return FullCollectorVisitor.super.visitRefinedSig(s);
  }
}
