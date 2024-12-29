package visitors;

import ast.T;
import astFull.E;
import failure.Fail;
import id.Id;
import id.Id.GX;
import id.Id.IT;
import id.Mdf;
import program.typesystem.XBs;
import utils.Mapper;
import utils.Push;
import wellFormedness.UndefinedGXsVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class InjectionVisitor implements FullVisitor<ast.E>{
  private final Map<Id.GX<T>, Set<Mdf>> allBounds; 
  static public InjectionVisitor of(){ return of(Map.of()); } 
  static public InjectionVisitor of(Map<Id.GX<T>, Set<Mdf>> allBounds){
    return new InjectionVisitor(allBounds){
      InjectionVisitor renew(Map<GX<T>, Set<Mdf>> allBounds) {
        return of(allBounds);
      }};
  }
  protected InjectionVisitor(Map<Id.GX<T>, Set<Mdf>> allBounds){this.allBounds= allBounds;}
  
  public InjectionVisitor withMoreBounds(Map<Id.GX<T>, Set<Mdf>> moreBounds){
    assert Collections.disjoint(allBounds.keySet(),moreBounds.keySet());
    Map<Id.GX<T>, Set<Mdf>> map= Mapper.of(bs->{
      bs.putAll(allBounds);
      bs.putAll(moreBounds);
    });
    return renew(map);
  }
  abstract InjectionVisitor renew(Map<Id.GX<T>, Set<Mdf>> allBounds);
  public ast.E.MCall visitMCall(E.MCall e) {
    var recv = e.receiver().accept(this);
    if (e.ts().isEmpty()) {
      throw Fail.couldNotInferCallGenerics(e.name()).pos(e.pos());
    }
    List<ast.T> ts;
    try {ts= e.ts().get().stream().map(this::visitTInGens).toList();}
    catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed("Could not infer:\n"+e).pos(e.pos());
    }
    return new ast.E.MCall(recv,e.name(),ts,       
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos());
  }
  public ast.E.X visitX(E.X e){
    return new ast.E.X(e.name(), e.pos());
  }
  public ast.E.Lambda visitLambda(E.Lambda e){
    var gxs= e.id().gens().stream().map(this::visitGX).toList();
    var bounds= completeBounds(gxs,e.id().bounds());
    return visitLambda(e,gxs,bounds);
  }
  public ast.E.Lambda visitLambda(E.Lambda e,List<Id.GX<T>> gxs, Map<Id.GX<T>, Set<Mdf>> bounds){
    List<IT<astFull.T>> inferredType= e.it()
      .map(ti->Stream.concat(Stream.of(ti),e.its().stream()).distinct().toList())
      .orElse(List.of());
    var addSelfImpl= e.id().id().isFresh();
    //assert e.its().equals(inferredType):e.its()+"\n\n"+inferredType;//when we refactor so that this is true, much can be simplified
    var its= addSelfImpl ? inferredType : e.its();
    var lambdaId= new ast.E.Lambda.LambdaId(e.id().id(),gxs,bounds);
    Mdf lambdaMdf= e.mdf().orElse(Mdf.imm);

    List<Id.IT<T>> resIts;try{resIts = its.stream().map(this::visitIT).toList();}
    catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferImplementsFailed(its.toString()).pos(e.pos());
    }

    var resMeths= e.meths().stream().map(this::visitMeth).toList();
    boolean inferredName= lambdaId.id().isFresh();
    if (inferredName) {
      var freeGx= freeGx(resMeths,resIts);
      assert e.id().id().isFresh() || allBounds.keySet().containsAll(freeGx):
        allBounds.keySet()+" "+freeGx+" "+e;
        //TODO: it is quite ugly that fresh lambdas can be malformed in this sense
      lambdaId = computeId(lambdaId.id().name(), freeGx);
    }
    return new ast.E.Lambda(
      lambdaId, lambdaMdf,
      resIts,
      Optional.ofNullable(e.selfName()).orElseGet(E.X::freshName),
      resMeths,
      e.pos()
    );
  }
  private static List<Id.GX<T>> freeGx(List<ast.E.Meth> meths, List<Id.IT<T>> its){
    var visitor = new UndefinedGXsVisitor(List.of());//Find all the free type variables
    its.forEach(visitor::visitIT);
    meths.forEach(visitor::visitMeth);
    return visitor.res().stream()
      .sorted(Comparator.comparing(Id.GX::name)).toList();
  }
  private ast.E.Lambda.LambdaId computeId(String id, List<Id.GX<T>> gens) {
    Map<Id.GX<T>, Set<Mdf>> xbs = Mapper.of(xbs_->gens.stream()
      .filter(allBounds::containsKey).forEach(gx->xbs_.put(gx, allBounds.get(gx))));
    return new ast.E.Lambda.LambdaId(new Id.DecId(id, gens.size()), gens, xbs);
  }
  Map<Id.GX<T>, Set<Mdf>> completeBounds(
      List<Id.GX<T>> gxs, Map<Id.GX<astFull.T>, Set<Mdf>> bounds){
    return Mapper.of(xbs->gxs.forEach(xi->
      xbs.put(xi,bounds.getOrDefault(xi,XBs.defaultBounds))));
    //Note: it works across generic Id.GX<astFull.T> vs Id.GX<ast.T>
    //because of erasure + legacy get(Object)
  }
  public ast.T.Dec visitDec(astFull.T.Dec d){
    var e=d.lambda();
    var gxs= e.id().gens().stream().map(this::visitGX).toList();
    var bounds= completeBounds(gxs,e.id().bounds());
    var vBounds=this.withMoreBounds(bounds);
    var lambda= vBounds.visitLambda(d.lambda(),gxs,bounds);
    lambda= lambda.withMdf(Mdf.mdf);
    return new ast.T.Dec(lambda);
  }

  public ast.T visitT(astFull.T t){
    return t.toAstT();//throws MatchOnInfer if t == ?
  }

  public ast.T visitTInGens(astFull.T t){
    //throws MatchOnInfer if t contains infer as a type parameter
    return t.toAstT();
  }

  public Id.IT<ast.T> visitIT(Id.IT<astFull.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitTInGens).toList()
    );
  }

  public ast.E.Meth visitMeth(E.Meth m){
    if (m.sig().isEmpty() || m.name().isEmpty()) {
      throw Fail.inferFailed(m.toString()).pos(m.pos());
    }
    ast.E.Sig sig; try { sig = visitSig(m.sig().orElseThrow()); }
    catch (astFull.T.MatchOnInfer err) {
      throw Fail.inferFailed(m.toString()).pos(m.pos());
    }
    var vBounds=this.withMoreBounds(sig.bounds());
    return new ast.E.Meth(
      sig,
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(vBounds)),
      m.pos()
    );
  }

  public ast.E.Sig visitSig(E.Sig s){
    var gxs= s.gens().stream().map(this::visitGX).toList();
    return new ast.E.Sig(
      gxs, completeBounds(gxs,s.bounds()),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret()),
      s.pos()
    );
  }
  public ast.Program visitProgram(astFull.Program p){
    Map<Id.DecId, T.Dec> coreDs = p.ds().entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, kv->visitDec(kv.getValue())));
    return new ast.Program(p.tsf(), coreDs, Map.of());
  }
  private Id.GX<ast.T> visitGX(Id.GX<? extends Id.Ty> gx){
    return new Id.GX<>(gx.name());
  }
}