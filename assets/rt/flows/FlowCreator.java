package rt.flows;

import base.Opt_1;
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
//    System.out.println("from "+original+" intended "+intended);
    var op = original.unwrapOp$mut(_UnwrapFlowToken_0.$self);
    Long size = original.size$mut();
    return fromFlowOp(intended, op, size);
  }

  static Flow_1 fromFlowOp(_FlowFactory_0 intended, FlowOp_1 op, long size) {
    if (size < 0) { return intended.fromOp$imm(op, Opt_1.$self); }
    var optSize = Opts_0.$self.$hash$imm(size);
//    if (true) {
//      return _SeqFlow_0.$self.fromOp$imm(op, optSize);
//    }
//    if (op.canSplit$read() == base.False_0.$self && intended instanceof DataParallelFlowK) {
//      return _SeqFlow_0.$self.fromOp$imm(op, optSize);
//    }

    var couldBeForkJoinAttempt = size == 2;
    if (couldBeForkJoinAttempt) {
      return intended.fromOp$imm(op, optSize);
    }

    if (size <= 1) {
      return _SeqFlow_0.$self.fromOp$imm(op, optSize);
    }

    if (size < 4 && intended instanceof DataParallelFlowK) {
      return rt.flows.pipelineParallel.PipelineParallelFlowK.$self.fromOp$imm(op, optSize);
    }

    return intended.fromOp$imm(op, optSize);
  }
}
