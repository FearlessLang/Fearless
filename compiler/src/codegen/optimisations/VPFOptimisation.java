package codegen.optimisations;

import ast.E;
import ast.T;
import id.Mdf;
import program.TypeTable;
import program.typesystem.KindingJudgement;
import program.typesystem.TsT;
import utils.Streams;
import visitors.CloneVisitor;
import visitors.ShortCircuitVisitor;
import visitors.Visitor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class VPFOptimisation {
  private static final Set<Mdf> GOOD_RCs = Set.of(Mdf.iso, Mdf.imm, Mdf.read);
  private final TypeTable tt;
  private final Map<Long, TsT> resolvedCalls;

  public VPFOptimisation(TypeTable tt, Map<Long, TsT> resolvedCalls) {
    this.tt = tt;
    this.resolvedCalls = resolvedCalls;
  }

  public boolean shouldPromote(E.MCall call) {
    // must have at least 2 calls (incl. receiver)
    var subCalls = Stream.concat(Stream.of(call.receiver()), call.es().stream())
      .filter(e -> e instanceof E.MCall)
      .limit(2)
      .count();
    if (subCalls != 2) {
      return false;
    }

    return canPromote(call);
  }

  private boolean canPromote(E.MCall call) {
    var actualTypes = resolvedCalls.get(call.callId());

    // all args must be good rc
    var hasGoodRC = new KindingJudgement(tt, actualTypes.xbs(), GOOD_RCs, true);
    if (call.receiver() instanceof E.MCall recvCall) {
      var isRecvOk = canPromote(recvCall);
      if (!isRecvOk) { return false; }
    } else {
      var isRecvOk = isGoodRC(actualTypes.recv());
      if (!isRecvOk) { return false; }
    }

    var areArgsOk = Streams.zip(call.es(), actualTypes.ts())
      .allMatch((arg, t)->arg.accept(new IsGoodExpr(this, t, hasGoodRC)));
    return areArgsOk;
  }

  private boolean isGoodRC(Mdf mdf) {
    return GOOD_RCs.contains(mdf);
  }

  record IsGoodExpr(VPFOptimisation vpf, T exprT, KindingJudgement hasGoodRC) implements Visitor<Boolean> {
    @Override public Boolean visitMCall(E.MCall e) {
      return vpf.canPromote(e);
    }
    @Override public Boolean visitX(E.X e) {
      return exprT.accept(hasGoodRC).isRes();
    }
    @Override public Boolean visitLambda(E.Lambda e) {
      return exprT.accept(hasGoodRC).isRes();
    }
  }
}
