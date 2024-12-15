package ast;

import failure.CompileError;
import failure.Fail;
import files.Pos;
import id.Id;
import id.Mdf;
import magic.Magic;
import program.CM;
import program.TypeRename;
import program.TypeSystemFeatures;
import program.typesystem.TraitTypeSystem;
import program.typesystem.TsT;
import program.typesystem.XBs;
import utils.Mapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Program implements program.Program  {
  protected final Map<Id.DecId, T.Dec> ds;
  protected final Map<Id.DecId, T.Dec> inlineDs;
  private final TypeSystemFeatures tsf;
  public Program(TypeSystemFeatures tsf, Map<Id.DecId, T.Dec> ds, Map<Id.DecId, T.Dec> inlineDs) {
    this.tsf = tsf;
    this.ds = ds;
    this.inlineDs = inlineDs.isEmpty() ? Mapper.of(ds_->{
      ds_.putAll(inlineDs);
      var visitor = new AllLsVisitor();
      ds.values().forEach(dec->visitor.visitTrait(dec.lambda()));
      visitor.res().forEach(dec->ds_.put(dec.name(), dec));
    }) : inlineDs;
  }

  public Map<Id.DecId, T.Dec> ds() { return Collections.unmodifiableMap(this.ds); }
  public Map<Id.DecId, T.Dec> inlineDs() { return Collections.unmodifiableMap(this.inlineDs); }
  public List<ast.E.Lambda> lambdas() {
    return this.ds().values().stream().map(T.Dec::lambda).toList();
  }

  public void typeCheck(ConcurrentHashMap<Long, TsT> resolvedCalls) {
    var errors = new StringBuilder();
    TraitTypeSystem.dsOk(tsf, this.ds.values(), resolvedCalls)
      .forEach(err->errors.append(err.get().toString()).append("\n\n"));
    if (!errors.isEmpty()) { throw Fail.typeError(errors.toString()); }
  }

  public Program withDec(T.Dec d) {
    var ds = new HashMap<>(ds());
    assert !ds.containsKey(d.name());
    ds.put(d.name(), d);
    return new Program(tsf, Collections.unmodifiableMap(ds), inlineDs);
  }

  public Optional<Pos> posOf(Id.IT<ast.T> t) {
    return of(t.name()).pos();
  }

  @Override public TypeSystemFeatures tsf() {
    return this.tsf;
  }

  public T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = inlineDs.get(d); }
    if (res == null) { res = Magic.getDec(this::of, d); }
    if (res == null) { throw Fail.traitNotFound(d); }
    return res;
  }

  public boolean isInlineDec(Id.DecId d) {
    return this.inlineDs.containsKey(d);
  }

  @Override public List<Id.IT<T>> itsOf(Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<T>, T> f = TypeRename.core().renameFun(t.ts(), gxs);
    return d.lambda().its().stream()
      .filter(ti->!ti.name().equals(t.name()))
      .map(ti->TypeRename.core().renameIT(ti,f))
      .toList();
  }
  @Override public List<NormResult> cMsOf(Mdf recvMdf, Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core().renameFun(t.ts(), d.gxs());
    return d.lambda().meths().stream()
      .map(mi->cm(t, mi, XBs.empty().addBounds(d.gxs(), d.bounds()), f))
      .toList();
  }
  public CM plainCM(CM fancyCM) {
    var d=of(fancyCM.c().name());
    assert fancyCM.c().ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core().renameFun(fancyCM.c().ts(), gxs);
    return d.lambda().meths().stream()
      .filter(mi->mi.name().equals(fancyCM.name()))
      .map(mi->cmCore(d.toIT(), mi, f))
      .findFirst()
      .orElseThrow();
  }
  @Override public Set<Id.GX<T>> gxsOf(Id.IT<T> t) {
    return of(t.name()).gxs().stream().map(Id.GX::toAstGX).collect(Collectors.toSet());
  }

  private final Map<Id.DecId, Set<Id.DecId>> superDecIdsCache = new ConcurrentHashMap<>();
  public Set<Id.DecId> superDecIds(Id.DecId start) {
    if (superDecIdsCache.containsKey(start)) { return superDecIdsCache.get(start); }

    HashSet<Id.DecId> visited = new HashSet<>();
    visited.add(start);
    superDecIds(visited, start);
    var res = Collections.unmodifiableSet(visited);
    superDecIdsCache.put(start, res);
    return res;
  }

  public void superDecIds(HashSet<Id.DecId> visited, Id.DecId current) {
    if (superDecIdsCache.containsKey(current)) {
      visited.addAll(superDecIdsCache.get(current));
      return;
    }

    var currentDec = of(current);
    for(var it : currentDec.lambda().its()) {
      var novel=visited.add(it.name());
      try {
        if(novel){ superDecIds(visited, it.name()); }
      } catch (CompileError err) {
        throw err.parentPos(currentDec.pos());
      }
    }
  }

  @Override public String toString() { return this.ds.toString(); }

  private NormResult cm(Id.IT<ast.T> t, E.Meth mi, XBs xbs, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var normed = norm(CM.of(t, mi, mi.sig()));
    var cm = normed.cm();
    var normedMeth = new E.Meth(cm.sig(), cm.name(), cm.xs(), mi.body(), mi.pos());
    return new NormResult(CM.of(cm.c(), normedMeth, TypeRename.core().renameSig(cm.sig(), xbs, f)), normed.restoreSubst());
  }
  private CM cmCore(Id.IT<ast.T> t, E.Meth mi, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var normed = norm(CM.of(t, mi, mi.sig()));
    var cm = normed.cm();
    var normedMeth = new E.Meth(cm.sig(), cm.name(), cm.xs(), mi.body(), mi.pos());
    return CM.of(cm.c(), normedMeth, TypeRename.core().renameSig(cm.sig(), XBs.empty(), f));
  }
}
