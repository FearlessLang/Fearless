package rt;

import java.util.concurrent.*;

public final class VPF {
//  public static boolean isFlowRunning = false;

  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private static volatile boolean heartbeat = false;
  private static final long HEARTBEAT_INTERVAL = 2000;

  public static Runnable start() {
    var scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
    scheduleExecutor.scheduleAtFixedRate(() -> heartbeat = true, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.NANOSECONDS);
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
    return executor.submit(task);
  }
  public static <R> R join(Future<R> future) {
    try {
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

//  private static void beat() {
//    if (!isFlowRunning) {
//      heartbeat = true;
//    }
//  }
}
