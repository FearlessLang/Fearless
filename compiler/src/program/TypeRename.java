package program;

import failure.CompileError;
import id.Id;
import id.Mdf;
import program.typesystem.Gamma;
import program.typesystem.XBs;

import java.util.List;
import java.util.function.Function;

public interface TypeRename<T extends Id.Ty>{
  record FullTTypeRename(Program p) implements TypeRename<astFull.T> {
    public <R> R matchT(astFull.T t, Function<Id.GX<astFull.T>, R> gx, Function<Id.IT<astFull.T>, R> it) { return t.match(gx, it); }
    public Mdf mdf(astFull.T t) { return t.mdf(); }
    public astFull.T newT(Mdf mdf, Id.IT<astFull.T> t) {
      return new astFull.T(mdf, t);
    }
    public astFull.T withMdf(astFull.T t, Mdf mdf) { return t.withMdf(mdf); }
    public boolean isInfer(astFull.T t) { return t.isInfer(); }
  }
  class CoreTTypeRename implements TypeRename<ast.T> {
    private final Program p;
    public CoreTTypeRename(Program p) { this.p = p; }
    public <R> R matchT(ast.T t, Function<Id.GX<ast.T>,R>gx, Function<Id.IT<ast.T>,R>it) { return t.match(gx, it); }
    public Mdf mdf(ast.T t) { return t.mdf(); }
    public ast.T newT(Mdf mdf, Id.IT<ast.T> t) { return new ast.T(mdf, t); }
    public ast.T withMdf(ast.T t, Mdf mdf) { return t.withMdf(mdf); }
    public ast.E.Sig renameSig(ast.E.Sig sig, XBs bounds, Function<Id.GX<ast.T>,ast.T>f){
      assert sig.gens().stream().allMatch(gx->f.apply(gx)==null);
      try {
        return renameSigOnMCall(sig, bounds, f);
      } catch (CompileError err) {
        throw err.parentPos(sig.pos());
      }
    }
    public ast.E.Sig renameSigOnMCall(ast.E.Sig sig, XBs bounds, Function<Id.GX<ast.T>,ast.T>f){
      var allBounds = bounds.addBounds(sig.gens(), sig.bounds());
      return new ast.E.Sig(
        sig.gens(),
        sig.bounds(),
        sig.ts().stream().map(t->renameArgT(t, allBounds, f)).toList(),
        renameT(sig.ret(),f),
        sig.pos()
      );
    }
    public boolean isInfer(ast.T t) { return false; }
  }
  class CoreRecMdfTypeRename extends CoreTTypeRename {
    private final Mdf recvMdf;
    public CoreRecMdfTypeRename(Program p, Mdf recvMdf) {
      super(p);
      assert !recvMdf.isMdf();
      this.recvMdf = recvMdf;
    }

    /** This is part of MDF ITX[[MDF0; Ts=Xs]] */
    @Override public ast.T propagateMdf(Mdf mdf, ast.T t){
      if(!mdf.isRecMdf()){ return super.propagateMdf(mdf, t); }
      assert t!=null;
      if (recvMdf.isRecMdf() && t.mdf().isMdf()) {
        return t.withMdf(Mdf.recMdf);
      }
      // TODO: or maybe this (see commented tests in TestTypeSystem)
//      if (t.mdf().isMdf()) {
//        return t.withMdf(Mdf.recMdf);
//      }
      var returnMdf = recvMdf.adapt(t.mdf());
      return t.withMdf(returnMdf);
    }
    @Override public ast.T propagateArgMdf(XBs xbs, Mdf mdf, ast.T t){
      if(!mdf.isRecMdf()){ return super.propagateMdf(mdf, t); }
      assert t!=null;
      if (t.mdf().isMdf() || recvMdf.isRecMdf() || t.mdf().isRecMdf()) {
        // not sure about if this is sound
        // we enter this from inference where we replace all the gens with mdf FearN$
        return propagateMdf(mdf, t);
      }
      var argMdf = Gamma.xT(t.rt().toString(), xbs, recvMdf, t, recvMdf).mdf();
      return t.withMdf(argMdf);
    }
  }
  static FullTTypeRename full(Program p) { return new FullTTypeRename(p); }
  static CoreTTypeRename core(Program p) { return new CoreTTypeRename(p); }
  static CoreRecMdfTypeRename coreRec(Program p, Mdf recvMdf) { return new CoreRecMdfTypeRename(p, recvMdf); }

  <R> R matchT(T t, Function<Id.GX<T>,R> gx, Function<Id.IT<T>,R> it);
  Mdf mdf(T t);
  T newT(Mdf mdf, Id.IT<T> it);
  T withMdf(T t,Mdf mdf);

  default Id.IT<T> renameIT(Id.IT<T> it, Function<Id.GX<T>, T> f){
    return it.withTs(it.ts().stream().map(t->renameT(t,f)).toList());
  }
  default Id.IT<T> renameArgIT(Id.IT<T> it, XBs xbs, Function<Id.GX<T>, T> f){
    return it.withTs(it.ts().stream().map(t->renameArgT(t,xbs,f)).toList());
  }
  default Function<Id.GX<T>, T> renameFun(List<T> ts, List<Id.GX<T>> gxs) {
    assert ts.size() == gxs.size();
    return gx->{
      int i = gxs.indexOf(gx);
      if(i==-1){ return null; }
      return ts.get(i);
    };
  }
  boolean isInfer(T t);
  enum RenameKind { Arg, Return }
  default T renameT(T t, Function<Id.GX<T>,T> f){
    if(isInfer(t)){ return t; }
    return matchT(t,
      gx->{
        var renamed = f.apply(gx);
        if(renamed==null){ return t; }
        if (isInfer(renamed)){ return renamed; }
        return propagateMdf(mdf(t), renamed);
      },
      // TODO: this is new (was not going via propagateMdf before, what breaks?
      it->propagateMdf(mdf(t), newT(mdf(t), renameIT(it,f)))
//      it->newT(mdf(t), renameIT(it,f))
    );
  }
  default T renameArgT(T t, XBs xbs, Function<Id.GX<T>,T> f){
    if(isInfer(t)){ return t; }
    return matchT(t,
      gx->{
        var renamed = f.apply(gx);
        if(renamed==null){ return t; }
        if (isInfer(renamed)){ return renamed; }
        return propagateArgMdf(xbs, mdf(t), renamed);
      },
      // TODO: this is new (was not going via propagateMdf before, what breaks?
      it->propagateArgMdf(xbs, mdf(t), newT(mdf(t), renameArgIT(it, xbs, f)))
//      it->newT(mdf(t), renameIT(it,f))
    );
  }
  default T propagateMdf(Mdf mdf, T t){
    assert t!=null;
    if(mdf.isMdf()){ return t; }
    if (mdf.isReadImm() && mdf(t).isImm()) { return withMdf(t, Mdf.imm); }
    return withMdf(t,mdf);
  }
  default T propagateArgMdf(XBs xbs, Mdf mdf, T t){
    return propagateMdf(mdf, t);
  }
}