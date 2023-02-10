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
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class MIRInjectionVisitor implements GammaVisitor<MIR> {
  private final List<MIR.Trait> freshTraits = new ArrayList<>();

  public MIR.Program visitProgram(Program p) {
    var traits = p.ds().values().stream().map(d->visitDec(d.name().pkg(), d, p)).toList();
    Map<String, List<MIR.Trait>> ds = Stream.concat(
        traits.stream(),
        freshTraits.stream()
      ).collect(Collectors.groupingBy(t->t.name().split("\\..+$")[0]));
    return new MIR.Program(ds);
  }
  public MIR.Trait visitDec(String pkg, T.Dec dec, Program p) {
    List<String> gens = dec.gxs().stream().map(MIRInjectionVisitor::getName).toList();
    var ms = p.meths(dec.toIT(), 0).stream()
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
    return new MIR.Trait(
      getName(dec.name()),
      gens,
      dec.lambda().its().stream().distinct().map(MIRInjectionVisitor::getName).toList(),
      ms
    );
  }

  public MIR.MCall visitMCall(String pkg, E.MCall e, Map<String, T> gamma) {
    return new MIR.MCall(
      e.receiver().accept(this, pkg, gamma),
      getName(e.name()),
      e.es().stream().map(ei->ei.accept(this, pkg, gamma)).toList()
    );
  }

  public MIR.X visitX(E.X e, Map<String, T> gamma) { return visitX(e.name(), gamma); }
  public MIR.X visitX(String x, Map<String, T> gamma) {
    var type = requireNonNull(gamma.get(x));
    return new MIR.X(type.mdf(), x, getName(type));
  }

  public MIR.Lambda visitLambda(String pkg, E.Lambda e, Map<String, T> gamma) {
    var captureCollector = new CaptureCollector();
    captureCollector.visitLambda(e);
    List<MIR.X> captures = captureCollector.res().stream().map(x->visitX(x, gamma)).toList();

    var fresh = new Id.DecId(Id.GX.fresh().name(), 0);
    var freshName = pkg+"."+getName(fresh);
    MIR.Trait freshTrait = new MIR.Trait(freshName, List.of(), e.its().stream().distinct().map(MIRInjectionVisitor::getName).toList(), List.of());
    freshTraits.add(freshTrait);

    var g = new HashMap<>(gamma);
    g.put(e.selfName(), new T(e.mdf(), new Id.IT<>(fresh, List.of())));
    List<MIR.Meth> ms = e.meths().stream().map(m->visitMeth(pkg, m, g)).toList();
    return new MIR.Lambda(
      e.mdf(),
      freshName,
      e.selfName(),
      e.its().stream().distinct().map(MIRInjectionVisitor::getName).toList(),
      captures,
      ms
    );
  }

  public MIR.Meth visitMeth(String pkg, E.Meth m, Map<String, T> gamma) {
    var g = new HashMap<>(gamma);
    List<MIR.X> xs = Streams.zip(m.xs(), m.sig().ts())
      .map((x,t)->{
        g.put(x, t);
        return new MIR.X(t.mdf(), x, getNameGens(t));
      })
      .toList();
    List<String> gens = m.sig().gens().stream().map(MIRInjectionVisitor::getName).toList();

    return new MIR.Meth(
      getName(m.name()),
      m.sig().mdf(),
      gens,
      xs,
      getNameGens(m.sig().ret()),
      m.body().map(e->e.accept(this, pkg, g))
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
  private static String getName(Id.GX<T> gx) { return getBase(gx.name()); }
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
