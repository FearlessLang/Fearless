package rt.vpf;

import rt.FearlessError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class VPF {
  private static ExecutorService executor;
  private static volatile boolean heartbeat = false;
  private static final AtomicLong running = new AtomicLong(0);
  private static final long MAX_TASKS = ConfigureVPF.getTasksPerCPU(Runtime.getRuntime().availableProcessors());

  public static Runnable start(long heartbeatInterval) {
    assert running.get() == 0 : running.get();
    VPF.executor = Executors.newVirtualThreadPerTaskExecutor();

    var scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
    if (heartbeatInterval > 0) {
      scheduleExecutor.scheduleAtFixedRate(VPF::beat, heartbeatInterval, heartbeatInterval, TimeUnit.NANOSECONDS);
    }
    return () -> {
      scheduleExecutor.shutdown();
      VPF.executor.shutdown();
      heartbeat = false;
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
    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
    return res;
  }
  public static void spawnDirect(Runnable task, Thread.UncaughtExceptionHandler handler) {
    Thread.ofVirtual().uncaughtExceptionHandler(handler).start(task);
    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
  }
  public static <R> R join(Future<R> future) {
    try {
      if (MAX_TASKS >= 0) { running.decrementAndGet(); }
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
    if (MAX_TASKS >= 0 && running.getPlain() > MAX_TASKS) {
      return;
    }
    heartbeat = true;
  }
}
