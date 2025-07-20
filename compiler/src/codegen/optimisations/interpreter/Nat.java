package codegen.optimisations.interpreter;

import codegen.MIR;
import utils.Bug;

public interface Nat extends LiteralCall<Long> {
  @Override default MIR.E visitMCall(MIR.MCall call) {
    throw Bug.todo();
  }
}
