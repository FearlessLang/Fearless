package codegen;

import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Mapper;
import utils.Streams;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record Interpreter(Map<String, MIR.Trait> p) {
//
//  interface V {
//    Mdf mdf();
//    <R> R match(Function<FObj, R> fObj, Function<FRef, R> fRef);
//  }
//  class FRef implements V {
//    private V v;
//    public FRef(V v) { this.v = v; }
//    public Mdf mdf() { return v.mdf(); }
//    public V deref() { return v; }
//    public V swap(V v) {
//      V p = this.v;
//      this.v = v;
//      return p;
//    }
//    public <R> R match(Function<FObj, R> fObj, Function<FRef, R> fRef) { return fRef.apply(this); }
//  }
//  record FObj(Mdf mdf, String trait, Map<MIR.L, V> captures) implements V {
//    public <R> R match(Function<FObj, R> fObj, Function<FRef, R> fRef) { return fObj.apply(this); }
//  }
//  public void start(Id.DecId entry) {
//    var startName = MIR.getName(entry);
//    var start = p.get(startName);
//    assert start != null;
//    if (!start.impls().contains("base.Main")) { throw Bug.todo(); }
//    var startM = start.meths().get(MIR.getName(new Id.MethName("#",1)));
//    if (startM == null || startM.body().isEmpty()) { throw Bug.todo(); }
//
//    var mem = new HashMap<MIR.L, V>();
//    var startId = new MIR.L(Mdf.imm);
//    run(new MIR.NewStaticLambda(startId, startName), mem);
//
//  }
//
//  public void run(MIR.NewDynLambda e, HashMap<MIR.L, V> mem) {
//    var captures = e.captures().entrySet().stream()
//      .peek(kv->{ assert mem.containsKey(kv.getValue()); })
//      .collect(Collectors.toMap(kv->kv.getKey(), kv->mem.get(kv.getValue())));
//    mem.put(e.out(), new FObj(e.out().mdf(), .name(), captures));
//  }
//
//  /** We still make a dynamic lambda in the case of this interpreter. */
//  public void run(MIR.NewStaticLambda e, HashMap<MIR.L, V> mem) {
//    mem.put(e.out(), new FObj(e.out().mdf(), e.name(), Map.of()));
//  }
//
//  public void run(MIR.MCall e, HashMap<MIR.L, V> mem) {
//    var recv = requireNonNull(mem.get(e.recv()));
//    List<V> args = e.args().stream().map(mem::get).map(Objects::requireNonNull).toList();
//    mem.put(e.out(), recv.match(fObj->{
//      var m = p.get(fObj.trait).meths().get(e.name());
//      m.body().forEach(ex->run(ex, mem)); // leaks
//    }, fRef->{ throw Bug.todo(); }));
//  }
//  public void run(MIR e, HashMap<MIR.L, V> mem) {
//    throw Bug.unreachable();
//  }
}
