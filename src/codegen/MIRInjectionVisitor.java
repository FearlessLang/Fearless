package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import program.TypeRename;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.GammaVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  private final List<MIR.Trait> freshTraits = new ArrayList<>();
  private Program p;
  private final IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls;
  public MIRInjectionVisitor(Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) {
    this.p = p;
    this.resolvedCalls = resolvedCalls;
  }

  public Program getProgram() {
    return this.p.shallowClone();
  }

  public MIR.Program visitProgram() {
    Map<String, List<MIR.Trait>> ds = new HashMap<>(p.compilationCaches().size());
    p.compilationCaches().values().forEach(cache->ds.put(cache.pkg(), cache.compiled()));
    var traits = p.ds().values().stream()
      .filter(d->!ds.containsKey(d.name().pkg()))
      .map(d->visitDec(d.name().pkg(), d))
      .toList();

    Map<String, List<MIR.Trait>> ds_ = Stream.concat(
        traits.stream(),
        freshTraits.stream()
      ).collect(Collectors.groupingBy(t->t.name().pkg()));
    ds.putAll(ds_);
    return new MIR.Program(Collections.unmodifiableMap(ds));
  }
  public MIR.Trait visitDec(String pkg, T.Dec dec) {
    var ms = p.meths(XBs.empty(), Mdf.recMdf, dec.toIT(), 0).stream()
      .map(cm->{
        var m = p.ds().get(cm.c().name())
          .lambda()
          .meths()
          .stream()
          .filter(mi->mi.name().equals(cm.name()))
          .findAny()
          .orElseThrow();
        return visitMeth(pkg, m, Map.of(dec.lambda().selfName(), new T(cm.mdf(), dec.toIT())));
      })
      .toList();
    var impls = simplifyImpls(dec.lambda().its().stream().filter(it->!it.name().equals(dec.name())).toList());
    var canSingleton = p.meths(XBs.empty().addBounds(dec.gxs(), dec.bounds()), Mdf.recMdf, dec.toIT(), 0).stream().noneMatch(CM::isAbs);
    return new MIR.Trait(
      dec.name(),
      dec.gxs(),
      impls,
      ms,
      canSingleton
    );
  }

  public MIR.MCall visitMCall(String pkg, E.MCall e, Map<String, T> gamma) {
    var recv = e.receiver().accept(this, pkg, gamma);
    var tst = this.resolvedCalls.get(e);
    return new MIR.MCall(
      recv,
      new MIR.MethName(tst.original().mdf(), e.name().name(), e.name().num()),
      e.es().stream().map(ei->ei.accept(this, pkg, gamma)).toList(),
      tst.t(),
      tst.original().mdf()
    );
  }

  public MIR.X visitX(E.X e, Map<String, T> gamma) { return visitX(e.name(), gamma); }
  public MIR.X visitX(String x, Map<String, T> gamma) {
    var type = gamma.get(x);
    if (type == null) { throw new NotInGammaException(x); }
    return new MIR.X(x, type);
  }
  public static class NotInGammaException extends RuntimeException {
    public NotInGammaException(String x) { super(x); }
  }

  public MIR.Lambda visitLambda(String pkg, E.Lambda e, Map<String, T> gamma, boolean shallow) {
    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    Set<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).collect(Collectors.toSet());

    var impls = simplifyImpls(e.its());
    var fresh = Id.GX.fresh().name();
    var freshName = new Id.DecId(pkg+"."+fresh, 0);
    var freshDec = new T.Dec(freshName, List.of(), Map.of(), e, e.pos());
    var freshDecImplsOnly = new T.Dec(freshName, List.of(), Map.of(), new E.Lambda(
      e.mdf(),
      e.its(),
      e.selfName(),
      List.of(),
      e.pos()
    ), e.pos());
    var nonSelfImpls = impls.stream().filter(it->!it.name().equals(freshName)).toList();
    var recvMdf = e.mdf().isMdf() ? Mdf.recMdf : e.mdf();
    var declaredMeths = this.p.withDec(freshDecImplsOnly).meths(XBs.empty(), recvMdf, freshDec.toIT(), 0).stream()
      .map(CM::name)
      .collect(Collectors.toSet());
    var noExtraMeths = e.meths().stream().allMatch(m->declaredMeths.contains(m.name()));

    if (impls.size() == 1 && noExtraMeths) {
      var it = impls.get(0);
      var g = new HashMap<>(gamma);
      g.put(e.selfName(), new T(e.mdf(), it));
      List<MIR.Meth> ms = !shallow ? e.meths().stream().map(m->visitMeth(pkg, m, g)).toList() : List.of();
      var canSingleton = ms.isEmpty() && p.meths(XBs.empty(), recvMdf, it, 0).stream().noneMatch(CM::isAbs);
      return new MIR.Lambda(
        e.mdf(),
        it.name(),
        e.selfName(),
        List.of(),
        captures,
        ms,
        canSingleton
      );
    }

    this.p = p.withDec(freshDec);
    var noAbsMeths = this.p.meths(XBs.empty(), recvMdf, freshDec.toIT(), 0).stream().noneMatch(CM::isAbs);
    var canSingletonTrait = noAbsMeths && noExtraMeths;

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(freshName.name(), List.of())));

    List<MIR.Meth> msTrait = e.meths().stream().map(m->visitMeth(pkg, m.withBody(Optional.empty()), g)).toList();
    List<MIR.Meth> ms = !shallow ? e.meths().stream().map(m->visitMeth(pkg, m, g)).toList() : List.of();
    MIR.Trait freshTrait = new MIR.Trait(freshName, List.of(), nonSelfImpls, msTrait, canSingletonTrait);
    freshTraits.add(freshTrait);

    return new MIR.Lambda(
      e.mdf(),
      freshName,
      e.selfName(),
      nonSelfImpls,
      captures,
      ms,
      canSingletonTrait && ms.isEmpty()
    );
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        if (x.equals("_")) { x = astFull.E.X.freshName(); }
        g.put(x, t);
        return new MIR.X(x, t);
      })
      .toList();

    return new MIR.Meth(
      new MIR.MethName(m.name()),
      m.sig().mdf(),
      m.sig().gens(),
      xs,
      m.sig().ret(),
      m.body().map(e->e.accept(this, pkg, g)).orElse(null)
    );
  }

  /** Removes any redundant ITs from the list of impls for a lambda. */
  private List<Id.IT<T>> simplifyImpls(List<Id.IT<T>> its) {
    return its.stream()
      .filter(it->its.stream()
        .noneMatch(it1->it != it1 && p.isSubType(XBs.empty(), new T(Mdf.mdf, it1), new T(Mdf.mdf, it))))
      .toList();
  }

  private static class CaptureCollector implements CollectorVisitor<Set<String>> {
    private final Set<String> res = new HashSet<>();
    private Set<String> fresh = new HashSet<>();
    public Set<String> res() { return this.res; }

    public Void visitLambda(E.Lambda e) {
      var old = fresh;
      fresh = new HashSet<>(fresh);
      fresh.add(e.selfName());
      CollectorVisitor.super.visitLambda(e);
      this.fresh = old;
      return null;
    }

    public Void visitMeth(E.Meth m) {
      var old = fresh;
      fresh = new HashSet<>(fresh);
      fresh.addAll(m.xs());
      CollectorVisitor.super.visitMeth(m);
      this.fresh = old;
      return null;
    }

    public Void visitX(E.X e) {
      if (!fresh.contains(e.name())) { res.add(e.name()); }
      return CollectorVisitor.super.visitX(e);
    }
  }
}
