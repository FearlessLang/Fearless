package rt.flows.dataParallel.heartbeat;

import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.flows.dataParallel.BufferSink;
import rt.vpf.VPF;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public record HeartbeatFlow(_Sink_1 downstream, int size, List<FlowOp_1> splitData, BufferSink.FlushWorker flusher, CountDownLatch sync, Thread.UncaughtExceptionHandler exceptionHandler) implements Runnable {
  private static final int TASKS_PER_CORE = 4;
  private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
  public static final int PARALLELISM_POTENTIAL = TASKS_PER_CORE * N_CPUS;

  @Override public void run() {
    for (int i = 0; i < splitData.size(); ++i) {
      var worker = new HeartbeatFlowWorker(splitData().getFirst(), downstream, flusher, sync);
      if (splitData.size() == 1) {
        worker.run();
        return;
      }

      if (VPF.shouldSpawn()) {
        var next = new HeartbeatFlow(downstream, size, splitData.subList(i+1, splitData.size()), flusher, sync, exceptionHandler);
        VPF.spawnDirect(next, exceptionHandler);
        worker.run();
        return;
      }

      worker.run();
    }
  }
}
