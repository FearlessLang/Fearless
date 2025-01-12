package rt.flows.dataParallel;

import base.Info_0;
import base.OptMatch_2;
import base.Void_0;
import base.flows.FlowOp_1;
import base.flows._Sink_1;
import rt.FearlessError;
import rt.flows.dataParallel.dynamicSplit.DynamicSplitFlow;

import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

final class ForkJoinWorker extends RecursiveAction {
  private final FlowOp_1 source;
  private final _Sink_1 downstream;
  private final _Sink_1 original;
  private final ArrayList<Object> es;

  private ForkJoinWorker(FlowOp_1 source, _Sink_1 downstream, ArrayList<Object> es) {
    this.source = source;
    this.original = downstream;
    this.downstream = new _Sink_1() {
      @Override public Void_0 stopDown$mut() {
        return Void_0.$self;
      }
      @Override public Void_0 pushError$mut(Info_0 info_m$) {
        es.add(new Error(info_m$));
        return Void_0.$self;
      }
      @Override public Void_0 $hash$mut(Object x_m$) {
        es.add(x_m$);
        return Void_0.$self;
      }
    };
    this.es = es;
  }

  public static void for_(FlowOp_1 source, _Sink_1 downstream) {
    var es = new ArrayList<>();
    var worker = new ForkJoinWorker(source, downstream, es);
    worker.compute();
    for (var e : es) {
      if (e instanceof Error(Info_0 info)) {
        downstream.pushError$mut(info);
        break;
      }
      try {
        downstream.$hash$mut(e);
      } catch (FearlessError err) {
        downstream.pushError$mut(err.info);
      } catch (ArithmeticException err) {
        downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
      }
    }
    downstream.stopDown$mut();
  }

  @Override protected void compute() {
    var lhs = this;
    source.split$mut().match$mut(new OptMatch_2() {
      @Override public Object some$mut(Object split_) {
        var split = (FlowOp_1) split_;
        var rhsData = new ArrayList<>();
        var rhs = new ForkJoinWorker(split, original, rhsData);
        rhs.fork();
        lhs.compute();
        rhs.join();
        es.addAll(rhsData);
        return null;
      }
      @Override public Object empty$mut() {
        try {
          source.for$mut(downstream);
        }  catch (FearlessError err) {
          downstream.pushError$mut(err.info);
        } catch (ArithmeticException err) {
          downstream.pushError$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(err.getMessage())));
        }
        return null;
      }
    });
  }

  private record Error(Info_0 info) {}
}
