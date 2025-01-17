package rt.flows.pipelineParallel;

import rt.FearlessError;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

/*
  The plan:
  1. Magic .step in actor flow-ops to be a non blocking dispatch to a subject
  2. Magic .stop in actor flows to dispatch a stop message (and maybe .join on the signal?)
  ...
  Step ??: Profit!
 */

public interface PipelineParallelFlow {
  final class WrappedSinkK implements base.flows._PipelineParallelSink_0 {
    public static WrappedSinkK $self = new WrappedSinkK();
    // TODO: sink id is not actually used, just here to make debugging easier during development
//    static long SINK_ID = 0;
    @Override public base.flows._Sink_1 $hash$imm(base.flows._Sink_1 original) {
//      return original;
      return new WrappedSink(original);
    }
  }
  final class WrappedSink implements base.flows._PipelineParallelSink_1 {
    final base.flows._Sink_1 original;
    Subject subject;
    public WrappedSink(base.flows._Sink_1 original) {
      this.original = original;

//      System.out.println("SPAWNING SUBJ: "+subjectId);
//      this.subject = spawn(msg -> {
//        System.out.println("SUBJ: "+subjectId+", Message received: "+msg);
//        original.$35$mut$(msg);
//      }, () -> {
//        System.out.println("Stop received (SUBJ "+subjectId+")");
//        original.stop$mut$();
//      });
      this.subject = new Subject(original);
    }

    @Override public base.Void_0 stopDown$mut() {
//      System.out.println("Stopping subj "+subjectId);
      try {
        subject.submit(Message.Stop.INSTANCE);
        subject.join();
      } catch (DeterministicFearlessError fe) {
        throw fe;
      } catch (Throwable exception) {
        var message = exception.getMessage();
        if (exception instanceof StackOverflowError) { message = "Stack overflowed"; }
        throw new RuntimeException(message, exception);
      }
      return base.Void_0.$self;
    }
    @Override public base.Void_0 $hash$mut(Object x$) {
//      System.out.println("SUBJ: "+subjectId+" GOT MSG: "+x$);
      this.subject.submit(x$);
      return base.Void_0.$self;
    }
    @Override public base.Void_0 pushError$mut(base.Info_0 info$) {
      this.subject.submit(new Message.Error(info$));
      return base.Void_0.$self;
    }
  }

  sealed interface Message {
    record Error(base.Info_0 info) implements Message {}
    final class Stop implements Message {
      static final Stop INSTANCE = new Stop();
      private Stop() {}
    }
  }

  final class Subject implements Runnable {
    private final base.flows._Sink_1 downstream;
    private final BlockingQueue<Object> buffer = new ArrayBlockingQueue<>(512);
    private final Thread worker;
    private boolean softClosed = false;
    private volatile Throwable exception = null;
    private volatile CompletableFuture<Void> onEmpty = null;

    public Subject(base.flows._Sink_1 downstream) {
      this.downstream = downstream;
      this.worker = Thread.ofVirtual().start(this);
      this.worker.setUncaughtExceptionHandler(((_, e) -> this.exception = e));
    }

    public void submit(Object msg) {
      var didSubmit = buffer.offer(msg);
      if (didSubmit) { return; }
      assert onEmpty == null || onEmpty.isDone() : "onEmpty should be clean if we're not awaiting it.";
      onEmpty = new CompletableFuture<>();
      onEmpty.join();
      submit(msg);
    }

    @Override public void run() {
      while (true) {
        Object msg;
        if (onEmpty != null && !onEmpty.isDone()) {
          msg = buffer.poll();
          if (msg == null) {
            onEmpty.complete(null);
            continue;
          }
        } else {
          try {
            msg = buffer.take();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }

        if (msg == Message.Stop.INSTANCE) {
          downstream.stopDown$mut();
          break;
        }
        if (msg instanceof Message.Error info) {
          processError(info);
          continue;
        }
        processDataMsg(msg);
      }
    }

    public void join() {
      try {
        worker.join();
        downstream.stopDown$mut();
        if (this.exception != null) {
          switch (exception) {
            case RuntimeException re -> throw re;
            case Error error -> throw error;
            default -> throw new RuntimeException(exception);
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    private void processError(Message.Error info) {
      if (softClosed) {
        return;
      }
      softClosed = true;
      // If we're in a nested flow and the pushError throws, then we want to propagate that as a deterministic
      // exception (as opposed to a random error that we wrap in a RuntimeException to prevent it from being
      // caught by Try/1)
      try {
        downstream.pushError$mut(info.info());
      } catch (FearlessError err) {
        throw new DeterministicFearlessError(err.info);
      }
    }

    private void processDataMsg(Object data) {
      if (softClosed) {
        return;
      }
      try {
        downstream.$hash$mut(data);
      } catch (FearlessError err) {
        // Keep "accepting" new messages, but don't actually do anything with them because we're in an error state
        softClosed = true;

        // If we're in a nested flow and the pushError throws, then we want to propagate that as a deterministic
        // exception (as opposed to a random error that we wrap in a RuntimeException to prevent it from being
        // caught by Try/1)
        try {
          downstream.pushError$mut(err.info);
        } catch (FearlessError err1) {
          throw new DeterministicFearlessError(err1.info);
        }
      } catch (ArithmeticException err) {
        softClosed = true;
        downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
      }
    }
  }

  final class DeterministicFearlessError extends FearlessError {
    DeterministicFearlessError(base.Info_0 info) {
      super(info);
    }
  }
}