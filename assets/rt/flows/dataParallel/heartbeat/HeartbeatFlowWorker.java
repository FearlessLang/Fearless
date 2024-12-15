package rt.flows.dataParallel.heartbeat;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.FearlessError;
import rt.flows.dataParallel.BufferSink;
import rt.flows.dataParallel.SplitTasks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static rt.flows.dataParallel.heartbeat.HeartbeatFlow.PARALLELISM_POTENTIAL;

/**
 * EODWorker (Explosive Ordnance Disposal Worker) is a parallelism strategy that will optimistically parallelise tasks
 * until it runs out of capacity, blocking at that point or if there are a number of blocked workers, running
 * sequentially. This strategy aims to maximise parallelism while avoiding explosions of nested parallelism consuming
 * large amounts of memory.
 */
public final class HeartbeatFlowWorker implements Runnable {
  public static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    var splitData = SplitTasks.of(source, Math.max(PARALLELISM_POTENTIAL / 2, 1));
    int realSize = size >= 0 ? size : splitData.size();

    AtomicReference<RuntimeException> exception = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (_,err) -> {
      var message = err.getMessage();
      if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
      exception.compareAndSet(null, new RuntimeException(message, err));
    };
    var flusher = BufferSink.FlushWorker.start(handler);
    var sync = new CountDownLatch(splitData.size());
    new HeartbeatFlow(downstream, realSize, splitData, flusher, sync, handler).run();
    try {
      sync.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    flusher.stop(downstream);
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
  }


  private final FlowOp_1 source;
  private final BufferSink downstream;
  private final CountDownLatch sync;

  public HeartbeatFlowWorker(FlowOp_1 source, _Sink_1 downstream, BufferSink.FlushWorker flusher, CountDownLatch sync) {
    this.source = source;
    this.downstream = new BufferSink(downstream, flusher);
    this.sync = sync;
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
    } finally {
      this.flush();
      sync.countDown();
    }
  }

  public void flush() {
    this.downstream.flush();
  }
}
