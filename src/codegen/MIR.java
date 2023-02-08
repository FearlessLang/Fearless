package codegen;

import ast.E;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;
import utils.Bug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "op")
@JsonSerialize
public interface MIR {
  static String getName(Id.DecId dec) {
    return getBase(dec.name())+"_"+dec.gen()+"_"+0;
  }
  static String getName(E.Lambda inlineDec) {
    throw Bug.todo();
  }
  static String getName(Id.MethName m) {
    return getBase(m.name())+"_"+m.num();
  }
  static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c)) { return Character.toString(c); }
      return "$"+c;
    }).collect(Collectors.joining());
  }

  record Package(Map<String, Trait> ds) {}
  record Trait(List<String> gens, List<String> impls, Map<String, Meth> meths) {}
  record Meth(Mdf mdf, List<String> xs, List<MIR> body) {
    boolean isAbs() { return body.isEmpty(); }
  }

  record L(Mdf mdf, long id) {
    public static void reset() { N = 0; }
    static long N = 0;
    L(Mdf mdf) { this(mdf, N++); }
  }
  record X(Mdf mdf, String name) implements MIR  {}
  record MCall(Mdf mdf, MIR recv, String name, List<MIR> args) implements MIR {}
  record NewLambda(Mdf mdf, String kind, String name, String selfName, List<X> captures) implements MIR {}
  record NewDynLambda(Mdf mdf, String name, String selfName, List<X> captures) implements MIR {}
  record NewStaticLambda(Mdf mdf, String name, String selfName) implements MIR {}
  record Share(MIR e) implements MIR {}
  record RefK(L out, L v) implements MIR {}
  record DeRef(L out, L ref) implements MIR {}
  record RefSwap(L out, L ref, L v) implements MIR {}

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
