package program.typesystem;

import ast.E;
import ast.E.Sig;
import ast.T;
import failure.Fail;
import failure.FailOr;
import id.Id.IT;
import id.Mdf;
import program.CM;
import program.Program;
import program.TypeRename;
import utils.Push;
import utils.Range;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*//as in the paper
  Ls; Xs';G; empty |- e0 : RC0 D[Ts0]
  RC0 in {imm, iso}
  RC0 <= RC
  Xs' disjoint Xs
  RC m[Xs](x1:T1, .., xn:Tn):T -> e, in meths(RC0 D[Ts0])
  Ls; Xs'; G |- e1 : T1[Xs=Ts][⇑P] ... Xs',G |- en : Tn[Xs=Ts][⇑P]
-----------------------------------------------------------------(Prom-Call-T)
  Ls; Xs';G; Ts' |- e0 m[Ts](e1,...en): T[Xs=Ts][⇑r]
*/
public interface EMethTypeSystem extends ETypeSystem {
  /** priority for overloading over receiver modifier */
  List<Mdf> recvPriority = List.of(
    Mdf.iso, Mdf.mut, Mdf.imm, Mdf.recMdf, Mdf.read, Mdf.lent, Mdf.readOnly);
  static List<Mdf> inferPriority(Mdf recvMdf) {
    var base = recvPriority.stream().filter(mdf->mdf != recvMdf).toList();
    return Push.of(recvMdf, base);
  }
  /*  
  Ls; Xs'; G; empty |- e0 : T0
  sigs= meth(Ls,T0,m/n,Ts)//overloaded
  sig= selectOverload(sigs,T0,Ts')
  Ts1..Tsn -> Ts0= multiMeth(sig,T0,Ts')
  Ls; Xs'; G; Tsi |- ei : Ti  forall i in 1..n
  T= selectResult(T1..Tn, Ts1..Tsn -> Ts0)
---------------------------------------------------------------
  Ls; Xs'; G; Ts' |- e0 m[Ts](e1..en) : T
  
  |mut A|,|read B| ->|mut C|
  |iso A| |imm B |   |iso C|
*/
  default FailOr<T> visitMCall(E.MCall e) {
    var recV= this.withExpectedTs(List.of());
    var recT= e.receiver().accept(recV);
    return recT.flatMap(t0->visitMCall(t0,e));
  }
  private FailOr<T> visitMCall(T t0, E.MCall e) {
    return t0.match(
      gx->FailOr.err(()->Fail.noMethOnX(e,t0)),
      it->visitMCall(t0.mdf(),it,e)
      );
  }
  private CM applyGenerics(CM cm, List<T> ts){
    var renamer = TypeRename.core();
    var gens= cm.sig().gens();
    //var xbs= xbs().addBounds(gens, sig.bounds());//No? from 2 different scopes?
    var transformer= renamer.renameFun(ts, gens);
    Sig res=renamer.renameSigOnMCall(cm.sig(),xbs(),transformer);
    //Note: from this point on sig have 'generics' that have
    //already been replaced. Removed for clarity.
    res= new Sig(List.of(),Map.of(),res.ts(),res.ret(),res.pos());
    return cm.withSig(res);
  }
  private FailOr<T> visitMCall(Mdf mdf0, IT<T> recvIT, E.MCall e) {
    var sigs= p().meths(xbs(),mdf0,recvIT, e.name(),depth()).stream()
      .map(s->applyGenerics(s,e.ts()))
      .sorted(Comparator.comparingInt(cm->
          EMethTypeSystem.recvPriority.indexOf(cm.mdf())))
      .toList();    
    CM selected= selectOverload(e,sigs,mdf0,recvIT);
    List<T> ts= selected.sig().ts();
    TsT tst=new TsT(ts,selected.ret(),selected);
    resolvedCalls().put(e.callId(), tst);

    MultiSig multi= MultiSigBuilder.multiMethod(
      xbs(),selected.mdf(),//bounds,formalMdf
      selected.sig().ts(),//formalTs
      selected.sig().ret(),//formalRet,
      mdf0,this.expectedT());//mdf0,expectedRes
    FailOr<List<T>> ft1n= FailOr.fold(Range.of(e.es()),
      i-> e.es().get(i).accept(multi.expectedT(this, i)));
    return ft1n.flatMap(t1n->selectResult(e,multi,t1n));
  }
  private CM selectOverload(E.MCall e, List<CM> sigs, Mdf mdf0, IT<T> recvIT){
    if(sigs.size()==1){ return sigs.getFirst(); }
    return sigs.stream()
      .filter(cm->selectOverload(cm, mdf0))
      .findFirst()//Note: ok find first since ordered by mdf before
      .orElseThrow(()->Fail.undefinedMethod(e.name(), new T(mdf0, recvIT), sigs.stream()).pos(e.pos()));
  }
  private boolean selectOverload(CM cm, Mdf mdf0){
    if (!Program.isSubType(mdf0,cm.mdf())){ return false; }
    if (expectedT().isEmpty()){ return true; }
    //TODO: What about promotions? This strategy as it is now, is good enough to check the standard library.
    //readH could be passed to a read for example.
    //Full check. Too strict, promotions
    //return expectedT().stream()
    //  .anyMatch(t->p().isSubType(xbs(),cm.ret(),t));
    //Weaker check. Too strict, consider mut Opt[E] vs mut Opt[read/imm E]
    //return expectedT().stream().map(t->t.withMdf(Mdf.imm))
    //  .anyMatch(t->p().isSubType(xbs(),cm.ret().withMdf(Mdf.imm),t));
    return true;
  }
  private FailOr<T> selectResult(E.MCall e, MultiSig multi,List<T> t1n){
    assert multi.tss().size()==t1n.size();//That is, tss does not have 'this'?
    var sel= IntStream.range(0, multi.rets().size())
      .filter(i->ok(multi,i,t1n))
      .boxed()
      .findFirst();
   return sel
     .map(i->successType(e,i,multi))
     .orElse(FailOr.err(()->Fail.invalidMethodArgumentTypes(e,t1n,multi,expectedT())));
  }
  private FailOr<T> successType(E.MCall e, int i, MultiSig multi){
    T ret= multi.rets().get(i);
//    List<T> ts= multi.tss().stream().map(tsj->tsj.get(i)).toList();
//    TsT tst=new TsT(ts,ret,null);
//    resolvedCalls().put(e.callId(), tst);
    return FailOr.res(ret);
  }
  private boolean ok(MultiSig multi,int i,List<T> t1n){
    return IntStream.range(0, t1n.size()).allMatch(j->{
      var actualT= t1n.get(j);
      var formalT= multi.tss().get(j).get(i);//current par index, current attempt
      return p().isSubType(xbs(),actualT,formalT); 
    });
  }

}
  
  
  /*
  * meth(..) as before but return set in overloading

  * selectOverload(..) filters an overloaded version using T0,Ts

  * multiMeth with new order, and return ListListT->ListT, pre filtered by T0,Ts. If empty expected types Ts, we do not filter by Ts.
    sig[mut=iso, read=imm, readonly=imm]
    sig[mut=iso, read=imm]
    sig
    sig[result=hygienic][mut=iso, read=readonly] //ignoring the mut/iso
    sig[result=hygenic][1_mut=lent, other_muts=iso, read=imm ] //if only 1 mut exists

   */