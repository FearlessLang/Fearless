package visitors;

import ast.E;
import ast.Program;
import ast.T;
import id.Mdf;
import program.CM;
import program.typesystem.XBs;

import java.util.Optional;

public class ShortCircuitVisitorWithEnv<R> implements ShortCircuitVisitor<R> {
  protected final Program p;
  protected Env env = new Env();

  public ShortCircuitVisitorWithEnv(Program p) { this.p = p; }

  public Optional<R> visitMeth(E.Meth e){
    var oldEnv=env;
    this.env=env.add(e);
    try{ return ShortCircuitVisitor.super.visitMeth(e); }
    finally{ this.env=oldEnv; }
  }

  public Optional<R> visitDec(T.Dec d){
    var oldEnv=env;
    this.env=env.add(d);
    try{ return ShortCircuitVisitor.super.visitDec(d); }
    finally{ this.env=oldEnv;}

  }
  public Optional<R> visitLambda(E.Lambda e){
    var oldEnv = env;
    var ms = p.meths(XBs.empty(), Mdf.recMdf, e, 0).stream().map(CM::name).toList();
    this.env = env.add(e, ms);
    try { return ShortCircuitVisitor.super.visitLambda(e); }
    finally { this.env = oldEnv; }
  }

  @Override public Optional<R> visitX(E.X e) {
    env.addUsage(e.name());
    return ShortCircuitVisitor.super.visitX(e);
  }
}
