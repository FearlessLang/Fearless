package rt.vpf;

import rt.FearlessError;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public final class VPF {
  private static ExecutorService executor;
  private static volatile boolean heartbeat = true;
  private static final AtomicLong running = new AtomicLong(0);
  private static ScheduledExecutorService heartbeatSideEffectsThread;
  private static ScheduledExecutorService heartbeatThread;
  private static ScheduledFuture<?> heart;
  private static long HEARTBEAT_INTERVAL;
  private static boolean heartbeatEffectsEnabled;
//  private static final long MAX_TASKS = ConfigureVPF.getTasksPerCPU(Runtime.getRuntime().availableProcessors());

  public static Runnable start(long heartbeatInterval, boolean enableHBEffects) {
    assert running.get() == 0 : running.get();
    VPF.executor = Executors.newVirtualThreadPerTaskExecutor();
    heartbeatThread = Executors.newSingleThreadScheduledExecutor();
    heartbeatSideEffectsThread = Executors.newSingleThreadScheduledExecutor();
    HEARTBEAT_INTERVAL = heartbeatInterval;
    heartbeatEffectsEnabled = enableHBEffects;

    if (heartbeatInterval > 0) {
      startBeat(heartbeatInterval);
    } else {
      heartbeat = false;
    }
    return () -> {
      heartbeatThread.shutdown();
      heartbeatSideEffectsThread.shutdown();
      VPF.executor.shutdown();
      heartbeat = false;
    };
  }
  public static void pauseAndRun(Runnable runDuringPause) {
    if (heart == null) {
      heartbeat = false;
      runDuringPause.run();
      return;
    }
    heart.cancel(false);
    heartbeat = false;
    try {
      runDuringPause.run();
    } finally {
      heartbeat = true;
      startBeat(HEARTBEAT_INTERVAL);
    }
  }
  public static void onHeartbeat(Runnable r) {
    if (!heartbeatEffectsEnabled || HEARTBEAT_INTERVAL <= 0) { return; }
    heartbeatSideEffectsThread.scheduleAtFixedRate(r, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.NANOSECONDS);
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
//    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
    return res;
  }
  public static void spawnDirect(Runnable task, Thread.UncaughtExceptionHandler handler) {
    Thread.ofVirtual().uncaughtExceptionHandler(handler).start(task);
//    if (MAX_TASKS >= 0) { running.incrementAndGet(); }
  }
  public static <R> R join(Future<R> future) {
    try {
//      if (MAX_TASKS >= 0) { running.decrementAndGet(); }
      return future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      switch (e.getCause()) {
        case FearlessError fe -> throw fe;
        case ArithmeticException err -> throw new FearlessError(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        case StackOverflowError _ -> throw new RuntimeException("Stack overflowed");
        case RuntimeException re -> throw re;
        default -> throw new RuntimeException(e.getCause());
      }
    }
  }

  private static void startBeat(long heartbeatInterval) {
    heart = heartbeatThread.scheduleAtFixedRate(VPF::beat, heartbeatInterval, heartbeatInterval, TimeUnit.NANOSECONDS);
  }
  private static void beat() {
//    if (MAX_TASKS >= 0 && running.getPlain() > MAX_TASKS) {
//      return;
//    }
    heartbeat = true;
  }
}
