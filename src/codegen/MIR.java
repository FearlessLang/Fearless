package codegen;

import ast.E;
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

  record Program(Map<String, Trait> ds) {}
  record Trait(List<String> gens, List<String> its, Map<String, Meth> meths) {
    public boolean canSingleton() {
      return false; // TODO
//      return meths().values().stream().noneMatch(Meth::isAbs);
    }
  }
  record Meth(String name, Mdf mdf, List<String> gens, List<X> xs, String rt, Optional<MIR> body) {
    public boolean isAbs() { return body.isEmpty(); }
  }

  record L(Mdf mdf, long id) {
    public static void reset() { N = 0; }
    static long N = 0;
    L(Mdf mdf) { this(mdf, N++); }
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
