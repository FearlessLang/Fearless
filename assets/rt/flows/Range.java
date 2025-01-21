package rt.flows;

import base.*;
import base.flows.*;
import rt.FearlessError;
import rt.flows.dataParallel.DataParallelFlowK;

public final class Range implements base.flows._FlowRange_0 {
  public static final Range $self = new Range();

  @Override public Flow_1 $hash$imm(long start_m$, long end_m$) {
    var totalSize = end_m$ - start_m$;
    return _SeqFlow_0.$self.fromOp$imm(new FiniteRangeOp(start_m$, end_m$), Opts_0.$self.$hash$imm(totalSize));
  }
  @Override public Flow_1 $hash$imm(long start_m$) {
    return _SeqFlow_0.$self.fromOp$imm(new InfiniteRangeOp(start_m$), Opt_1.$self);
  }

  private final static class FiniteRangeOp implements FlowOp_1 {
    private long cursor;
    private long end;

    public FiniteRangeOp(long start, long end) {
      this.cursor = start;
      this.end = end;
    }

    @Override public Bool_0 isFinite$mut() {
      return True_0.$self;
    }

    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      if (!this.isRunning()) {
        sink_m$.stopDown$mut();
        return Void_0.$self;
      }
      var cursor = this.cursor;
      sink_m$.$hash$mut(cursor);
      this.cursor++;
      if (!this.isRunning()) {
        sink_m$.stopDown$mut();
      }
      return Void_0.$self;
    }

    @Override public Void_0 stopUp$mut() {
      this.cursor = this.end;
      return Void_0.$self;
    }

    @Override public Bool_0 isRunning$mut() {
      return this.isRunning() ? True_0.$self : False_0.$self;
    }
    private boolean isRunning() {
      return this.cursor < this.end;
    }

    @Override public Void_0 for$mut(_Sink_1 downstream_m$) {
      for (; this.cursor < this.end; ++this.cursor) {
        downstream_m$.$hash$mut(this.cursor);
      }
      return downstream_m$.stopDown$mut();
    }

    @Override public Opt_1 split$mut() {
      var size = this.end - this.cursor;
      if (size <= 1) { return Opt_1.$self; }
      var mid = this.cursor + (size / 2);
      var end_ = this.end;
      this.end = mid;
      return Opts_0.$self.$hash$imm(new FiniteRangeOp(mid, end_));
    }

    @Override public Bool_0 canSplit$read() {
      return this.end - this.cursor > 1 ? True_0.$self : False_0.$self;
    }
  }

  private final static class InfiniteRangeOp implements FlowOp_1 {
    private long cursor;
    private volatile boolean isRunning = true;

    public InfiniteRangeOp(long start) {
      this.cursor = start;
    }

    @Override public Bool_0 isFinite$mut() {
      return False_0.$self;
    }

    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      if (!this.isRunning()) {
        sink_m$.stopDown$mut();
        return Void_0.$self;
      }
      sink_m$.$hash$mut(this.increment());
      if (!this.isRunning()) {
        sink_m$.stopDown$mut();
      }
      return Void_0.$self;
    }

    @Override public Void_0 stopUp$mut() {
      this.isRunning = false;
      return Void_0.$self;
    }

    @Override public Bool_0 isRunning$mut() {
      return this.isRunning() ? True_0.$self : False_0.$self;
    }
    private boolean isRunning() {
      return this.isRunning;
    }

    @Override public Void_0 for$mut(_Sink_1 downstream_m$) {
      for (; isRunning(); this.increment()) {
        downstream_m$.$hash$mut(this.cursor);
      }
      return downstream_m$.stopDown$mut();
    }

    @Override public Opt_1 split$mut() {
      return Opt_1.$self;
    }

    @Override public Bool_0 canSplit$read() {
      return False_0.$self;
    }

    private long increment() {
      if (this.cursor == Long.MAX_VALUE) {
        throw new FearlessError(Infos_0.$self.msg$imm(rt.Str.fromJavaStr("Cannot continue base.flows.Flow.range/1, reached the maximum value of an Int.")));
      }
      return this.cursor++;
    }
  }
}
