package program;

import ast.T;
import astFull.E;
import failure.CompileError;
import failure.Fail;
import files.Pos;
import id.Id;
import id.Mdf;
import id.Refresher;
import program.inference.RefineTypes;
import program.typesystem.ETypeSystem;
import program.typesystem.Gamma;
import program.typesystem.XBs;
import utils.*;
import visitors.CloneVisitor;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Program {
  Id.Dec of(Id.DecId dec);
  List<Id.IT<T>> itsOf(Id.IT<T> t);
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  List<CM> cMsOf(Mdf recvMdf, Id.IT<T> t);
  CM plainCM(CM fancyCM);
  Set<Id.GX<ast.T>> gxsOf(Id.IT<T> t);
  Program withDec(T.Dec d);
  List<ast.E.Lambda> lambdas();
  Optional<Pos> posOf(Id.IT<ast.T> t);
  /** Produce a clone of Program without any cached data */
  Program shallowClone();

  default void reset() {
    this.methsCache().clear();
    this.subTypeCache().clear();
  }

  default boolean isSubType(Mdf m1, Mdf m2) { //m1<m2
    if(m1 == m2){ return true; }
    if (m2.is(Mdf.readOnly)) { return true; }
    return switch(m1){
      case mut -> m2.isLikeMut();
      case imm -> m2.is(Mdf.read);
      case iso -> true;
      case readOnly, mdf, recMdf, read, lent -> false;
    };
  }
  default boolean isSubType(XBs xbs, astFull.T t1, astFull.T t2) { return isSubType(xbs, t1.toAstT(), t2.toAstT()); }
  record SubTypeQuery(XBs xbs, T t1, T t2){}
  enum SubTypeResult { Yes, No, Unknown }
  HashMap<SubTypeQuery, SubTypeResult> subTypeCache();
  default boolean tryIsSubType(XBs xbs, T t1, T t2) {
    try {
      return isSubType(xbs, t1, t2);
    } catch (CompileError ce) { // due to the parallelism in okAll we can no longer easily prevent us getting stuck here
      System.out.println("sub-typing ignoring "+ce);
      return false;
    }
  }
  default boolean isSubType(XBs xbs, T t1, T t2) {
//    System.out.println(t1+" < "+t2);
    var q = new SubTypeQuery(xbs, t1, t2);
    var subTypeCache = this.subTypeCache();
    if (subTypeCache.containsKey(q)) {
      var res = subTypeCache.get(q);
      if (res == SubTypeResult.Unknown) {
        throw Fail.circularSubType(t1, t2);
      }
      return subTypeCache.get(q) == SubTypeResult.Yes;
    }
    subTypeCache.put(q, SubTypeResult.Unknown);
    var isSubType = isSubTypeAux(xbs, t1, t2);
    var result = isSubType ? SubTypeResult.Yes : SubTypeResult.No;
    subTypeCache.put(q, result);
    return isSubType;
  }
  default boolean isSubTypeAux(XBs xbs, T t1, T t2) {
    if (t1.equals(t2)) { return true; }
    if (t1.isMdfX()) {
      var bounds = xbs.get(t1.gxOrThrow());
      var mdf = t2.mdf();
      return bounds.stream().allMatch(mdfi->isSubType(mdfi, mdf));
    }
    if (t2.isMdfX()) {
      var bounds = xbs.get(t2.gxOrThrow());
      var mdf = t1.mdf();
      return bounds.stream().anyMatch(mdfi->isSubType(mdf, mdfi));
    }
    if(!isSubType(t1.mdf(), t2.mdf())){ return false; }
    t1 = t1.withMdf(t1.mdf()); t2 = t2.withMdf(t1.mdf());
    if(t1.rt().equals(t2.rt())){ return true; }
    if(!t1.isIt() || !t2.isIt()){ return false; }

    if (isTransitiveSubType(xbs, t1, t2)) { return true; }

    if (t1.mdf() != t2.mdf()) { return false; }
    if (t1.itOrThrow().name().equals(t2.itOrThrow().name())) {
      return isAdaptSubType(xbs, t1, t2);
    }
    return false;
  }

  default boolean isAdaptSubType(XBs xbs, T t1, T t2) {
  /*MDF C[T1..Tn]< MDF C[T1'..Tn']
    where
      adapterOk(MDF,C,T1..Tn,T1'..Tn')
  */
    assert t1.mdf() == t2.mdf();
    var mdf = t1.mdf();
    if (mdf.isMdf()) { return true; }

    /*
    #Define adapterOk(MDF,C,Ts1,Ts2)
    adapterOk(MDF0,C,Ts1,Ts2)
      filterByMdf(MDF0, meths(C[Ts1]) = Ms1
      filterByMdf(MDF0, meths(C[Ts2]) = Ms2
      forall MDF m[Xs](G1):T1 _,MDF m[Xs](G2):T2 _ in mWisePairs(Ms1,Ms2)
        G2, this:MDF0 C[Ts1] |- this.m[Xs](G2.xs) : T2
     */
    var it1 = t1.itOrThrow();
    var it2 = t2.itOrThrow();
    assert it1.name().equals(it2.name());
    List<CM> cms1 = meths(xbs, mdf, it1, 0).stream()
      .filter(cm->filterByMdf(mdf, cm.sig().stream().map(ast.E.Sig::mdf)))
      .toList();
    List<CM> cms2 = meths(xbs, mdf, it2, 0).stream()
      .filter(cm->filterByMdf(mdf, cm.sig().stream().map(ast.E.Sig::mdf)))
      .toList();

    var methsByName = Stream.concat(cms1.stream(), cms2.stream())
      .collect(Collectors.groupingBy(CM::name))
      .values();
    return methsByName.stream()
      .allMatch(ms->{
        assert ms.size() == 2;
        var m1 = ms.get(0);
        var m2 = ms.get(1);
        var recv = new ast.E.X("this", Optional.empty());
        var xs=Push.of(m1.xs(),"this");
        return Streams.zip(m1.sig(), m2.sig()).allMatch((s1,s2)->{
          List<T> ts=Push.of(s2.ts(),t1);

          if (!s1.gens().equals(s2.gens()) || !s1.bounds().equals(s2.bounds())) { return false; }
          var gxs = s2.gens().stream().map(gx->new T(Mdf.mdf, gx)).toList();
          var e=new ast.E.MCall(recv, m1.name(), gxs, m1.xs().stream().<ast.E>map(x->new ast.E.X(x, Optional.empty())).toList(), Optional.empty());
          return isType(xs, ts, xbs.addBounds(s2.gens(), s2.bounds()), e, s2.ret());
        });
      });
  }

  default boolean isType(List<String>xs, List<ast.T>ts, XBs xbs, ast.E e, ast.T expected) {
    var g = Streams.zip(xs,ts).fold(Gamma::add, Gamma.empty());
    var v = ETypeSystem.of(this, g, xbs, Optional.of(expected),0);
    var res = e.accept(v);
    return res.resMatch(t->isSubType(xbs,t,expected),err->false);
  }

  static boolean filterByMdf(Mdf mdf, Stream<Mdf> mMdfs) {
    assert !mdf.isMdf();
    return mMdfs.anyMatch(mMdf_->filterSig(mdf, mMdf_));
  }
  static boolean filterSig(Mdf mdf, Mdf mMdf) {
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.recMdf, Mdf.mdf)) { return true; }
    if (mdf.isLent() && !mMdf.isIso()) { return true; }
    return mdf.is(Mdf.imm, Mdf.read, Mdf.readOnly) && mMdf.is(Mdf.imm, Mdf.read, Mdf.readOnly, Mdf.recMdf);
  }

  record FullMethSig(Id.MethName name, List<E.Sig> sig){}
  default Optional<FullMethSig> fullSig(XBs xbs, Mdf recvMdf, List<Id.IT<astFull.T>> its, int depth, Predicate<CM> pred) {
    var nFresh = new Box<>(0);
    var coreIts = its.stream().map(it->it.toAstIT(t->t.toAstTFreshenInfers(nFresh))).distinct().toList();
    var dec = new T.Dec(new Id.DecId(Id.GX.fresh().name(), 0), List.of(), Map.of(), new ast.E.Lambda(
      Mdf.mdf,
      coreIts,
      "fearTmp$",
      List.of(),
      Optional.empty()
    ), Optional.empty());
    var p = this.withDec(dec);
    var myM_ = p.meths(xbs, recvMdf, dec.toIT(), depth).stream()
      .filter(pred)
      .toList();
    if(myM_.isEmpty()){ return Optional.empty(); }
    // TODO: error message listing all the Ms here
    assert myM_.size()==1;

    var cm = myM_.get(0);
    var fullSigs = cm.sig().stream().map(s->{
      var sig = s.toAstFullSig();
      var freshGXsSet = IntStream.range(0, nFresh.get()).mapToObj(n->new Id.GX<T>("FearTmp"+n+"$")).collect(Collectors.toSet());
      var restoredArgs = sig.ts().stream().map(t->RefineTypes.regenerateInfers(this, freshGXsSet, t)).toList();
      var restoredRt = RefineTypes.regenerateInfers(this, freshGXsSet, sig.ret());
      return new E.Sig(sig.mdf(), sig.gens(), sig.bounds(), restoredArgs, restoredRt, sig.pos());
    }).toList();
    return Optional.of(new FullMethSig(cm.name(), fullSigs));
  }

  default Optional<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, Id.MethName name, int depth){
    var myM_ = meths(xbs, recvMdf, it, depth).stream().filter(mi->mi.name().equals(name)).toList();
    if(myM_.isEmpty()){ return Optional.empty(); }
    assert myM_.size()==1;
    return Optional.of(myM_.get(0));
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, ast.E.Lambda l, int depth) {
    var dec = new T.Dec(new Id.DecId(Id.GX.fresh().name(), 0), List.of(), Map.of(), l, l.pos());
    var p_ = this.withDec(dec);
    return p_.methsAux(xbs, recvMdf, dec.toIT()).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, int depth) {
    return methsAux(xbs, recvMdf, it).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }
  record MethsCacheKey(XBs xbs, Mdf recvMdf, Id.IT<T> it){}
  HashMap<MethsCacheKey, List<CM>> methsCache();
  default List<CM> methsAux(XBs xbs, Mdf recvMdf, Id.IT<T> it) {
    var methsCache = this.methsCache();
    var cacheKey = new MethsCacheKey(xbs, recvMdf, it);
    // Can't use computeIfAbsent here because concurrent modification thanks to mutual recursion :-(
    if (methsCache.containsKey(cacheKey)) { return methsCache.get(cacheKey); }
    List<CM> cms = Stream.concat(
      cMsOf(recvMdf, it).stream(),
      itsOf(it).stream().flatMap(iti->methsAux(xbs, recvMdf, iti).stream())
    ).toList();
    var res = prune(xbs, cms, posOf(it));
    methsCache.put(cacheKey, res);
    return res;
  }

  record RenameGens(Map<Id.GX<T>,Id.GX<T>> subst) implements CloneVisitor {
    public Id.GX<T> visitGX(Id.GX<T> t) {
      var thisSubst = subst.get(t);
      if (thisSubst != null) { return thisSubst; }
      return t;
    }

    @Override public T.Dec visitDec(T.Dec d) {
      var xbs = d.bounds().entrySet().stream().collect(Collectors.toMap(
        kv->{
          var thisSubst = subst.get(kv.getKey());
          if (thisSubst != null) { return thisSubst; }
          return kv.getKey();
        },
        Map.Entry::getValue
      ));
      return new T.Dec(
        visitDecId(d.name()),
        d.gxs().stream().map(this::visitGX).toList(),
        xbs,
        visitLambda(d.lambda()),
        d.pos()
      );
    }
    @Override public ast.E.Sig visitSig(ast.E.Sig e) {
      var xbs = e.bounds().entrySet().stream().collect(Collectors.toMap(
        kv->{
          var thisSubst = subst.get(kv.getKey());
          if (thisSubst != null) { return thisSubst; }
          return kv.getKey();
        },
        Map.Entry::getValue
      ));
      return new ast.E.Sig(
        visitMdf(e.mdf()),
        e.gens().stream().map(this::visitGX).toList(),
        xbs,
        e.ts().stream().map(this::visitT).toList(),
        visitT(e.ret()),
        e.pos()
      );
    }
  }
  default CM norm(CM cm) {
    /*
    norm(CM) = CM' // Note: the (optional) body is not affected
    //Note, in CM ::= C[Ts].sig the class Xs are already replaced with Ts
    norm(C[Ts].m[X1..Xn](xTs):T->e) = C[Ts] (.m[](xTs):T->e)[X1=Par1..Xn=Parn]
      where we consistently select Par1..Parn globally so that
      it never happens that the current Ds contains Par1..Parn anywhere as a nested X
      Note: to compile with a pre compiled program we must add that
    norm(C[Ts].m[Par1 Xs](xTs):T->e) = C[Ts].m[Par1 Xs](xTs):T->e
     */
    //standardNames(n)->List.of(Par1..Parn)
    var gx=cm.prioritySig().gens();
    List<Id.GX<ast.T>> names = new Refresher<ast.T>(0).freshNames(gx.size());
    Map<Id.GX<T>,Id.GX<T>> subst=IntStream.range(0,gx.size()).boxed()
      .collect(Collectors.toMap(gx::get, names::get));
    var renamer = new RenameGens(subst);
    var newSigs=cm.sig().stream().map(renamer::visitSig).toList();
    return cm.withSig(newSigs);
  }

  /**
   * Normalised CMs are required for 5a, but the rest of the type system needs fresh names.
   */
  default CM freshenMethGens(CM cm, int depth) {
    var gxs=cm.prioritySig().gens();
    var names = new Refresher<T>(depth).freshNames(gxs.size());
    Map<Id.GX<T>,Id.GX<T>> subst=IntStream.range(0,gxs.size()).boxed()
      .collect(Collectors.toMap(gxs::get, names::get));
    var renamer = new RenameGens(subst);
    var newSigs=cm.sig().stream().map(renamer::visitSig).toList();
    return cm.withSig(newSigs);
  }

  default List<CM> prune(XBs xbs, List<CM> cms, Optional<Pos> lambdaPos) {
    /*
    prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
      where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
     */
    var cmsMap = cms.stream()
      .distinct()
      .collect(Collectors.groupingBy(CM::name));
    return cmsMap.values().stream()
      .map(cmsi->pruneAux(xbs, cmsi, lambdaPos, cmsi.size()+1))
      .toList();
  }

  default CM pruneAux(XBs xbs, List<CM> cms, Optional<Pos> lambdaPos, int limit) {
    if(limit==0){
      throw Fail.uncomposableMethods(cms.stream()
        .map(cm->Fail.conflict(cm.pos(), cm.toStringSimplified()))
        .toList()
      ).pos(lambdaPos);
    }
    /*
    pruneAux(CM) = CM
    pruneAux(CM0..CMn)= pruneAux(CMs) where
      CMs=allCases(CM0..CMn)
      n > 0
      CMs.size < n //else error
     */
    assert !cms.isEmpty();
    var first=cms.get(0);
    if (cms.size() == 1) { return first; }
    var validGroup = cms.stream().allMatch(cm->first.sig().size() == cm.sig().size());
    if (!validGroup) {
      throw Fail.uncomposableMethods(cms.stream().map(cm->Fail.conflict(cm.pos(), cm.toStringSimplified())).toList());
    }

    var nextCms=cms.stream().skip(1)
      .filter(cmi->!firstIsMoreSpecific(xbs, first, cmi) && !firstIsMoreSpecific(xbs, plainCM(first), plainCM(cmi)))
      .toList();

    return pruneAux(xbs, Push.of(nextCms,first), lambdaPos, limit - 1);
  }

  /*default List<CM> allCases(List<CM> cms) {

    allCases(CMs) = CMs.enumerate().permutations(2)
      .filter(<<i,m1>,<j,m2>> -> i < j)
      .map(<<i,m1>,<j,m2>>->selectMoreSpecific(m1,m2)).toList()

    var res = new LinkedHashSet<CM>();
    for (int i : Range.of(cms)) {
      for (int j : Range.of(cms)) {
        if (i <= j) { continue; }
        res.add(selectMoreSpecific(cms.get(i), cms.get(j)));
      }
    }
    return res.stream().toList();
  }*/

  // TODO: Write some tests to check that this is transitive
  /*default CM selectMoreSpecific(CM a, CM b) {
    return selectMoreSpecificAux(a,b)
      .or(()->selectMoreSpecificAux(b,a))
      .orElseThrow(()->Fail.uncomposableMethods(a.c(), b.c()));
  }*/
  default boolean firstIsMoreSpecific(XBs xbs, CM a, CM b) {
      /*
      selectMoreSpecific(CM1,CM2) = CMi
        where
          CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
          {j} = {1,2}\i
          not Ds|- Cj[Tsj]<=Ci[Tsi]
          either
           - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
           - e?j is empty and Ti = Tj//only not derm on syntactically eq
           - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti
       */
    return Streams.zip(a.sig(), b.sig()).allMatch((aSig,bSig)->{
      assert a.name().equals(b.name());
      assert aSig.gens().equals(bSig.gens());
      assert aSig.bounds().equals(bSig.bounds());
      var xbs_ = xbs.addBounds(aSig.gens(), aSig.bounds());
      var ta = new T(Mdf.mut, a.c());
      var tb = new T(Mdf.mut, b.c());
      if(tryIsSubType(xbs, tb, ta)){ return false; }
      var ok=aSig.gens().equals(bSig.gens())
        && aSig.ts().equals(bSig.ts())
        && aSig.mdf()==bSig.mdf();
      if(!ok){ return false; }

      var isSubType = tryIsSubType(xbs, ta, tb) && tryIsSubType(xbs_, aSig.ret(), bSig.ret());
      if(isSubType){ return true; }

      var is1AbsAndRetEq = b.isAbs() && aSig.ret().equals(bSig.ret());
      if(is1AbsAndRetEq){ return true; }

      var is1AbsAndRetSubtype = b.isAbs()
        && tryIsSubType(xbs_, aSig.ret(), bSig.ret())
        && !tryIsSubType(xbs_, bSig.ret(), aSig.ret());
      if(is1AbsAndRetSubtype){ return true; }

      return false;
    });
  }

  default boolean isTransitiveSubType(XBs xbs, T t1, T t3) {
  /*
  MDF IT1 < MDF IT3
  where
    MDF IT1 < MDF IT2
    MDF IT2 < MDF IT3
  */
    var mdf = t1.mdf();
    if (mdf != t3.mdf()) { return false; }
    return itsOf(t1.itOrThrow()).stream()
      .anyMatch(t2->isSubType(xbs, new T(mdf, t2), t3));
  }

  default Id.IT<ast.T> liftIT(Id.IT<astFull.T>it){
    var ts = it.ts().stream().map(astFull.T::toAstT).toList();
    return new Id.IT<>(it.name(), ts);
  }
}
//----

/* m(xs) is just using the length of xs and the name (m) to extract the method
  Ds,C[Xs]|-m(xs)->e => sig->e, // task 1
  where
    C[Xs] : ITs {_} in Ds //TODO: in docs
    exists sig such that
      forall IT in ITs such that m(xs) in dom(Ds,IT) //TODO: in docs
        sig.xs = xs
        Ds|-meths(IT)(m(xs)) compatible with sig //that is, equal except for sig.xs


#Define meths(C[Ts])=CMs   meths(C[Xs]:MDF B)=CMs

meths(C[Ts]) = prune(Ms[Xs=Ts], meths(IT1[Xs=Ts]),..,meths(ITn[Xs=Ts]))
  where C[Xs]: IT1..ITn {x, Ms} in Ds

meths(C[Xs]:MDF IT1..ITn {x, Ms}) = prune(Ms, meths(IT1),..,meths(ITn))


#Define prune(CMs) = CMs'   norm(CM)=CM'   allCases(CMs)=CMss  pruneAux(CMs)=CM

prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
  where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
pruneAux(CM) = CM
pruneAux(CMs)= pruneAux(selectMoreSpecific(CMs1)..selectMoreSpecific(CMsn))
  where allCases(CMs) = CMs1..CMsn
  otherwise

allCases(CMs) = CMs.enumerate().permutations(2)
  .filter(<<i,m1>,<j,m2>> -> i < j)
  .map(<<i,m1>,<j,m2>> ->[m1,m2]).toList()

selectMoreSpecific(CM1,CM2) = CMi
  where
    CMi = Ci[Tsi] . MDF m[Xs](G):Ti e?i //Xs (not Xsi) requires the same (normed) Xs
    j = {1,2}\i
    either
     - Ds|- Ci[Tsi]<=Cj[Tsj] and Ds|- Ti<=Tj
     - e?j is empty and Ti = Tj//only not derm on syntactically eq
     - e?j is empty, Ds|- Ti<=Tj and not Ds|- Tj<=Ti

#Define T<T'

T<T

MDF X < MDF' X
  where MDF<MDF'

MDF IT < MDF' IT'
  where
    MDF<MDF'
    MDF IT < MDF IT'


MDF C[T1..Tn]<MDF C'[Ts]
  where
    C[X1..Xn]:ITs {_}
    C'[Ts] in ITs[X1..Xn=T1..Tn]

MDF C[T1..Tn]< MDF C[T1'..Tn']
  where
    adapterOk(MDF,C,T1..Tn,T1'..Tn')


#Define adapterOk(MDF,C,Ts1,Ts2)

adapterOk(MDF0,C,Ts1,Ts2)
filterByMdf(MDF0, meths(C[Ts1]) = Ms1
filterByMdf(MDF0, meths(C[Ts2]) = Ms2
forall MDF m[Xs](G1):T1 _,MDF m[Xs](G2):T2 _ in mWisePairs(Ms1,Ms2)
G2,inner:MDF0 C[Ts1] |- inner.m[Xs](G2.xs) : T2

_______
#Define filterByMdf(MDF,Ms) = Ms'

filterByMdf(MDF,empty) = empty
filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
  where MDF in {capsule,mut,lent}
filterByMdf(MDF,M Ms) = M,filterByMdf(MDF,Ms)
  where MDF in {imm,read} M.MDF in {imm,read}
filterByMdf(MDF,M Ms) = filterByMdf(MDF,Ms)
  otherwise


#Define MDF < MDF'

MDF < MDF
capsule < MDF
MDF < read
mut < lent
*/
