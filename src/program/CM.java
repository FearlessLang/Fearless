package program;

import ast.E;
import ast.T;
import files.Pos;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface CM {
  Id.MethName name();
  List<String> xs();
  boolean isAbs();
  CM withSig(List<ast.E.Sig> sig);
  Pos pos();
  Id.IT<T> c();
  List<ast.E.Sig> sig();
  <TT extends Id.Ty> Map<Id.GX<TT>, Set<Mdf>> bounds();
  default String qualifiedName() {
    return c().name()+"$"+name();
  }
  default ast.E.Sig prioritySig(){ return sig().get(sig().size() - 1); }
  default String toStringSimplified(){
    var sigs = sig().stream()
      .map(sig->name()+"["+ sig.gens().stream().map(Id.GX::toString).collect(Collectors.joining(", "))+"]"+"("+sig.ts().stream().map(T::toString).collect(Collectors.joining(", "))+"): "+ sig.ret())
      .collect(Collectors.joining(","));
    return c() + ", " + sigs;
  }
  static CM of(Id.IT<T> c, ast.E.Meth m, List<ast.E.Sig> sig){ return new CoreCM(c,m,sig); }
  static CM of(Id.IT<T> c, astFull.E.Meth m, List<ast.E.Sig> sig){ return new FullCM(c,m,sig); }

  record CoreCM(Id.IT<T> c, ast.E.Meth m, List<ast.E.Sig> sig) implements CM{
    public Id.MethName name(){ return m.name(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    public CM withSig(List<ast.E.Sig> sig){ return new CoreCM(c, m, sig); }
    public Pos pos() { return this.m.posOrUnknown(); }
    @Override public Map<Id.GX<T>, Set<Mdf>> bounds() {
      return prioritySig().bounds();
    }

    @Override
    public String toString() {
      var sigs = sig().stream()
        .map(sig->sig.mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")" + sig.gens() + sig.ts() + ":" + sig.ret())
        .collect(Collectors.joining(","));
      return c + "," + sigs + (isAbs() ? "abs" : "impl");
    }
  }
  record FullCM(Id.IT<T> c, astFull.E.Meth m, List<ast.E.Sig> sig) implements CM{
    public Id.MethName name(){ return m.name().orElseThrow(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    @Override public Map<Id.GX<T>, Set<Mdf>> bounds() {
      return prioritySig().bounds();
    }
    public CM withSig(List<ast.E.Sig> sig){ return new FullCM(c, m, sig); }
    public Pos pos() { return this.m.posOrUnknown(); }

    @Override
    public String toString() {
      var sigs = sig().stream()
        .map(sig->sig.mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")" + sig.gens() + sig.ts() + ":" + sig.ret())
        .collect(Collectors.joining(","));
      return c + "," + sigs + (isAbs() ? "abs" : "impl");
    }
  }
}
