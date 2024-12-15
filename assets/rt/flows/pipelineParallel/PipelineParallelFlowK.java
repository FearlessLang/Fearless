package rt.flows.pipelineParallel;

import base.Bool_0;
import base.Opt_1;
import base.Void_0;
import base.flows.*;
import rt.FearlessError;

public interface PipelineParallelFlowK extends _PipelineParallelFlow_0 {
  PipelineParallelFlowK $self = new PipelineParallelFlowK(){};

  @Override default Flow_1 fromOp$imm(FlowOp_1 source_m$, Opt_1 size_m$) {
    var source = RestrictFlowReuse_0.$hash$imm$fun(new SafeSource(source_m$), RestrictFlowReuse_0.$self);
    return _PipelineParallelFlow_0.fromOp$imm$fun(source, size_m$, this);
  }
  @Override default Flow_1 $hash$imm(Object e_m$) {
    return _PipelineParallelFlow_0.$hash$imm$fun(e_m$, this);
  }

  /**
   * A variant of FlowOp which catches all FearlessErrors during execution of the operation.
   * We only catch errors in .step/.forRemaining. Any other methods should never throw. This is unenforceable for user-written
   * FlowOps, but we control all FlowOps used within any parallel flows-- so we can make sure to uphold this property.
   * More thought would be needed if we wanted to support parallel flows of user created data structures in the future.
   */
  final class SafeSource implements FlowOp_1 {
    private final FlowOp_1 original;
    private SafeSource(FlowOp_1 original) {
      this.original = original;
    }

    @Override public Bool_0 isFinite$mut() {
      return original.isFinite$mut();
    }

    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      try {
        return original.step$mut(sink_m$);
      } catch (PipelineParallelFlow.DeterministicFearlessError err) {
        throw err;
      } catch (FearlessError err) {
        sink_m$.pushError$mut(err.info);
        return Void_0.$self;
      } catch (ArithmeticException err) {
        sink_m$.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        return Void_0.$self;
      }
    }

    @Override public Void_0 stop$mut() {
      return original.stop$mut();
    }

    @Override public Bool_0 isRunning$mut() {
      return original.isRunning$mut();
    }

    @Override public Void_0 forRemaining$mut(_Sink_1 downstream_m$) {
      try {
        return original.forRemaining$mut(downstream_m$);
      } catch (PipelineParallelFlow.DeterministicFearlessError err) {
        throw err;
      } catch (FearlessError err) {
        downstream_m$.pushError$mut(err.info);
        return Void_0.$self;
      } catch (ArithmeticException err) {
        downstream_m$.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        return Void_0.$self;
      }
    }

    @Override public Opt_1 split$mut() {
      return original.split$mut();
    }

    @Override public Bool_0 canSplit$read() {
      return original.canSplit$read();
    }
  }
}