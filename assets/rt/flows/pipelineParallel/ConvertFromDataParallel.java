package rt.flows.pipelineParallel;

import base.*;
import base.flows.FlowOp_1;
import base.flows.Flow_1;
import base.flows._PipelineParallelFlow_0;
import base.flows._Sink_1;
import rt.flows.dataParallel.DataParallelFlow;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Certain operations are not safe in DP, so we need to convert to PP.
 * However, if we just start adapting the flow-op from that point,
 * everything before the operator that made us convert to DP will be
 * sequential-- which is sad.
 * This operation will effectively split the flow up into two flows:
 * A DP flow (1) with a terminal operator that runs a PP flow (2).
 */
public interface ConvertFromDataParallel {
  static Flow_1 of(DataParallelFlow source, Opt_1 size) {
    var dpSource = source.getDataParallelSource();
    var dpConsumer = new DPSource(dpSource);

    return _PipelineParallelFlow_0.$self.fromOp$imm(dpConsumer, size);
  }
}

class DPSource implements FlowOp_1 {
  private final BlockingDeque<Object> buffer = new LinkedBlockingDeque<>();
  private boolean isRunning = true;
  private boolean hasStarted = false;
  private final DataParallelFlow.DataParallelSource source;
  DPSource(DataParallelFlow.DataParallelSource source) {
    this.source = source;
  }

  private void start() {
    assert !hasStarted : "start called twice on DPSource";
    hasStarted = true;
    Thread.ofVirtual().start(()->
      source.for$mut(new _Sink_1() {
        @Override public Void_0 stopDown$mut() {
          buffer.offer(PipelineParallelFlow.Message.Stop.INSTANCE);
          return Void_0.$self;
        }
        @Override public Void_0 pushError$mut(Info_0 info_m$) {
          buffer.offer(new PipelineParallelFlow.Message.Error(info_m$));
          return Void_0.$self;
        }
        @Override public Void_0 $hash$mut(Object x_m$) {
          buffer.offer(x_m$);
          return Void_0.$self;
        }
      }));
  }

  @Override public Bool_0 isFinite$mut() {
    return source.isFinite$mut();
  }
  @Override public Void_0 step$mut(_Sink_1 sink_m$) {
    if (isRunning$mut() == False_0.$self) {
      return Void_0.$self;
    }
    if (!hasStarted) {
      start();
    }
    var sink = (PipelineParallelFlow.WrappedSink) sink_m$;
    try {
      var msg = buffer.take();
      sink.subject.submit(msg);
      if (msg == PipelineParallelFlow.Message.Stop.INSTANCE) {
        isRunning = false;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (!isRunning) {
      sink.stopDown$mut();
      this.stopUp$mut();
    }
    return Void_0.$self;
  }
  @Override public Void_0 stopUp$mut() {
    isRunning = false;
    source.stopUp$mut();
    return Void_0.$self;
  }
  @Override public Bool_0 isRunning$mut() {
    return isRunning ? True_0.$self : False_0.$self;
  }
  @Override public Void_0 for$mut(_Sink_1 downstream_m$) {
    return base.flows.FlowOp_1.for$mut$fun(downstream_m$, this);
  }
  @Override public Opt_1 split$mut() {
    return Opt_1.$self;
  }
  @Override public Bool_0 canSplit$read() {
    return False_0.$self;
  }
}
