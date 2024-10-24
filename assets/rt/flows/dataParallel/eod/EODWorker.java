package rt.flows.dataParallel.eod;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.FearlessError;
import rt.flows.dataParallel.BufferSink;
import rt.flows.dataParallel.SplitTasks;

import java.util.concurrent.atomic.AtomicBoolean;

import static rt.flows.dataParallel.eod.EODStrategies.AVAILABLE_PARALLELISM;
import static rt.flows.dataParallel.eod.EODStrategies.PARALLELISM_POTENTIAL;

/**
 * EODWorker (Explosive Ordnance Disposal Worker) is a parallelism strategy that will optimistically parallelise tasks
 * until it runs out of capacity, blocking at that point or if there are a number of blocked workers, running
 * sequentially. This strategy aims to maximise parallelism while avoiding explosions of nested parallelism consuming
 * large amounts of memory.
 */
public final class EODWorker implements Runnable {
  public static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    var splitData = SplitTasks.of(source, Math.max(PARALLELISM_POTENTIAL / 2, 1));
    var nTasks = splitData.size();

    int realSize = size >= 0 ? size : nTasks;
    new EODStrategies(downstream, realSize, splitData, nTasks).run(nTasks);
  }


  private final FlowOp_1 source;
  private final BufferSink downstream;

  public EODWorker(FlowOp_1 source, _Sink_1 downstream, int sizeHint, BufferSink.FlushWorker flusher) {
    this.source = source;
    this.downstream = new BufferSink(downstream, flusher, sizeHint);
  }

  @Override public void run() {
    impl();
  }

  public void impl() {
    try {
      source.forRemaining$mut(downstream);
    } catch (FearlessError err) {
      downstream.pushError$mut(err.info);
    } catch (ArithmeticException err) {
      downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
    }
    this.flush();
  }

  public void flush() {
    this.downstream.flush();
  }
//
//  public void release() {
//    if (!HAS_RELEASED.isBound()) {
//      AVAILABLE_PARALLELSIM.release(1);
//    }
//  }
}
