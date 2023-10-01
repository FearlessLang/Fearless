package ast;

import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import parser.Parser;
import utils.Mapper;
import visitors.CloneVisitor;
import visitors.GammaVisitor;
import visitors.Visitor;

import java.util.*;
import java.util.stream.Collectors;

public interface E extends HasPos {
  E accept(CloneVisitor v);
  <R>  R accept(Visitor<R> v);
  <R>  R accept(GammaVisitor<R> v, String pkg, Map<String, T> gamma);

  // TODO: we could cache lambda's type checking like so:
  // - map from a pair (or a composed string of the two) of a string of gamma AND an expected T to a Res
  // could use newline as a delimiter. Could filter gamma to only include what is actually captured in the lambda
  record Lambda(Mdf mdf, List<Id.IT<T>> its, String selfName, List<Meth> meths, Optional<Pos> pos) implements E {
    public Lambda {
      assert mdf != null;
      assert !its.isEmpty();
      assert X.validId(selfName);
      assert meths != null;
    }

    @Override public E accept(CloneVisitor v) {
      return v.visitLambda(this);
    }
    @Override public <R> R accept(Visitor<R> v) {
      return v.visitLambda(this);
    }
    @Override public <R> R accept(GammaVisitor<R> v, String pkg, Map<String, T> gamma) {
      return v.visitLambda(pkg, this, gamma);
    }
    public ast.E.Lambda withMeths(List<Meth> meths) {
      return new ast.E.Lambda(mdf, its, selfName, meths, pos);
    }
    public ast.E.Lambda withITs(List<Id.IT<T>> its) {
      return new ast.E.Lambda(mdf, its, selfName, meths, pos);
    }
    @Override
    public String toString() {
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));
      var selfName = Optional.ofNullable(selfName()).map(sn->"'" + sn).orElse("");
      return String.format("[-%s-]%s{%s %s}", mdf, its, selfName, meths);
    }
  }
  record MCall(E receiver, MethName name, List<T> ts, List<E> es, Optional<Pos> pos)implements E{
    public MCall{ assert receiver!=null && name.num()==es.size() && ts!=null; }
    @Override public E accept(CloneVisitor v){return v.visitMCall(this);}
    @Override public <R> R accept(Visitor<R> v){return v.visitMCall(this);}
    @Override public <R> R accept(GammaVisitor<R> v, String pkg, Map<String, T> gamma) {return v.visitMCall(pkg, this, gamma);}
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
    @Override public <R> R accept(GammaVisitor<R> v, String pkg, Map<String, T> gamma) {return v.visitX(this, gamma);}
    @Override public String toString(){ return name; }
  }
  record Meth(List<Sig> sigs, MethName name, List<String> xs, Optional<E> body, Optional<Pos> pos) implements HasPos{
    public Meth{ //noinspection OptionalAssignedToNull
      assert sigs != null && name.num()==xs.size() && body!=null; }
    public boolean isAbs(){ return body().isEmpty(); }
    public ast.E.Meth withBody(Optional<ast.E> body) {
      return new ast.E.Meth(sigs, name, xs, body, pos);
    }
    public ast.E.Meth withSig(Sig sig) {
      return new ast.E.Meth(sig, name, xs, body, pos);
    }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name, xs, sigs, body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<Id.GX<T>> gens, Map<Id.GX<T>, Set<Mdf>> bounds,  List<T> ts, T ret, Optional<Pos> pos){
    public Sig{ assert mdf!=null && gens!=null && ts!=null && ret!=null; }
    public astFull.E.Sig toAstFullSig() {
      return new astFull.E.Sig(
        mdf,
        gens.stream().map(gx->new Id.GX<astFull.T>(gx.name())).toList(),
        Mapper.of(xbs->bounds.forEach((k,v)->xbs.put(k.toFullAstGX(), v))),
        ts.stream().map(T::toAstFullT).toList(),
        ret.toAstFullT(),
        pos
      );
    }
    @Override public String toString() {
      if (bounds.values().stream().mapToLong(Collection::size).sum() == 0) {
        return "Sig[mdf="+mdf+",gens="+gens+",ts="+ts+",ret="+ret+"]";
      }
      var boundsStr = bounds.entrySet().stream()
        .sorted(Comparator.comparing(a->a.getKey().name()))
        .map(kv->kv.getKey()+"="+kv.getValue().stream().sorted(Comparator.comparing(Enum::toString)).toList())
        .collect(Collectors.joining(","));
      return "Sig[mdf="+mdf+",gens="+gens+",bounds={"+boundsStr+"},ts="+ts+",ret="+ret+"]";
    }
  }
}