package program.typesystem;

import ast.E;
import ast.T;
import failure.FailOr;
import program.Program;
import visitors.Visitor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface ETypeSystem extends Visitor<FailOr<T>> {
  @SuppressWarnings("preview")
  ScopedValue<Boolean> isRuntimeInvoked = ScopedValue.newInstance();

  Program p();
  Gamma g();
  XBs xbs();
  ConcurrentHashMap<Long, TsT> resolvedCalls();
  List<T> expectedT();
  int depth(); // used to call Program.meths with normalisation done correctly
  TypeSystemCache cache();
  default FailOr<T> visitX(E.X e){
    return g().get(e);
  }

  static ETypeSystem of(Program p, Gamma g, XBs xbs, List<T> expectedT, ConcurrentHashMap<Long, TsT> resolvedCalls, TypeSystemCache cache, int depth){
    record Ts(Program p, Gamma g, XBs xbs, List<T> expectedT, ConcurrentHashMap<Long, TsT> resolvedCalls, TypeSystemCache cache, int depth) implements EMethTypeSystem, ELambdaTypeSystem{}
    return new Ts(p,g,xbs,expectedT,resolvedCalls,cache,depth);
  }
  default ETypeSystem withExpectedTs(List<T> expectedT){ return of(p(), g(), xbs(), expectedT, resolvedCalls(), cache(), depth()); }
  default ETypeSystem withGamma(Gamma g){ return of(p(), g, xbs(), expectedT(), resolvedCalls(), cache(), depth()); }
  default ETypeSystem withXBs(XBs xbs){ return of(p(), g(), xbs, expectedT(), resolvedCalls(), cache(), depth()); }
  default ETypeSystem withProgram(Program p){ return of(p, g(), xbs(), expectedT(), resolvedCalls(), cache(), depth()); }
}