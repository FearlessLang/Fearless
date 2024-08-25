package rt;

import base.*;
import base.flows.FlowOp_1;
import base.flows.Flow_1;
import base.iter.Iter_1;

import java.util.ArrayList;

public interface ListK extends base.List_0 {
  ListK $self = new ListK(){};
  @Override default List_1 $hash$imm(Object e1_m$) {
    var res = new ArrayList<>(1);
    res.add(e1_m$);
    return new ListImpl<>(res);
  }
  @Override default List_1 $hash$imm(Object e1_m$, Object e2_m$) {
    var res = new ArrayList<>(2);
    res.add(e1_m$);
    res.add(e2_m$);
    return new ListImpl<>(res);
  }

  @Override default List_1 $hash$imm(Object e1_m$, Object e2_m$, Object e3_m$) {
    var res = new ArrayList<>(3);
    res.add(e1_m$);
    res.add(e2_m$);
    res.add(e3_m$);
    return new ListImpl<>(res);
  }

  @Override default List_1 $hash$imm(Object e1_m$, Object e2_m$, Object e3_m$, Object e4_m$) {
    var res = new ArrayList<>(4);
    res.add(e1_m$);
    res.add(e2_m$);
    res.add(e3_m$);
    res.add(e4_m$);
    return new ListImpl<>(res);
  }

  @Override default List_1 $hash$imm() {
    return new ListImpl<>(new ArrayList<>());
  }

  @Override default List_1 fromLList$imm(LList_1 list_m$) {
    var res = new ArrayList<>(list_m$.size$read().intValue());
    list_m$.iter$mut().for$mut(e->{
      res.add(e);
      return Void_0.$self;
    });
    return new ListImpl<>(res);
  }

  @Override default List_1 withCapacity$imm(long n) {
    if (n > Integer.MAX_VALUE) {
      rt.Error.throwFearlessError(base.Infos_0.$self.msg$imm(
        rt.Str.fromJavaStr("Lists may not have a capacity greater than "+Integer.MAX_VALUE)
      ));
    }
    return new ListImpl<>(new ArrayList<>((int) n));
  }

  record ListImpl<E>(java.util.List<E> inner) implements base.List_1 {
    @Override public FlowOp_1 _flowimm$imm(long start_m$, long end_m$) {
      return List_1._flowimm$imm$fun(start_m$, end_m$, this);
    }

    @Override public Object get$imm(long i_m$) {
      return inner.get((int) i_m$);
    }

    @Override public Object get$read(long i_m$) {
      return inner.get((int) i_m$);
    }

    @Override public Object get$mut(long i_m$) {
      return inner.get((int) i_m$);
    }

    @Override public Void_0 addAll$mut(List_1 other_m$) {
      @SuppressWarnings("unchecked") // validated by the Fearless type system
      var other = (ListImpl<E>) other_m$;
      inner.addAll(other.inner);
      return Void_0.$self;
    }

    @Override public Opt_1 tryGet$imm(long i_m$) {
      if (i_m$ >= inner.size()) { return Opt_1.$self; }
      return Opts_0.$self.$hash$imm(inner.get((int) i_m$));
    }

    @Override public Opt_1 tryGet$read(long i_m$) {
      if (i_m$ >= inner.size()) { return Opt_1.$self; }
      return Opts_0.$self.$hash$imm(inner.get((int) i_m$));
    }

    @Override public Opt_1 tryGet$mut(long i_m$) {
      if (i_m$ >= inner.size()) { return Opt_1.$self; }
      return Opts_0.$self.$hash$imm(inner.get((int) i_m$));
    }

    @Override public Iter_1 iter$imm() {
      return List_1.iter$imm$fun(this);
    }

    @Override public Iter_1 iter$read() {
      return List_1.iter$read$fun(this);
    }

    @Override public Iter_1 iter$mut() {
      return List_1.iter$mut$fun(this);
    }

    @Override public Flow_1 flow$imm() {
      return List_1.flow$imm$fun(this);
    }

    @Override public Flow_1 flow$read() {
      return List_1.flow$read$fun(this);
    }

    @Override public Flow_1 flow$mut() {
      return List_1.flow$mut$fun(this);
    }

    @Override public Bool_0 isEmpty$read() {
      return inner.isEmpty() ? True_0.$self : False_0.$self;
    }

    @Override public Void_0 clear$mut() {
      inner.clear();
      return Void_0.$self;
    }

    @Override public Long size$read() {
      return (long) inner.size();
    }

    @Override public FlowOp_1 _flowread$read(long start_m$, long end_m$) {
      return List_1._flowread$read$fun(start_m$, end_m$, this);
    }

    @Override public Void_0 add$mut(Object e_m$) {
      @SuppressWarnings("unchecked") // validated by the Fearless type system
      E e = (E) e_m$;
      inner.add(e);
      return Void_0.$self;
    }
    @Override public ListImpl<E> addAnd$mut(Object e_m$) {
      @SuppressWarnings("unchecked") // validated by the Fearless type system
      E e = (E) e_m$;
      inner.add(e);
      return this;
    }
  }
}
