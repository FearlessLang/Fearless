package program.inference;

import ast.Program;
import astFull.E;
import astFull.T;
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
    if (lambda.meths().stream().anyMatch(m->m.sigs().isEmpty())) {
      return lambda;
    }

    var c = lambda.it().orElseThrow();

    List<E.Meth> lambdaOnlyMeths = lambda.meths().stream()
      .filter(m->{
        try {
          return m.name().isPresent() && p().meths(lambda.mdf().orElse(Mdf.imm), c.toAstIT(T::toAstT), m.name().get(), depth).isEmpty();
        } catch (T.MatchOnInfer err) {
          return false;
        }
      }).toList();
    List<E.Meth> traitMeths = lambda.meths().stream()
      .filter(m->!lambdaOnlyMeths.contains(m))
      .toList();
    List<RefinedSig> sigs = traitMeths.stream()
      .map(this::tSigOf)
      .toList();
    var res = refineSig(lambda.mdf().orElse(Mdf.imm), c, sigs, depth);
    var ms = Stream.concat(
      Streams.zip(traitMeths, res.sigs()).map(this::tM),
      lambdaOnlyMeths.stream()
    ).toList();
    var newIT = replaceOnlyInfers(lambda.t(), new T(lambda.t().mdf(), res.c()));
    return lambda.withMeths(ms).withT(Optional.ofNullable(newIT.itOrThrow()).map(it->new T(lambda.t().mdf(), it)));
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
    assert m.sigs().isPresent();
    var oldS=m.sigs().get();
    var refinedSig = refined.toSig(m.sigs().flatMap(E.Sig::pos));
    assert oldS.gens().equals(refinedSig.gens());
    var fixedTs=replaceOnlyInfers(oldS.ts(), refinedSig.ts());
    var retT=replaceOnlyInfers(oldS.ret(),refinedSig.ret());
    oldS = oldS.withRet(retT).withTs(fixedTs);
    return m.withSigs(oldS);
  }
  RefinedSig tSigOf(E.Meth m){
    var sig = m.sigs().orElseThrow();
    var name = m.name().orElseThrow();
    var gens = sig.gens().stream().map(g->new T(Mdf.mdf,g)).toList();
    return new RefinedSig(sig.mdf(), name, gens, sig.bounds(), sig.ts(),sig.ret());
  }
  E.Sig fixSig(E.Sig sig, T iTi){
    var ret  = sig.ret();
    var best = best(ret, iTi);
    if(best==ret){ return sig; }
    var res  = sig.withRet(best);
    // TODO: poorly written programs can fail this assertion, should throw a CompileError instead.
    assert res.ret().equals(ret)
      || ret.isInfer() || ret.rt() instanceof Id.IT<?>;
    return res;
  }
  List<E> fixSig(List<E> ies, List<T> iTs, int depth) {
    return Streams.zip(ies, iTs).map((ie, iT)->fixType(ie, iT, depth)).toList();
  }
  E fixType(E ie, T iT, int depth) { // TODO: not in formalism yet
        return fixType(ie, iT);
  }
  E fixType(E ie, T iT) {
    return ie.withT(best(ie.t(), iT));
  }
  T best(T iT1, T iT2) {
    if(iT1.equals(iT2)){ return iT1; }
    if(iT1.isInfer()){ return iT2; }
    if(iT2.isInfer()){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> && iT2.rt() instanceof Id.IT<?>){ return iT2; }
    if(iT2.rt() instanceof Id.GX<?> && iT1.rt() instanceof Id.IT<?>){ return iT1; }
    if(iT1.rt() instanceof Id.GX<?> g1 && iT2.rt() instanceof Id.GX<?> g2){
      if(g1.equals(g2)){ return iT1; }
    }
    if(!(iT1.rt() instanceof Id.IT<T> c1)  || !(iT2.rt() instanceof Id.IT<T> c2)){
      throw Bug.unreachable();
    }

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
//    var mdf = eMdf.orElse(iT1.mdf());
//    iT1 = iT1.propagateMdf(mdf);

    List<RP> refined = refineSigGens(RP.of(c1.ts(),c2.ts()), Set.of());
    if(refined.isEmpty()){ return iT1; }
    List<T> refinedTs = refined.stream().map(RP::t1).toList();
//    List<T> refinedTs = refined.stream().map(rp->best(rp.t1(), rp.t2())).toList();

    //TODO: if the MDFs are different? take the most specific? not on iso?
    // We were throwing. I'm gonna try taking iT1 on the assumption that's the user-provided type
//    if(iT1.mdf()!=iT2.mdf()){
//      throw Fail.incompatibleMdfs(iT1, iT2);
//    }
    return new T(iT1.mdf(), c1.withTs(refinedTs));
  }
  public record RP(T t1, T t2){
    public RP {
      if (!t1.isInfer() && !t2.isInfer() && t1.mdf().isRecMdf()) { t1 = t1.withMdf(t2.mdf()); }
    }

    static List<RP> of(List<T> iTs, List<T> iTs1){
      return Streams.zip(iTs,iTs1).map(RP::new).toList();
    }
    static List<RP> ofCore(List<ast.T> iTs, List<ast.T> iTs1){
      return Streams.zip(iTs,iTs1).map((iT,iT1)->new RP(iT.toAstFullT(), iT1.toAstFullT())).toList();
    }
  }

  public record RefinedSig(Mdf mdf, Id.MethName name, List<T> gens, Map<Id.GX<astFull.T>, Set<Mdf>> bounds, List<T> args, T rt){
    E.Sig toSig(Optional<Pos> pos) {
      return new astFull.E.Sig(mdf, gens.stream().map(T::gxOrThrow).toList(), bounds, args, rt, pos);
    }
  }

  public static T regenerateInfers(program.Program p, Set<Id.GX<ast.T>> fresh, T t) {
    var renamer = TypeRename.full(p);
    return renamer.renameT(t, gx->{
      if (fresh.contains(gx)) { return T.infer; }
      return new T(Mdf.mdf, gx);
    });
  }

  RefinedSig freshXs(Optional<CM> cm, Id.MethName name, List<Id.GX<ast.T>> gxs) {
    var sig = cm.map(cmi->cmi.sigs().toAstFullSig()).orElseThrow();
    assert sig.gens().size() == gxs.size();
    var tgxs = gxs.stream().map(gx->new T(Mdf.mdf, gx.toFullAstGX())).toList();
    var f = renamer.renameFun(tgxs,sig.gens());
    return new RefinedSig(
      sig.mdf(),
      name,
      tgxs,
      sig.bounds(),
      sig.ts().stream().map(t->renamer.renameT(t, f)).toList(),
      renamer.renameT(sig.ret(),f)
    );
  }

  record RefinedLambda(Id.IT<astFull.T> c, List<RefinedSig> sigs){}
  RefinedLambda refineSig(Mdf mdf, Id.IT<astFull.T> c, List<RefinedSig> sigs, int depth) {
    int nGens = sigs.stream().mapToInt(s->s.gens().size()).sum();
    var freshGXs = Id.GX.standardNames(c.ts().size() + nGens);
    var freshGXsQueue = new ArrayDeque<>(freshGXs);
    var freshGXsSet = new HashSet<>(freshGXs);
    var ts  = c.ts().stream().map(t->new ast.T(Mdf.mdf, freshGXsQueue.poll())).toList();
    List<List<Id.GX<ast.T>>> methGens = sigs.stream()
      .map(s->s.gens().stream().map(gx->freshGXsQueue.poll()).toList())
      .toList();
    var cTs = new Id.IT<>(c.name(), ts);
    var cT = new astFull.T(mdf, cTs.toFullAstIT(ast.T::toAstFullT));
    var cTOriginal = new T(mdf, c);
    List<List<RP>> rpsSigs = Streams.zip(sigs,methGens)
      .map((sig,mGens)->pairUp(mdf, mGens, cTs, sig, depth))
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

  List<RP> pairUp(Mdf lambdaMdf, List<Id.GX<ast.T>> gxs, Id.IT<ast.T> c, RefinedSig sig, int depth) {
    var ms = p.meths(lambdaMdf, c, sig.name(), depth);
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
    return new RefinedSig(sig.mdf(), sig.name(), gens, sig.bounds(), args, q.poll().t1());
  }

  RP easyInfer(RP rp){
    if(rp.t1().isInfer()){ return new RP(rp.t2(), rp.t2()); }
    if(rp.t2().isInfer()){ return new RP(rp.t1(), rp.t1()); }
    return rp;
  }

  List<RP> refineSigGens(List<RP>rps, Set<Id.GX<ast.T>> freshInfers) {
    List<Sub> subs = collect(rps);
    Map<Id.GX<T>, T> map = refineSubs(subs);
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
    var mdf = res.t1.mdf();
    Sub s = new Sub(res.t1().gxOrThrow(), res.t2.propagateMdf(mdf));
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
    return refineSubs(e).orElseThrow();
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
    var sub= refineSubs(head);
    if (sub.isEmpty()){ return collectRec(rps); }
    rename(rps,sub.get());
    return Push.of(sub.get(), collectRec(rps));
  }
  private Optional<Sub> refineSubs(RP rp){
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

  Map<Id.GX<T>, T> refineSubs(List<Sub> subs) {
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
