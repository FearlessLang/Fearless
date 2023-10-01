package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import astFull.Program;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface FullShortCircuitVisitor<R> extends FullVisitor<Optional<R>> {
  default Optional<R> visitMeth(E.Meth e){
    return e.sigs().flatMap(this::visitSig)
      .or(()->e.name().flatMap(this::visitMethName))
      .or(()->e.body().flatMap(b->b.accept(this)));
  }
  static <R,ET> Optional<R> visitAll(List<ET> ts, Function<ET,Optional<R>> f){
    //return ts.stream().map(f).dropWhile(Optional::isEmpty).findFirst().flatMap(a->a);
    // ts.map(f).find(o=>o.isPresent()).flatMap(o=>o)
    //ts.filterMap(f).take(1)
    for(var e:ts){
      var r=f.apply(e);
      if(r.isPresent()){ return r; }
    }
    return Optional.empty();
  }
  default Optional<R> visitMCall(E.MCall e){
    return e.receiver().accept(this)
      .or(()->visitMethName(e.name()))
      .or(()->e.ts().flatMap(ts->visitAll(ts,this::visitT)))
      .or(()->visitAll(e.es(),ei->ei.accept(this)))
      .or(()->visitT(e.t()));
  }
  default Optional<R> visitX(E.X e){ return visitT(e.t()); }
  default Optional<R> visitLambda(E.Lambda e){
    return e.mdf().flatMap(this::visitMdf)
      .or(()->visitAll(e.its(),this::visitIT))
      .or(()->visitAll(e.meths(),this::visitMeth))
      .or(()->e.it().flatMap(this::visitIT));
  }
  default Optional<R> visitMdf(Mdf mdf){return Optional.empty();}
  default Optional<R> visitMethName(MethName e){ return Optional.empty(); }
  default Optional<R> visitSig(E.Sig e){
    return visitMdf(e.mdf())
      .or(()->visitAll(e.gens(),this::visitGX))
      .or(()->visitAll(e.ts(),this::visitT))
      .or(()->visitT(e.ret()));
  }
  default Optional<R> visitT(T t){
    if (t.isInfer()) { return Optional.empty(); }
    return visitMdf(t.mdf())
      .or(()->t.rt().match(this::visitGX,this::visitIT));
  }
  default Optional<R> visitIT(Id.IT<T> t){ return visitAll(t.ts(),this::visitT); }
  default Optional<R> visitGX(Id.GX<T> t){ return Optional.empty(); }
  default Optional<R> visitDec(T.Dec d){
    return visitAll(d.gxs(),this::visitGX).or(()->visitLambda(d.lambda()));
  }
  default Optional<R> visitDecId(Id.DecId di){ return Optional.empty(); }
  default Optional<R> visitAlias(T.Alias a){ return visitIT(a.from()); }
  default Optional<R> visitProgram(Program p){
    for (var d : p.ds().values()) {
      var r = visitDec(d);
      if (r.isPresent()) { return r; }
    }
    return Optional.empty();
  }
}
