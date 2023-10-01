package visitors;

import ast.E;
import ast.T;
import id.Id;
import id.Id.DecId;
import id.Id.MethName;
import id.Mdf;
import utils.Mapper;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CloneVisitor{
  default E.Meth visitMeth(E.Meth e){
    return new E.Meth(
      visitSig(e.sigs()),
      visitMethName(e.name()),
      e.xs(),
      e.body().map(b->b.accept(this)),
      e.pos()
    );
  }
  default E visitMCall(E.MCall e){ return new E.MCall(
    e.receiver().accept(this),
    visitMethName(e.name()),
    e.ts().stream().map(this::visitT).toList(),
    e.es().stream().map(ei->ei.accept(this)).toList(),
    e.pos()
  );}
  default E.X visitX(E.X e){return e;}
  default E.Lambda visitLambda(E.Lambda e){
    Supplier<Stream<T>> its = ()->e.its().stream().map(it->new T(e.mdf(), it)).map(this::visitT);
    return new E.Lambda(
      its.get().map(T::mdf).findFirst().orElseThrow(),
      its.get().map(T::itOrThrow).toList(),
      e.selfName(),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }
  default Mdf visitMdf(Mdf mdf){return mdf;}
  default MethName visitMethName(MethName e){ return e; }
  default E.Sig visitSig(E.Sig e){
    return new E.Sig(
      visitMdf(e.mdf()),
      e.gens().stream().map(this::visitGX).toList(),
      Mapper.of(acc->e.bounds().forEach((key, value)->{
        var res = value.stream().map(this::visitMdf).collect(Collectors.toSet());
        acc.put(key, res);
      })),
      e.ts().stream().map(this::visitT).toList(),
      visitT(e.ret()),
      e.pos()
    );
  }
  default T visitT(T t){ return new T(
    visitMdf(t.mdf()),
    t.rt().match(this::visitGX,this::visitIT)
  );}
  default Id.IT<T> visitIT(Id.IT<T> t){ return new Id.IT<>(
    t.name(),
    t.ts().stream().map(this::visitT).toList()
  );}
  default Id.GX<T> visitGX(Id.GX<T> t){ return t; }
  default T.Dec visitDec(T.Dec d) { return new T.Dec(
    visitDecId(d.name()),
    d.gxs().stream().map(this::visitGX).toList(),
    Mapper.of(acc->d.bounds().forEach((key, value)->{
      var res = value.stream().map(this::visitMdf).collect(Collectors.toSet());
      acc.put(key, res);
    })),
    visitLambda(d.lambda()),
    d.pos()
  );}
  default DecId visitDecId(DecId di){ return di; }
}