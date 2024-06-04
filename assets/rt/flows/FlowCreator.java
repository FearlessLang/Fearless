package rt.flows;

import base.Opts_0;
import base.flows.*;
import rt.flows.dataParallel.DataParallelFlowK;

public interface FlowCreator {
  /**
   * @param intended The flow factory the compiler intends for us to use
   * @param original The original flow that we are trying to promote
   * @return A flow that may or may not have been created with intended. This will only ever degrade a flow (i.e.
   * go from DP -> Seq) and never upgrade it (i.e. Seq -> DP).
   */
  static Flow_1 fromFlow(_FlowFactory_0 intended, Flow_1 original) {
    var op = original.unwrapOp$mut(_UnwrapFlowToken_0.$self);
    Long size = original.size$mut();
    var optSize = Opts_0.$self.$hash$imm(size);
    var couldBeForkJoinAttempt = size == 2;
    if (couldBeForkJoinAttempt) {
      return intended.fromOp$imm(op, optSize);
    }

    if (size <= 1) {
      return _SeqFlow_0.$self.fromOp$imm(op, optSize);
    }

    if (size < 4 && intended instanceof DataParallelFlowK) {
      return _PipelineParallelFlow_0.$self.fromOp$imm(op, optSize);
    }

    return intended.fromOp$imm(op, optSize);
  }
}
