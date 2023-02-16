package codegen;

import ast.E;
import ast.T;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "op")
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface MIR {
  <R> R accept(MIRVisitor<R> v);

  record Program(Map<String, List<Trait>> pkgs) {}
  record Trait(String name, List<String> gens, List<String> its, List<Meth> meths) {
    public boolean canSingleton() {
      return false; // TODO
//      return meths().values().stream().noneMatch(Meth::isAbs);
    }
  }
  record Meth(String name, Mdf mdf, List<String> gens, List<X> xs, String rt, Optional<MIR> body) {
    public boolean isAbs() { return body.isEmpty(); }
  }
  record X(Mdf mdf, String name, String type) implements MIR  {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitX(this);
    }
  }
  record MCall(MIR recv, String name, List<MIR> args) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitMCall(this);
    }
  }
//  record NewLambda(Mdf mdf, String kind, String name, String selfName, List<X> captures) implements MIR {
//    public <R> R accept(MIRVisitor<R> v) {
//      return v.visitNewLambda(this);
//    }
//  }
//  record NewDynLambda(Mdf mdf, String name, List<X> captures, List<Meth> meths) implements MIR {
//    public <R> R accept(MIRVisitor<R> v) {
//      return v.visitNewDynLambda(this);
//    }
//  }
//  record NewStaticLambda(Mdf mdf, String name) implements MIR {
//    public <R> R accept(MIRVisitor<R> v) {
//      return v.visitNewStaticLambda(this);
//    }
//  }
  record Lambda(Mdf mdf, String freshName, String selfName, List<String> its, List<X> captures, List<Meth> meths) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitLambda(this);
    }
  }
  record Ref(T type, MIR value) implements MIR {
    public <R> R accept(MIRVisitor<R> v) {
      return v.visitRef(this);
    }
  }
  record Num(Mdf mdf, long n) implements MIR {
    @Override public <R> R accept(MIRVisitor<R> v) {
      return v.visitNum(this);
    }
  }
  record UInt(Mdf mdf, long n) implements MIR {
    @Override public <R> R accept(MIRVisitor<R> v) {
      return v.visitUInt(this);
    }
  }
  record Str(Mdf mdf, String str) implements MIR {
    @Override public <R> R accept(MIRVisitor<R> v) {
      return v.visitStr(this);
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
