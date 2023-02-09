package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;
import utils.Mapper;
import utils.Streams;
import visitors.CollectorVisitor;
import visitors.GammaVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  record FreshTrait(String name, MIR.Trait t){}
  private final List<FreshTrait> freshTraits = new ArrayList<>();

  public MIR.Program visitProgram(Program p) {
    Map<String, MIR.Trait> ds = Mapper.ofMut(c->p.ds().values().forEach(d->c.put(getName(d.name()), visitDec(d, p))));
    freshTraits.forEach(ft->ds.put(ft.name, ft.t));
    return new MIR.Program(ds);
  }
  public MIR.Trait visitDec(T.Dec dec, Program p) {
    List<String> gens = dec.gxs().stream().map(MIRInjectionVisitor::getName).toList();
    var ms = p.meths(dec.toIT(), 0).stream()
      .collect(Collectors.toMap(
        cm->getName(cm.name()),
        cm->{
          var m = p.ds().get(cm.c().name())
            .lambda()
            .meths()
            .stream()
            .filter(mi->mi.name().equals(cm.name()))
            .findAny()
            .orElseThrow();
          return visitMeth(m, Map.of(dec.lambda().selfName(), new T(cm.mdf(), dec.toIT())));
        }
      ));
    return new MIR.Trait(
      gens,
      dec.lambda().its().stream().distinct().map(MIRInjectionVisitor::getName).toList(),
      ms
    );
  }

  public MIR.MCall visitMCall(E.MCall e, Map<String, T> gamma) {
    return new MIR.MCall(
      e.receiver().accept(this, gamma),
      getName(e.name()),
      e.es().stream().map(ei->ei.accept(this, gamma)).toList()
    );
  }

  public MIR.X visitX(E.X e, Map<String, T> gamma) { return visitX(e.name(), gamma); }
  public MIR.X visitX(String x, Map<String, T> gamma) {
    var type = requireNonNull(gamma.get(x));
    return new MIR.X(type.mdf(), x, getName(type));
  }

  public MIR.Lambda visitLambda(E.Lambda e, Map<String, T> gamma) {
    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    List<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).toList();

    var fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    var freshName = getName(fresh);
    MIR.Trait freshTrait = new MIR.Trait(List.of(), e.its().stream().distinct().map(MIRInjectionVisitor::getName).toList(), Map.of());
    freshTraits.add(new FreshTrait(freshName, freshTrait));

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(fresh, List.of())));
    List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(m, g)).toList();
    return new MIR.Lambda(
      e.mdf(),
      freshName,
      e.selfName(),
      e.its().stream().distinct().map(MIRInjectionVisitor::getName).toList(),
      captures,
      ms
    );
  }

  public MIR.Meth visitMeth(E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        g.put(x, t);
        return new MIR.X(t.mdf(), x, getName(t));
      })
      .toList();
    List<String> gens = m.sig().gens().stream().map(MIRInjectionVisitor::getName).toList();

    return new MIR.Meth(
      getName(m.name()),
      m.sig().mdf(),
      gens,
      xs,
      getNameGens(m.sig().ret()),
      m.body().map(e->e.accept(this, g))
    );
  }

  private static String getName(T t) { return t.match(MIRInjectionVisitor::getName, MIRInjectionVisitor::getName); }
  private static String getNameGens(T t) {
    var base = getName(t);
    return base + t.match(
      gx->"",
      it->{
        if (it.ts().isEmpty()) { return ""; }
        return "<"+it.ts().stream().map(MIRInjectionVisitor::getNameGens).collect(Collectors.joining(","))+">";
      }
    );
  }
  private static String getName(Id.GX<T> gx) { return gx.name(); }
  private static String getName(Id.IT<T> it) { return getName(it.name()); }
  private static String getName(Id.DecId d) { return getBase(d.name())+"_"+d.gen(); }
  private static String getName(Id.MethName m) { return getBase(m.name()); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c)) { return Character.toString(c); }
      return "$"+c;
    }).collect(Collectors.joining());
  }

  private static class CaptureCollector implements CollectorVisitor<List<String>> {
    private final List<String> res = new ArrayList<>();
    private Set<String> fresh = new HashSet<>();
    public List<String> res() { return this.res; }

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
