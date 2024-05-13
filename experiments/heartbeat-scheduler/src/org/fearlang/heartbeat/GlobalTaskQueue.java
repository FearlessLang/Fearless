package org.fearlang.heartbeat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

public final class GlobalTaskQueue {
  private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
  private WorkerQueue[] workers;
  private Thread worker;
  private volatile boolean isWaiting = false;
  private final CompletableFuture<Void> onSettled = new CompletableFuture<>();

  public void start() {
    this.worker = Thread.ofVirtual().name("[Heartbeat] Main loop").start(() -> {
      while (true) {
        var task = this.tasks.poll();
        if (task != null) {
          task.run();
        } else {
          if (this.isWaiting) {
            this.onSettled.complete(null);
            return;
          }
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
    this.isWaiting = true;
    LockSupport.unpark(this.worker);
    this.onSettled.join();
    Arrays.stream(workers).parallel().forEach(WorkerQueue::waitForSettle);
  }
}
