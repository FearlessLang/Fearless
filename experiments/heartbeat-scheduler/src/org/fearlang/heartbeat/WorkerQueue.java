package org.fearlang.heartbeat;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public final class WorkerQueue {
  private static final long HEARTBEAT_INTERVAL_MS = 80;

  private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private volatile boolean isWaiting = false;
  private CompletableFuture<Void> onSettled;
//  private Thread worker;
  private SubmissionPublisher<Msg> worker2;

  private final ConcurrentLinkedQueue<Runnable> globalTasks;
  public WorkerQueue(ConcurrentLinkedQueue<Runnable> globalTasks) {
    this.globalTasks = globalTasks;
  }

  sealed interface Msg {
    record Task(Runnable task) implements Msg {}
    record Stop() implements Msg {}
  }
  public void start(int index) {
//    this.worker = Thread.ofVirtual().name("[Heartbeat] Worker "+index).start(() -> {
//      while (true) {
//        var task = this.tasks.poll();
//        if (task != null) {
//          task.run();
//        } else {
//          if (this.isWaiting) {
//            this.onSettled.complete(null);
//            return;
//          }
//          LockSupport.park();
//        }
//      }
//    });
//    var singleVirtualThreadExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    this.worker2 = new SubmissionPublisher<>(ForkJoinPool.commonPool(), Flow.defaultBufferSize());
    this.onSettled = worker2.consume(msg -> {
      switch (msg) {
        case Msg.Task task -> task.task().run();
        case Msg.Stop _ -> worker2.close();
      }
    });

    this.scheduler.scheduleAtFixedRate(
      () -> {
        var task = globalTasks.poll();
        if (task == null) { return; }
        this.worker2.submit(new Msg.Task(task));
//        tasks.add(task);
//        LockSupport.unpark(this.worker);
      },
      HEARTBEAT_INTERVAL_MS,
      HEARTBEAT_INTERVAL_MS,
      TimeUnit.MILLISECONDS
    );
  }

  public void waitForSettle() {
    this.scheduler.shutdown();
    this.worker2.submit(new Msg.Stop());
    this.onSettled.join();
//    this.isWaiting = true;
//    LockSupport.unpark(this.worker);
//    this.onSettled.join();
  }
}
