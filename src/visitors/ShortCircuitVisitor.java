package visitors;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Id.DecId;
import id.Id.MethName;
import id.Mdf;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public interface ShortCircuitVisitor<R> extends Visitor<Optional<R>> {
  default Optional<R> visitMeth(E.Meth e){
    return visitAll(e.sigs(), this::visitSig)
      .or(()->visitMethName(e.name()))
      .or(()->e.body().flatMap(b->b.accept(this)));
  }
  default Optional<R> visitSig(E.Sig e){
    return visitMdf(e.mdf())
      .or(()->visitAll(e.gens(),this::visitGX))
      .or(()->visitAll(e.ts(),this::visitT))
      .or(()->visitT(e.ret()));
  }
  static <R,ET> Optional<R> visitAll(Collection<ET> ts, Function<ET,Optional<R>> f){
    for(var e:ts){
      var r=f.apply(e);
      if(r.isPresent()){ return r; }
    }
    return Optional.empty();
  }
  default Optional<R> visitMCall(E.MCall e){
    return e.receiver().accept(this)
      .or(()->visitMethName(e.name()))
      .or(()->visitAll(e.ts(),this::visitT))
      .or(()->visitAll(e.es(),ei->ei.accept(this)));
  }
  default Optional<R> visitX(E.X e){ return Optional.empty(); }
  default Optional<R> visitLambda(E.Lambda e){
    return visitMdf(e.mdf())
      .or(()->visitAll(e.its(),this::visitIT))
      .or(()->visitAll(e.meths(),this::visitMeth));
  }
  default Optional<R> visitMdf(Mdf mdf){return Optional.empty();}
  default Optional<R> visitMethName(MethName e){ return Optional.empty(); }
  default Optional<R> visitT(T t){
    return visitMdf(t.mdf())
      .or(()->t.rt().match(this::visitGX,this::visitIT));
  }
  default Optional<R> visitIT(Id.IT<T> t){ return visitAll(t.ts(),this::visitT); }
  default Optional<R> visitGX(Id.GX<T> t){ return Optional.empty(); }
  default Optional<R> visitDec(T.Dec d){
    return visitAll(d.gxs(),this::visitGX).or(()->visitLambda(d.lambda()));
  }
  default Optional<R> visitProgram(Program p){
    return visitAll(p.ds().values(), this::visitDec);
  }
  default Optional<R> visitDecId(DecId di){ return Optional.empty(); }
}