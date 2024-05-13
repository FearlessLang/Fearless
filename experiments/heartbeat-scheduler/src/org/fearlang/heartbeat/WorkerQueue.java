package org.fearlang.heartbeat;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class WorkerQueue {
  private static final long HEARTBEAT_INTERVAL_MS = 80;

  private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
  private volatile boolean isRunning = false;
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private final ConcurrentLinkedQueue<Runnable> globalTasks;
  public WorkerQueue(ConcurrentLinkedQueue<Runnable> globalTasks) {
    this.globalTasks = globalTasks;
  }

  public void start(int index) {
    var worker = Thread.ofVirtual().name("[Heartbeat] Worker "+index).start(() -> {
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

    this.scheduler.scheduleAtFixedRate(
      () -> {
        var task = globalTasks.poll();
        if (task == null) { return; }
        tasks.add(task);
        LockSupport.unpark(worker);
      },
      HEARTBEAT_INTERVAL_MS,
      HEARTBEAT_INTERVAL_MS,
      TimeUnit.MILLISECONDS
    );
  }

  public void waitForSettle() {
    while (!this.tasks.isEmpty() || this.isRunning) {
      Thread.onSpinWait();
    }
    this.scheduler.shutdown();
  }
}
