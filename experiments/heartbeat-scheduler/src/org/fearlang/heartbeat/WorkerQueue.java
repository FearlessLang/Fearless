package org.fearlang.heartbeat;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public final class WorkerQueue {
  private static final long BASE_HEARTBEAT_INTERVAL_MS = 60;

  private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean isWaiting = false;
  private CompletableFuture<Void> onSettled = new CompletableFuture<>();
  private Thread worker;
  private SubmissionPublisher<Runnable> worker2;

  private final ConcurrentLinkedQueue<Runnable> globalTasks;
  private final long heartbeatInterval;
  public WorkerQueue(ConcurrentLinkedQueue<Runnable> globalTasks) {
    this.globalTasks = globalTasks;
    this.heartbeatInterval = BASE_HEARTBEAT_INTERVAL_MS + new Random().nextInt(-10, 10);
  }

  public void start(int index) {
    this.worker = Thread.ofVirtual().name("[Heartbeat] Worker "+index).start(() -> {
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
//    var singleVirtualThreadExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
//    this.worker2 = new SubmissionPublisher<>(Executors.newVirtualThreadPerTaskExecutor(), 1);
//    this.onSettled = worker2.consume(Runnable::run);

    this.scheduler.scheduleAtFixedRate(
      () -> {
        var task = globalTasks.poll();
        if (task == null) { return; }
//        this.worker2.submit(task);
        tasks.add(task);
        LockSupport.unpark(this.worker);
      },
      this.heartbeatInterval,
      this.heartbeatInterval,
      TimeUnit.MILLISECONDS
    );
  }

  public void waitForSettle() {
    this.scheduler.shutdown();
//    this.worker2.submit(() -> this.worker2.close());
//    this.onSettled.join();
    this.isWaiting = true;
    LockSupport.unpark(this.worker);
    this.onSettled.join();
  }
}
