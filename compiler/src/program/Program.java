package program;

import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.FailOr;
import files.Pos;
import id.Id;
import id.Mdf;
import id.Normaliser;
import id.Refresher;
import program.typesystem.ETypeSystem;
import program.typesystem.Gamma;
import program.typesystem.XBs;
import utils.*;
import visitors.CloneVisitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Program {
  Id.Dec of(Id.DecId dec);
  boolean isInlineDec(Id.DecId d);
  List<Id.IT<T>> itsOf(Id.IT<T> t);
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  List<NormResult> cMsOf(Mdf recvMdf, Id.IT<T> t);
  CM plainCM(CM fancyCM);
  Set<Id.GX<T>> gxsOf(Id.IT<T> t);
  Program withDec(T.Dec d);
  List<ast.E.Lambda> lambdas();
  Optional<Pos> posOf(Id.IT<T> t);
  /** Produce a clone of Program without any cached data */
  Program shallowClone();
  TypeSystemFeatures tsf();

  default void reset() {
    this.methsCache().clear();
    this.subTypeCache().clear();
  }

  static boolean isSubType(Mdf m1, Mdf m2) { //m1<m2
    if(m1 == m2){ return true; }
    if (m2.is(Mdf.readH)) { return true; }
    return switch(m1){
      case mut -> m2.isLikeMut();
      case imm -> m2.is(Mdf.read, Mdf.readImm);
      case readImm -> m2.is(Mdf.read);
      case iso -> true;
      case readH, mdf, recMdf, read, mutH -> false;
    };
  }
  default boolean isSubType(XBs xbs, astFull.T t1, astFull.T t2) {
    return isSubType(xbs, t1.toAstT(), t2.toAstT());
    }
  record SubTypeQuery(XBs xbs, T t1, T t2){}
  enum SubTypeResult { Yes, No, Adapting, Unknown }
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
    var q = new SubTypeQuery(xbs, t1, t2);
    var subTypeCache = this.subTypeCache();
    if (subTypeCache.containsKey(q)) {
      var res = subTypeCache.get(q);
//      if (res == SubTypeResult.Adapting) {
//        return true;
//      }
      if (res == SubTypeResult.Unknown) {
        return true;
//        throw Fail.cyclicSubType(t1, t2);
      }
      return res == SubTypeResult.Yes;
    }
    subTypeCache.put(q, SubTypeResult.Unknown);
    var isSubType = isSubTypeAux(xbs, t1, t2);
    var result = isSubType ? SubTypeResult.Yes : SubTypeResult.No;
    subTypeCache.put(q, result);
    return isSubType;
  }
  /**<pre>
  #define XBs |- T <= T
  (1) XBs |- T < RC' X where
    T = X or T = read/imm X
    forall RC in XBs(X) RC < RC'

  (2) XBs |- RC X <= T where
    T = X or T = read/imm X
    forall RC' in XBs(X) RC < RC'

  (3) XBs |- RC X <= RC' X
    where RC<RC'
   </pre>*/
  static boolean isSubTypeGX(XBs xbs, T t1, T t2) {
    if (t1.equals(t2)) { return true; }
    var gx1= t1.gxOrThrow();
    var gx2= t2.gxOrThrow();
    if (!gx1.equals(gx2)) { return false; }
    var mdf1 = t1.mdf();
    var mdf2 = t2.mdf();

    var bothSyntax= !mdf1.isMdf() && !mdf2.isMdf();
    if (bothSyntax){ return isSubType(mdf1,mdf2); } // (3)

    var bounds = xbs.get(gx1); //since gx1==gx2
    return bounds.stream().allMatch(mi->{
      if (mdf1.isMdf()) { return isSubType(mi, mdf2); } // (1)
      if (mdf2.isMdf()) { return isSubType(mdf1, mi); } // (2)
      throw Bug.unreachable();
    });
  }
  default boolean isSubTypeAux(XBs xbs, T t1, T t2) {//t1<=t2
    if (t1.equals(t2)) { return true; }
    if (t1.isGX() && t2.isGX()){ return isSubTypeGX(xbs, t1, t2); }
    if (t1.isGX()!=t2.isGX()){ return false; }
    if(!isSubType(t1.mdf(), t2.mdf())){ return false; }
    return Stream.of(t1.mdf(), t2.mdf()).distinct().anyMatch(mdf->{
      T t1_ = t1.withMdf(mdf); T t2_ = t2.withMdf(mdf);
      if(t1_.rt().equals(t2_.rt())){ return true; }
      if(!t1_.isIt() || !t2_.isIt()){ return false; }

      if (isTransitiveSubType(xbs, t1_, t2_)) { return true; }

      if (t1_.mdf() != t2_.mdf()) { return false; }
      if (t1_.itOrThrow().name().equals(t2_.itOrThrow().name())) {
        return isAdaptSubType(xbs, t1_, t2_);
      }
      return false;
    });
  }

  default boolean isAdaptSubType(XBs xbs, T t1, T t2) {
    if (!tsf().adapterSubtyping()) { return false; }
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
      .filter(cm->filterByMdf(mdf, cm.mdf()))
      .toList();
    List<CM> cms2 = meths(xbs, mdf, it2, 0).stream()
      .filter(cm->filterByMdf(mdf, cm.mdf()))
      .toList();

    var methsByName = Stream.concat(cms1.stream(), cms2.stream())
      .collect(Collectors.groupingBy(CM::name))
      .values();
    return methsByName.stream()
      .allMatch(ms->{
        assert ms.size() == 2;
        var m1 = ms.get(0);
        var m2 = ms.get(1);
//        var mdf_ = mdf.restrict(m1.mdf()).orElseThrow();
        var bounds = xbs.addBounds(m2.sig().gens(), m2.bounds());
        var g = Gamma.empty().captureSelf(bounds, "this", new T(mdf, it1), m1.mdf());
        var recv = new ast.E.X("this", Optional.empty());
        g = Streams.zip(m2.xs(), m2.sig().ts()).fold(Gamma::add, g);
//        subTypeCache().put(new SubTypeQuery(bounds, t1, t2), SubTypeResult.Adapting);

        var gxs = m2.sig().gens().stream().map(gx->new T(Mdf.mdf, gx)).toList();
        var e=new ast.E.MCall(recv, m2.name(), gxs, m2.xs().stream().<ast.E>map(x->new ast.E.X(x, Optional.empty())).toList(), Optional.empty());
        return  isType(g, bounds, e, m2.sig().ret());
        // TODO: automate this into some error logging for when adapt fails and it is the ultimate cause of a failed compilation
//        if (t1.toString().equals("read base.LList[mdf E]") && t2.toString().equals("read base.LList[read E]") && res.isPresent()) {
//          System.out.println("hdfgh");
//        }
      });
  }

  default boolean isType(Gamma g, XBs xbs, ast.E e, T expected) {
    var v = ETypeSystem.of(this, g, xbs, List.of(expected), new ConcurrentHashMap<>(), 0);
    FailOr<T> res = e.accept(v);
    if(!res.isRes()){ return false; }
    return isSubType(xbs,res.get(),expected);
  }

  static boolean filterByMdf(Mdf mdf, Mdf mMdf) {
    assert !mdf.isMdf();
    if (mdf.is(Mdf.iso, Mdf.mut, Mdf.recMdf, Mdf.mdf)) { return true; }
    if (mdf.isMutH() && !mMdf.isIso()) { return true; }
    return mdf.is(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.readH) && mMdf.is(Mdf.imm, Mdf.read, Mdf.readH, Mdf.recMdf);
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, Id.MethName name, int depth){
    return meths(xbs, recvMdf, it, depth).stream().filter(mi->mi.name().nameArityEq(name)).toList();
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, ast.E.Lambda l, int depth) {
    return methsAux(xbs, recvMdf, l.id().toIT()).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }

  default List<CM> meths(XBs xbs, Mdf recvMdf, Id.IT<T> it, int depth) {
    return methsAux(xbs, recvMdf, it).stream().map(cm->freshenMethGens(cm, depth)).toList();
  }
  record MethsCacheKey(XBs xbs, Mdf recvMdf, Id.IT<T> it){}
  HashMap<MethsCacheKey, List<NormResult>> methsCache();
  default List<NormResult> methsAux(XBs xbs, Mdf recvMdf, Id.IT<T> it) {
    var methsCache = this.methsCache();
    var cacheKey = new MethsCacheKey(xbs, recvMdf, it);
    // Can't use computeIfAbsent here because concurrent modification thanks to mutual recursion :-(
    if (methsCache.containsKey(cacheKey)) { return methsCache.get(cacheKey); }
    List<NormResult> cms = Stream.concat(
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
        e.gens().stream().map(this::visitGX).toList(),
        xbs,
        e.ts().stream().map(this::visitT).toList(),
        visitT(e.ret()),
        e.pos()
      );
    }
  }

  record NormResult(CM cm, Map<Id.GX<T>,Id.GX<T>> restoreSubst) {
    public CM restoreMethodGens() {
      var newSig = new RenameGens(restoreSubst).visitSig(cm.sig());
      if (newSig.gens().equals(List.of(new Id.GX<>("R"))) && newSig.ts().size() == 1 && newSig.ts().getFirst().toString().equals("mut base.ControlFlowMatch[R, R]")){
        System.out.println(newSig);
      }
      return cm.withSig(newSig);
    }
  }
  default NormResult norm(CM cm) {
    var gxs=cm.sig().gens();
    var normaliser = new Normaliser<T>(0);
    Map<Id.GX<T>,Id.GX<T>> subst = new HashMap<>();
    Map<Id.GX<T>,Id.GX<T>> restore = new HashMap<>();
    var normedNames = normaliser.normalisedNames(gxs.size());
    Streams.zip(gxs, normedNames).forEach((original,normed)->{
      subst.put(original, normed);
      var plain = original.name().endsWith("$") ? original : new Id.GX<T>(original.name()+"$0");
      restore.put(normed, plain);
    });
    var newSig = new RenameGens(subst).visitSig(cm.sig());
    return new NormResult(cm.withSig(newSig), Collections.unmodifiableMap(restore));
  }

  /**
   * Normalised CMs are required for 5a, but the rest of the type system needs fresh names.
   */
  default CM freshenMethGens(NormResult normedCM, int depth) {
    var cm = normedCM.restoreMethodGens();
    var gxs=cm.sig().gens();
    var refresher = new Refresher<T>(depth);
    Map<Id.GX<T>,Id.GX<T>> subst = refresher.substitutes(gxs);
    var newSig=new RenameGens(subst).visitSig(cm.sig());
    return cm.withSig(newSig);
  }

  default List<NormResult> prune(XBs xbs, List<NormResult> normed, Optional<Pos> lambdaPos) {
    /*
    prune(CMs) = pruneAux(CMs1)..pruneAux(CMsn)
      where CMs1..CMsn = groupByM(norm(CMs)) //groupByM(CMs)=CMss groups for the same m,n
     */
    var seen = new HashSet<CM>();
    var cmsMap = normed.stream()
      .filter(n->seen.add(n.cm()))
      .collect(Collectors.groupingBy(n->n.cm().name()));
    return cmsMap.values().stream()
      .map(ns->pruneAux(xbs, ns, lambdaPos, ns.size()+1))
      .toList();
  }

  default NormResult pruneAux(XBs xbs, List<NormResult> normedCMs, Optional<Pos> lambdaPos, int limit) {
    if(limit==0){
      throw Fail.uncomposableMethods(normedCMs.stream()
        .map(ncm->Fail.conflict(ncm.cm.pos(), ncm.restoreMethodGens().toStringSimplified()))
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
    assert !normedCMs.isEmpty();
    var first=normedCMs.get(0);
    if (normedCMs.size() == 1) { return first; }
    var nextCms=normedCMs.stream().skip(1)
      .peek(cmi->{
        var sameGens = first.cm.sig().gens().equals(cmi.cm.sig().gens());
        var sameBounds = first.cm.sig().bounds().equals(cmi.cm.sig().bounds());
        if (!sameGens || !sameBounds) {
          throw Fail.uncomposableMethods(List.of(
            Fail.conflict(first.cm.pos(), first.restoreMethodGens().toStringSimplified()),
            Fail.conflict(cmi.cm.pos(), cmi.restoreMethodGens().toStringSimplified())
          )).pos(lambdaPos);
        }
      })
      .filter(cmi->!firstIsMoreSpecific(xbs, first.cm, cmi.cm) && !firstIsMoreSpecific(xbs, plainCM(first.cm), plainCM(cmi.cm)))
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
    assert a.name().equals(b.name());
    assert a.sig().gens().equals(b.sig().gens());
    assert a.sig().bounds().equals(b.sig().bounds());
    var xbs_ = xbs.addBounds(a.sig().gens(), a.sig().bounds());
    var ta = new T(Mdf.mut, a.c());
    var tb = new T(Mdf.mut, b.c());
    if(tryIsSubType(xbs, tb, ta)){ return false; }
    var ok=a.sig().gens().equals(b.sig().gens())
      && a.sig().ts().equals(b.sig().ts())
      && a.mdf()==b.mdf();
    if(!ok){ return false; }

    var isSubType = tryIsSubType(xbs, ta, tb) && tryIsSubType(xbs_, a.ret(), b.ret());
    if(isSubType){ return true; }

    var is1AbsAndRetEq = b.isAbs() && a.ret().equals(b.ret());
    if(is1AbsAndRetEq){ return true; }

    var is1AbsAndRetSubtype = b.isAbs()
      && tryIsSubType(xbs_, a.ret(), b.ret())
      && !tryIsSubType(xbs_, b.ret(), a.ret());
    if(is1AbsAndRetSubtype){ return true; }

    return false;
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

  default Id.IT<T> liftIT(Id.IT<astFull.T>it){
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
