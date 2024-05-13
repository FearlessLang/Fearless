package org.fearlang.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

public class Main {
  private static final int N = 1_000_000;
  private static final GlobalTaskQueue global = new GlobalTaskQueue();
  public static void main(String[] args) {
    global.start();
    System.out.println("HB:");
    measure(Main::heartbeatMain);

    System.out.println("Parallel Stream:");
    measure(Main::javaParStreamMain);

    System.out.println("Seq:");
    measure(Main::seqMain);
  }

  private static void measure(Runnable task) {
    var start = Instant.now();
    task.run();
    var total = Duration.between(start, Instant.now()).toMillis();
    System.out.println("Took: "+total+"ms");
  }

  private static void heartbeatMain() {
    IntStream.range(0, N).forEach(i -> global.submitTask(() -> evenQuickButSaneTask(i)));
    global.waitForSettle();
//    System.out.println(b);
  }

  private static void seqMain() {
    var b = new StringBuilder();
    IntStream.range(0, N).forEach(Main::evenQuickButSaneTask);
//    System.out.println(b);
  }

  private static void javaParStreamMain() {
    var b = new StringBuilder();
    IntStream.range(0, N)
      .parallel()
      .forEach(Main::evenQuickButSaneTask);
//    System.out.println(b);
  }

  private static void nonEvenTask(int i) {
    fib(i);
  }

  private static void evenLongTask(int i) {
    fib(43);
  }

  private static void evenQuickButSaneTask(int i) {
    fib(10);
  }

  private static void evenQuickTask(int i) {
    fib(5);
  }

  static long fib(long n) {
    if (n <= 2) { return n; }
    return fib(n - 1) + fib(n - 2);
  }
}