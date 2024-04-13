package program.typesystem;

import ast.E;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import failure.TypeSystemErrors;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static program.Program.filterByMdf;

interface ELambdaTypeSystem extends ETypeSystem{
  default Optional<Supplier<? extends CompileError>> visitLambda(E.Lambda b){
    Mdf mdf=b.mdf();
    Id.DecId fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    Dec d=new Dec(fresh, List.of(),Map.of(),b,b.pos());
    Program p0=p().withDec(d);

    var expected = expectedT().orElseThrow();
    if (!p0.isSubType(xbs(), new T(b.mdf(), d.toIT()), expected)) {
      return Optional.of(()->Fail.lambdaTypeError(expected).pos(b.pos()));
    }

    var validMethods = b.meths().stream()
      .filter(m->filterByMdf(mdf, m.sig().mdf()))
      .toList();

    var validMethNames = validMethods.stream().map(E.Meth::name).collect(Collectors.toSet());
    if (b.meths().stream().anyMatch(m -> !validMethNames.contains(m.name()))) {
      throw Fail.uncallableMeths(
        mdf,
        b.meths().stream().filter(m->!validMethods.contains(m)).toList()
      ).pos(b.pos());
    }

    var filtered=p0.meths(xbs(), Mdf.recMdf, d.toIT(), depth()+1).stream()
      .filter(cmi->filterByMdf(mdf, cmi.sig().mdf()))
      .toList();
    var sadlyAbs=filtered.stream()
      .filter(CM::isAbs)
      .toList();
    if (!sadlyAbs.isEmpty()) {
      return Optional.of(()->Fail.unimplementedInLambda(sadlyAbs).pos(b.pos()));
    }
    var sadlyExtra=b.meths().stream()
      .filter(m->filtered.stream().noneMatch(cm->cm.name().equals(m.name())))
      .toList();
    assert sadlyExtra.isEmpty();//TODO: can we break this assertion? We think no.
    return ((ELambdaTypeSystem)withProgram(p0)).bothT(d);
  }

  default Optional<Supplier<? extends CompileError>> bothT(Dec d) {
    var b = d.lambda();
    if (expectedT().map(t->t.rt() instanceof Id.GX<T>).orElse(false)) {
      return Optional.of(()->Fail.bothTExpectedGens(expectedT().orElseThrow(), d.name()).pos(b.pos()));
    }
    var xbs = xbs();
    for (var gx : d.gxs()) {
      var bounds = d.bounds().get(gx);
      if (bounds == null || bounds.isEmpty()) { continue; }
      xbs = xbs.add(gx.name(), bounds);
    }
    var invalidGens = GenericBounds.validGenericLambda(p(), xbs, b);
    XBs finalXbs = xbs;
    Supplier<ELambdaTypeSystem> boundedTypeSys = ()->(ELambdaTypeSystem) ETypeSystem.of(p().shallowClone(), g(), finalXbs, expectedT(), resolvedCalls(), depth());
    if (invalidGens.isPresent()) {
      return Optional.of(()->invalidGens.get().get().pos(b.pos()));
    }
    //var errMdf = expectedT.map(ti->!p().isSubType(ti.mdf(),b.mdf())).orElse(false);
    //after discussion, line above not needed
    var its = p().itsOf(d.toIT());
    var expectedT=expectedT().stream()
      .filter(ti->ti.match(gx->false, its::contains))
      .findFirst();
    T retT = expectedT //TOP LEVEL = declared type
      .map(t->t.withMdf(b.mdf()))
      .orElseGet(()->new T(b.mdf(), b.its().getFirst()));
    T selfT = new T(b.mdf(), d.toIT());
    var selfName=b.selfName();
    List<Supplier<? extends CompileError>> mRes = b.meths().parallelStream().flatMap(mi->{
      try {
        return boundedTypeSys.get()
          .mOk(selfName, selfT, mi)
          .stream()
          .map(rawError->()->TypeSystemErrors.fromMethodError(rawError.get()));
      } catch (CompileError err) {
        return Optional.<Supplier<? extends CompileError>>of(()->{
          var rawError = err.parentPos(mi.pos());
          return TypeSystemErrors.fromMethodError(rawError);
        }).stream();
      }
    }).toList();
    if(mRes.isEmpty()){ return Optional.empty(); }
    return Optional.of(mRes.getFirst());
  }
  default Optional<Supplier<? extends CompileError>> mOk(String selfName, T selfT, E.Meth m){
    var xbs_ = xbs();
    for (var gx : m.sig().gens()) {
      var bounds = m.sig().bounds().get(gx);
      if (bounds == null || bounds.isEmpty()) { continue; }
      xbs_ = xbs_.add(gx.name(), bounds);
    }
    final var xbs = xbs_;
    var typeSysBounded = (ELambdaTypeSystem) withXBs(xbs);

    var sigInvalid = Stream.concat(m.sig().ts().stream(), Stream.of(m.sig().ret()))
      .map(t->GenericBounds.validGenericT(p(), xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .<Supplier<? extends CompileError>>map(errorSupplier->()->errorSupplier.get().pos(m.pos()))
      .findAny();
    if (sigInvalid.isPresent()) { return sigInvalid; }
    if(m.isAbs()){
      return Optional.empty();
    }

    return typeSysBounded.mOkEntry(selfName, selfT, m, m.sig());
  }

  default Optional<Supplier<? extends CompileError>> mOkEntry(String selfName, T selfT, E.Meth m, E.Sig sig) {
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

  default Optional<Supplier<? extends CompileError>> mOkReadPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    var readOnlyAsReadG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).map(t->{
          if (t.mdf().isMdf()) { return t.withMdf(Mdf.iso); }
          return t.mdf().isReadOnly() ? t.withMdf(Mdf.read) : t;
        });
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

  default Optional<Supplier<? extends CompileError>> mOkIsoPromotion(String selfName, T selfT, E.Meth m, E.Sig sig) {
    Function<T, T> mdfTransform = t->{
      if (t.mdf().isMut()) { return t.withMdf(Mdf.lent); }
      if (t.mdf().is(Mdf.read, Mdf.readImm)) { return t.withMdf(Mdf.readOnly); }
      return t;
    };
    var mutAsLentG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).filter(t->!t.mdf().isMdf()).map(mdfTransform);
      }
      @Override public List<String> dom() { return g().dom(); }
    };
    var mMdf = mdfTransform.apply(selfT.withMdf(sig.mdf())).mdf();
    var g0 = mutAsLentG.captureSelf(xbs(), selfName, selfT, mMdf.isMut() ? Mdf.lent : mMdf);
    var gg  = Streams.zip(
      m.xs(),
      sig.ts().stream().map(mdfTransform).toList()
    ).filter((x,t)->!t.mdf().isMdf()).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.mut));
  }

  default Optional<Supplier<? extends CompileError>> mOkImmPromotion(String selfName, T selfT, E.Meth m, E.Sig sig, Mdf selfTMdf) {
    var noMutyG = new Gamma() {
      @Override public Optional<T> getO(String x) {
        return g().getO(x).filter(t->!(t.mdf().isLikeMut() || t.mdf().isRecMdf() || t.mdf().isMdf()));
      }
      @Override public List<String> dom() { return g().dom(); }
    };
    var mMdf = sig.mdf();
    var g0 = selfTMdf.isLikeMut() || selfTMdf.isRecMdf() ? Gamma.empty() : noMutyG.captureSelf(xbs(), selfName, selfT, mMdf);
    var gg = Streams.zip(m.xs(), sig.ts()).filter((x,t)->!t.mdf().isLikeMut() && !t.mdf().isMdf() && !t.mdf().isRecMdf()).fold(Gamma::add, g0);
    return topLevelIso(gg, m, m.body().orElseThrow(), sig.ret().withMdf(Mdf.readOnly));
  }

  /**
   * G1,x:iso ITX,G2;XBs |= e : T               (TopLevel-iso)
   *   where
   *   G1,x:mut ITX,G2;XBs |= e : T
   */
  default Optional<Supplier<? extends CompileError>> topLevelIso(Gamma g, E.Meth m, E e, T expected) {
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
  default Optional<Supplier<? extends CompileError>> isoAwareJudgment(Gamma g, E.Meth m, E e, T expected) {
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
        return e.accept(new ShortCircuitVisitor<Supplier<? extends CompileError>>(){
          @Override public Optional<Supplier<? extends CompileError>> visitLambda(E.Lambda e) {
            if (hasCapturedX.get()) { return Optional.empty(); }
            return new ShortCircuitVisitor<Supplier<? extends CompileError>>(){
              @Override public Optional<Supplier<? extends CompileError>> visitX(E.X e) {
                if (!e.name().equals(x)) { return ShortCircuitVisitor.super.visitX(e); }
                hasCapturedX.set(true);
                if (nUsages.get() > 0) { return Optional.of(()->Fail.multipleIsoUsage(e).pos(e.pos())); }
                return Optional.empty();
              }
            }.visitLambda(e);
          }

          @Override public Optional<Supplier<? extends CompileError>> visitX(E.X e) {
            if (!e.name().equals(x)) { return ShortCircuitVisitor.super.visitX(e); }
            if (hasCapturedX.get()) {
              return Optional.of(()->Fail.multipleIsoUsage(e).pos(e.pos()));
            }
            int n = nUsages.update(usages->usages+1);
            if (n > 1) { return Optional.of(()->Fail.multipleIsoUsage(e).pos(e.pos())); }
            return Optional.empty();
          }
        });
      })
      .filter(Optional::isPresent)
      .findFirst()
      .flatMap(opt->opt)
    );
  }

  default Optional<Supplier<? extends CompileError>> okWithSubType(Gamma g, E.Meth m, E e, T expected) {
    var res = e.accept(ETypeSystem.of(p(), g, xbs(), Optional.of(expected), resolvedCalls(), depth()+1));
    return res.map(err->()->err.get().parentPos(m.pos()));
  }
}
