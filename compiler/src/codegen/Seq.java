package codegen;

import java.util.SequencedCollection;
import java.util.function.Consumer;

public interface Seq {
  static <T> void of(StringBuilder buffer, SequencedCollection<T> items, Consumer<T> apply) {
    of(buffer, ", ", items, apply);
  }

  static <T> void of(StringBuilder buffer, String separator, SequencedCollection<T> items, Consumer<T> apply) {
    if (items.isEmpty()) { return; }
    items.stream()
      .limit(items.size() - 1)
      .forEach(item -> {
        apply.accept(item);
        buffer.append(separator);
      });
    apply.accept(items.getLast());
  }
}
