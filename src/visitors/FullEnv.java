package visitors;

import astFull.E;
import astFull.T;
import id.Id;
import id.Mdf;
import utils.Err;
import utils.Push;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record FullEnv(List<String> xs, List<Id.GX<T>> gxs) {
  public FullEnv(){ this(List.of(), List.of()); }
  public FullEnv {
    assert xs.stream().distinct().count()==xs.size();
    assert gxs.stream().distinct().count()==gxs.size();
    assert Err.ifMut(xs): "gxs should not be mutable";
    assert Err.ifMut(gxs): "gxs should not be mutable";
  }
  public FullEnv add(E.Meth m){
    return new FullEnv(
      Push.of(xs,m.xs()),
      m.preferredSig().map(sig->Push.of(gxs,sig.gens())).orElse(gxs)
    );
  }
  public FullEnv add(List<Id.GX<T>>gxs){ return new FullEnv(xs,Push.of(gxs(),gxs)); }
  public FullEnv add(E.X x, T t){ return add(x.name(),t); }
  public FullEnv add(String x, T t){ return new FullEnv(Push.of(xs,x),gxs); }
  public FullEnv add(Id.GX<T> gx){ return new FullEnv(xs,Push.of(gxs,gx)); }
  public FullEnv add(T.Dec dec){ return new FullEnv(xs,Push.of(gxs,dec.gxs())); }
  public boolean has(String x){return x.equals("this") || xs.contains(x); }
  public boolean has(E.X x){ return has(x.name()); }
  public boolean has(Id.GX<T> gx){ return gxs.contains(gx); }
}
