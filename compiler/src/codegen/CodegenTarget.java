package codegen;

import visitors.MIRVisitor;

import java.util.List;

public interface CodegenTarget<T> extends MIRVisitor<Void> {
  List<? extends CodegenType<T>> typeHandlers();
  CodegenType<T> standardType();
  MIR.Program p();
  default CodegenType<T> getHandler(MIR.MT t) {
    return this.typeHandlers().stream()
      .filter(h->h.canHandle(t))
      .findFirst()
      .orElseThrow();
  }
}
