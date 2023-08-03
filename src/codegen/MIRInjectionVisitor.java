package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import program.TypeRename;
import utils.Push;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.GammaVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  private final List<MIR.Trait> freshTraits = new ArrayList<>();
  private Program p;
  public MIRInjectionVisitor(Program p) { this.p = p; }

  public MIR.Program visitProgram() {
    var traits = p.ds().values().stream().map(d->visitDec(d.name().pkg(), d)).toList();
    Map<String, List<MIR.Trait>> ds = Stream.concat(
        traits.stream(),
        freshTraits.stream()
      ).collect(Collectors.groupingBy(t->t.name().pkg()));
    return new MIR.Program(ds);
  }
  public MIR.Trait visitDec(String pkg, T.Dec dec) {
    var ms = p.meths(Mdf.mdf, dec.toIT(), 0).stream()
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
    var canSingleton = p.meths(Mdf.mdf, dec.toIT(), 0).stream().noneMatch(CM::isAbs);
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
    var meth = p.meths(recv.t().mdf(), recv.t().itOrThrow(), e.name(), 0).orElseThrow();
    var renamer = TypeRename.core(p);
    var cm = renamer.renameSigOnMCall(meth.sig(), renamer.renameFun(e.ts(), meth.sig().gens()));
    return new MIR.MCall(
      recv,
      e.name(),
      e.es().stream().map(ei->ei.accept(this, pkg, gamma)).toList(),
      cm.ret()
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

  public MIR.Lambda visitLambda(String pkg, E.Lambda e, Map<String, T> gamma) {
    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    Set<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).collect(Collectors.toSet());

    var impls = simplifyImpls(e.its());
    if (impls.size() == 1) {
      var it = impls.get(0);
      var g = new HashMap<>(gamma);
      g.put(e.selfName(), new T(e.mdf(), it));
      List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(pkg, m, g)).toList();
      var canSingleton = ms.isEmpty() && p.meths(e.mdf(), it, 0).stream().noneMatch(CM::isAbs);
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

    var fresh = new Id.DecId(pkg+"."+Id.GX.fresh().name(), 0);
    var freshDec = new T.Dec(fresh, List.of(), e.withITs(Push.of(new Id.IT<>(fresh, List.of()), e.its())), e.pos());
    impls = impls.stream().filter(it->!it.name().equals(fresh)).toList();
    p = p.withDec(freshDec);
    var canSingletonTrait = p.meths(e.mdf(), freshDec.toIT(), 0).stream().noneMatch(CM::isAbs);
    MIR.Trait freshTrait = new MIR.Trait(fresh, List.of(), impls, List.of(), canSingletonTrait);
    freshTraits.add(freshTrait);

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(fresh, List.of())));
    List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(pkg, m, g)).toList();
    return new MIR.Lambda(
      e.mdf(),
      fresh,
      e.selfName(),
      impls,
      captures,
      ms,
      canSingletonTrait && ms.isEmpty()
    );
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        g.put(x, t);
        return new MIR.X(x, t);
      })
      .toList();

    return new MIR.Meth(
      m.name(),
      m.sig().mdf(),
      m.sig().gens(),
      xs,
      m.sig().ret(),
      m.body().map(e->e.accept(this, pkg, g))
    );
  }

  /** Removes any redundant ITs from the list of impls for a lambda. */
  private List<Id.IT<T>> simplifyImpls(List<Id.IT<T>> its) {
    return its.stream()
      .filter(it->its.stream()
        .noneMatch(it1->it != it1 && p.isSubType(new T(Mdf.mdf, it1), new T(Mdf.mdf, it))))
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
