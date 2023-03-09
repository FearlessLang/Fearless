package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import id.Id.GX;
import program.TypeRename;
import utils.Bug;
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
    var optTst=multiMeth(t_,e.name(),e.ts());
    if(optTst.isEmpty()){ return new CompileError(); } //TODO: list the available methods
    List<TsT> tst = optTst.get().stream()
      .filter(this::filterOnRes)
      .toList();

    if (tst.isEmpty()) {
      return Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.get()).pos(e.pos());
    }

    List<E> es = Push.of(e0,e.es());
    for (var tsti : tst) {
      if(okAll(es, tsti.ts())) {
        return tsti.t();
      }
    }

    String calls = tst.stream()
      .map(tst1->{
        var call = Streams.zip(es, tst1.ts())
          .map((e1,t1)->{
            var getT = this.withT(Optional.of(t1));
//            return e1.accept(getT).t().map(e1T->e1+": "+e1T);
            return e1.accept(getT).t().map(T::toString).orElseGet(()->"?"+e1+"?");
          })
          .toList();
        return "("+ String.join(", ", call) +") <: "+tst1;
      })
      .collect(Collectors.joining("\n"));
    return Fail.callTypeError(e, calls).pos(e.pos());
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(tst.t().mdf(), expectedT().get().mdf());
  }
  default boolean okAll(List<E>es,List<T> ts) {
    return Streams.zip(es,ts).allMatch(this::ok);
  }
  default boolean ok(E e,T t) {
    var v = this.withT(Optional.of(t));
    var res = e.accept(v);
    if (res.t().isEmpty()){
      return false;
    }
    return p().tryIsSubType(res.tOrThrow(), t);
  }

  default Optional<List<TsT>> multiMeth(T rec, MethName m, List<T> ts) {
    return extractMeth(rec, m, ts).map(this::allMeth);
  }

  default List<TsT> allMeth(TsT tst) {
    return Stream.concat(Stream.of(
      tst,
      tst.renameMdfs(Map.of(Mdf.mut, Mdf.iso)),
      tst.renameMdfs(Map.of(
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      ))),
      oneLentToMut(tst).stream()
    ).toList();
  }

  default Optional<TsT> extractMeth(T rec, MethName m, List<T> ts) {
    if (!(rec.rt() instanceof Id.IT<T> recIT)) { return Optional.empty(); }
    return p().meths(recIT, m, depth()).map(cm -> {
      var mdf = rec.mdf();
      var mdf0 = cm.mdf();
      Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(cm.sig().gens(), ts).forEach(c::put));
      var t0 = rec.withMdf(mdf0);
      var params = Push.of(
        t0,
        cm.sig().ts().stream().map(ti->fancyRename(ti, mdf, xsTsMap)).toList()
      );
      var t = fancyRename(cm.ret(), mdf, xsTsMap);
      return new TsT(params, t);
    });
  }

  default T fancyRename(T t,Mdf mdf0, Map<GX<T>,T> map) {//[MDF, Xs=Ts]
    Mdf mdf=t.mdf();
    return t.match(
      gx->{
        if(!mdf.isRecMdf()){ return map.getOrDefault(gx,t); }
        var ti = map.getOrDefault(gx,t);
        return ti.withMdf(mdf0.adapt(ti));
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(ti,mdf0,map)).toList();
        if(!mdf.isRecMdf()){ return new T(mdf,it.withTs(newTs)); }
        if(!mdf0.isIso()){ return new T(mdf0,it.withTs(newTs)); }
        return new T(Mdf.mut,it.withTs(newTs));
      });
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut()?t.withMdf(Mdf.iso):t; }
  default TsT transformLents(int i,List<T> ts, T t){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.mut)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0,mutToIso(t));
  }
  default List<TsT> oneLentToMut(TsT tst){
    Stream<TsT> r = Stream.of();
    var ts = tst.ts();
    var t = tst.t();
    if(t.mdf().isMut()){ r=Stream.of(new TsT(mutToIso(ts),t.withMdf(Mdf.mut))); }
    var lents = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isLent()).boxed().toList();
    Stream<TsT> ps=lents.stream()
      .map(i->transformLents(i,ts,t));
    return Stream.concat(r,ps).toList();
  }

  record TsT(List<T> ts, T t){
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return new TsT(ts, t);
    }
  }
}
