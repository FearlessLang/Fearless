package rt.flows.dataParallel;

import base.OptMatch_2;
import base.flows.FlowOp_1;
import base.flows._Sink_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

final class UnrestrictedWorker implements Runnable {
  static void forRemaining(FlowOp_1 source, _Sink_1 downstream, int size) {
    // Try to split up the source N_CPU times
    // TODO: we probably want to just do this on some of the dataset to try it out first
    var splitData = new ArrayList<FlowOp_1>();
    int i = 1;
    splitData.add(source);
    for (;; ++i) {
      var split = (FlowOp_1) splitData.get(i - 1).split$mut().match$mut(new OptMatch_2() {
        @Override public Object some$mut(Object split_) {
          return split_;
        }
        @Override public Object empty$mut() {
          return null;
        }
      });
      if (split == null) { break; }
      splitData.add(split);
    }
    int realSize = size >= 0 ? size : i;
    int perWorkerSize = realSize / i;

    var doneSignal = new CountDownLatch(i);
    var workers = new UnrestrictedWorker[i];
    AtomicReference<RuntimeException> exception = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (_,err) -> {
      var message = err.getMessage();
      if (err instanceof StackOverflowError) { message = "Stack overflowed"; }
      exception.compareAndSet(null, new RuntimeException(message, err));
    };
    var flusher = BufferSink.FlushWorker.start(handler);
    for (int j = 0; j < i; ++j) {
      var actualException = exception.getAcquire();
      if (actualException != null) {
        throw actualException;
      }
      var subSource = splitData.get(j);
      assert subSource != null;
      var worker = new UnrestrictedWorker(subSource, downstream, perWorkerSize, doneSignal, flusher);
      Thread.ofVirtual().start(worker);
      workers[j] = worker;
    }
    try {
      doneSignal.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    for (var worker : workers) {
      if (worker == null) { break; }
      worker.downstream.flush();
    }
    var actualException = exception.get();
    if (actualException != null) {
      throw actualException;
    }
  }

  private final FlowOp_1 source;
  private final BufferSink downstream;
  private final CountDownLatch doneSignal;
  public UnrestrictedWorker(FlowOp_1 source, _Sink_1 downstream, int size, CountDownLatch doneSignal, BufferSink.FlushWorker flusher) {
    this.source = source;
    this.downstream = new BufferSink(downstream, flusher);
    this.doneSignal = doneSignal;
  }

  @Override public void run() {
    source.forRemaining$mut(downstream);
    doneSignal.countDown();
  }
}
