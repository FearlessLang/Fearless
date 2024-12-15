package program.typesystem;

import ast.E;
import ast.T;
import failure.FailOr;
import id.Mdf;
import program.Program;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class TypeSystemCache {
  // Identity of program + lambda id + mdf
  record LambdaRef(Program p, E.Lambda.LambdaId id, Mdf mdf) {}
  private final Map<LambdaRef, FailOr<T>> litTCache = new WeakHashMap<>();

  public FailOr<T> litT(Program p, E.Lambda lit, Supplier<FailOr<T>> compute) {
    var id = new LambdaRef(p, lit.id(), lit.mdf());
    var res = litTCache.get(id);
    if (res != null) { return res; }
    res = compute.get();
    litTCache.put(id, res);
    return res;
  }
}
