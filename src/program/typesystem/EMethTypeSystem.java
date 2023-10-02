package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Id.GX;
import id.Id.MethName;
import id.Mdf;
import program.TypeRename;
import utils.Mapper;
import utils.Push;
import utils.Streams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface EMethTypeSystem extends ETypeSystem {
  default Res visitMCall(E.MCall e) {
    var e0 = e.receiver();
    var v = this.withT(Optional.empty());
    Res rE0 = e0.accept(v);
    if(rE0.err().isPresent()){ return rE0; }
    T t_=rE0.tOrThrow();
    var invalidBounds = GenericBounds.validGenericMeth(p(), xbs(), t_.mdf(), t_.itOrThrow(), depth(), e.name(), e.ts());
    if (invalidBounds.isPresent()) { return invalidBounds.get().pos(e.pos()); }

    var optTst=multiMeth(t_,e.name(),e.ts());
    if(optTst.isEmpty()){ return new CompileError(); } //TODO: list the available methods
    List<TsT> tst = optTst.get().stream()
      .filter(this::filterOnRes)
      .toList();

    if (tst.isEmpty()) {
      return Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.get().stream().distinct().toList()).pos(e.pos());
    }

    List<E> es = Push.of(e0,e.es());
    var nestedErrors = new ArrayDeque<ArrayList<CompileError>>(tst.size());
    var methArgCache = IntStream.range(0, es.size()).mapToObj(i_->new HashMap<T, Res>()).toList();
    for (TsT(List<T> ts, T t) : tst) {
      var errors = new ArrayList<CompileError>();
      nestedErrors.add(errors);
      if (okAll(es, ts, errors, methArgCache)) {
        return t;
      }
    }

    var calls1 = tst.stream()
      .map(tst1->{
        var call = Streams.zip(es, tst1.ts())
          .map((e1,t1)->{
            var getT = this.withT(Optional.empty());
            return e1.accept(getT).t()
              .map(T::toString)
              .orElseGet(()->"?"+e1+"?");
          })
          .toList();
        var dependentErrorMsgs = nestedErrors.removeFirst().stream()
          .map(CompileError::toString)
          .collect(Collectors.joining("\n"))
          .indent(4);
        var dependentErrors = !dependentErrorMsgs.isEmpty()
          ? "\n"+"The following errors were found when checking this sub-typing:\n".indent(2)+dependentErrorMsgs
          : "";
        return "("+ String.join(", ", call) +") <: "+tst1+dependentErrors;
      }).toList();
    var calls= String.join("\n", calls1);
    return Fail.callTypeError(e, expectedT(), calls).pos(e.pos());
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(tst.t().mdf(), expectedT().get().mdf());
  }
  default boolean okAll(List<E> es, List<T> ts, ArrayList<CompileError> errors, List<HashMap<T, Res>> caches) {
    assert es.size() == ts.size() && caches.size() == es.size();
    return IntStream.range(0, es.size())
      .allMatch(i->ok(es.get(i), ts.get(i), errors, caches.get(i)));
  }
  default boolean ok(E e, T t, ArrayList<CompileError> errors, HashMap<T, Res> cache) {
    var res = cache.computeIfAbsent(t, t_->e.accept(this.withT(Optional.of(t_))));
    // TODO: cache res based on the same visitor hashcode
    /*
     v has program (will stay the same)
     v has the T
     v has a gamma (should stay the same)
     v has a XBs (should stay the same)
     v has a depth (should stay the same)
     */
    if (res.t().isEmpty()){
      res.err().ifPresent(errors::add);
      return false;
    }
    return p().tryIsSubType(res.tOrThrow(), t);
  }

  default Optional<List<TsT>> multiMeth(T rec, MethName m, List<T> ts) {
    if (!(rec.rt() instanceof Id.IT<T> recIT)) { return Optional.empty(); }
    var sigs = p().meths(rec.mdf(), recIT, m, depth()).map(cm -> {
      var mdf = rec.mdf();
      List<TsT> candidates = new ArrayList<>(cm.sig().size());
      for (int i = cm.sig().size() - 1; i >= 0; --i) {
        var sig = cm.sig().get(i);
        Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(sig.gens(), ts).forEach(c::put));
        var xbs = xbs().addBounds(sig.gens(), sig.bounds());

        var params = Push.of(
          fancyRename(rec.rt().toString(), rec.withMdf(sig.mdf()), mdf, xsTsMap, TypeRename.RenameKind.Arg, xbs),
          Streams.zip(cm.xs(), sig.ts())
            .map((xi, ti)->fancyRename(xi+": "+ti.rt().toString(), ti, mdf, xsTsMap, TypeRename.RenameKind.Arg, xbs))
            .toList()
        );
        var t = fancyRename(sig.ret().rt().toString(), sig.ret(), mdf, xsTsMap, TypeRename.RenameKind.Return, xbs);

        candidates.add(new TsT(params, t));
      }
      return Collections.unmodifiableList(candidates);
    });
    return sigs.map(tsts->tsts.stream().map(this::allMeth).flatMap(List::stream).toList());
  }

  default List<TsT> allMeth(TsT tst) {
    return Stream.concat(Stream.of(
      tst,
      tst.renameMdfs(Map.of(
        Mdf.mut, Mdf.iso,
        Mdf.read, Mdf.imm
      )),
      tst.renameMdfs(Map.of(
        Mdf.readOnly, Mdf.imm,
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      )),
      tst.renameTsMdfs(Map.of(
        Mdf.read, Mdf.readOnly,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      )).renameTMdfs(Map.of(Mdf.mut,Mdf.lent,    Mdf.read,Mdf.readOnly))),
      oneLentToMut(tst).stream())
      .distinct()
      .toList();
  }

  /** This is [MDF, Xs=Ts] (recMdf rewriting for meth calls) */
  default T fancyRename(String x, T t, Mdf mdf0, Map<GX<T>,T> map, TypeRename.RenameKind kind, XBs xbs) {
    assert !mdf0.isMdf();
    Mdf mdf=t.mdf();
    return t.match(
      gx->{
        if(!mdf.isRecMdf()){
          var renamed = map.getOrDefault(gx, t);
          return TypeRename.core(p()).propagateMdf(mdf, renamed);
        }
        var ti = map.get(gx);
        if (ti == null) { return t; }

        return switch (kind) {
          case Return -> ti.withMdf(mdf0.adapt(ti));
          case Arg -> {
//            if (ti.mdf().isMdf()) { yield ti.withMdf(mdf0.adapt(ti)); } // this is probably unsound
            var newMdf = Gamma.xT(x, xbs, mdf0, ti, mdf0).mdf();
            yield ti.withMdf(newMdf);
          }
        };
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(x, ti, mdf0, map, TypeRename.RenameKind.Arg, xbs)).toList();
        if(!mdf.isRecMdf() && !mdf.isMdf()){ return new T(mdf, it.withTs(newTs)); }
        if(mdf0.isIso()) { return new T(Mdf.mut, it.withTs(newTs)); }
        return new T(mdf0, it.withTs(newTs));
      });
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut()?t.withMdf(Mdf.iso):t; }
  default T mutToLent(T t) { return t.mdf().isMut() ? t.withMdf(Mdf.lent) : t; }
  default TsT transformMuts(int i, List<T> ts, T t){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.lent)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0, mutToLent(t));
  }
  default List<TsT> oneLentToMut(TsT tst){
    var ts = tst.ts();
    var t = tst.t();
    Stream<TsT> r = Stream.of(new TsT(mutToIso(ts), mutToLent(t)));
    var muts = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isMut()).boxed().toList();
    Stream<TsT> ps=muts.stream()
      .map(i->transformMuts(i, ts, t));
    return Stream.concat(r,ps).toList();
  }

  static boolean containsRecMdf(T t) {
    return t.mdf().isRecMdf() || t.match(
      gx->false,
      it->it.ts().stream().anyMatch(EMethTypeSystem::containsRecMdf)
    );
  }

  record TsT(List<T> ts, T t){
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      var ts = renameTsMdfs(replacements).ts();
      var t = renameTMdfs(replacements).t();
      return new TsT(ts, t);
    }
    public TsT renameTsMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      return new TsT(ts, t);
    }
    public TsT renameTMdfs(Map<Mdf, Mdf> replacements) {
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return new TsT(ts, t);
    }
    @Override public String toString() {
      return "("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
    }
  }
}
