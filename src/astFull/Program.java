package astFull;

import files.Pos;
import id.Id;
import id.Mdf;
import magic.Magic;
import failure.CompileError;
import failure.Fail;
import program.CM;
import program.TypeRename;
import program.typesystem.XBs;
import utils.Bug;
import utils.Mapper;
import utils.OneOr;
import utils.Range;
import visitors.InjectionVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Program implements program.Program{
  private final Map<Id.DecId, T.Dec> ds;
  private final Map<Id.DecId, T.Dec> inlineDs;
  public Program(Map<Id.DecId, T.Dec> ds) {
    this.ds = ds;
    this.inlineDs = Mapper.of(ds_->{
      var visitor = new AllLsFullVisitor();
      ds.values().forEach(dec->visitor.visitTrait(dec.lambda()));
      visitor.res().forEach(dec->ds_.put(dec.name(), dec));
    });
  }
  public Program(Map<Id.DecId, T.Dec> ds, Map<Id.DecId, T.Dec> inlineDs) {
    this.ds = ds;
    this.inlineDs = inlineDs.isEmpty() ? Mapper.of(ds_->{
      var visitor = new AllLsFullVisitor();
      ds.values().forEach(dec->visitor.visitTrait(dec.lambda()));
      visitor.res().forEach(dec->ds_.put(dec.name(), dec));
    }) : inlineDs;
  }

  public List<ast.E.Lambda> lambdas() { throw Bug.unreachable(); }
  public program.Program withDec(ast.T.Dec d) {
    throw Bug.unreachable();
  }

  public Optional<Pos> posOf(Id.IT<ast.T> t) {
    return of(t).pos();
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
    if (res == null) { res = inlineDs.get(d); }
    if (res == null) { res = Magic.getFullDec(this::of, d); }
    if (res == null) { throw Fail.traitNotFound(d); }
    return res;
  }

  public T.Dec of(Id.IT<ast.T> t) {
    return of(t.name());
  }

  public boolean isInlineDec(Id.DecId d) {
    return this.inlineDs.containsKey(d);
  }

  @Override public List<Id.IT<ast.T>> itsOf(Id.IT<ast.T> t){
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core(this).renameFun(t.ts(), gxs);
    return d.lambda().its().stream().map(ti->TypeRename.core(this).renameIT(liftIT(ti),f)).toList();
  }
  /** with t=C[Ts]  we do  C[Ts]<<Ms[Xs=Ts],*/
  @Override public List<CM> cMsOf(Mdf recvMdf, Id.IT<ast.T> t){
    var d=of(t.name());
    assert t.ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core(this).renameFun(t.ts(), gxs);
    var bounds = XBs.empty().addBounds(gxs, Mapper.of(xbs->d.bounds().forEach((gx,bs)->xbs.put(new Id.GX<>(gx.name()), bs))));
    return d.lambda().meths().stream()
      .filter(mi->mi.sig().isPresent())
      .map(mi->cm(recvMdf, t, mi, bounds, f))
      .toList();
  }
  @Override public CM plainCM(CM fancyCM){
    var d=of(fancyCM.c().name());
    assert fancyCM.c().ts().size()==d.gxs().size();
    var gxs=d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList();
    Function<Id.GX<ast.T>, ast.T> f = TypeRename.core(this).renameFun(fancyCM.c().ts(), gxs);
    return d.lambda().meths().stream()
      .filter(mi->mi.name().map(name->name.equals(fancyCM.name())).orElse(false))
      .map(mi->cmCore(fancyCM.c(), mi, XBs.empty(), f))
      .findFirst()
      .orElseThrow();
  }

  @Override public Set<Id.GX<ast.T>> gxsOf(Id.IT<ast.T> t) {
    return of(t).gxs().stream().map(Id.GX::toAstGX).collect(Collectors.toSet());
  }

  private CM cm(Mdf recvMdf, Id.IT<ast.T> t, astFull.E.Meth mi, XBs xbs, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var sig=mi.sig().orElseThrow();
    var cm = CM.of(t, mi, TypeRename.coreRec(this, recvMdf).renameSig(new InjectionVisitor().visitSig(sig), xbs, f));
    return norm(cm);
  }
  private CM cmCore(Id.IT<ast.T> t, astFull.E.Meth mi, XBs xbs, Function<Id.GX<ast.T>, ast.T> f){
    // This is doing C[Ts]<<Ms[Xs=Ts] (hopefully)
    var sig=mi.sig().orElseThrow();
    var cm = CM.of(t, mi, TypeRename.core(this).renameSig(new InjectionVisitor().visitSig(sig), xbs, f));
    return norm(cm);
  }
  public Map<Id.DecId, T.Dec> ds() { return this.ds; }
  public Map<Id.DecId, T.Dec> inlineDs() { return this.inlineDs; }

  private final HashMap<Id.DecId, Set<Id.DecId>> superDecIdsCache = new HashMap<>();
  public Set<Id.DecId> superDecIds(Id.DecId start) {
    if (superDecIdsCache.containsKey(start)) { return superDecIdsCache.get(start); }

    HashSet<Id.DecId> visited = new HashSet<>();
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

  /**
   * Applies inference 5a
   */
  public astFull.Program inferSignatures(){
    var is=new InferSignatures(this);
    for (int i : Range.of(is.decs)){
      var di = is.inferSignatures(is.decs.get(i));
      is.updateDec(di,i);
    }
    this.reset();
    for (int i : Range.of(is.inlineDecs)){
      var di = is.inferInlineSignatures(is.inlineDecs.get(i));
      is.updateInlineDec(di,i);
    }
    this.reset();
    return is.p;
  }
  public List<E.Meth> dom(Id.DecId id){ return this.of(id).lambda().meths(); }

  public static class InferSignatures {
    List<T.Dec> decs;
    List<T.Dec> inlineDecs;
    Program p;
    InferSignatures(Program p){
      this.p=p;
      this.decs = orderDecs(p.ds().values());
      this.inlineDecs = p.inlineDs().values().stream().filter(dec->!dec.name().isFresh()).collect(Collectors.toCollection(ArrayList::new));
    }
    private List<T.Dec> orderDecs(Collection<T.Dec> ds){
      // Do a topological sort on the dep graph (should be a DAG) so we infer parents before children.
      // Just using Kahn's algorithm here
      var sorted = new ArrayList<T.Dec>();
      var roots = ds.stream().filter(d->d.lambda().its().isEmpty()).collect(Collectors.toCollection(ArrayDeque::new));
      var unvisited = roots.stream().map(T.Dec::name).collect(Collectors.toCollection(HashSet::new));
      var visited = new HashSet<Id.DecId>(ds.size());
      while (!roots.isEmpty()) {
        var dec = roots.pop();
        unvisited.remove(dec.name());
        sorted.add(dec);
        ds.stream()
          .filter(d->{
            var its = d.lambda().its().stream().filter(it->!visited.contains(it.name())).toList();
            return !its.isEmpty() && its.stream().allMatch(it->it.name().equals(dec.name()));
          })
          .filter(d->unvisited.add(d.name()))
          .forEach(roots::add);
        visited.add(dec.name());
      }

      assert unvisited.isEmpty();
      assert sorted.size() == ds.size();
      return sorted;
    }
    private void updateDec(T.Dec d, int i) {
      decs.set(i,d);
      p=new Program(decs.stream().collect(Collectors.toMap(T.Dec::name, di->di)));
    }
    private void updateInlineDec(T.Dec d, int i) {
      inlineDecs.set(i,d);
      p=new Program(decs.stream().collect(Collectors.toMap(T.Dec::name, di->di)), inlineDecs.stream().collect(Collectors.toMap(T.Dec::name, di->di)));
    }

    private T.Dec inferSignatures(T.Dec d) {
      return d.withLambda(inferSignatures(d, d.lambda().withSelfName("this")));
    }
    private T.Dec inferInlineSignatures(T.Dec d) {
      return d.withLambda(inferSignatures(d, d.lambda()));
    }
    private E.Lambda inferSignatures(T.Dec dec, E.Lambda l) {
      if (l.selfName() == null) {
        l = l.withSelfName(new E.X(T.infer).name());
      }
      var ms = l.meths().stream().flatMap(m->inferSignature(dec,m).stream()).toList();
      return l.withMeths(ms);
    }
    Id.MethName onlyAbs(XBs xbs, T.Dec dec){
      // depth doesn't matter here because we just extract the name
      var m = OneOr.of(()->Fail.cannotInferAbsSig(dec.name()), p.meths(xbs, Mdf.recMdf, dec.toAstT(), -1).stream().filter(CM::isAbs));
      return m.name();
    }
    List<E.Meth> inferSignature(T.Dec dec, E.Meth m) {
      try {
        if(m.sig().isPresent()){ return List.of(m); }
        var xbs = XBs.empty().addBounds(
          dec.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
          Mapper.of(xbs_->dec.bounds().forEach((gx,bs)->xbs_.put(new Id.GX<>(gx.name()), bs)))
        );
        var name=m.name().orElseGet(()->onlyAbs(xbs, dec));
        if (m.xs().size() != name.num()) {
          throw Fail.cannotInferSig(dec.name(), name);
        }
        var namedMeth = m.withName(name);
        assert name.num()==namedMeth.xs().size();
        var res = p.meths(xbs, Mdf.recMdf, dec.toAstT(), name, 0).stream()
          .map(inferred->namedMeth.withSig(inferred.sig().toAstFullSig()).withName(name.withMdf(Optional.of(inferred.mdf()))))
          .toList();
        if (res.isEmpty()) {
          throw Fail.cannotInferSig(dec.name(), name);
        }
        return res;
      } catch (CompileError e) {
        throw e.pos(m.pos());
      }
    }
  }
  @Override public String toString() { return this.ds().toString(); }
}
