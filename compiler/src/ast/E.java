package ast;

import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import parser.Parser;
import utils.Mapper;
import visitors.CloneVisitor;
import visitors.CtxVisitor;
import visitors.Visitor;
import wellFormedness.UndefinedGXsVisitor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public interface E extends HasPos {
  E accept(CloneVisitor v);
  <R>  R accept(Visitor<R> v);
  <Ctx, R>  R accept(CtxVisitor<Ctx,R> v, Ctx ctx);

  // TODO: we could cache lambda's type checking like so:
  // - map from a pair (or a composed string of the two) of a string of gamma AND an expected T to a Res
  // could use newline as a delimiter. Could filter gamma to only include what is actually captured in the lambda
  record Lambda(LambdaId id, Mdf mdf, List<Id.IT<T>> its, String selfName, List<Meth> meths, Optional<Pos> pos) implements E {
    public Lambda {
      assert mdf != null;
      assert X.validId(selfName);
      assert meths != null;
      }

    public record LambdaId(Id.DecId id, List<Id.GX<T>> gens, Map<Id.GX<T>, Set<Mdf>> bounds) {
      public Id.IT<T> toIT() {
        return new Id.IT<>(id, gens.stream().map(gx->new T(Mdf.mdf, gx)).toList());
      }
      public LambdaId withId(Id.DecId id){
        return new LambdaId(id,gens,bounds); }
      public LambdaId withGens(List<Id.GX<T>> gens){
        return new LambdaId(id,gens,bounds); }
      public LambdaId withBounds(Map<Id.GX<T>, Set<Mdf>> bounds){
        return new LambdaId(id,gens,bounds); }
      public astFull.E.Lambda.LambdaId toFullAst() {
        return new astFull.E.Lambda.LambdaId(
          id,
          gens.stream().map(Id.GX::toFullAstGX).toList(),
          Mapper.of(xbs->bounds.forEach((k, v)->xbs.put(k.toFullAstGX(), v)))
        );
      }
    }

    @Override public E accept(CloneVisitor v) {
      return v.visitLambda(this);
    }
    @Override public <R> R accept(Visitor<R> v) {
      return v.visitLambda(this);
    }
    @Override public <Ctx,R> R accept(CtxVisitor<Ctx,R> v, Ctx ctx) {
      return v.visitLambda(this, ctx);
    }
    public ast.E.Lambda withMeths(List<Meth> meths) {
      return new ast.E.Lambda(id, mdf, its, selfName, meths, pos);
    }
    public ast.E.Lambda withId(LambdaId id) {
      return new ast.E.Lambda(id, mdf, its, selfName, meths, pos);
    }
    public ast.E.Lambda withITs(List<Id.IT<T>> its) {
      return new ast.E.Lambda(id, mdf, its, selfName, meths, pos);
    }
    public ast.E.Lambda withSelfName(String selfName) {
      return new ast.E.Lambda(id, mdf, its, selfName, meths, pos);
    }
    public ast.E.Lambda withMdf(Mdf mdf) {
      return new ast.E.Lambda(id, mdf, its, selfName, meths, pos);
    }
    public boolean isTopLevel() {
      return mdf.isMdf();
    }
    @Override
    public String toString() {
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));
      var selfName = Optional.ofNullable(selfName()).map(sn->"'" + sn).orElse("");
      return String.format("[-%s-]%s{%s %s}", mdf, its, selfName, meths);
    }
  }
  record MCall(long callId, E receiver, MethName name, List<T> ts, List<E> es, Optional<Pos> pos)implements E{
    private static AtomicInteger nextCallId = new AtomicInteger();
    public MCall(E receiver, MethName name, List<T> ts, List<E> es, Optional<Pos> pos) {
      this(nextCallId.getAndIncrement(), receiver, name, ts, es, pos);
    }
    public MCall{ assert receiver!=null && name.num()==es.size() && ts!=null; }
    @Override public E accept(CloneVisitor v){return v.visitMCall(this);}
    @Override public <R> R accept(Visitor<R> v){return v.visitMCall(this);}
    @Override public <Ctx,R> R accept(CtxVisitor<Ctx,R> v, Ctx ctx) {
      return v.visitMCall(this, ctx);
    }
    @Override public String toString() {
      return String.format("%s %s%s(%s)", receiver, name, ts, es);
    }
  }
  record X(String name, Optional<Pos> pos) implements E{
    public X{ assert validId(name); }
    public static boolean validId(String x){
      assert x!=null && !x.isEmpty() && !x.equals("_");
      if (x.endsWith("$")) { return true; }
      return new parser.Parser(Parser.dummy,x).parseX();
    }
    @Override public E accept(CloneVisitor v){ return v.visitX(this); }
    @Override public <R> R accept(Visitor<R> v){ return v.visitX(this); }
    @Override public <Ctx,R> R accept(CtxVisitor<Ctx,R> v, Ctx ctx) {
      return v.visitX(this, ctx);
    }
    @Override public String toString(){ return name; }
  }
  record Meth(Sig sig, MethName name, List<String> xs, Optional<E> body, Optional<Pos> pos) implements HasPos{
    public Meth{ //noinspection OptionalAssignedToNull
      assert sig!= null && name.num()==xs.size() && body!=null; }
    public boolean isAbs(){ return body().isEmpty(); }
    public ast.E.Meth withBody(Optional<ast.E> body) {
      return new ast.E.Meth(sig, name, xs, body, pos);
    }
    public ast.E.Meth withSig(Sig sig) {
      return new ast.E.Meth(sig, name, xs, body, pos);
    }
    public Mdf mdf() { return name.mdf().orElseThrow(); }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name, xs, sig, body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(List<Id.GX<T>> gens, Map<Id.GX<T>, Set<Mdf>> bounds, List<T> ts, T ret, Optional<Pos> pos) implements HasPos {
    public Sig{ assert gens!=null && ts!=null && ret!=null; }
    public astFull.E.Sig toAstFullSig() {
      return new astFull.E.Sig(
        gens.stream().map(gx->new Id.GX<astFull.T>(gx.name())).toList(),
        Mapper.of(xbs->bounds.forEach((k,v)->xbs.put(k.toFullAstGX(), v))),
        ts.stream().map(T::toAstFullT).toList(),
        ret.toAstFullT(),
        pos
      );
    }

    @Override public String toString() {
      if (bounds.values().stream().mapToLong(Collection::size).sum() == 0) {
        return "Sig[gens="+gens+",ts="+ts+",ret="+ret+"]";
      }
      var boundsStr = bounds.entrySet().stream()
        .sorted(Comparator.comparing(a->a.getKey().name()))
        .map(kv->kv.getKey()+"="+kv.getValue().stream().sorted(Comparator.comparing(Enum::toString)).toList())
        .collect(Collectors.joining(","));
      return "Sig[gens="+gens+",bounds={"+boundsStr+"},ts="+ts+",ret="+ret+"]";
    }
  }
}