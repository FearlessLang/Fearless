package program.typesystem;

import ast.E;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Mdf;
import program.CM;
import program.Program;
import utils.Streams;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

interface ELambdaTypeSystem extends ETypeSystem{
  default Res visitLambda(E.Lambda b){
    Mdf mdf=b.mdf();
    var parent = b.its().get(0);
    var parentGxs = p().gxsOf(parent).stream().toList(); // TODO: why parentGXs here?
    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), parentGxs.size());
    Dec d=new Dec(fresh,parentGxs,b,b.pos());
    Program p0=p().withDec(d);
    var filtered=p0.meths(d.toIT(), depth()+1).stream()
      .filter(cmi->filterByMdf(mdf,cmi))
      .toList();
    var sadlyAbs=filtered.stream()
      .filter(CM::isAbs)
      .toList();
    if (!sadlyAbs.isEmpty()) {
      return Fail.unimplementedInLambda(sadlyAbs);
    }
    var sadlyExtra=b.meths().stream()
      .filter(m->filtered.stream().anyMatch(cm->cm.name().equals(m.name())))
      .toList();
//    assert sadlyExtra.isEmpty();//TODO: can we break this assertion? Yes. If we override a method.
      // TODO: figure out a better error for the case where a non-overriden method is added (or can we just allow it?)
    return withProgram(p0).bothT(d);
  }

  default Res bothT(Dec d) {
    var b = d.lambda();
    //var errMdf = expectedT.map(ti->!p().isSubType(ti.mdf(),b.mdf())).orElse(false);
    //after discussion, line above not needed
    var expectedT=expectedT().stream()
      .filter(ti->ti.match(gx->true, it->b.its().contains(it)))
      .findFirst();
    T retT = expectedT //TOP LEVEL = declared type
      .map(t->t.withMdf(b.mdf()))
      .orElseGet(()->new T(b.mdf(), b.its().get(0)));
    T selfT = new T(b.mdf(),d.toIT());
    var selfName=b.selfName();
    var mRes=b.meths().stream().flatMap(mi->mOk(selfName,selfT,mi).stream()).toList();
    if(mRes.isEmpty()){ return retT; }
    return mRes.get(0);
  }
  default Optional<CompileError> mOk(String x, T t, E.Meth m){
    if(m.isAbs()){ return Optional.empty(); }
    var mMdf=m.sig().mdf();
    var g0  = g().capture(p(),x,t,mMdf);
    var gg  = Streams.zip(m.xs(),m.sig().ts()).fold(Gamma::add, g0);
    var e   = m.body().orElseThrow();
    Res res = e.accept(ETypeSystem.of(p(),gg,Optional.of(m.sig().ret()),depth()+1));
    var subOk=res.t()
      .flatMap(ti->p().isSubType(ti,m.sig().ret())
        ? Optional.empty()
        : Optional.of(Fail.methTypeError(m.sig().ret(), ti, m.name()).pos(m.pos()))
      );
    return res.err().or(()->subOk);
  }

  default boolean filterByMdf(Mdf mdf, CM m) {
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.lent)) { return true; }
    return mdf.is(Mdf.imm, Mdf.read) && m.mdf().is(Mdf.imm, Mdf.read);
  }
}
