package org.fearlang.heartbeat;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

public final class GlobalTaskQueue {
  private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
  private volatile boolean isRunning = false;
  private WorkerQueue[] workers;
  private Thread worker;

  public void start() {
    this.worker = Thread.ofVirtual().name("[Heartbeat] Main loop").start(() -> {
      while (true) {
        var task = tasks.poll();
        if (task != null) {
          this.isRunning = true;
          task.run();
        } else {
          this.isRunning = false;
          LockSupport.park();
        }
      }
    });
    var nWorkers = Runtime.getRuntime().availableProcessors();
    this.workers = new WorkerQueue[nWorkers];
    IntStream.range(0, nWorkers).forEach(i->{
      workers[i] = new WorkerQueue(tasks);
      workers[i].start(i);
    });
  }

  public void submitTask(Runnable task) {
    this.tasks.add(task);
    LockSupport.unpark(this.worker);
  }

  public void waitForSettle() {
    while (!this.tasks.isEmpty() || this.isRunning) {
      Thread.onSpinWait();
    }
    Arrays.stream(workers).forEach(WorkerQueue::waitForSettle);
  }
}
