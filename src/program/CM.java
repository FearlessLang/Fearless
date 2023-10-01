package program;

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
  CM withSigs(List<ast.E.Sig> sig);
  Pos pos();
  Id.IT<T> c();
  List<ast.E.Sig> sigs();
  <TT extends Id.Ty> Map<Id.GX<TT>, Set<Mdf>> bounds();
  default Mdf mdf(){ return sigs().mdf(); }
  default T ret(){ return sigs().ret(); }
  default String qualifiedName() {
    return c().name()+"$"+name();
  }
  default String toStringSimplified(){ return c() + ", " + name()+"["+ sigs().gens().stream().map(Id.GX::toString).collect(Collectors.joining(", "))+"]"+"("+ sigs().ts().stream().map(T::toString).collect(Collectors.joining(", "))+"): "+ sigs().ret(); }
  static CM of(Id.IT<T> c, ast.E.Meth m, List<ast.E.Sig> sigs){ return new CoreCM(c,m,sigs); }
  static CM of(Id.IT<T> c, astFull.E.Meth m, List<ast.E.Sig> sigs){ return new FullCM(c,m,sigs); }

  record CoreCM(Id.IT<T> c, ast.E.Meth m, List<ast.E.Sig> sigs) implements CM{
    public Id.MethName name(){ return m.name(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    public CM withSigs(List<ast.E.Sig> sigs){ return new CoreCM(c, m, sigs); }
    public Pos pos() { return this.m.posOrUnknown(); }
    @Override public Map<Id.GX<T>, Set<Mdf>> bounds() {
      return sigs.get(sigs.size()-1).bounds();
    }

    @Override
    public String toString() {
      return c + "," + mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")"
        + sigs.gens() + sigs.ts() + ":" + ret()
        + (isAbs() ? "abs" : "impl");
    }
  }
  record FullCM(Id.IT<T> c, astFull.E.Meth m, ast.E.Sig sig) implements CM{
    public Id.MethName name(){ return m.name().orElseThrow(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    @Override public Map<Id.GX<T>, Set<Mdf>> bounds() {
      return sig.bounds();
    }
    public CM withSigs(ast.E.Sig sig){ return new FullCM(c, m, sig); }
    public Pos pos() { return this.m.posOrUnknown(); }

    @Override
    public String toString() {
      return c + "," + mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")"
        + sig.gens() + sig.ts() + ":" + ret()
        + (isAbs() ? "abs" : "impl");
    }
  }
}
