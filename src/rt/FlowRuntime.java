package rt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FlowRuntime {
  int BUFFER_SIZE = 256;
  ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  static <E> SubmissionPublisher<Message<E>> spawnWorker() {
    return new SubmissionPublisher<>(executor, BUFFER_SIZE);
  }
  //  // TODO: I might not need this wrapper because I may always want to call ref.consume(..) where I would use signal.
  record Subject<E>(SubmissionPublisher<Message<E>> ref, CompletableFuture<Void> signal) implements AutoCloseable {
    @Override public void close() {
      ref.close();
    }
    @SuppressWarnings("unchecked")
    public void stop() {
      ref.submit(FlowRuntime.Message.Stop.INSTANCE);
    }
  }

  sealed interface Message<E> {
    record Data<E>(E data) implements Message<E> {}
    record Stop<E>() implements Message<E> {
      @SuppressWarnings("rawtypes")
      static final Stop INSTANCE = new Stop<>();
    }
  }
}
