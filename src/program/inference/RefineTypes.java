package program.inference;

import ast.Program;
import astFull.E;
import astFull.T;
import failure.Fail;
import files.Pos;
import id.Id;
import id.Mdf;
import program.CM;
import program.TypeRename;
import utils.*;
import visitors.FullShortCircuitVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static program.inference.InferBodies.replaceOnlyInfers;

public record RefineTypes(ast.Program p, TypeRename.FullTTypeRename renamer) {
  public RefineTypes(Program p) {
    this(p, new TypeRename.FullTTypeRename(p));
  }

  E.Lambda fixLambda(E.Lambda lambda, int depth) {
    /*
    fixTypes(MDF ITs{'x Ms}:MDF C[iTs]) = MDF ITs{'x toMs(Ms,TSigs)}:MDF C[iTs']
  refineSigMassive(C[iTs],tSigOf(Ms)) = C[iTs'], TSigs

  tSigOf(Ms) = turns every M in Ms into a TSig, trashes the body
  tMs(Ms,TSigs) = zips the Ms and the TSigs into better Ms; Ms is used to recover the bodies
     */
    if (lambda.meths().stream().anyMatch(m->m.sig().isEmpty())) {
      return lambda;
    }

    var c = lambda.it().orElseThrow();
//    var nFresh = new Box<>(0);
//    var its = p().itsOf(c.toAstIT(it->it.toAstTFreshenInfers(nFresh))).stream().map(it->it.toFullAstIT(ast.T::toAstFullT)).toList();
//    var freshGXsSet = IntStream.range(0, nFresh.get()).mapToObj(n->new Id.GX<ast.T>("FearTmp"+n+"$")).collect(Collectors.toSet());
//    its = its.stream().map(it->RefineTypes.regenerateInfers(freshGXsSet, new T(Mdf.mdf, it))).map(T::itOrThrow).toList();
//
//    var l = lambda.withITs(Push.of(lambda.its(), its).stream().distinct().toList());

    List<RefinedSig> sigs = lambda.meths().stream()
      .map(this::tSigOf)
      .toList();
    var res = refineSigMassive(lambda.mdf().orElse(Mdf.imm), c, sigs, depth);
    var ms = Streams.zip(lambda.meths(), res.sigs())
      .map(this::tM)
      .toList();
//    Id.IT<T> lT = best(new T(Mdf.mdf, res.c()), l.t()).itOrThrow();S
    var newIT = replaceOnlyInfers(lambda.t(Mdf.mdf), new T(lambda.t(Mdf.mdf).mdf(), res.c()));
    return lambda.withMeths(ms).withIT(Optional.ofNullable(newIT.itOrThrow()));
  }

  E.Meth tM(E.Meth m, RefinedSig refined) {
    /*
    #Define tMs(Ms,TSigs)= Ms' tM(M,TSig)=M'
    // zips the Ms and the TSigs into better Ms; Ms is used to recover the bodies
    tM(MDF m[Xs](x1:iT1..xn:iTn):iT0(->e)?,  m[iTs1](iT'1..iT'n):iT'0  )
      = MDF m[Xs](x1:iT"1..xn:iT"n):iT"0(->e)?
        with replaceOnlyInfer(iTi,iT'i)=iT"i
      assert iTs1==Xs
     */
    assert m.sig().isPresent();
    var oldS=m.sig().get();
    var refinedSig = refined.toSig(m.sig().flatMap(E.Sig::pos));
    assert oldS.gens().equals(refinedSig.gens());
    var fixedTs=replaceOnlyInfers(oldS.ts(), refinedSig.ts());
    var retT=replaceOnlyInfers(oldS.ret(),refinedSig.ret());
    oldS = oldS.withRet(retT).withTs(fixedTs);
    return m.withSig(oldS);
  }
  RefinedSig tSigOf(E.Meth m){
    var sig = m.sig().orElseThrow();
    var name = m.name().orElseThrow();
    var gens = sig.gens().stream().map(g->new T(Mdf.mdf,g)).toList();
    return new RefinedSig(sig.mdf(), name, gens, sig.ts(),sig.ret());
  }
  E.Sig fixTypes(E.Sig sig, T iTi){
    var ret  = sig.ret();
    var best = best(iTi, ret);
    if(best==ret){ return sig; }
    var res  = sig.withRet(best);
    // TODO: poorly written programs can fail this assertion, should throw a CompileError instead.
    assert res.ret().equals(ret)
      || ret.isInfer() || ret.rt() instanceof Id.IT<?>;
    return res;
  }
  List<E> fixTypes(List<E> ies, List<T> iTs, int depth) {
    return Streams.zip(ies, iTs).map((ie, iT)->fixType(ie, iT, depth)).toList();
  }
  E fixType(E ie, T iT, int depth) { // TODO: not in formalism yet
    var fixed = fixType(ie, iT);
    if (fixed instanceof E.Lambda l) {
      return fixLambda(l, depth);
    }
    return fixed;
  }
  E fixType(E ie, T iT) {
    T ieT = iT.isInfer() ? ie.t(Mdf.imm) : ie.t(iT.mdf());
    return ie.withT(best(ie.mdf(), ieT, iT));
  }

  T best(T iT1, T iT2) {
    return best(Optional.empty(), iT1, iT2);
  }
  T best(Optional<Mdf> eMdf, T iT1, T iT2) {
    if(iT1.equals(iT2)){ return iT1; }
    if(iT1.isInfer()){ return iT2; }
    if(iT2.isInfer()){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> && iT2.rt() instanceof Id.IT<?>){ return iT2; }
    if(iT2.rt() instanceof Id.GX<?> && iT1.rt() instanceof Id.IT<?>){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> g1 && iT2.rt() instanceof Id.GX<?> g2){
      if(g1.equals(g2)){ return iT1; }
    }
    if( !(iT1.rt() instanceof Id.IT<T> c1)
     || !(iT2.rt() instanceof Id.IT<T> c2)){
      throw Bug.unreachable();
    }

    // TODO should we check the subtyping between C and C' instead? Yes. Update the formalism
    var notMatch=!c1.name().equals(c2.name()); //name includes gen size
    if(notMatch){
      var t1 = new T(Mdf.mdf, c1);
      var t2 = new T(Mdf.mdf, c2);
      ast.T t1C; try { t1C = t1.toAstT(); }
        catch (T.MatchOnInfer e) { return iT2; }
      ast.T t2C; try { t2C = t2.toAstT(); }
        catch (T.MatchOnInfer e) { return iT1; }

      if (p.isSubType(t1C, t2C)) { return iT1; }
      if (p.isSubType(t2C, t1C)) { return iT2; }

//      throw Fail.noSubTypingRelationship(c1, c2);
      return iT1;
    }

    // Keep the explicit mdf from the expression if it has one
    var mdf = eMdf.orElse(iT1.mdf());
    iT1 = iT1.propagateMdf(mdf);

    List<RP> refined = refineSigGens(RP.of(c1.ts(),c2.ts()), Set.of());
    if(refined.isEmpty()){ return iT1; }
    List<T> refinedTs = refined.stream().map(RP::t1).toList();
//    List<T> refinedTs = refined.stream().map(rp->best(rp.t1(), rp.t2())).toList();

    //TODO: if the MDFs are different? take the most specific? not on iso?
    // We were throwing. I'm gonna try taking iT1 on the assumption that's the user-provided type
//    if(iT1.mdf()!=iT2.mdf()){
//      throw Fail.incompatibleMdfs(iT1, iT2);
//    }
    return new T(mdf, c1.withTs(refinedTs));
  }
  record RP(T t1, T t2){
    RP {
      if (!t1.isInfer() && !t2.isInfer() && t1.mdf().isRecMdf()) { t1 = t1.withMdf(t2.mdf()); }
    }

    static List<RP> of(List<T> iTs, List<T> iTs1){
      return Streams.zip(iTs,iTs1).map(RP::new).toList();
    }
    static List<RP> ofCore(List<ast.T> iTs, List<ast.T> iTs1){
      return Streams.zip(iTs,iTs1).map((iT,iT1)->new RP(iT.toAstFullT(), iT1.toAstFullT())).toList();
    }
  }

  public record RefinedSig(Mdf mdf, Id.MethName name, List<T> gens, List<T> args, T rt){
    E.Sig toSig(Optional<Pos> pos) {
      return new astFull.E.Sig(mdf, gens.stream().map(T::gxOrThrow).toList(), args, rt, pos);
    }
  }

  public static T regenerateInfers(program.Program p, Set<Id.GX<ast.T>> fresh, T t) {
    var renamer = TypeRename.full(p);
    return renamer.renameT(t, gx->{
      if (fresh.contains(gx)) { return T.infer; }
      return new T(Mdf.mdf, gx);
    });
  }

  RefinedSig freshXs(List<CM> ms, Id.MethName m, List<Id.GX<ast.T>> gxs) {
    // TODO: what about helper methods added on the creation of a lambda that aren't in the top dec?
    // TODO: also potentially no method to find here
    var meth = OneOr.of(
      "More than one valid method found for "+m,
      ms.stream().filter(mi->mi.name().equals(m))
    );
    var sig = meth.sig().toAstFullSig();
    assert meth.sig().gens().size() == gxs.size();
    var tgxs = gxs.stream().map(gx->new T(Mdf.mdf, gx.toFullAstGX())).toList();
    var f = renamer.renameFun(tgxs,sig.gens());
    return new RefinedSig(
      sig.mdf(),
      meth.name(),
      tgxs,
      sig.ts().stream().map(t->renamer.renameT(t, f)).toList(),
      renamer.renameT(sig.ret(),f)
    );
  }

  record RefinedLambda(Id.IT<astFull.T> c, List<RefinedSig> sigs){}
  RefinedLambda refineSigMassive(Mdf mdf, Id.IT<astFull.T> c, List<RefinedSig> sigs, int depth) {
    int nGens = sigs.stream().mapToInt(s->s.gens().size()).sum();
    var freshGXs = Id.GX.standardNames(c.ts().size() + nGens);
    var freshGXsQueue = new ArrayDeque<>(freshGXs);
    var freshGXsSet = new HashSet<>(freshGXs);
    var ts  = c.ts().stream().map(t->new ast.T(Mdf.mdf, freshGXsQueue.poll())).toList();
    List<List<Id.GX<ast.T>>> methGens = sigs.stream()
      .map(s->s.gens().stream().map(gx->freshGXsQueue.poll()).toList())
      .toList();
    var cTs = new Id.IT<ast.T>(c.name(), ts);
    var cT = new astFull.T(mdf, cTs.toFullAstIT(ast.T::toAstFullT));
    var cTOriginal = new T(mdf, c);
    List<List<RP>> rpsSigs = Streams.zip(sigs,methGens)
      .map((sig,mGens)->pairUp(mGens, cTs, sig, depth))
      .toList();
    List<RP> rpsAll = Stream.concat(
      Stream.of(new RP(cT, cTOriginal)),
      //Stream.of(new RP(cTOriginal, cT)) // alternatively
      rpsSigs.stream().flatMap(Collection::stream)
    ).toList();
    var refined = refineSigGens(rpsAll, freshGXsSet);
    var resC = regenerateInfers(p, freshGXsSet, refined.get(0).t1());

    var q = new ArrayDeque<>(refined.subList(1, refined.size()));
    var refinedSigs = sigs.stream()
      .map(refinedSig->toTSig(refinedSig, q))
//      .map(sig->sig.regenerateInfers(freshGXsSet)) // this has no impact
      .toList();

    return new RefinedLambda(resC.itOrThrow(), refinedSigs);
  }

  List<RP> pairUp(List<Id.GX<ast.T>> gxs, Id.IT<ast.T> c, RefinedSig sig, int depth) {
    var ms = p.meths(Mdf.mdf, c, depth);
    var freshSig = freshXs(ms, sig.name(), gxs);
    var freshGens = freshSig.gens();
    return Streams.of(
      RP.of(freshGens, sig.gens()).stream(),
      RP.of(freshSig.args(), sig.args()).stream(),
      Stream.of(new RP(freshSig.rt(), sig.rt()))
    ).toList();
  }

  RefinedSig toTSig(RefinedSig sig, ArrayDeque<RP> q) {
    var gens = sig.gens().stream().map(unused->q.poll().t1()).toList();
    var args = sig.args().stream().map(unused->q.poll().t1()).toList();
    return new RefinedSig(sig.mdf(), sig.name(),gens,args,q.poll().t1());
  }

  RP easyInfer(RP rp){
    if(rp.t1().isInfer()){ return new RP(rp.t2(), rp.t2()); }
    if(rp.t2().isInfer()){ return new RP(rp.t1(), rp.t1()); }
    return rp;
  }

  List<RP> refineSigGens(List<RP>rps, Set<Id.GX<ast.T>> freshInfers) {
    List<Sub> subs = collect(rps);
    Map<Id.GX<T>, T> map = toSub(subs);
    return rps.stream()
      .map(rp->renameRP(rp, map, renamer))
      .map(rp->new RP(regenerateInfers(p, freshInfers, rp.t1()), regenerateInfers(p, freshInfers, rp.t2())))
      .map(this::easyInfer)
      .toList();
  }
  boolean isXX(RP rp){
    if (rp.t1().isInfer() || rp.t2().isInfer()) { return false; }
    return rp.t1().rt() instanceof Id.GX<?> && rp.t2().rt() instanceof Id.GX<?>;
  }
  boolean sameC(int i, List<RP> rps) {
    var rp = rps.get(i);
    if (rp.t1().isInfer() || rp.t2().isInfer()) { return false; }
    if ((rp.t1().rt() instanceof Id.IT<T> t1) && (rp.t2().rt() instanceof Id.IT<T> t2)) {
      return t1.name().equals(t2.name());
    }
    return false;
  }
  record Sub(Id.GX<T> x,T t){
    Sub {
      assert x.name().endsWith("$") || !t.match(gx->gx.name().endsWith("$"),it->false);
    }
    boolean isCircular() {
      if (t.isInfer() || t.rt() instanceof Id.GX<?>) { return false; }
      assert t.rt() instanceof Id.IT<?>;
      var hasXVisitor = new FullShortCircuitVisitor<Boolean>(){
        @Override
        public Optional<Boolean> visitGX(Id.GX<T> t) {
          if (t.equals(x)) { return Optional.of(true); }
          return Optional.empty();
        }
      };
      return hasXVisitor.visitT(t).orElse(false);
    }
  }
  Sub collectXXOut(int index, ArrayList<RP> rps){
    var res = rps.remove(index);
    //collect(RPs, MDF X = _ X', RPs') =   X=MDF X', collect(RPs[X = mdf X'], RPs'[X = mdf X'])//proposed
    //collect(RPs, MDF X = _ X', RPs') =   X'=MDF X, collect(RPs[X' = mdf X], RPs'[X = mdf X])//proposed
    //var target = res.t1().withMdf(Mdf.mdf);
    //rename(rps, new Sub(res.t2().gxOrThrow(), target));
    //var other = res.t2().withMdf(res.t1().mdf());
    //return new Sub(res.t1().gxOrThrow(), other);

    //Sub s = new Sub(res.t2().gxOrThrow(),res.t1);
    //Sub sMdf = new Sub(res.t2().gxOrThrow(),res.t1.withMdf(Mdf.mdf));
    // TODO: This recMdf handling is new and not in the formalism
//    var mdf = res.t1.mdf().isRecMdf() ? res.t2.mdf() : res.t1.mdf();
    var mdf = res.t1.mdf();
    Sub s = new Sub(res.t1().gxOrThrow(), res.t2.propagateMdf(mdf)); // TODO: change in formalism, was withMdf
    Sub sMdf = new Sub(res.t1().gxOrThrow(), new T(Mdf.mdf, res.t2.rt()));
    rename(rps, sMdf);
    return s;
  }
  void collectSameC(int index, ArrayList<RP> rps){
    var e = rps.remove(index);
    var its1=e.t1().itOrThrow().ts();
    var its2=e.t2().itOrThrow().ts();
    Range.of(its1).forEach(i->rps.add(index+i,new RP(its1.get(i),its2.get(i))));
  }
  Sub collectMulti(int index1, int index2, ArrayList<RP> rps){
    var e = rps.remove(index1);
    index2 -= 1;
    insertGens(rps, index2, gensOf(e), gensOf(rps.get(index2)));
    return toSub(e).orElseThrow();
  }
  List<T> gensOf(RP rp){
    if(rp.t1().rt() instanceof Id.GX<?>){ return rp.t2().itOrThrow().ts(); }
    assert rp.t2().rt() instanceof Id.GX<?>;
    return rp.t1().itOrThrow().ts();
  }
  void insertGens(ArrayList<RP> rps, int index, List<T> its1, List<T> its2) {
    Range.of(its1).forEach(i->rps.add(index+i,new RP(its1.get(i),its2.get(i))));
  }
  boolean isInferRP(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    return rp.t1().isInfer() || rp.t2().isInfer();
  }
  boolean isSameXC(RP rp1, RP rp2) {
    Id.GX<?> x1 = rp1.t1().match(gx->gx,it->rp1.t2().gxOrThrow());
    Id.DecId c1 =  rp1.t1().match(gx->rp1.t2().itOrThrow(),it->it).name();
    Optional<Id.IT<?>> c21 = rp2.t1().match(gx->Optional.empty(), Optional::of);
    Optional<Id.IT<?>> c22 = rp2.t2().match(gx->Optional.empty(), Optional::of);
    if(c21.isPresent() && c22.isPresent()){ return false; }
    if(c21.isEmpty() && c22.isEmpty()){ return false; }
    Id.DecId c2=c21.or(()->c22).get().name();
    if(!c1.equals(c2)){ return false; }
    return rp2.t1().match(
      x2->x1.equals(x2),
      it->rp2.t2().match(x1::equals, it_->false)
    );
  }
  boolean isXC(int index, ArrayList<RP> rps) {
    var rp = rps.get(index);
    var leftX = (rp.t1().rt() instanceof Id.GX<?>) && (rp.t2().rt() instanceof Id.IT<?>);
    var rightX = (rp.t2().rt() instanceof Id.GX<?>) && (rp.t1().rt() instanceof Id.IT<?>);
    return leftX || rightX;
  }
  List<Sub> collect(List<RP>rps) { return collectRec(new ArrayList<>(rps)); }
  void rename(ArrayList<RP>rps,Sub s){
    if(s.isCircular()){ return; }
    var map = Map.of(s.x(),s.t());
    Range.of(rps).forEach(i->rps.set(i,renameRP(rps.get(i),map,renamer)));
  }
  List<Sub> collectRec(ArrayList<RP> rps) {
    if(rps.isEmpty()){ return List.of(); }

    var optXX = IntStream.range(0, rps.size())
      .filter(i->isXX(rps.get(i))).findFirst();
    if (optXX.isPresent()) {
      Sub xx = collectXXOut(optXX.getAsInt(), rps);
      return Push.of(xx, collectRec(rps));
    }

    var optSameC = IntStream.range(0, rps.size())
      .filter(i->sameC(i,rps)).findFirst();
    if (optSameC.isPresent()) {
      collectSameC(optSameC.getAsInt(), rps);
      return collectRec(rps);
    }
    var optInfer = IntStream.range(0, rps.size())
      .filter(i->isInferRP(i, rps)).findFirst();
    if (optInfer.isPresent()){
      rps.remove(optInfer.getAsInt());
      return collectRec(rps);
    }
    var optXC1 = IntStream.range(0, rps.size()).boxed()
      .filter(i->isXC(i,rps)).findFirst();
    var optXC2 = optXC1.flatMap(xc1->Streams
      .firstPos(xc1+1,rps,i->isXC(i,rps) && isSameXC(rps.get(i), rps.get(xc1))));
    if(optXC2.isPresent()){
      Sub xc = collectMulti(optXC1.get(),optXC2.get(), rps);
      return Push.of(xc, collectRec(rps));
    }
    // otherwise...
    assert !rps.isEmpty();
    RP head = rps.remove(0);
    var sub=toSub(head);
    if (sub.isEmpty()){ return collectRec(rps); }
    rename(rps,sub.get());
    return Push.of(sub.get(), collectRec(rps));
  }
  private Optional<Sub> toSub(RP rp){
    var t1=rp.t1();
    var t2=rp.t2();
    var leftX = (t1.rt() instanceof Id.GX<?>) && (t2.rt() instanceof Id.IT<?>);
    var rightX = (t2.rt() instanceof Id.GX<?>) && (t1.rt() instanceof Id.IT<?>);
    if(leftX){ return Optional.of(new Sub(t1.gxOrThrow(), t2.propagateMdf(t1.mdf()))); }
    if(rightX){ return Optional.of(new Sub(t2.gxOrThrow(), t1)); }
    return Optional.empty();
  }

  /*
//In the rules below we always select the smallest RPs possible
collect(RPs, MDF X = _ X', RPs') =   X'=MDF X, collect(RPs[X = mdf X'], RPs'[X = mdf X'])
collect(RPs, _ C[iTs] = _ C[iTs'], RPs') = collect(RPs, iTs = iTs', RPs)
collect(RPs, infer = _, RPs') = collect(RPs')
collect(RPs, _ = infer, RPs') = collect(RPs')
collect(RPs, _ X = _ C[iTs], RPs', MDF X = MDF' C[iTs'], RPs")
  = collect(RPs,RPs', MDF X = MDF' C[iTs'], iTs = ITs', RPs")
collect(RPs, _ X = _ C[iTs], RPs', MDF' C[iTs'] = MDF X, RPs")
  = collect(RPs,RPs',MDF' C[iTs'] = MDF X, iTs = iTs', RPs")
collect(RPs, _ C[iTs] = _ X, RPs', MDF' C[iTs'] = MDF X, RPs")
   = collect(RPs,RPs',MDF' C[iTs'] = MDF X, iTs = iTs', RPs')
otherwise, if no other rule is applicable
collect(MDF X = MDF' C[iTs], RPs) =     X=MDF C[iTs] collect(RPs)
collect(MDF C[iTs] = MDF' X, RPs) =     X=MDF C[iTs] collect(RPs)
collect(empty) = empty
  */

  Map<Id.GX<T>, T> toSub(List<Sub> subs) {
    Map<Id.GX<T>, List<Sub>> res = subs.stream()
      .filter(si->!si.isCircular())
      .filter(si->!si.x().equals(si.t().rt()))
      .collect(Collectors.groupingBy(Sub::x));
    return res.values().stream().collect(Collectors.toMap(
      si->si.get(0).x(),
      si->bestAll(si.stream().map(Sub::t))
    ));
  }
  T bestAll(Stream<T> ts){ return ts.reduce(this::best).orElseThrow(); }

  public RP renameRP(RP rp, Map<Id.GX<T>, T> map, TypeRename.FullTTypeRename rename){
    var t1 = rename.renameT(rp.t1(),map::get);
    var t2 = rename.renameT(rp.t2(),map::get);
    return new RP(t1, t2);
  }
}
