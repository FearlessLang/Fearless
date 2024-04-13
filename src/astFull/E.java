package astFull;

import failure.Fail;
import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import utils.Bug;
import utils.Mapper;
import visitors.FreeGensFullVisitor;
import visitors.FullCloneVisitor;
import visitors.FullVisitor;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public sealed interface E extends HasPos {
  E accept(FullCloneVisitor v);
  <R> R accept(FullVisitor<R> v);
  T t();
  default Optional<Mdf> mdf() {
    return t().isInfer() ? Optional.empty() : Optional.of(t().mdf());
  }
  E withPos(Optional<Pos> pos);
  E withT(T t);

  record Lambda(LambdaId name, Optional<Mdf> mdf, List<Id.IT<T>>its, String selfName, List<Meth> meths, Optional<Id.IT<T>> it, Optional<Pos> pos) implements E {
    public Lambda {
      Objects.requireNonNull(mdf);
      Objects.requireNonNull(meths);
      Objects.requireNonNull(it);

      if (it.isEmpty() && mdf.isPresent() && !name.id.isFresh()) {
        it = Optional.of(new Id.IT<>(name.id, name.gens.stream().map(gx->new T(Mdf.mdf, gx)).toList()));
      }

      assert mdf.isPresent() == it.isPresent();
    }

    public record LambdaId(Id.DecId id, List<Id.GX<T>> gens, Map<Id.GX<T>, Set<Mdf>> bounds) {}

    /** This method correctly throw assertion error if called on a top level lambda
    */
    @Override public T t() {
      if (mdf().isEmpty() && it().isEmpty()) { return T.infer; }
      assert mdf().isPresent() && it().isPresent();
      return new T(mdf().get(), it().get());
    }

    @Override public Lambda withT(T t) {
      var mdf = Optional.ofNullable(t.isInfer() ? null:t.mdf());
      if (!t.isInfer() && t.match(gx->true, it->false)) {
        throw Fail.lambdaImplementsGeneric(t).pos(pos);
      }
      Optional<Id.IT<T>> it = Optional.ofNullable(t.isInfer() ? null : t.match(gx->null, iti->iti));
      return new Lambda(name, mdf, its, selfName, meths, it, pos);
    }

    @Override public E accept(FullCloneVisitor v){return v.visitLambda(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitLambda(this);}
    @Override public String toString() {
      var mdf = this.mdf().map(Mdf::toString).orElse("");
      var name = this.name.id.isFresh() ? "" : this.name+":";
      var type = mdf().isEmpty() && it().isEmpty() ? "infer" : it().map(Id.IT::toString).orElse("infer");
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));
      var selfName = Optional.ofNullable(selfName()).map(sn->"'"+sn).orElse("");
      return String.format("%s[-%s %s-]%s{%s %s}", name, mdf, type, its(), selfName, meths);
    }
    public String toStringNoName() {
      var mdf = this.mdf().map(Mdf::toString).orElse("");
      var type = mdf().isEmpty() && it().isEmpty() ? "infer" : it().map(Id.IT::toString).orElse("infer");
      var meths = meths().stream().map(Meth::toString).collect(Collectors.joining(",\n"));
      var selfName = Optional.ofNullable(selfName()).map(sn->"'"+sn).orElse("");
      return String.format("[-%s %s-]%s{%s %s}", mdf, type, its(), selfName, meths);
    }

    public Lambda withSelfName(String selfName) {
      return new Lambda(name, mdf, its, selfName, meths, it, pos);
    }
    public Lambda withT(Optional<T> t) {
      return new Lambda(name, t.map(T::mdf), its, selfName, meths, t.map(T::itOrThrow), pos);
    }
    public Lambda withITs(List<Id.IT<T>> its) {
      return new Lambda(name, mdf, its, selfName, meths, it, pos);
    }
    public Lambda withMdf(Mdf mdf) {
      return new Lambda(name, Optional.of(mdf), its, selfName, meths, it, pos);
    }
    public Lambda withMeths(List<Meth> meths) {
      return new Lambda(name, mdf, its, selfName, meths, it, pos);
    }
    public Lambda withPos(Optional<Pos> pos) {
      return new Lambda(name, mdf, its, selfName, meths, it, pos);
    }
  }
  record MCall(E receiver,MethName name,Optional<List<T>>ts,List<E>es, T t, Optional<Pos> pos) implements E{
    public MCall { assert name.num() == es.size(); }
    public MCall withReceiver(E receiver) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    public MCall withEs(List<E> es) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    public MCall withTs(Optional<List<T>> ts) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public MCall withT(T t) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public MCall withPos(Optional<Pos> pos) {
      return new MCall(receiver, name, ts, es, t, pos);
    }
    @Override public E accept(FullCloneVisitor v){return v.visitMCall(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitMCall(this);}
    @Override public String toString() {
      return String.format("%s %s%s(%s):%s", receiver, name, ts.map(Object::toString).orElse("[-]"), es, t);
    }
  }
  record X(String name, T t, Optional<Pos> pos) implements E{
    private static final AtomicInteger FRESH_N = new AtomicInteger(0);
    public static void reset() {
//      if (!Main.isUserInvoked() && FRESH_N.get() > 1000) {
//        throw Bug.of("FRESH_N is larger than we expected for tests.");
//      }
      FRESH_N.set(0);
    }
    public static String freshName() {
      var n = FRESH_N.getAndUpdate(n_ -> {
        int next = n_ + 1;
        if (next == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
        return next;
      });
      return "fear" + n + "$";
    }
    public X(T t){
      this(freshName(), t, Optional.empty());
      if (FRESH_N.get() == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
    }
    public X withT(T t) {
      return new X(name, t, pos);
    }
    @Override public E withPos(Optional<Pos> pos) { return new X(name, t, pos); }

    @Override public E accept(FullCloneVisitor v){return v.visitX(this);}
    @Override public <R> R accept(FullVisitor<R> v){return v.visitX(this);}
    @Override public String toString(){ return name+":"+t; }
  }
  record Meth(Optional<Sig> sig,Optional<MethName> name, List<String>xs, Optional<E> body, Optional<Pos> pos) implements HasPos {
    public Meth {
      // TODO: throw a Fail error (can be caused by implementing a method of a lambda with the wrong number of gens)
      name.ifPresent(n->{ assert n.num() == xs.size(); });
    }
    public boolean isAbs(){ return body().isEmpty(); }
    public Meth withSig(Sig s) {
      return new Meth(Optional.of(s), name, xs, body, pos);
    }
    public Meth withName(MethName name) {
      return new Meth(sig, Optional.of(name), xs, body, pos);
    }
    public Meth withBody(Optional<E> body) {
      return new Meth(sig, name, xs, body, pos);
    }
    @Override public String toString() {
      return String.format("%s(%s): %s -> %s", name.map(Object::toString).orElse("[-]"), xs, sig.map(Object::toString).orElse("[-]"), body.map(Object::toString).orElse("[-]"));
    }
  }
  record Sig(Mdf mdf, List<Id.GX<T>> gens, Map<Id.GX<astFull.T>, Set<Mdf>> bounds, List<T> ts, T ret, Optional<Pos> pos) {
    public Sig{ assert mdf!=null && gens!=null && ts!=null && ret!=null; }
    public Sig withGens(List<Id.GX<T>> gens){
      return new Sig(mdf, gens, bounds, ts, ret, pos);
    }
    public Sig withRet(T ret){
      return new Sig(mdf, gens, bounds, ts, ret, pos);
    }
    public Sig withTs(List<T> ts){
      return new Sig(mdf, gens, bounds, ts, ret, pos);
    }
    public ast.E.Sig accept(InjectionVisitor visitor) {
      return visitor.visitSig(this);
    }
    @Override public String toString() {
      if (bounds.values().stream().mapToLong(Collection::size).sum() == 0) {
        return "Sig[mdf="+mdf+",gens="+gens+",ts="+ts+",ret="+ret+"]";
      }
      var boundsStr = bounds.entrySet().stream()
        .sorted(Comparator.comparing(a->a.getKey().name()))
        .map(kv->kv.getKey()+"="+kv.getValue().stream().sorted(Comparator.comparing(Enum::toString)).toList())
        .collect(Collectors.joining(","));
      return "Sig[mdf="+mdf+",gens="+gens+",bounds={"+boundsStr+"},ts="+ts+",ret="+ret+"]";
    }
  }
}

