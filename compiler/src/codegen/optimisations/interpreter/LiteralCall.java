package codegen.optimisations.interpreter;

import codegen.MIR;

interface LiteralCall<T> {
  T recvValue();
  MIR.E visitMCall(MIR.MCall call);
}
