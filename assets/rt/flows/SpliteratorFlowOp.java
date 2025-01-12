package rt.flows;

import base.*;
import base.flows.FlowOp_1;
import base.flows._Sink_1;

import java.util.Spliterator;

public interface SpliteratorFlowOp {
  static <T> FlowOp_1 of(Spliterator<T> spliterator) {
    return new FlowOp_1() {
      boolean hasStopped = false;
      @Override public Bool_0 isFinite$mut() {
        return True_0.$self;
      }
      @Override public Void_0 step$mut(_Sink_1 sink_m$) {
        if (hasStopped || !spliterator.tryAdvance(sink_m$::$hash$mut)) {
          hasStopped = true;
          sink_m$.stopDown$mut();
        }
        return Void_0.$self;
      }
      @Override public Void_0 stopUp$mut() {
        hasStopped = true;
        return Void_0.$self;
      }
      @Override public Bool_0 isRunning$mut() {
        return hasStopped ? False_0.$self : True_0.$self;
      }
      @Override public Void_0 for$mut(_Sink_1 downstream_m$) {
        spliterator.forEachRemaining(downstream_m$::$hash$mut);
        downstream_m$.stopDown$mut();
        return Void_0.$self;
      }
      @Override public Opt_1 split$mut() {
        var other = spliterator.trySplit();
        if (other == null) { return Opt_1.$self; }
        return Opts_0.$self.$hash$imm(of(other));
      }
      @Override public Bool_0 canSplit$read() {
        return spliterator.estimateSize() > 1 ? True_0.$self : False_0.$self;
      }
    };
  }
}
