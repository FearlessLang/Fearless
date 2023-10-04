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
import utils.Box;
import utils.Streams;
import visitors.ShortCircuitVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static program.Program.filterByMdf;
import static program.Program.filterSig;

interface ELambdaTypeSystem extends ETypeSystem{
  default Res visitLambda(E.Lambda b){
    Mdf mdf=b.mdf();
    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    Dec d=new Dec(fresh,List.of(),Map.of(),b,b.pos());
    Program p0=p().withDec(d);

    var validMethods = b.meths().stream()
      .filter(m->filterByMdf(mdf,m.sigs().stream().map(E.Sig::mdf)))
      .toList();
    // todo: check set of meth names are equal instead
    if (validMethods.size() != b.meths().size()) {
      throw Fail.uncallableMeths(
        mdf,
        b.meths().stream().filter(m->!validMethods.contains(m)).toList()
      ).pos(b.pos());
    }

    var filtered=p0.meths(xbs(), Mdf.recMdf, d.toIT(), depth()+1).stream()
      .filter(cmi->filterByMdf(mdf,cmi.sig().stream().map(E.Sig::mdf)))
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
    var xbs = xbs();
    for (var gx : d.gxs()) {
      var bounds = d.bounds().get(gx);
      if (bounds == null || bounds.isEmpty()) { continue; }
      xbs = xbs.add(gx.name(), bounds);
    }
    var invalidGens = GenericBounds.validGenericLambda(p(), xbs, b);
    ELambdaTypeSystem boundedTypeSys = (ELambdaTypeSystem) withXBs(xbs);
    if (invalidGens.isPresent()) { return invalidGens.get().pos(b.pos()); }
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
        return boundedTypeSys.mOk(selfName, selfT, mi).stream();
      } catch (CompileError err) {
        return Optional.of(err.parentPos(mi.pos())).stream();
      }
    }).toList();
    if(mRes.isEmpty()){ return retT; }
    return mRes.get(0);
  }
  default Optional<CompileError> mOk(String selfName, T selfT, E.Meth m){
    var xbs_ = xbs();
    for (var gx : m.prioritySig().gens()) {
      var bounds = m.prioritySig().bounds().get(gx);
      if (bounds == null || bounds.isEmpty()) { continue; }
      xbs_ = xbs_.add(gx.name(), bounds);
    }
    final var xbs = xbs_;
    var typeSysBounded = (ELambdaTypeSystem) withXBs(xbs);

    var sigInvalid = Stream.concat(m.prioritySig().ts().stream(), Stream.of(m.prioritySig().ret()))
      .map(t->GenericBounds.validGenericT(p(), xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(err->err.pos(m.pos()))
      .findAny();
    if (sigInvalid.isPresent()) { return sigInvalid; }
    if(m.isAbs()){
      return Optional.empty();
    }

    return IntStream.range(0, m.sigs().size()).mapToObj(i->m.sigs().get((m.sigs().size() - 1) - i))
      .filter(sig->filterSig(selfT.mdf(), sig.mdf()))
      .map(sig->typeSysBounded.mOkEntry(selfName, selfT, m, sig))
      .filter(Optional::isPresent)
      .findFirst()
      .flatMap(x->x);
  }

  default Optional<CompileError> mOkEntry(String selfName, T selfT, E.Meth m, E.Sig sig) {
    var e   = m.body().orElseThrow();
    var mMdf = sig.mdf();

    var args = sig.ts();
    var ret = sig.ret();
    assert !selfT.mdf().isMdf() || g().dom().isEmpty();
    var g0  = g().captureSelf(xbs(), selfName, selfT, mMdf);
    Mdf selfTMdf = g0.get(selfName).mdf();
    var gg  = Streams.zip(m.xs(), args).fold(Gamma::add, g0);

    var baseCase = topLevelIso(gg, m, e, ret);
    var baseDestiny = baseCase.isEmpty() || ret.mdf().is(Mdf.mut, Mdf.read);
    if (baseDestiny) { return baseCase; }
    //res is iso or imm, thus is promotable

    var criticalFailure = topLevelIso(gg, m, e, ret.withMdf(Mdf.readOnly));
    if (criticalFailure.isPresent()) { return baseCase; }

    var readPromotion = mOkReadPromotion(selfName, selfT, m, sig);
    if (readPromotion.isEmpty() || !ret.mdf().is(Mdf.imm, Mdf.iso)) {
      return readPromotion.flatMap(ignored->baseCase);
    }

    var isoPromotion = mOkIsoPromotion(selfName, selfT, m, sig);
    if(ret.mdf().isIso() || isoPromotion.isEmpty()){ return isoPromotion; }

    return mOkImmPromotion(selfName, selfT, m, sig, selfTMdf).flatMap(ignored->baseCase);
  }

  default Optional<CompileError> mOkReadPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    var readOnlyAsReadG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).map(t->t.mdf().isReadOnly() ? t.withMdf(Mdf.read) : t);
      }
      @Override public List<String> dom() { return g().dom(); }
    };
    var mMdf = sig.mdf();
    var g0 = readOnlyAsReadG.captureSelf(xbs(), selfName, selfT, mMdf.isReadOnly() ? Mdf.read : mMdf);
    var gg  = Streams.zip(
      m.xs(),
     sig.ts().stream().map(t->t.mdf().isReadOnly() ? t.withMdf(Mdf.read) : t).toList()
    ).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret());
  }

  default Optional<CompileError> mOkIsoPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    Function<T, T> mdfTransform = t->{
      if (t.mdf().isMut()) { return t.withMdf(Mdf.lent); }
      if (t.mdf().isRead()) { return t.withMdf(Mdf.readOnly); }
      return t;
    };
    var mutAsLentG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).map(mdfTransform);
      }
      @Override public List<String> dom() { return g().dom(); }
    };
    var mMdf = mdfTransform.apply(selfT.withMdf(sig.mdf())).mdf();
    var g0 = mutAsLentG.captureSelf(xbs(), selfName, selfT, mMdf.isMut() ? Mdf.lent : mMdf);
    var gg  = Streams.zip(
      m.xs(),
      sig.ts().stream().map(mdfTransform).toList()
    ).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.mut));
  }

  default Optional<CompileError> mOkImmPromotion(String selfName, T selfT, E.Meth m, E.Sig sig, Mdf selfTMdf) {
    var noMutyG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).flatMap(t->{
          if (t.mdf().isLikeMut() || t.mdf().isRecMdf()) { return Optional.empty(); }
          return Optional.of(t);
        });
      }
      @Override public List<String> dom() { return g().dom(); }
    };
    var mMdf = sig.mdf();
    var g0 = selfTMdf.isLikeMut() || selfTMdf.isRecMdf() ? Gamma.empty() : noMutyG.captureSelf(xbs(), selfName, selfT, mMdf);
    var gg = Streams.zip(m.xs(), sig.ts()).filter((x,t)->!t.mdf().isLikeMut() && !t.mdf().isRecMdf()).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.readOnly));
  }

  /**
   * G1,x:iso ITX,G2;XBs |= e : T               (TopLevel-iso)
   *   where
   *   G1,x:mut ITX,G2;XBs |= e : T
   */
  default Optional<CompileError> topLevelIso(Gamma g, E.Meth m, E e, T expected) {
    var res = isoAwareJudgment(g, m, e, expected);
    if (res.isEmpty()) { return res; }
    var isoNames = g.dom().stream().filter(x->{
      try {
        return g.get(x).mdf().isIso();
      } catch (CompileError err) {
        // we cannot capture something it's not in our domain, so skip it
        return false;
      }
    }).toList();

    for (var name : isoNames) {
      var g_ = g.add(name, g.get(name).withMdf(Mdf.mut));
      if (isoAwareJudgment(g_, m, e, expected).isEmpty()) { return Optional.empty(); }
    }
    return res;
  }

  /** G;XBs |= e : T */
  default Optional<CompileError> isoAwareJudgment(Gamma g, E.Meth m, E e, T expected) {
    return okWithSubType(g, m, e, expected).or(()->g.dom().stream()
      .filter(x->{
        try {
          var xT = g.get(x);
          return xT.mdf().isIso() || (xT.isMdfX() && xbs().get(xT.gxOrThrow()).contains(Mdf.iso));
        } catch (CompileError err) {
          // we cannot capture something it's not in our domain, so skip it
          return false;
        }
      })
      .map(x->{
        var nUsages = new Box<>(0);
        var hasCapturedX = new Box<>(false);
        return e.accept(new ShortCircuitVisitor<CompileError>(){
          @Override public Optional<CompileError> visitLambda(E.Lambda e) {
            if (hasCapturedX.get()) { return Optional.empty(); }
            return new ShortCircuitVisitor<CompileError>(){
              @Override public Optional<CompileError> visitX(E.X e) {
                if (!e.name().equals(x)) { return ShortCircuitVisitor.super.visitX(e); }
                hasCapturedX.set(true);
                if (nUsages.get() > 0) { return Optional.of(Fail.multipleIsoUsage(e).pos(e.pos())); }
                return Optional.empty();
              }
            }.visitLambda(e);
          }

          @Override public Optional<CompileError> visitX(E.X e) {
            if (!e.name().equals(x)) { return ShortCircuitVisitor.super.visitX(e); }
            if (hasCapturedX.get()) {
              return Optional.of(Fail.multipleIsoUsage(e).pos(e.pos()));
            }
            int n = nUsages.update(usages->usages+1);
            if (n > 1) { return Optional.of(Fail.multipleIsoUsage(e).pos(e.pos())); }
            return Optional.empty();
          }
        });
      })
      .filter(Optional::isPresent)
      .findFirst()
      .flatMap(opt->opt)
    );
  }

  default Optional<CompileError> okWithSubType(Gamma g, E.Meth m, E e, T expected) {
    var res = e.accept(ETypeSystem.of(p(), g, xbs(), Optional.of(expected), depth()+1));
    try {
      var subOk = res.t()
        .flatMap(ti->p().isSubType(xbs(), ti, expected)
          ? Optional.empty()
          : Optional.of(Fail.methTypeError(expected, ti, m.name()).pos(m.pos()))
        );
      return res.err().or(()->subOk);
    } catch (CompileError err) {
      return Optional.of(err.parentPos(e.pos()));
    }
  }
}
