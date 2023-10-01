package visitors;

import ast.E;
import ast.T;
import id.Id;
import id.Mdf;
import utils.Err;
import utils.Mapper;
import utils.Push;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record Env(List<String> xs, List<T> ts, List<Id.GX<T>> gxs, T decT, List<Id.MethName> ms, HashMap<String, Integer> usages) {
  public Env(){ this(List.of(), List.of(), List.of(), null, List.of(), new HashMap<>()); }
  public Env {
    assert xs.size()==ts.size();
    assert xs.stream().distinct().count()==xs.size();
    assert gxs.stream().distinct().count()==gxs.size();
    assert Err.ifMut(xs): "gxs should not be mutable";
    assert Err.ifMut(ts): "ts should not be mutable";
    assert Err.ifMut(gxs): "gxs should not be mutable";
  }
  public Env add(E.Meth m){
    return new Env(
      Push.of(xs,m.xs()),
      Push.of(ts,m.sigs().ts()),
      Push.of(gxs, m.sigs().gens()),
      decT.withMdf(m.sigs().mdf()),
      Push.of(ms, m.name()),
      Mapper.ofMut(c->{
        c.putAll(usages);
        m.xs().forEach(x->c.put(x, 0));
      })
    );
  }
  public Env add(E.Lambda l, List<Id.MethName> ms){
    return new Env(
      Push.of(xs,l.selfName()),
      Push.of(ts,new T(l.mdf(), new Id.IT<>(Id.GX.fresh().name(), List.of()))),
      gxs,
      decT,
      Push.of(this.ms, ms),
      new HashMap<>()
    );
  }
  public Env add(List<Id.GX<T>>gxs){ return new Env(xs,ts,Push.of(gxs(),gxs),decT,ms,usages); }
  public Env add(E.X x, T t){ return add(x.name(),t); }
  public Env add(String x, T t){ return new Env(Push.of(xs,x),Push.of(ts,t),gxs,decT,ms,Mapper.ofMut(c->{
    c.putAll(usages);
    c.put(x, 0);
  })); }
  public Env add(Id.GX<T> gx){ return new Env(xs,ts,Push.of(gxs,gx),decT,ms,usages); }
  public Env add(T.Dec dec){ return new Env(xs,ts,Push.of(gxs,dec.gxs()),new T(Mdf.readOnly, dec.toIT()),ms,usages); }
  public void addUsage(String x) { usages.computeIfPresent(x, (x_,n)->n+1); }
  public T get(E.X x){ return get(x.name()); }
  public T get(String x){
    if (x.equals("this")) { return requireNonNull(decT); }
    var res=xs.indexOf(x);
    assert res!=-1;
    return ts.get(res);
  }
  public boolean has(String x){return x.equals("this") || xs.contains(x); }
  public boolean has(E.X x){ return has(x.name()); }
  public boolean has(Id.GX<T> gx){ return gxs.contains(gx); }
}
