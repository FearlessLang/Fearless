package codegen;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import magic.Magic;
import program.CM;
import program.typesystem.EMethTypeSystem;
import program.typesystem.XBs;
import utils.*;
import visitors.CollectorVisitor;
import visitors.CtxVisitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static program.Program.filterByMdf;

public class MIRInjectionVisitor implements CtxVisitor<MIRInjectionVisitor.Ctx, MIRInjectionVisitor.Res<? extends MIR.E>> {
  private final Program p;
  private final ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls;
  private final Collection<String> cached;

  public record Res<EE extends MIR.E>(EE e, List<MIR.TypeDef> defs, List<MIR.Fun> funs) {
    public TopLevelRes mergeAsTopLevel(Res<?> other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
  }
  public record TopLevelRes(List<MIR.TypeDef> defs, List<MIR.Fun> funs) {
    public static TopLevelRes EMPTY = new TopLevelRes(List.of(), List.of());
    public TopLevelRes merge(TopLevelRes other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
    public TopLevelRes mergeAsTopLevel(Res<?> other) {
      return new TopLevelRes(Push.of(defs(), other.defs()), Push.of(funs(), other.funs()));
    }
  }

  public record Ctx(Map<String, MIR.X> xXs) {
    public static Ctx EMPTY = new Ctx();
    public Ctx {
      xXs = Collections.unmodifiableMap(xXs);
    }
    public Ctx withXXs(Map<String, MIR.X> xXs) {
      return new Ctx(xXs);
    }
    private Ctx() { this(Map.of()); }
  }

  public MIRInjectionVisitor(Collection<String>cached, Program p, ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls) {
    this.p = p;
    this.resolvedCalls = resolvedCalls;
    this.cached = cached;//TODO: clean up mearless so that can work
    //on partial programs
  }

  public MIR.Program visitProgram() {
    var pkgs = p.ds().values().stream()
      .collect(Collectors.groupingBy(t->t.name().pkg()))
      .entrySet().stream()
      //.filter(kv->!cached.contains(kv.getKey()))//uncomment when cached TODO is sorted
      .map(kv->visitPackage(kv.getKey(), kv.getValue()))
      .toList();

    return new MIR.Program(p.shallowClone(), pkgs);
  }

  public MIR.Package visitPackage(String pkg, List<T.Dec> ds) {
    var allTDefs = new ArrayList<MIR.TypeDef>(ds.size());
    var allFuns = new ArrayList<MIR.Fun>(ds.size());
    ds.stream()
      .map(d->visitTopDec(d.withLambda(d.lambda().withMdf(Mdf.mut)), Ctx.EMPTY))
      .forEach(res->{
        allTDefs.addAll(res.defs());
        allFuns.addAll(res.funs());
      });
    return new MIR.Package(pkg, Mapper.of(defs->allTDefs.forEach(def->defs.put(def.name(), def))), Collections.unmodifiableList(allFuns));
  }

  public TopLevelRes visitTopDec(T.Dec dec, Ctx ctx) {
    if (getTransparentSource(dec).isPresent()) {
      return TopLevelRes.EMPTY;
    }

    var it = dec.toIT();
    var freshTops = dec.lambda().meths().stream()
      .filter(m->!m.isAbs())
      .map(m->{
        var g = new HashMap<>(ctx.xXs());
        g.put(dec.lambda().selfName(), new MIR.X(dec.lambda().selfName(), MIR.MT.of(new T(m.sig().mdf(), it))));
        var ctx_ = ctx.withXXs(g);
        return function(new CM.CoreCM(it, m, m.sig()), ctx_);
      })
      .reduce(TopLevelRes::merge).orElse(TopLevelRes.EMPTY);

    var allConcrete = new Box<>(true);
    var sigs = p.meths(XBs.empty().addBounds(dec.gxs(), dec.bounds()), Mdf.recMdf, dec.lambda(), 0).stream()
      .peek(cm->{
        if (cm.isAbs()) { allConcrete.set(false); }
      })
      .map(cm->visitSig((CM.CoreCM)cm))
      .toList();
    var impls = dec.lambda().its().stream().map(it_->new MIR.MT.Plain(Mdf.mdf, it_.name())).toList();
    var canSingleton = allConcrete.get() && freeVariables(dec.lambda()).isEmpty();
    var singleton = canSingleton ? Optional.of(constr(dec.lambda(), ctx)) : Optional.<MIR.CreateObj>empty();
    var tDef = new MIR.TypeDef(dec.name(), impls, sigs, singleton);

    return freshTops.merge(new TopLevelRes(List.of(tDef), List.of()));
  }

  public MIR.CreateObj constr(E.Lambda e, Ctx ctx) {
    var ms =  p.meths(XBs.empty(), Mdf.recMdf, e, 0).stream()
      .filter(cm->filterByMdf(e.mdf(), cm.mdf()))
      .map(cm->(CM.CoreCM)cm)
      .map(cm->visitMeth(cm, visitSig(cm)))
      .peek(m->{assert m.fName().isPresent();})
      .toList();

    var uncallableMs =  p.meths(XBs.empty(), Mdf.recMdf, e, 0).stream()
      .filter(cm->!filterByMdf(e.mdf(), cm.mdf()))
      .map(cm->(CM.CoreCM)cm)
      .map(cm->visitMeth(cm, visitSig(cm)))
      .toList();

    return new MIR.CreateObj(
      MIR.MT.of(new T(e.mdf(), e.id().toIT())),
      e.selfName(),
      ms,
      uncallableMs,
      captures(e, ctx)
    );
  }

  public MIR.Sig visitSig(CM.CoreCM cm) {
    return new MIR.Sig(
      cm.name(),
      cm.mdf(),
      Streams.zip(cm.xs(), cm.sig().ts()).map((x,t)->{
        if (x.equals("_")) { x = astFull.E.X.freshName(); }
        return new MIR.X(x, MIR.MT.of(t));
      }).toList(),
      MIR.MT.of(cm.ret())
    );
  }

  public TopLevelRes function(CM.CoreCM cm, Ctx ctx) {
    var sig = visitSig(cm);
    var captures = captures(cm.m(), ctx);

    var mCtx = new Ctx(Mapper.of(xXs->{
      xXs.putAll(ctx.xXs());
      sig.xs().forEach(x->xXs.put(x.name(), x));
      captures.forEach(x->xXs.put(x.name(), x));
    }));

    var x = ctx.xXs().get(selfNameOf(cm.c().name()));
    // We always produce a self-arg even if it is not captured to keep the function signatures consistent
    Stream<MIR.X> selfArg =Stream.of(x);
    var args = Streams.of(sig.xs().stream(), selfArg, captures.stream().filter(xi->!xi.name().equals(x.name()))).toList();

    var rawBody = cm.m().body().orElseThrow();
    var bodyRes = rawBody.accept(this, mCtx);
    var fun = new MIR.Fun(new MIR.FName(cm, captures.contains(x)), args, sig.rt(), bodyRes.e());
    return new TopLevelRes(bodyRes.defs(), Push.of(bodyRes.funs(), fun));
  }
  public MIR.Meth visitMeth(CM.CoreCM cm, MIR.Sig sig) {
    // uncallable meths can be abstract
    if (cm.isAbs()) {
      return new MIR.Meth(cm.c().name(), sig, false, Collections.emptySortedSet(), Optional.empty());
    }
    assert !cm.isAbs();
    var x = selfNameOf(cm.c().name());

    var fv = new FreeVariables();
    fv.visitMeth(cm.m());
    var xs = fv.res();
    var capturesSelf = xs.remove(x);

    return new MIR.Meth(cm.c().name(), sig, capturesSelf, Collections.unmodifiableSortedSet(xs), Optional.of(new MIR.FName(cm, capturesSelf)));
  }

  @Override public Res<MIR.MCall> visitMCall(E.MCall e, Ctx ctx) {
    var recvRes = e.receiver().accept(this, ctx);
    var tst = this.resolvedCalls.get(e.callId());
    var args = e.es().stream().map(ei->ei.accept(this, ctx)).toList();
    var topLevel = Stream.concat(Stream.of(recvRes), args.stream())
      .reduce(TopLevelRes.EMPTY, TopLevelRes::mergeAsTopLevel, TopLevelRes::merge);

    var call = new MIR.MCall(
      recvRes.e(),
      e.name().withMdf(Optional.of(tst.original().mdf())),
      args.stream().map(Res::e).toList(),
      MIR.MT.of(tst.t()),
      MIR.MT.of(((CM.CoreCM) tst.original()).m().sig().ret()),
      tst.original().mdf(),
      getVariants(recvRes.e(), e)
    );

    return new Res<>(call, topLevel.defs(), topLevel.funs());
  }

  @Override public Res<MIR.X> visitX(E.X e, Ctx ctx) {
    return new Res<>(visitX(e.name(), ctx), List.of(), List.of());
  }
  public MIR.X visitX(String x, Ctx ctx) {
    var fullX = ctx.xXs.get(x);
    if (fullX == null) {
      throw new NotInGammaException(x);
    }
    return fullX;
  }

  @Override public Res<MIR.CreateObj> visitLambda(E.Lambda e, Ctx ctx) {
    var dec = p.of(e.id().id());
    assert dec.lambda() == e;
    var transparentSource = getTransparentSource(p.of(e.id().id()));
    if (transparentSource.isPresent()) {
      var realDec = transparentSource.get();
      var k = new MIR.CreateObj(
        MIR.MT.of(new T(e.mdf(), realDec.toIT())),
        realDec.lambda().selfName(),
        List.of(),
        List.of(),
        MIR.createCapturesSet()
      );
      return new Res<>(k, List.of(), List.of());
    }

    var k = constr(e, ctx);
    var topLevel = visitTopDec(dec, ctx);
    return new Res<>(k, topLevel.defs(), topLevel.funs());
  }

  private Optional<T.Dec> getTransparentSource(T.Dec d) {
    if (d.name().isFresh() && d.lambda().meths().isEmpty()) {
      var nonSelfImpls = d.lambda().its().stream().filter(it->!it.name().equals(d.name())).toList();
      if (nonSelfImpls.size() != 1) { return Optional.empty(); }
      var realIT = nonSelfImpls.getFirst();
      return Optional.of(p.of(realIT.name()));
    }
    return Optional.empty();
  }

  private String selfNameOf(Id.DecId d) {
    return p.of(d).lambda().selfName();
  }

  private EnumSet<MIR.MCall.CallVariant> getVariants(MIR.E recv, E.MCall e) {
    // Standard library .flow methods:
    var recvT = (MIR.MT.Usual) recv.t();
    var recvIT = recvT.it();
    if (e.name().name().equals(".flow")) {
      if (recvIT.name().equals(new Id.DecId("base.LList", 1))) {
        var flowElem = recvIT.ts().getFirst();
//        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
      if (recvIT.name().equals(Magic.FList)) {
        var flowElem = recvIT.ts().getFirst();
        if (recvT.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow); }
        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow); }
//        if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.SafeMutSourceFlow); }
        return EnumSet.of(MIR.MCall.CallVariant.Standard);
      }
    }
    if (recvIT.name().equals(Magic.FlowK) && e.name().name().equals("#")) {
      var flowElem = e.ts().getFirst();
      if (flowElem.mdf().is(Mdf.read, Mdf.imm)) { return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow); }
    }
//    if (recvIT.name().equals(Magic.FlowK) && e.name().name().equals(".range")) {
//      return EnumSet.of(MIR.MCall.CallVariant.DataParallelFlow, MIR.MCall.CallVariant.PipelineParallelFlow, MIR.MCall.CallVariant.SafeMutSourceFlow);
//    }

    return EnumSet.of(MIR.MCall.CallVariant.Standard);
  }

  private SortedSet<String> freeVariables(E.Lambda e) {
    var fv = new FreeVariables();
    fv.visitLambda(e);
    return Collections.unmodifiableSortedSet(fv.res());
  }
  private SortedSet<String> freeVariables(E.Meth m) {
    var fv = new FreeVariables();
    fv.visitMeth(m);
    return Collections.unmodifiableSortedSet(fv.res());
  }
  private SortedSet<MIR.X> captures(E.Lambda e, Ctx ctx) {
    var fv = new FreeVariables();
    fv.visitLambda(e);
    return Collections.unmodifiableSortedSet(fv.res().stream()
        .map(x->visitX(x, ctx))
        .collect(Collectors.toCollection(MIR::createCapturesSet)));
  }
  private SortedSet<MIR.X> captures(E.Meth m, Ctx ctx) {
    var fv = new FreeVariables();
    fv.visitMeth(m);
    return Collections.unmodifiableSortedSet(fv.res().stream()
      .map(x->visitX(x, ctx))
      .collect(Collectors.toCollection(MIR::createCapturesSet)));
  }

  private static class FreeVariables implements CollectorVisitor<SortedSet<String>> {
    private final SortedSet<String> res = new TreeSet<>(String::compareTo);
    private Set<String> fresh = new HashSet<>();
    public SortedSet<String> res() { return this.res; }

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

  private static class NotInGammaException extends RuntimeException {
    public NotInGammaException(String x) { super(x); }
  }

}
