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
import program.TypeRename;
import utils.Streams;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

interface ELambdaTypeSystem extends ETypeSystem{
  default Res visitLambda(E.Lambda b){
    Mdf mdf=b.mdf();
//    var parent = b.its().get(0);
//    var parentGxs = p().gxsOf(parent).stream().toList(); // TODO: why parentGXs here?
//    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), parentGxs.size());
//    Dec d=new Dec(fresh,parentGxs,b,b.pos());
//    var gxs = b.its().stream().flatMap(it->it.ts().stream().flatMap(T::deepGXs)).distinct().toList();
    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    Dec d=new Dec(fresh,List.of(),Map.of(),b,b.pos());
    Program p0=p().withDec(d);
    var validMethods = b.meths().stream()
      .filter(m->filterByMdf(mdf,m.sig().mdf()))
      .toList();
    if (validMethods.size() != b.meths().size()) {
      throw Fail.uncallableMeths(
        mdf,
        b.meths().stream().filter(m->!validMethods.contains(m)).toList()
      ).pos(b.pos());
    }

    var filtered=p0.meths(Mdf.recMdf, d.toIT(), depth()+1).stream()
      .filter(cmi->filterByMdf(mdf,cmi.mdf()))
      .toList();
    var sadlyAbs=filtered.stream()
      .filter(CM::isAbs)
      .toList();
    if (!sadlyAbs.isEmpty()) {
      return Fail.unimplementedInLambda(sadlyAbs).pos(b.pos());
    }
    var sadlyExtra=b.meths().stream()
      .filter(m->filtered.stream().noneMatch(cm->cm.name().equals(m.name())))
      .toList();
    assert sadlyExtra.isEmpty();//TODO: can we break this assertion? We think no.
    return withProgram(p0).bothT(d);
  }

  default Res bothT(Dec d) {
    var b = d.lambda();
    if (expectedT().map(t->t.rt() instanceof Id.GX<T>).orElse(false)) {
      throw Fail.bothTExpectedGens(expectedT().orElseThrow(), d.name()).pos(b.pos());
    }
    //var errMdf = expectedT.map(ti->!p().isSubType(ti.mdf(),b.mdf())).orElse(false);
    //after discussion, line above not needed
    var its = p().itsOf(d.toIT());
    var expectedT=expectedT().stream()
      .filter(ti->ti.match(gx->false, its::contains))
      .findFirst();
    T retT = expectedT //TOP LEVEL = declared type
      .map(t->t.withMdf(b.mdf()))
      .orElseGet(()->new T(b.mdf(), b.its().get(0)));
    T selfT = new T(b.mdf(), d.toIT());
    var selfName=b.selfName();
    List<CompileError> mRes = b.meths().stream().flatMap(mi->{
      try {
        return mOk(selfName, selfT, mi).stream();
      } catch (CompileError err) {
        return Optional.of(err.parentPos(mi.pos())).stream();
      }
    }).toList();
    if(mRes.isEmpty()){ return retT; }
    return mRes.get(0);
  }
  default Optional<CompileError> mOk(String selfName, T selfT, E.Meth m){
    if(m.isAbs()){ return Optional.empty(); }
    var e   = m.body().orElseThrow();
    var mMdf = m.sig().mdf();

//    var selfTi = selfT;
    var args = m.sig().ts();
    var ret = m.sig().ret();
    // todo: assert empty gamma for MDF mdf
    var g0  = g().captureSelf(xbs(), selfName, selfT, mMdf);
    Mdf selfTMdf = g0.get(selfName).mdf();
    var gg  = Streams.zip(m.xs(), args).fold(Gamma::add, g0);

    var baseCase=okWithSubType(gg, m, e, ret);
    var baseDestiny=baseCase.isEmpty() || !ret.mdf().is(Mdf.iso, Mdf.imm);
    if(baseDestiny){ return baseCase; }
    //res is iso or imm, thus is promotable

    var criticalFailure = okWithSubType(gg, m, e, ret.withMdf(Mdf.read));
    if (criticalFailure.isPresent()) { return baseCase; }

//    var isoPromotion1 = okWithSubType(gg, m, e, ret.withMdf(Mdf.mut));
//    var has1OrLessMutArgs = args.stream().filter(t->t.mdf().isMut()).skip(1).findAny().isEmpty();
//    if (m.sig().mdf().isImm() && has1OrLessMutArgs && (ret.mdf().isIso() || isoPromotion1.isEmpty())) {
//      return isoPromotion1;
//    }

    Gamma mutAsLentG = x->g().getO(x).map(t->t.mdf().isMut() ? t.withMdf(Mdf.lent) : t);
    g0 = mutAsLentG.captureSelf(xbs(), selfName, selfT, mMdf.isMut() ? Mdf.lent : mMdf);
    gg  = Streams.zip(
      m.xs(),
      args.stream().map(t->t.mdf().isMut() ? t.withMdf(Mdf.lent) : t).toList()
    ).fold(Gamma::add, g0);
    var isoPromotion2 = okWithSubType(gg, m, e, ret.withMdf(Mdf.mut));
    if(ret.mdf().isIso() || isoPromotion2.isEmpty()){ return isoPromotion2; }

    Gamma noMutyG = x->g().getO(x).flatMap(t->{
      if (t.mdf().isLikeMut() || t.mdf().isRecMdf()) { return Optional.empty(); }
      return Optional.of(t);
    });
    g0 = selfTMdf.isLikeMut() || selfTMdf.isRecMdf() ? Gamma.empty() : noMutyG.captureSelf(xbs(), selfName, selfT, mMdf);
    gg = Streams.zip(m.xs(), args).filter((x,t)->!t.mdf().isLikeMut() && !t.mdf().isRecMdf()).fold(Gamma::add, g0);
    return okWithSubType(gg, m, e, ret.withMdf(Mdf.read)).flatMap(ignored->baseCase);
  }

//  Box<XTsMap> xTsMap = new Box<>(XTsMap.empty());
  default Optional<CompileError> okWithSubType(Gamma g, E.Meth m, E e, T expected) {
    var res = e.accept(ETypeSystem.of(p(), g, xbs(), Optional.of(expected), depth()+1));
    try {
      var subOk = res.t()
        .flatMap(ti->p().isSubType(ti, expected)
          ? Optional.empty()
          : Optional.of(Fail.methTypeError(expected, ti, m.name()).pos(m.pos()))
        );
      return res.err().or(()->subOk);
    } catch (CompileError err) {
      return Optional.of(err.parentPos(e.pos()));
    }
  }

  default boolean filterByMdf(Mdf mdf, Mdf mMdf) {
    assert !mdf.isMdf();
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.recMdf)) { return true; }
    if (mdf.isLent() && !mMdf.isIso()) { return true; }
    return mdf.is(Mdf.imm, Mdf.read) && mMdf.is(Mdf.imm, Mdf.read, Mdf.recMdf);
  }
}
