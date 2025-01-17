package program.inference;

import astFull.E;
import astFull.T;
import failure.CompileError;
import failure.Fail;
import failure.TypingAndInferenceErrors;
import id.Id;
import id.Mdf;
import magic.Magic;
import program.CM;
import program.typesystem.XBs;
import utils.*;
import visitors.InjectionVisitor;
import visitors.ShallowInjectionVisitor;

import java.util.*;
import java.util.stream.Collectors;

public record InferBodies(ast.Program p) {
  public static ast.Program inferAll(astFull.Program fullProgram){
    var inferredSigs = fullProgram.inferSignatures();
    var coreP = ShallowInjectionVisitor.of().visitProgram(inferredSigs);
    return new ast.Program(inferredSigs.tsf(), inferDecs(coreP, inferredSigs), Map.of());
  }

  static Map<Id.DecId, ast.T.Dec> inferDecs(ast.Program p, astFull.Program fullProgram){
    var inferBodies = new InferBodies(p);
    return fullProgram.ds().values().parallelStream()
      .map(inferBodies::inferDec)
      .collect(Collectors.toConcurrentMap(ast.T.Dec::name, d->d));
  }
  ast.T.Dec inferDec(astFull.T.Dec d){
    var coreDecl = p.ds().get(d.name());
    var l = coreDecl.lambda();
    return coreDecl.withLambda(l.withMeths(
      Streams.zip(d.lambda().meths(),l.meths())
        .map((fullMeth, coreMeth)->fullMeth.body().map(b->inferMethBody(coreDecl, b, coreMeth)).orElse(coreMeth.withBody(Optional.empty())))
        .toList()
    ));
  }
  ast.E.Meth inferMethBody(ast.T.Dec dec, E e, ast.E.Meth coreMeth) {
    var refiner = new RefineTypes(p);
    assert dec.bounds().keySet().containsAll(dec.gxs());
    assert coreMeth.sig().bounds().keySet()
      .containsAll(coreMeth.sig().gens());
    var iV = InjectionVisitor.of(dec.bounds())
      .withMoreBounds(coreMeth.sig().bounds());
    var type = refiner.fixType(e, coreMeth.sig().toAstFullSig().ret());
    ast.E newBody;try{newBody = fixInferStep(iGOf(dec, coreMeth), type, 0).accept(iV);}
    catch (StackOverflowError err) {
      throw Fail.inferFailed(e.toString()).pos(e.pos());
    }
    return coreMeth.withBody(Optional.of(newBody));
  }

  private Map<String, astFull.T> iGOf(ast.T.Dec dec, ast.E.Meth m) {
    assert dec.lambda().selfName() != null;
    var t = new astFull.T(m.mdf(), dec.toIT().toFullAstIT(ast.T::toAstFullT));
    return iGOf(dec.lambda().selfName(), t, m);
  }

  private Map<String, astFull.T> iGOf(String selfName, astFull.T lambdaT, ast.E.Meth m) {
    Map<String, astFull.T> gamma = new HashMap<>();
    assert selfName != null;
    gamma.put(selfName, lambdaT);
    var sig = m.sig();
//    var sig = p.fullSig(lambdaT.itOrThrow(), cm->cm.name().equals(m.name())).orElseThrow();
    Streams.zip(m.xs(), sig.ts()).forEach((k,t)->gamma.put(k, t.toAstFullT()));
    return Collections.unmodifiableMap(gamma);
  }

  //TODO: this may have to become iterative if the recursion gets out of control
  E fixInferStep(Map<String, T> gamma, E e, int depth) {
    var next = inferStep(gamma, e, depth);
    assert next.map(ei->!ei.equals(e)).orElse(true);
    if (next.isEmpty()) { return e; }
    return fixInferStep(gamma, next.get(), depth);
  }

  // rule name version
  Optional<E> inferStep(Map<String, T> gamma, E e, int depth){
    return switch(e){
      case E.X e1->var(gamma, e1);
      case E.MCall e1->methCall(gamma, e1, depth);
      case E.Lambda e1->bProp(gamma, e1, depth + 1);
      };
  }

  // propagation
  Optional<E> refineLambda(E.Lambda e, E.Lambda baseLambda, int depth){
    var anyNoSig = e.meths().stream().anyMatch(mi->mi.sig().isEmpty());
    if(anyNoSig){ return Optional.of(baseLambda); }
    return Optional.of(new RefineTypes(p).fixLambda(baseLambda, depth));
  }
  Optional<E> bProp(Map<String, T> gamma, E.Lambda e, int depth) {
    if (e.it().isEmpty()) { return Optional.empty(); }
    var fixedLambda = (E.Lambda) refineLambda(e, e, depth).orElse(e);
    List<E.Meth> oldMs= fixedLambda.meths();
    List<E.Meth> newMs= new ArrayList<>();
    boolean done= false;
    for (E.Meth mi : oldMs) {
      Optional<List<E.Meth>> bProp= bProp(gamma, mi, fixedLambda, depth);
      if (bProp.isEmpty()) { newMs.add(mi); continue; }
      var err= bProp.get().isEmpty();
      if(err){ throw Fail.inferFailed("Could not infer method "+
        mi.name().map(Id.MethName::name).orElse("-name to infer-")
        +" on:\n"+e).pos(e.pos()); }
      newMs.addAll(bProp.get());
      done= true;
    }
    if (!done) { return Optional.empty(); }
    newMs = Collections.unmodifiableList(newMs);
    var res = refineLambda(e, e.withMeths(newMs), depth);
    // TODO: why can't we get rid of this?
    // var res = Optional.of(e.withMeths(newMs));
    return res.flatMap(e1->!e1.equals(e) ? res : Optional.empty());
  }
  Optional<List<E.Meth>> bProp(Map<String, T> gamma, E.Meth m, E.Lambda e, int depth) {
    var res = bPropWithSig(gamma,m,e,depth).map(List::of)
      .or(()->bPropGetSigM(gamma,m,e,depth))
      .or(()->bPropGetSig(gamma,m,e,depth));
    assert res.stream().flatMap(List::stream).noneMatch(m::equals);
    return res;
  }
  Optional<E.Meth> bPropWithSig(Map<String, T> gamma, E.Meth m, E.Lambda e, int depth) {
    var anyNoSig = e.meths().stream().anyMatch(mi->mi.sig().isEmpty());
    if(anyNoSig){ return Optional.empty(); }
    if(m.body().isEmpty()){ return Optional.empty(); }
    if(m.sig().isEmpty()){ return Optional.empty(); }

    var sig = m.sig().orElseThrow();
    Map<String, T> richGamma = new HashMap<>(gamma);
    assert e.selfName() != null;
    richGamma.put(e.selfName(),new T(m.mdf().orElseThrow(), e.it().orElseThrow()));
    Streams.zip(m.xs(), sig.ts()).forEach(richGamma::put);
    richGamma = Collections.unmodifiableMap(richGamma);
    var refiner = new RefineTypes(p);
    var e1 = m.body().get();
    var e2 = refiner.fixType(e1,sig.ret());
    var optBody = inferStep(richGamma,e2,depth);

    var bestITStrategy = BestITStrategy.MostUserWritten.$self;
    var res = optBody.map(b->m.withBody(Optional.of(b)).withSig(refiner.fixSig(sig, b.t(), bestITStrategy)));
    var finalRes = res.or(()->e1==e2
      ? Optional.empty()
      : Optional.of(m.withBody(Optional.of(e2)).withSig(refiner.fixSig(sig, e2.t(), bestITStrategy))));
    return finalRes.map(m1->!m.equals(m1)).orElse(true)
      ? finalRes : Optional.empty();
//    assert finalRes.map(m1->!m.equals(m1)).orElse(true);
//    return finalRes;
  }
  Optional<List<E.Meth>> bPropGetSigM(Map<String, T> gamma, E.Meth m, E.Lambda e, int depth) {
    assert !e.it().isEmpty();
    assert m.sig().isEmpty() || m.name().isPresent();
    if(m.sig().isPresent()){ return Optional.empty(); }
    if(m.name().isPresent()){ return Optional.empty(); }
    var res = onlyAbs(e, depth).stream()
      .filter(fullSig->fullSig.sig().ts().size() == m.xs().size())
      .map(fullSig->m.withName(fullSig.name()).withSig(fullSig.sig()).makeBodyUnique())
      .toList();

    // TODO: this can throw if the abstract method is not callable (i.e. we're returning imm so no abstract meths exist)
    assert res.stream().noneMatch(m::equals);
    return Optional.of(res);
  }

  Optional<List<E.Meth>> bPropGetSig(Map<String, T> gamma, E.Meth m, E.Lambda e, int depth) {
    assert !e.it().isEmpty();
    if(m.sig().isPresent()){ return Optional.empty(); }
    if(m.name().isEmpty()){ return Optional.empty(); }
    List<FullMethSig> sigs; try { sigs = onlyMName(e, m.name().get(), depth); }
    catch (CompileError err) { throw err.parentPos(m.pos()); }
    // e.pos().isPresent() && e.pos().get().fileName.toString().endsWith("lists.fear") && m.name().get().name().equals("++")
    if(sigs.isEmpty()){ return Optional.empty(); }
    var res = sigs.stream().map(s->m.withName(s.name()).withSig(s.sig()).makeBodyUnique()).toList();
    assert res.stream().noneMatch(m::equals);
    return Optional.of(res);
  }

  List<FullMethSig> onlyAbs(E.Lambda e, int depth){
    var its= e.its().isEmpty()
      ?e.it().stream().toList()
      :e.its();
    List<FullMethSig> sigs;try{sigs = FullMethSig.of(
      p,
      XBs.empty(),
      e.mdf().orElse(Mdf.recMdf),
      its,
      depth,
      CM::isAbs
    );
    } catch (CompileError err) { throw err.parentPos(e.pos()); }
    @SuppressWarnings("preview")
    var nUniqueSigs = sigs.stream()
      .gather(DistinctBy.of(sig->sig.name().withMdf(Optional.empty())))
      .limit(2)
      .count();
    if (nUniqueSigs > 1) { throw Fail.cannotInferAbsSig(e.id().id()).pos(e.pos()); }
    return sigs;
  }
  List<FullMethSig> onlyMName(E.Lambda e, Id.MethName name, int depth){
    var its = e.it().map(it->Push.of(it, e.its())).orElse(e.its());
    return FullMethSig.of(p, XBs.empty(), e.mdf().orElse(Mdf.recMdf), its, depth, cm->cm.name().nameArityEq(name));
  }

  Optional<E> methCall(Map<String, T> gamma, E.MCall e, int depth) {
    Optional<E> res; try { res =
      methCallProp(gamma, e, depth)
        .or(()->methArgProp(gamma,e, depth))
        .or(()->methCallHasGens(gamma, e, depth))
        .or(()->methCallNoGens(gamma, e, depth));
    } catch (CompileError err) {
      throw err.parentPos(e.pos());
    }
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methCallProp(Map<String, T> gamma, E.MCall e, int depth) {
    Optional<E> res = inferStep(gamma, e.receiver(), depth).map(e::withReceiver);
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methArgProp(Map<String, T> gamma, E.MCall e, int depth) {
    boolean[] done = {false};
    var newEs = e.es().stream().map(ei->done[0]
      ? ei
      : inferStep(gamma, ei, depth).map(ej->{ done[0]=true; return ej; }).orElse(ei)
    ).toList();
    if(!done[0]){ return Optional.empty(); }
    //Sub s = new Sub(res.t2().gxOrThrow(),res.t1);
    //Sub sMdf = new Sub(res.t2().gxOrThrow(),res.t1.withMdf(Mdf.mdf));
    Optional<E> res = Optional.of(e.withEs(newEs));
    assert res.map(e1->!e.equals(e1)).orElse(true);
    return res;
  }
  Optional<E> methCallHasGens(Map<String, T> gamma, E.MCall e, int depth) {
    if (e.ts().isEmpty()) { return Optional.empty(); }
    var gens = e.ts().get();
    var c = e.receiver().t();
    var iTs = typesOf(e.es());
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recv))) { return Optional.empty(); }
    try {
      var ms = p().meths(XBs.empty(), c.mdf(), recv.toAstIT(it->it.toAstTFreshenInfers(new Box<>(0))), e.name(), depth);
//        .filter(cm->filterByMdf(c.mdf(), cm.mdf()) && gens.size() == cm.sig().gens().size())
      if (ms.isEmpty()) { // TODO: might need to be ms.size != 1
        return Optional.empty();
      }
    } catch (T.MatchOnInfer ignored) {}

    try {
      var refiner = new RefineTypes(p);
      var baseSig = new RefineTypes.RefinedSig(e.name(), gens, Map.of(), iTs, e.t());
      // TODO: this doesn't consider narrowing down to gens on ITs (i.e. UnrestrictedIO:FCap[...] does not help refine FCap[...] because this only uses UnrestrictedIO)
      var refined = refiner.refineSig(c.mdf(), recv, List.of(baseSig), depth);
      var refinedSig = refined.sigs().getFirst();
//      var fixedRecvT = e.receiver().t(Mdf.imm); // default to imm if nothing was written here
      var fixedRecv = refiner.fixType(e.receiver(), new T(c.mdf(), refined.c()));
      var fixedArgs = refiner.fixSig(e.es(), refinedSig.args());
      var fixedGens = e.ts().map(userGens->replaceOnlyInfers(userGens, refinedSig.gens())).orElse(refinedSig.gens());

      assert refinedSig.name().equals(e.name());
      var res = new E.MCall(
        fixedRecv,
        refinedSig.name(),
        Optional.of(fixedGens),
        fixedArgs,
        refiner.best(e.t(), refinedSig.rt(), new BestITStrategy.MostSpecific(p)),
        e.pos()
      );
      return e.equals(res) ? Optional.empty() : Optional.of(res);
    } catch (CompileError err) {
      throw err.pos(e.pos());
    }
  }
  public static T replaceOnlyInfers(T user, T inferred) {
    if (user.isInfer()) {
      if (!inferred.isInfer() && inferred.rt() instanceof Id.IT<T> inferredIT && Magic.isLiteral(inferredIT.name().name())) {
          inferred = new T(inferred.mdf(), LiteralToType.of(inferredIT));
      }
      return inferred;
    }
    if (inferred.isInfer()) {
      return user;
    }
    if (!(user.rt() instanceof Id.IT<T> userIT
      && inferred.rt() instanceof Id.IT<T> inferredIT)) { return user; }

    if (Magic.isLiteral(inferredIT.name().name())) {
      inferredIT = LiteralToType.of(inferredIT);
    }

    if (!userIT.name().equals(inferredIT.name())) { return user; }
    return new T(user.mdf(), userIT.withTs(replaceOnlyInfers(userIT.ts(), inferredIT.ts())));
  }
  public static List<T> replaceOnlyInfers(List<T> userGens, List<T> inferredGens) {
    return Streams.zip(userGens, inferredGens)
      .map(InferBodies::replaceOnlyInfers)
      .toList();
  }
  Optional<E> methCallNoGens(Map<String, T> gamma, E.MCall e, int depth) {
    if (e.ts().isPresent()) { return Optional.empty(); }

    var c = e.receiver().t();
    if (c.isInfer() || (!(c.rt() instanceof Id.IT<T> recvIT))) { return Optional.empty(); }
    var its = List.of(recvIT);
    if (e.receiver() instanceof E.Lambda recv) {
      its = recv.its();
    }

    Optional<FullMethSig> cm;
    try {
      var res = FullMethSig.of(p, XBs.empty(), c.mdf(), its, depth, cm1->cm1.name().nameArityEq(e.name()));
      cm = !res.isEmpty() ? Optional.of(res.getFirst()) : Optional.empty();
    } catch (CompileError err) { throw err.parentPos(e.pos()); }
    if (cm.isEmpty()) {
      throw TypingAndInferenceErrors.fromInference(p(), Fail.undefinedMethod(e.name(), c).pos(e.pos()));
    }
    var sig = cm.get().sig();
    var k = sig.gens().size();
    var infers = Collections.nCopies(k, T.infer);
    return Optional.of(e.withTs(Optional.of(infers)));
  }

  Optional<E> var(Map<String, T> gamma, E.X e) {
    if (!e.t().isInfer()) { return Optional.empty(); }
    Optional<E> res = Optional.ofNullable(gamma.get(e.name())).map(e::withT);
    if (res.map(e1->e1.equals(e)).orElse(true)) {
//      throw Fail.undefinedName(e.name()).pos(e.pos());
      return Optional.empty();
    }
    return res;
  }

  // helpers

  /** extracts the annotated types for all the ie in ies */
  List<T> typesOf(List<E> ies) {
    return ies.stream().map(E::t).toList();
  }
}

/*
1 Currerent Ds -->For all classes in Ds, for all methods with a body, infer the body
-->new Ds (core) with a core program
by running fixInferStep and finally injectionToCore

2 fixInferStep(iG,e)=e' //runs inferStep(iG,E)=Optional<E> until e' is not 'empty',
the one just before none is the final result

3
iG |- e=>e'
inferStep(iG,E)=Optional<E> so that on 'empty' it 'could not be done'

every rule should be a method with the rule name

 */