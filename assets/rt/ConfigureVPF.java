package rt;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class ConfigureVPF {
  public static AtomicInteger spawned = new AtomicInteger(0);
  private static final int DEPTH = 30;
  private static final String HB_PREF_NAME = "org.fearlang.vpf.heartbeat";

  public static int getHeartbeatInterval() {
    if (System.getenv("FEARLESS_NO_VPF") != null) {
      return -1;
    }
    if (System.getenv("FEARLESS_HEARTBEAT_INTERVAL") != null) {
      return Integer.parseInt(System.getenv("FEARLESS_HEARTBEAT_INTERVAL"));
    }

    var prefs = Preferences.userRoot();
    var hbPref = prefs.getInt(HB_PREF_NAME, 0);
    var mustRecompute = System.getenv("FEARLESS_RECOMPUTE_HEARTBEAT_INTERVAL") != null;
    if (hbPref < 1 || mustRecompute) {
      System.err.println("""
      Fearless Runtime: Computing heartbeat interval. This will only happen once. Be not afraid...
      You can use the environment variable FEARLESS_HEARTBEAT_INTERVAL to set the heartbeat interval manually.
      You can also set the environment variable FEARLESS_NO_VPF to disable VPF entirely.
      You can re-run this configuration by setting the environment variable FEARLESS_RECOMPUTE_HEARTBEAT_INTERVAL.
      """);
      var hb = computeHeartbeatInterval();
      System.err.println("Fearless Runtime: Heartbeat interval computed to "+hb+"ns.");
      prefs.putInt(HB_PREF_NAME, hb);
      try {
        prefs.flush();
      } catch (BackingStoreException e) {
        System.err.println("""
          Fearless Runtime Warning: Could not save heartbeat parameter to system preferences.
          You can use the environment variable FEARLESS_HEARTBEAT_INTERVAL to set the heartbeat interval manually.
          You can also set the environment variable FEARLESS_NO_VPF to disable VPF entirely.
          """);
        e.printStackTrace();
      }
      return hb;
    }
    return hbPref;
  }
  public static void main(String[] ignored) {
    System.err.println("Heartbeat interval: "+getHeartbeatInterval());
  }

  private static int computeHeartbeatInterval() {
    // TODO: maybe binary search, starting from 3000, using a timeout against the best option

    System.err.println("Fearless Runtime [ConfigureVPF]: Running test 1 of 2...");
    var stop = VPF.start(-1);
    var seq = avgTime(FibBenchmark.$self);
    stop.run();
    assert spawned.get() == 0;

    System.err.println("Fearless Runtime [ConfigureVPF]: Running test 2 of 2 (this one will take longer)...");
    stop = VPF.start(1);
    var par = avgTime(FibBenchmark.$self);
    stop.run();

    var spawnOverhead = (par - seq) / spawned.get(); // 3ns on my M1 Pro
//    System.out.println("spawned overhead: "+spawnOverhead);
    var heartbeat = 3 * spawnOverhead;
    assert heartbeat > 0;
    assert heartbeat <= Integer.MAX_VALUE;
//    System.out.printf("Sequential: %f ns\nParallel: %f ns\nHb: %d", seq, par, heartbeat);
    return Math.max(1, (int) heartbeat);
  }
  private static double avgTime(Runnable r) {
    long time = 0;
    int iterations = 400;
    // burn some time for JIT
    System.err.println("Fearless Runtime [ConfigureVPF]: Warming up...");
    for (int i = 0; i < iterations; ++i) {
      if (i % 10 == 0) {
        System.err.printf("Fearless Runtime [ConfigureVPF warm-up]: %d out of %d\n", i, iterations);
      }
      time += time(r);
    }
    System.err.println("Fearless Runtime [ConfigureVPF]: Starting test...");
    time = 0;
    iterations = 100;
    for (int i = 0; i < iterations; ++i) {
      if (i % 10 == 0) {
        System.err.printf("Fearless Runtime [ConfigureVPF test]: %d out of %d\n", i, iterations);
      }
      spawned = new AtomicInteger(0);
      time += time(r);
    }
    System.err.println("Fearless Runtime [ConfigureVPF]: Test done!");
    return time / (double)(iterations / 3);
  }
  private static long time(Runnable r) {
    var start = System.nanoTime();
    r.run();
    return System.nanoTime() - start;
  }

  private interface FibBenchmark extends Runnable {
    @Override default void run() {
      FibBenchmark.$self.$hash$imm(DEPTH);
    }
    FibBenchmark $self = new FibBenchmark(){
      @Override public Long $hash$imm(long n_m$) {
        return FibBenchmark.$hash$imm$fun(n_m$, this);
      }
    };
    Long $hash$imm(long n_m$);
    static Long $hash$imm$fun(long n_m$, FibBenchmark $this) {
      return (((Long.compareUnsigned(n_m$,1L)<=0?base.True_0.$self:base.False_0.$self) == base.True_0.$self
        ? n_m$
        : (VPF.shouldSpawn() ? promoted(n_m$, $this) : ((long) $this.$hash$imm((n_m$ - 1L)) + $this.$hash$imm((n_m$ - 2L))))
      ));
    }
    static Long promoted(long n_m$, FibBenchmark $this) {
      // Weirdly the macOS JVM's scheduler gets very fussy if we don't also spawn for the receiver
      var recv = VPF.spawn(()->$this.$hash$imm((n_m$ - 1L)));
      var arg0 = VPF.spawn(()->$this.$hash$imm((n_m$ - 2L)));
      var res = ((long)VPF.join(recv) + VPF.join(arg0));
      spawned.addAndGet(2);
      return res;
    }
  }
}
