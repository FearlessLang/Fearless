package program.typesystem;

import ast.T;
import failure.CompileError;
import id.Id;
import id.Mdf;
import program.TypeTable;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface SubTyping extends TypeTable {
  Map<SubTypeQuery, SubTypeResult> subTypeCache = new ConcurrentHashMap<>();
  default boolean isSubType(XBs xbs, astFull.T t1, astFull.T t2) {
    return isSubType(xbs, t1.toAstT(), t2.toAstT());
  }
  record SubTypeQuery(XBs xbs, T t1, T t2){}
  enum SubTypeResult { Yes, No }
  default boolean tryIsSubType(XBs xbs, T t1, T t2) {
    try {
      return isSubType(xbs, t1, t2);
    } catch (CompileError ce) { // due to the parallelism in okAll we can no longer easily prevent us getting stuck here
      System.err.println("sub-typing ignoring "+ce);
      return false;
    }
  }
  default boolean isSubType(XBs xbs, T t1, T t2) {
//    return isSubTypeAux(xbs, t1, t2);
    var q = new SubTypeQuery(xbs, t1, t2);
    if (subTypeCache.containsKey(q)) {
      var res = subTypeCache.get(q);
      return res == SubTypeResult.Yes;
    }
    var isSubType = isSubTypeAux(xbs, t1, t2);
    var result = isSubType ? SubTypeResult.Yes : SubTypeResult.No;
    subTypeCache.put(q, result);
    return isSubType;
  }

  default boolean isSubTypeAux(XBs xbs, T t1, T t2) {//t1<=t2
    if (t1.equals(t2)) { return true; }
    if (isReadImmSub(xbs, t1, t2)) { return true; }
    if (isRCSub(xbs, t1, t2)) { return true; }
    return isTransitiveSubType(xbs, t1, t2);
  }

  default boolean isReadImmSub(XBs xbs, T t1, T t2) {
    /* isRCSub handles most of the cases this addresses. One case where this is useful is:
       A[X:iso,mut]: {#(x: X): read/imm X -> x}
       because XBs |- read/imm X : read,imm,_ (and not read,_ or imm,_)
       but mut is not a subtype of imm.
     */
    if (!t1.isMdfX() || !t2.mdf().isReadImm()) {
      return false;
    }
    // No hyg allowed
    var kj = new KindingJudgement(this, xbs, Set.of(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.read), true);
    return t1.accept(kj).isRes();
  }

  default boolean isRCSub(XBs xbs, T t1, T t2) {
    if (!t1.withMdf(Mdf.mut).equals(t2.withMdf(Mdf.mut))) {
      return false;
    }

    // A cheap perf-hack that's not in the formalism that we can
    // do for RC partial type <= RC' same partial type
    if (t1.mdf().isSyntaxMdf() && t2.mdf().isSyntaxMdf()) {
      return isSubType(t1.mdf(), t2.mdf());
    }

    var rcs1 = t1.accept(new KindingJudgement(this, xbs, false)).get();
    var rcs2 = t2.accept(new KindingJudgement(this, xbs, false)).get();
    return isSubType(rcs1, rcs2);
  }
  static boolean isSubType(List<Set<Mdf>> rcss1, List<Set<Mdf>> rcss2) {
    for (var rcs1 : rcss1) {
      for (var rcs2 : rcss2) {
        if (isSubType(rcs1, rcs2)) {
          return true;
        }
      }
    }
    return false;
  }
  static boolean isSubType(Set<Mdf> rcs1, Set<Mdf> rcs2) {
    for (Mdf rc1 : rcs1) {
      for (Mdf rc2 : rcs2) {
        if (!isSubType(rc1, rc2)) {
          return false;
        }
      }
    }
    return true;
  }

  default boolean isTransitiveSubType(XBs xbs, T t1, T t3) {
    // RC-Sub handles transitivity already for generics, so we can combine this with (Inst-Sub) too.
    if (!(t1.rt() instanceof Id.IT<T> it1) || !(t3.rt() instanceof Id.IT<T>)) {
      return false;
    }
    return itsOf(it1).stream()
      .anyMatch(t2->isSubType(xbs, new T(t1.mdf(), t2), t3));
  }

  static boolean isSubType(Mdf m1, Mdf m2) { //m1<m2
    assert m1.isSyntaxMdf() && m2.isSyntaxMdf();
    if(m1 == m2){ return true; }
    if (m2.isReadH()) { return true; }
    return switch(m1){
      case mut -> m2.is(Mdf.mutH, Mdf.read);
      case imm -> m2.isRead();
      case iso -> true;
      case readH, read, mutH -> false;
      case mdf,recMdf,readImm -> throw Bug.unreachable();
    };
  }
}
