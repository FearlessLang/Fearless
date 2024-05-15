package org.fearlang.heartbeat;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public final class WorkerQueue {
  private static final long BASE_HEARTBEAT_INTERVAL_MS = 10;

  private final ConcurrentLinkedQueue<GlobalTaskQueue.Task> tasks = new ConcurrentLinkedQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private Thread worker;
  private SubmissionPublisher<GlobalTaskQueue.Task> worker2;

  private final ConcurrentLinkedQueue<GlobalTaskQueue.Task> globalTasks;
  private final long heartbeatInterval;
  public WorkerQueue(ConcurrentLinkedQueue<GlobalTaskQueue.Task> globalTasks) {
    this.globalTasks = globalTasks;
    this.heartbeatInterval = BASE_HEARTBEAT_INTERVAL_MS + new Random().nextInt(-5, 5);
  }

  public void start(int index) {
//    this.worker = Thread.ofVirtual().name("[Heartbeat] Worker "+index).start(() -> {
//      while (true) {
//        var task = this.tasks.poll();
//        if (task != null) {
//          task.run();
//        } else {
//          LockSupport.park();
//        }
//      }
//    });
    var singleVirtualThreadExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    this.worker2 = new SubmissionPublisher<>(singleVirtualThreadExecutor, 1);
    worker2.consume(GlobalTaskQueue.Task::run);

    this.scheduler.scheduleAtFixedRate(
      () -> {
        var task = globalTasks.poll();
        if (task == null) { return; }
        this.worker2.submit(task);
//        tasks.add(task);
//        LockSupport.unpark(this.worker);
      },
      this.heartbeatInterval,
      this.heartbeatInterval,
      TimeUnit.MILLISECONDS
    );
  }

  public void shutdown() {
    this.scheduler.shutdown();
  }
}
