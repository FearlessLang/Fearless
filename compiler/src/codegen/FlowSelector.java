package codegen;

import id.Id;
import magic.Magic;

import java.util.Optional;

public interface FlowSelector {
  static Optional<Id.DecId> bestParallelConstr(MIR.MCall call) {
    if (!call.canFlowParallelise()) { return Optional.empty(); }
    var res = call.variant().contains(MIR.MCall.CallVariant.DataParallelFlow)
      ? Magic.DataParallelFlowK
      : Magic.PipelineParallelFlowK;
    return Optional.of(res);
  }
}
