package utils;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

import java.util.List;
import java.util.Optional;

public interface CoreGenerator {
  record Scope(List<Id.DecId> ds, List<Id.GX<T>> gxs) {}
  @Provide default Arbitrary<Id.GX<T>> gx() {
    var name = Arbitraries.strings().alpha().numeric().ofMinLength(1);
    return name.filter(Id::validGX).map(Id.GX::new);
  }
  @Provide default Arbitrary<Mdf> mdf() {
    return Arbitraries.of(Mdf.values());
  }
  @Provide default Arbitrary<Id.IT<T>> it(Scope s) {
    var dec = Arbitraries.of(s.ds);
    return dec.flatMap(d->t(s)
      .collect(gxs->gxs.size() == d.gen())
      .map(gxs->new Id.IT<>(d.name(), gxs)));
  }
  @Provide default Arbitrary<T> t(Scope s) {
    Arbitrary<Id.RT<T>> rt = Arbitraries.oneOf(List.of(
      it(s),
      Arbitraries.of(s.gxs)
    ));
    return rt.flatMap(rti->mdf().map(mdf->new T(mdf, rti)));
  }

  @Provide default Arbitrary<E.X> x(Scope s) {
    var name = Arbitraries.strings().alpha().numeric().ofMinLength(1).filter(E.X::validId);
    return name.map(x->new E.X(x, Optional.empty()));
  }
}
