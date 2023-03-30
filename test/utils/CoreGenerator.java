package utils;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import id.Id;
import id.Mdf;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;
import program.typesystem.TraitTypeSystem;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CoreGenerator extends DomainContextBase {
  record Scope(List<Id.DecId> ds, List<Id.GX<T>> gxs, List<String> xs) {}

  @Provide public Arbitrary<T> t(Scope s) {
    Arbitrary<Id.RT<T>> rt = Arbitraries.oneOf(List.of(
      Arbitraries.of(s.gxs),
      it(s)
    ));
    return rt.flatMap(rti->mdf().map(mdf->new ast.T(mdf, rti)));
  }
  @Provide public Arbitrary<Id.GX<T>> gx() {
    var name = Arbitraries.strings().alpha().numeric().ofMinLength(1);
    return name.filter(Id::validGX).map(Id.GX::new);
  }
  @Provide public Arbitrary<Mdf> mdf() {
    return Arbitraries.of(Mdf.values());
  }
  @Provide public Arbitrary<Id.IT<T>> it(Scope s) {
    var dec = Arbitraries.of(s.ds);
    return dec.flatMap(d->t(s)
      .collect(gxs->gxs.size() == d.gen())
      .map(gxs->new Id.IT<>(d.name(), gxs)));
  }

  @Provide public Arbitrary<E> e(Scope s) {
    return Arbitraries.oneOf(List.of(x(s)));
  }
  @Provide public Arbitrary<E.X> x(Scope s) {
    var name = Arbitraries.strings().alpha().numeric().ofMinLength(1).filter(E.X::validId);
    return name.map(x->new E.X(x, Optional.empty()));
  }
  @Provide public Arbitrary<E.MCall> mCall(Scope s) {
    var x = e(s);
    var name = Arbitraries.strings().ascii().ofMinLength(1).filter(Id::validM);

    throw Bug.todo();
//    return x.map(recv->new E.MCall(recv, new Id.MethName(name, ), ))
  }
  @Provide public Arbitrary<E.Lambda> lambda(Scope s) {
    return mdf().flatMap(mdf->{
      var selfName = Arbitraries.strings().ascii().ofMinLength(1).filter(E.X::validId);
      var nImpls = Arbitraries.integers()
        .between(1, s.ds.size())
        .shrinkTowards(1)
        .map(Box::new);
      return nImpls
        .flatMap(nIts->it(s).collect(d->nIts.up(i->i-1) == 0))
        .flatMap(its->selfName.map(name->new E.Lambda(mdf, its, name, List.of(), Optional.empty())));
    });
  }

  @Provide public Arbitrary<Program> program() {
    var generated = new Box<>(0);
    var decs = dec().collect(d->{
      if (generated.get() == 15) { return true; }
      generated.up(i->i+1);
      return false;
    });
    return decs
      .filter(ds->{
        try {
          return TraitTypeSystem.dsOk(ds).isEmpty();
        } catch (CompileError e) { return false; }
      })
      .map(ds->new Program(Mapper.of(m->ds.forEach(d->m.put(d.name(), d)))))
      .filter(p->new WellFormednessShortCircuitVisitor(p).visitProgram(p).isEmpty());
  }
  @Provide public Arbitrary<T.Dec> dec() {
    Arbitrary<List<Id.GX<T>>> nGens = Arbitraries.integers().between(0, 0).shrinkTowards(0).map(n->{
      var gxs = gx().generator(n);
      return IntStream.range(0, n).mapToObj(i->gxs.next(Arbitraries.randoms().sample()).value()).toList();
    });

    return nGens.flatMap(gxs->{
      var pkg = Arbitraries.strings().alpha().ofMinLength(1);
      var name = pkg.flatMap(pkgi->Arbitraries.strings().alpha().numeric().ofMinLength(1)
        .map(n->pkgi+"."+n)
        .filter(Id::validDecName)
        .map(n->new Id.DecId(n, gxs.size())));

      return name.flatMap(n->lambda(new Scope(List.of(n), gxs, List.of()))
        .map(l->new T.Dec(n, gxs, l.withSelfName("this").withMdf(Mdf.mdf), Optional.empty())));
    });
  }
  @Provide public Arbitrary<E.Meth> meth(Scope s) {
    throw Bug.todo();
  }
}
