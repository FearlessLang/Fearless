package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import utils.Mapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public interface FullCloneVisitor {
  default E.Meth visitMeth(E.Meth e){ return new E.Meth(
    e.sigs().map(sigs->sigs.stream().map(this::visitSig).toList()),
    e.name().map(this::visitMethName),
    e.xs(),
    e.body().map(b->b.accept(this)),
    e.pos()
  );}
  default E visitMCall(E.MCall e){ return new E.MCall(
    e.receiver().accept(this),
    visitMethName(e.name()),
    e.ts().map(tts->tts.stream().map(this::visitT).toList()),
    e.es().stream().map(ei->ei.accept(this)).toList(),
    visitT(e.t()),
    e.pos()
  );}
  default E visitX(E.X e){return visitXX(e);}
  default E.X visitXX(E.X e){return e;}
  default E visitLambda(E.Lambda e){ return visitLLambda(e); }
  default E.Lambda visitLLambda(E.Lambda e){ return new E.Lambda(
    e.mdf().map(this::visitMdf),
    e.its().stream().map(this::visitIT).toList(),
    e.selfName(),//visitXX is not ok since this is just a String
    e.meths().stream().map(this::visitMeth).toList(),
    e.it().map(this::visitIT),
    e.pos()
  ); }
  default Mdf visitMdf(Mdf mdf){return mdf;}
  default MethName visitMethName(MethName e){ return e; }
  default E.Sig visitSig(E.Sig e){return new E.Sig(
    visitMdf(e.mdf()),
    e.gens().stream().map(this::visitGX).toList(),
    Mapper.of(acc->e.bounds().forEach((key, value)->{
      var res = value.stream().map(this::visitMdf).collect(Collectors.toSet());
      acc.put(key, res);
    })),
    e.ts().stream().map(this::visitT).toList(),
    visitT(e.ret()),
    e.pos()
  );}
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
    d.name(),
    d.gxs().stream().map(this::visitGX).toList(),
    Mapper.of(acc->d.bounds().forEach((key, value)->{
      var res = value.stream().map(this::visitMdf).collect(Collectors.toSet());
      acc.put(key, res);
    })),
    visitLLambda(d.lambda()),
    d.pos()
  );}
  default T.Alias visitAlias(T.Alias a){ return new T.Alias(
    visitIT(a.from()),
    a.to(),
    a.pos()
  ); }
}