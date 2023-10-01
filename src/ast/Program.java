package ast;

import failure.CompileError;
import files.Pos;
import id.Id;
import id.Mdf;
import magic.Magic;
import failure.Fail;
import program.CM;
import program.TypeRename;
import program.typesystem.TraitTypeSystem;
import program.typesystem.XBs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Program implements program.Program  {
  private final Map<Id.DecId, T.Dec> ds;
  public Program(Map<Id.DecId, T.Dec> ds) { this.ds = ds; }

  public Map<Id.DecId, T.Dec> ds() { return this.ds; }
  public List<ast.E.Lambda> lambdas() { return this.ds().values().stream().map(T.Dec::lambda).toList(); }

  public void typeCheck() {
    var errors = new StringBuilder();
    TraitTypeSystem.dsOk(this.ds.values())
      .forEach(err->errors.append(err.toString()).append("\n\n"));
    if (!errors.isEmpty()) { throw new CompileError(errors.toString()); }
  }

  public Program withDec(T.Dec d) {
    var ds = new HashMap<>(ds());
    assert !ds.containsKey(d.name());
    ds.put(d.name(), d);
    return new Program(Collections.unmodifiableMap(ds));
  }

  public Optional<Pos> posOf(Id.IT<ast.T> t) {
    return of(t.name()).pos();
  }

  @Override public Program shallowClone() {
    var subTypeCache = new HashMap<>(this.subTypeCache);
    var methsCache = new HashMap<>(this.methsCache);
    return new Program(ds){
      @Override public HashMap<SubTypeQuery, SubTypeResult> subTypeCache() {
        return subTypeCache;
      }
      @Override public HashMap<MethsCacheKey, List<CM>> methsCache() {
        return methsCache;
      }
    };
  }

  private final HashMap<SubTypeQuery, SubTypeResult> subTypeCache = new HashMap<>();
  @Override public HashMap<SubTypeQuery, SubTypeResult> subTypeCache() {
    return subTypeCache;
  }

  private final HashMap<MethsCacheKey, List<CM>> methsCache = new HashMap<>();
  @Override public HashMap<MethsCacheKey, List<CM>> methsCache() {
    return methsCache;
  }

  public T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = Magic.getDec(this::of, d); }
    if (res == null) { throw Fail.traitNotFound(d); }
    return res;
  }

  @Override public List<Id.IT<T>> itsOf(Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<T>, T> f = TypeRename.core(this).renameFun(t.ts(), gxs);
    return d.lambda().its().stream()
      .filter(ti->!ti.name().equals(t.name()))
      .map(ti->TypeRename.core(this).renameIT(ti,f))
      .toList();
  }
  @Override
  public List<CM> cMsOf(Mdf recvMdf, Id.IT<T> t) {
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core(this).renameFun(t.ts(), d.gxs());
    return d.lambda().meths().stream()
      .map(mi->cm(recvMdf, t, mi, XBs.empty().addBounds(d.gxs(), d.bounds()), f))
      .toList();
  }
  public CM plainCM(CM fancyCM) {
    var d=of(fancyCM.c().name());
    assert fancyCM.c().ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core(this).renameFun(fancyCM.c().ts(), gxs);
    return d.lambda().meths().stream()
      .filter(mi->mi.name().equals(fancyCM.name()))
      .map(mi->cmCore(d.toIT(), mi, f))
      .findFirst()
      .orElseThrow();
  }
  @Override public Set<Id.GX<T>> gxsOf(Id.IT<T> t) {
    return of(t.name()).gxs().stream().map(Id.GX::toAstGX).collect(Collectors.toSet());
  }

  @Override public String toString() { return this.ds.toString(); }

  private CM cm(Mdf recvMdf, Id.IT<ast.T> t, E.Meth mi, XBs xbs, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var cm = norm(CM.of(t, mi, mi.sigs()));
    var normedMeth = new E.Meth(cm.sigs(), cm.name(), cm.xs(), mi.body(), mi.pos());
    return CM.of(cm.c(), normedMeth, TypeRename.coreRec(this, recvMdf).renameSig(cm.sigs(), xbs, f));
  }
  private CM cmCore(Id.IT<ast.T> t, E.Meth mi, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var cm = norm(CM.of(t, mi, mi.sigs()));
    var normedMeth = new E.Meth(cm.sigs(), cm.name(), cm.xs(), mi.body(), mi.pos());
    return CM.of(cm.c(), normedMeth, TypeRename.core(this).renameSig(cm.sigs(), XBs.empty(), f));
  }
}
