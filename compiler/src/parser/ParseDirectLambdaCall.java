package parser;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;
import utils.Mapper;
import wellFormedness.FullUndefinedGXsVisitor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
/**
 * If a method is called directly on a literal, we should infer an explicit name for it immediately because what it
 * implements is irrelevant.
 */
public interface ParseDirectLambdaCall {
  static E of(E recv, Map<Id.GX<T>, Set<Mdf>> xbs) {
    if (!(recv instanceof E.Lambda lit)) {
      return recv;
    }
    if (lit.it().isPresent()) {
      return recv;
    }

    var freeGensVisitor = new FullUndefinedGXsVisitor(List.of());
    freeGensVisitor.visitLambda(lit);
    var freeGens = freeGensVisitor.res().stream().sorted(Comparator.comparing(Id.GX::name)).toList();

    // We need to do generic funnelling inference early here to generate the correct explicit name.
    // This whole thing would be unneeded if lambdas and their inference was not written assuming that they never had
    // a publicly visible name
    var fullId = new E.Lambda.LambdaId(
      new Id.DecId(lit.id().id().name(), freeGens.size()),
      freeGens,
      Mapper.of(res->freeGens.forEach(gx->res.put(gx, xbs.getOrDefault(gx, XBs.defaultBounds))))
    );
    var it = fullId.toIT();
    return lit
      .withLambdaId(fullId)
      .withITs(Stream.concat(lit.its().stream(), Stream.of(it)).distinct().toList())
      .withT(new T(Mdf.imm, it));
  }
}