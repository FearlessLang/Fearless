package org.fearlang.heartbeat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

public final class GlobalTaskQueue {
  private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
  private WorkerQueue[] workers;
  private Thread worker;

  public void start() {
    this.worker = Thread.ofVirtual().name("[Heartbeat] Main loop").start(() -> {
      while (true) {
        var task = this.tasks.poll();
        if (task != null) {
          task.run();
        } else {
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

  public CompletableFuture<Void> submitTask(Task task) {
    this.tasks.add(task);
    LockSupport.unpark(this.worker);
    return task.onComplete;
  }

  public void shutdown() {
    Arrays.stream(workers).parallel().forEach(WorkerQueue::shutdown);
  }

  public abstract static class Task {
    abstract void impl();

    private final CompletableFuture<Void> onComplete = new CompletableFuture<>();
    public void run() {
      this.impl();
      this.onComplete.complete(null);
    }
  }
}
