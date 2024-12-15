package rt.flows.dataParallel;

import base.Info_0;
import base.Infos_0;
import base.Void_0;
import base.flows._Sink_1;
import rt.FearlessError;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class BufferSink implements _Sink_1 {
  public static final class FlushWorker implements Runnable {
    private FlushWorker() {}
    private final BlockingQueue<FlusherElement> toFlush = new LinkedBlockingQueue<>();
    private Thread thread;
    public static FlushWorker start(Thread.UncaughtExceptionHandler exceptionHandler) {
      var worker = new FlushWorker();
      worker.thread = Thread.ofVirtual().uncaughtExceptionHandler(exceptionHandler).start(worker);
      return worker;
    }
    public void stop(_Sink_1 original) {
      toFlush.add(FlusherElement.StopToken.$self);
      try {
        thread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      original.stop$mut();
    }
    @Override public void run() {
      while (true) {
        FlusherElement e;try{e = toFlush.take();}
        catch (InterruptedException ex) {throw new RuntimeException(ex);}
        if (e == FlusherElement.StopToken.$self) {
          assert toFlush.isEmpty() : toFlush;
          break;
        }
        if (!(e instanceof FlusherElement.Sink sink)) {
          throw new UnsupportedOperationException("Only stop tokens and buffer sinks are supported.");
        }
        this.flush(sink.sink);
      }
    }
    private void flush(BufferSink sink) {
      // keep flushing if there are elements to flush, or if there might be more elements in the future.
      while (true) {
        Object e;try{e = sink.buffer.take();}
        catch (InterruptedException ex) {throw new RuntimeException(ex);}
        if (e == FlusherElement.StopToken.$self) {
          assert sink.buffer.isEmpty() : "Buffer should be empty when stopping.";
          break;
        }

        if (e instanceof Error err) {
          sink.original.pushError$mut(err.info);
          continue;
        }
        try {
          sink.original.$hash$mut(e);
        } catch (FearlessError err) {
          sink.original.pushError$mut(err.info);
        } catch (ArithmeticException err) {
          sink.original.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        }
      }
      assert sink.buffer.isEmpty() : "Buffer should be empty after flushing. Got: "+sink.buffer;
    }
  }

  public final _Sink_1 original;
  private final BlockingQueue<Object> buffer;

  private record Error(Info_0 info) {}

  //  private static final int BUFFER_MAX = (int) OSInfo.memoryAndCpuScaledValue(500);
//  private static final int BUFFER_MAX = 1;
  private static final int BUFFER_MAX = Integer.MAX_VALUE;
  public BufferSink(_Sink_1 original, FlushWorker flusher, int sizeHint) {
    this.original = original;
    this.buffer = new LinkedBlockingQueue<>(BUFFER_MAX);
    flusher.toFlush.add(new FlusherElement.Sink(this));
  }
  public BufferSink(_Sink_1 original, FlushWorker flusher) {
    this.original = original;
    this.buffer = new LinkedBlockingQueue<>(BUFFER_MAX);
    flusher.toFlush.add(new FlusherElement.Sink(this));
  }

  @Override public Void_0 stop$mut() {
//    return original.stop$mut();
    return Void_0.$self;
  }

  void safePut(Object e) {
    try {
      buffer.put(e);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override public Void_0 pushError$mut(Info_0 info_m$) {
    safePut(new Error(info_m$));
    return Void_0.$self;
  }
  @Override public Void_0 $hash$mut(Object x_m$) {
    safePut(x_m$);
    return Void_0.$self;
  }

  public void flush() {
    safePut(FlusherElement.StopToken.$self);
  }
  sealed interface FlusherElement {
    record StopToken() implements FlusherElement {
      public static final StopToken $self = new StopToken();
    }
    record Sink(BufferSink sink) implements FlusherElement {}
  }
}
