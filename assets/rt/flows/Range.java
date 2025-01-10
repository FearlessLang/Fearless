package rt.flows;

import base.*;
import base.flows.*;
import rt.flows.dataParallel.DataParallelFlowK;

public interface Range extends base.flows._FlowRange_0 {
  Range $self = new Range() {
  };

  @Override default Flow_1 $hash$imm(long start_m$, long end_m$) {
    var totalSize = end_m$ - start_m$;

    return _SeqFlow_0.$self.fromOp$imm(new RangeOp(start_m$, end_m$), Opts_0.$self.$hash$imm(totalSize));
  }

  final class RangeOp implements FlowOp_1 {
    private long cursor;
    private long end;

    public RangeOp(long start, long end) {
      this.cursor = start;
      this.end = end;
    }

    @Override public Bool_0 isFinite$mut() {
      return True_0.$self;
    }

    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      if (!this.isRunning()) {
        sink_m$.stop$mut();
        return Void_0.$self;
      }
      var cursor = this.cursor;
      sink_m$.$hash$mut(cursor);
      this.cursor++;
      if (!this.isRunning()) {
        sink_m$.stop$mut();
      }
      return Void_0.$self;
    }

    @Override public Void_0 stop$mut() {
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
      return downstream_m$.stop$mut();
    }

    @Override public Opt_1 split$mut() {
      var size = this.end - this.cursor;
      if (size <= 1) { return Opt_1.$self; }
      var mid = this.cursor + (size / 2);
      var end_ = this.end;
      this.end = mid;
      return Opts_0.$self.$hash$imm(new RangeOp(mid, end_));
    }

    @Override public Bool_0 canSplit$read() {
      return this.end - this.cursor > 1 ? True_0.$self : False_0.$self;
    }
  }
}
