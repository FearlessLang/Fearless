package codegen;

import ast.T;
import codegen.java.SimplifiedTraitMIRVisitor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;
import visitors.MIRVisitor;

import java.util.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "op")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface MIR {
  <R> R accept(MIRVisitor<R> v);
  <R> R accept(MIRVisitor<R> v, boolean checkMagic);
  MIR accept(SimplifiedTraitMIRVisitor v);
  T t();

  record Program(Map<String, List<Trait>> pkgs) {}
  record Trait(Id.DecId name, List<Id.GX<T>> gens, List<Id.IT<T>> its, List<Meth> meths, boolean canSingleton) {}
  record Meth(Id.MethName name, Mdf mdf, List<Id.GX<T>> gens, List<X> xs, T rt, Optional<MIR> body) {
    public boolean isAbs() { return body.isEmpty(); }
  }
  record X(String name, T t) implements MIR  {
    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitX(this, checkMagic);
    }
    @Override public MIR accept(SimplifiedTraitMIRVisitor v) { return v.visitX(this); }
  }
  record MCall(MIR recv, Id.MethName name, List<MIR> args, T t) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitMCall(this, checkMagic);
    }
    @Override public MIR accept(SimplifiedTraitMIRVisitor v) { return v.visitMCall(this); }
  }
  record Lambda(Mdf mdf, Id.DecId freshName, String selfName, List<Id.IT<T>> its, Set<X> captures, List<Meth> meths, boolean canSingleton) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return this.accept(v, true);
    }
    public <R> R accept(MIRVisitor<R> v, boolean checkMagic) {
      return v.visitLambda(this, checkMagic);
    }
    @Override public MIR accept(SimplifiedTraitMIRVisitor v) { return v.visitLambda(this); }
    public T t() {
      return new T(mdf, new Id.IT<>(freshName, Collections.nCopies(freshName.gen(), new T(Mdf.mdf, new Id.GX<>("FearIgnored$")))));
    }
    public Lambda withITs(List<Id.IT<T>> its) {
      return new Lambda(mdf, freshName, selfName, its, captures, meths, canSingleton);
    }
  }
//  record Share(MIR e) implements MIR {
//    public <R> R accept(MIRVisitor<R> v) {
//      return v.visitShare(this);
//    }
//  }
//  }
//  record RefK(L out, L v) implements MIR {}
//  record DeRef(L out, L ref) implements MIR {}
//  record RefSwap(L out, L ref, L v) implements MIR {}

  enum Op {
    X,
    MCall,
    NewLambda,
    NewDynLambda,
    NewStaticLambda,
    NewInlineLambda,
    Share,
    RefK,
    DeRef,
    RefSwap
  }
}
