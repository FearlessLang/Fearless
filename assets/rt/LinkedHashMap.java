package rt;

import base.*;
import base.flows.*;
import rt.flows.SpliteratorFlowOp;

import java.util.Spliterator;
import java.util.function.BiFunction;

public final class LinkedHashMap implements LinkedHashMap_2 {
  private final java.util.LinkedHashMap<Object, Object> inner = new java.util.LinkedHashMap<>();
  private final F_2 hashFn;
  private final F_3 keyEq;
  public LinkedHashMap(F_3 keyEq, F_2 hashFn) {
    this.hashFn = hashFn;
    this.keyEq = keyEq;
  }

  @Override public Opt_1 get$read(Object k_m$) {
    var res = inner.get(keyOf(k_m$));
    if (res == null) { return Opt_1.$self; }
    return Opts_0.$self.$hash$imm(res);
  }
  @Override public Opt_1 get$imm(Object k_m$) {
    return get$read(k_m$);
  }
  @Override public Opt_1 get$mut(Object k_m$) {
    return get$read(k_m$);
  }

  @Override public LinkedHashMap_2 $plus$mut(Object k_m$, Object v_m$) {
    inner.put(keyOf(k_m$), v_m$);
    return this;
  }
  @Override public Opt_1 remove$mut(Object k_m$) {
    var res = inner.remove(keyOf(k_m$));
    if (res == null) { return Opt_1.$self; }
    return Opts_0.$self.$hash$imm(res);
  }
  @Override public Void_0 clear$mut() {
    inner.clear();
    return Void_0.$self;
  }
  @Override public Bool_0 keyEq$read(Object k1_m$, Object k2_m$) {
    return (Bool_0)keyEq.$hash$read(k1_m$, k2_m$);
  }
  @Override public Bool_0 isEmpty$read() {
    return inner.isEmpty() ? True_0.$self : False_0.$self;
  }
  @Override public Void_0 put$mut(Object k_m$, Object v_m$) {
    return LinkedHashMap_2.put$mut$fun(k_m$, v_m$, this);
  }

  @Override public Flow_1 keys$read() {
    var keys = inner.keySet().stream()
      .map(k -> ((Key)k).k)
      .spliterator();
    return Flow_0.$self.fromOp$imm(SpliteratorFlowOp.of(keys), inner.size());
  }

  @Override public Flow_1 values$mut() {
    var values = inner.values().spliterator();
    return Flow_0.$self.fromMutSource$imm(SpliteratorFlowOp.of(values), inner.size());
  }
  @Override public Flow_1 values$read() {
    var keys = inner.values().spliterator();
    return Flow_0.$self.fromOp$imm(SpliteratorFlowOp.of(keys), inner.size());
  }
  @Override public Flow_1 values$imm() {
    return values$read();
  }
  @Override public Flow_1 flowMut$mut() {
    return Flow_0.$self.fromMutSource$imm(SpliteratorFlowOp.of(mapToEntries()), inner.size());
  }
  @Override public Flow_1 flow$read() {
    return Flow_0.$self.fromOp$imm(SpliteratorFlowOp.of(mapToEntries()), inner.size());
  }
  @Override public Flow_1 flow$imm() {
    return flow$read();
  }

  private Spliterator<? extends Entry_2> mapToEntries() {
    return inner.entrySet().stream()
      .map(kv -> new Entry_2() {
        @Override public Object value$read() {
          return kv.getValue();
        }
        @Override public Object value$mut() {
          return kv.getValue();
        }
        @Override public Object key$read() {
          return ((Key)kv.getKey()).k;
        }
      })
      .spliterator();
  }

  private Key keyOf(Object k) {
    return Key.of(k, hashFn, this::keyEq$read);
  }
  private record Key(Object k, ToHash_0 hashFn, BiFunction<Object,Object,Bool_0> keyEqFn) {
    public static Key of(Object k, F_2 hashFn, BiFunction<Object,Object,Bool_0> keyEqFn) {
      return new Key(k, (ToHash_0)hashFn.$hash$read(k), keyEqFn);
    }
    @Override public int hashCode() {
      return Math.toIntExact(hashFn.hash$read(new rt.CheapHash()).compute$mut());
    }
    @Override public boolean equals(Object o) {
      assert o instanceof Key;
      return keyEqFn.apply(this.k, ((Key)o).k) == True_0.$self;
    }
  }
}
