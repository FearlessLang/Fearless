package codegen;

import visitors.MIRVisitor;

import java.util.List;
import java.util.function.Consumer;

public interface CodegenTarget<T, S extends CodegenTarget<T, S>> extends MIRVisitor<Void> {
  List<? extends CodegenType<T, S>> typeHandlers();
  CodegenType<T, S> standardType();
  MIR.Program p();
  void withState(T state, Consumer<S> task);
  default CodegenType<T, S> getHandler(MIR.MT t) {
    return this.typeHandlers().stream()
      .filter(h->h.canHandle(t))
      .findFirst()
      .orElseThrow();
  }
}
