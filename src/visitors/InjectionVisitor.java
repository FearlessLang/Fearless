package visitors;

import ast.T;
import astFull.E;
import failure.Fail;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Mapper;
import utils.Push;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InjectionVisitor implements FullVisitor<ast.E>{
  public ast.E.MCall visitMCall(E.MCall e) {
    // TODO: WHYYYYYY
    var recv = e.receiver().accept(this);
    assert e.ts().isPresent();
    return new ast.E.MCall(
      recv,
      e.name(),
      e.ts().orElseThrow().stream().map(this::visitTInGens).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos()
    );
  }

  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }

  public ast.E.Lambda visitLambda(E.Lambda e){
    var base = e.it().map(List::of).orElseGet(List::of);
    var its = Push.of(base, e.its().stream().filter(it->{
      if (base.isEmpty()) { return true; }
      return !it.name().equals(base.get(0).name());
    }).toList());

    // TODO: throw if no ITs (i.e. cannot infer type of lambda)

    return new ast.E.Lambda(
      e.mdf().orElse(Mdf.mdf),
      its.stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }

  public ast.T.Dec visitDec(astFull.T.Dec d){
    return new ast.T.Dec(
      d.name(),
      d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
      Mapper.of(xbs->d.bounds().forEach((gx, bs)->xbs.put(new Id.GX<>(gx.name()), bs))),
      this.visitLambda(d.lambda().withITs(Push.of(d.toIT(), d.lambda().its()))),
      d.pos()
    );
  }

  public ast.T visitT(astFull.T t){
    if (t.isInfer()) {
      // TODO: throw Fail.....
      throw new astFull.T.MatchOnInfer();
    }
    return t.toAstT();
  }

  public ast.T visitTInGens(astFull.T t){
    if (t.isInfer()) {
      // TODO: throw Fail.....
      throw Bug.todo();
    }
    // TODO: this has unintended (although maybe positive) side-effects
    // TODO: The best thing might be to just not infer iso generic params
//    if (t.mdf().isIso()) { t = t.withMdf(Mdf.mut); }
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitTInGens).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    // TODO: throw CompileError (i.e. no single abstract method)
    return new ast.E.Meth(
      visitSig(m.sigs().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this)),
      m.pos()
    );
  }

  public ast.E.Sig visitSig(E.Sig s){
    try {
      return new ast.E.Sig(
        s.mdf(),
        s.gens().stream().map(this::visitGX).toList(),
        Mapper.of(bounds->s.bounds().forEach((gx,bs)->bounds.put(visitGX(gx), bs))),
        s.ts().stream().map(this::visitT).toList(),
        this.visitT(s.ret()),
        s.pos()
      );
    } catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed(s.toString()).pos(s.pos());
    }
  }

  public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(kv.getValue())));
    return new ast.Program(coreDs);
  }

  private Id.GX<ast.T> visitGX(Id.GX<? extends Id.Ty> gx){
    return new Id.GX<>(gx.name());
  }
}
