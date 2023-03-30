package program;

import astFull.T;
import id.Id;
import id.Mdf;
import magic.Magic;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface TypeRename<T>{
  record FullTTypeRename(astFull.Program p) implements TypeRename<astFull.T> {
    public <R> R matchT(astFull.T t, Function<Id.GX<astFull.T>, R> gx, Function<Id.IT<astFull.T>, R> it) { return t.match(gx, it); }
    public Mdf mdf(astFull.T t) { return t.mdf(); }
    public astFull.T newT(Mdf mdf, Id.IT<astFull.T> t) {
      return new astFull.T(mdf, t);
    }
    public astFull.T withMdf(astFull.T t, Mdf mdf) { return t.withMdf(mdf); }
    public boolean isInfer(astFull.T t) { return t.isInfer(); }
    public boolean isNoMutHyg(astFull.T t) {
      if (t.isInfer()) { return false; }
      return t.match(
        gx->false,
        it->p.isSubType(new astFull.T(Mdf.mdf, it), new astFull.T(Mdf.mdf, new Id.IT<astFull.T>(Magic.NoMutHyg)))
      );
    }
  }
  class CoreTTypeRename implements TypeRename<ast.T> {
    public <R> R matchT(ast.T t, Function<Id.GX<ast.T>,R>gx, Function<Id.IT<ast.T>,R>it) { return t.match(gx, it); }
    public Mdf mdf(ast.T t) { return t.mdf(); }
    public ast.T newT(Mdf mdf, Id.IT<ast.T> t) { return new ast.T(mdf, t); }
    public ast.T withMdf(ast.T t, Mdf mdf) { return t.withMdf(mdf); }
    public ast.E.Sig renameSig(ast.E.Sig sig, Function<Id.GX<ast.T>,ast.T>f){
      assert sig.gens().stream().allMatch(gx->f.apply(gx)==null);
      return new ast.E.Sig(
        sig.mdf(),
        sig.gens(),
        sig.ts().stream().map(t->renameT(t,f)).toList(),
        renameT(sig.ret(),f),
        sig.pos()
      );
    }
    public boolean isInfer(ast.T t) { return false; }
  }
  class CoreRecMdfTypeRename extends CoreTTypeRename {
    /** This is adaptRecMDF(MDF) ITX with t = MDF ITX */
    public ast.T propagateMdf(Mdf mdf, ast.T t){
      if(!mdf.isRecMdf()){ return super.propagateMdf(mdf,t); }
      assert t!=null;
      var m=t.mdf();
      if(m.isImm()){ return t; }
      if(m.isRead()){ return t; }
      assert !m.isIso();
      return t.withMdf(Mdf.recMdf);
    }
  }
  static FullTTypeRename full() { return new FullTTypeRename(); }
  static CoreTTypeRename core() { return new CoreTTypeRename(); }
  static CoreRecMdfTypeRename coreRec() { return new CoreRecMdfTypeRename(); }

  <R> R matchT(T t, Function<Id.GX<T>,R> gx, Function<Id.IT<T>,R> it);
  Mdf mdf(T t);
  T newT(Mdf mdf, Id.IT<T> it);
  T withMdf(T t,Mdf mdf);

  default Id.IT<T> renameIT(Id.IT<T> it, Function<Id.GX<T>, T> f){
    return it.withTs(it.ts().stream().map(t->renameT(t,f)).toList());
  }
  default Function<Id.GX<T>, T> renameFun(List<T> ts, List<Id.GX<T>> gxs) {
    return gx->{
      int i = gxs.indexOf(gx);
      if(i==-1){ return null; }
      return ts.get(i);
    };
  }
  boolean isInfer(T t);
  boolean isNoMutHyg(T t);
  default T renameT(T t, Function<Id.GX<T>,T> f){
    if(isInfer(t)){ return t; }
    return matchT(t,
      gx->{
        var renamed = f.apply(gx);
        if(renamed==null){ return t; }
        if (isInfer(renamed)){ return renamed; }
        return fixMut(propagateMdf(mdf(t),renamed));
      },
      it->fixMut(newT(mdf(t),renameIT(it,f)))
    );
  }
  default T propagateMdf(Mdf mdf, T t){
    assert t!=null;
    if(mdf.isMdf()){ return t; }
    return withMdf(t,mdf);
  }
  default T fixMut(T t) {
    if (!mdf(t).isMut() || !isNoMutHyg(t)) { return t; }
    return propagateMdf(Mdf.lent, t);
  }
}