package rt;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class VPF {
  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private static volatile boolean heartbeat = false;
  private static final long HEARTBEAT_INTERVAL = 3000;
  private static final AtomicLong running = new AtomicLong(0);
  private static final long MAX_TASKS = Runtime.getRuntime().availableProcessors() * 8000L;

  public static Runnable start() {
    var scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
    scheduleExecutor.scheduleAtFixedRate(VPF::beat, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.NANOSECONDS);
    return () -> {
      scheduleExecutor.shutdown();
      executor.shutdown();
    };
  }
  public static boolean shouldSpawn() {
    if (!heartbeat) {
      return false;
    }
    heartbeat = false;
    return true;
  }
  public static <R> Future<R> spawn(Callable<R> task) {
    var res = executor.submit(task);
    running.incrementAndGet();
    return res;
  }
  public static <R> R join(Future<R> future) {
    try {
      running.decrementAndGet();
      return future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      switch (e.getCause()) {
        case FearlessError fe -> throw fe;
        case RuntimeException re -> throw re;
        default -> throw new RuntimeException(e.getCause());
      }
    }
  }

  private static void beat() {
    if (running.getPlain() > MAX_TASKS) {
      return;
    }
    heartbeat = true;
  }
}
