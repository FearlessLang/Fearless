package rt.flows.dataParallel.eod;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;
import rt.flows.dataParallel.DelayedStopSink;
import rt.flows.dataParallel.ParallelStrategies;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import static rt.flows.FlowCreator.IS_SEQUENTIALISED;

public record EODStrategies(_Sink_1 downstream, int size, List<FlowOp_1> splitData, int nTasks) implements ParallelStrategies {
  private static final int TASKS_PER_CORE = 128;
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  public static final int PARALLELISM_POTENTIAL = TASKS_PER_CORE * N_CPUS;
  //    public static final int PARALLELISM_POTENTIAL = 2;
  public static final Semaphore AVAILABLE_PARALLELISM = new Semaphore(PARALLELISM_POTENTIAL);

  @Override public void seqOnly() {
    var sink = new DelayedStopSink(downstream);
    for (var data : splitData) {
      if (data == null) { break; }
      data.for$mut(sink);
    }
    sink.stop();
  }

  @Override public void oneParOneSeq() {
    manyPar();
  }

  @SuppressWarnings("preview")
  @Override public void manyPar() {
    assert nTasks > 1;
    int perWorkerSize = size / nTasks;

    AtomicReference<RuntimeException> exception = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (_,err) -> {
      var message = err.getMessage();
      if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
      exception.compareAndSet(null, new RuntimeException(message, err));
    };
    var flusher = BufferSink.FlushWorker.start(handler);
    var sync = new CountDownLatch(nTasks);
    for (int i = 0; i < nTasks; ++i) {
      var subSource = splitData.get(i);
      var worker = new EODWorker(subSource, downstream, perWorkerSize, flusher, sync);
      if (i == nTasks - 1) {
        worker.run();
        break;
      }

      var willParallelise = AVAILABLE_PARALLELISM.tryAcquire();
      if (willParallelise) {
        worker.releaseOnDone = true;
        Thread.ofVirtual().uncaughtExceptionHandler(handler).start(worker);
      } else {
        ScopedValue.runWhere(IS_SEQUENTIALISED, null, worker);
      }
    }

    try {
      sync.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    flusher.stop(downstream);
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
  }
}
