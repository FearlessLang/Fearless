package rt.flows.dataParallel;

import base.Info_0;
import base.Void_0;
import base.flows._Sink_1;

public record DelayedStopSink(_Sink_1 original) implements _Sink_1 {
  @Override public Void_0 stopDown$mut() {
    return Void_0.$self;
  }

  @Override public Void_0 pushError$mut(Info_0 info_m$) {
    return original.pushError$mut(info_m$);
  }

  @Override public Void_0 $hash$mut(Object x_m$) {
    return original.$hash$mut(x_m$);
  }

  public void stop() {
    original.stopDown$mut();
  }
}
