package program.typesystem;

import ast.T;
import id.Id;
import id.Mdf;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface XBs {
  Set<Mdf> defaultBounds = Set.of(Mdf.mut, Mdf.imm, Mdf.read);

  default Set<Mdf> get(Id.GX<?> x) {
    return getO(x).orElse(defaultBounds);
  }
  default Optional<Set<Mdf>> getO(Id.GX<?> x) {
    return getO(x.name());
  }
  Optional<Set<Mdf>> getO(String s);
  static XBs empty(){ return x->Optional.empty(); }
  default XBs add(String x, Set<Mdf> bounds) {
    assert !bounds.contains(Mdf.mdf) && !bounds.contains(Mdf.recMdf) && !bounds.contains(Mdf.readImm);
    return xi->xi.equals(x) ? Optional.of(bounds) : this.getO(xi);
  }
  default XBs addBounds(Collection<Id.GX<T>> gens, Map<Id.GX<T>, Set<Mdf>> newBounds) {
    var xbs = this;
    for (var gx : gens) {
      var bounds = newBounds.get(gx);
      if (bounds == null || bounds.isEmpty()) { continue; }
      xbs = xbs.add(gx.name(), bounds);
    }
    return xbs;
  }
}
