package program;

import ast.T;
import files.Pos;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.stream.Collectors;

public interface CM {
  Id.MethName name();
  List<String> xs();
  boolean isAbs();
  CM withSig(ast.E.Sig sig);
  Pos pos();
  Id.IT<T> c();
  Mdf recvMdf();
  ast.E.Sig sig();
  default Mdf mdf(){ return sig().mdf(); }
  default T ret(){ return sig().ret(); }
  default String qualifiedName() {
    return c().name()+"$"+name();
  }
  default String toStringSimplified(){ return c() + ", " + name()+"["+ sig().gens().stream().map(Id.GX::toString).collect(Collectors.joining(", "))+"]"+"("+sig().ts().stream().map(T::toString).collect(Collectors.joining(", "))+"): "+ sig().ret(); }
  static CM of(Mdf recvMdf, Id.IT<T> c, ast.E.Meth m, ast.E.Sig sig){ return new CoreCM(recvMdf,c,m,sig); }
  static CM of(Mdf recvMdf, Id.IT<T> c, astFull.E.Meth m, ast.E.Sig sig){ return new FullCM(recvMdf,c,m,sig); }

  record CoreCM(Mdf recvMdf, Id.IT<T> c, ast.E.Meth m, ast.E.Sig sig) implements CM{
    public Id.MethName name(){ return m.name(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    public CM withSig(ast.E.Sig sig){ return new CoreCM(recvMdf, c, m, sig); }
    public Pos pos() { return this.m.posOrUnknown(); }

    @Override
    public String toString() {
      return c + "," + mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")"
        + sig.gens() + sig.ts() + ":" + ret()
        + (isAbs() ? "abs" : "impl");
    }
  }
  record FullCM(Mdf recvMdf, Id.IT<T> c, astFull.E.Meth m, ast.E.Sig sig) implements CM{
    public Id.MethName name(){ return m.name().orElseThrow(); }
    public List<String> xs(){ return m.xs(); }
    public boolean isAbs(){ return m.isAbs(); }
    public CM withSig(ast.E.Sig sig){ return new FullCM(recvMdf, c, m, sig); }
    public Pos pos() { return this.m.posOrUnknown(); }

    @Override
    public String toString() {
      return c + "," + mdf() + " " + name() + "(" + String.join(",", m.xs()) + ")"
        + sig.gens() + sig.ts() + ":" + ret()
        + (isAbs() ? "abs" : "impl");
    }
  }
}
