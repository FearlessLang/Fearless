package program;

import id.Id;
import id.Mdf;
import program.inference.RefineTypesOldBoo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface TypeRename<T>{
  record FullTTypeRename() implements TypeRename<astFull.T> {
    public <R> R matchT(astFull.T t, Function<Id.GX<astFull.T>, R> gx, Function<Id.IT<astFull.T>, R> it) { return t.match(gx, it); }
    public Mdf mdf(astFull.T t) { return t.mdf(); }
    public astFull.T newT(Mdf mdf, Id.IT<astFull.T> t) {
      return new astFull.T(mdf, t);
    }
    public astFull.T withMdf(astFull.T t, Mdf mdf) { return t.withMdf(mdf); }
    public boolean isInfer(astFull.T t) { return t.isInfer(); }
  }
  record CoreTTypeRename() implements TypeRename<ast.T> {
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
        renameT(sig.ret(),f)
      );
    }
    public boolean isInfer(ast.T t) { return false; }
  }
  static FullTTypeRename full() { return new FullTTypeRename(); }
  static CoreTTypeRename core() { return new CoreTTypeRename(); }

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
  default T renameT(T t, Function<Id.GX<T>,T> f){
    if(isInfer(t)){ return t; }
    return matchT(t,
      gx->{
        var renamed = f.apply(gx);
        if(renamed==null){ return t; }
        return propagateMdf(mdf(t),renamed);
      },
      it->newT(mdf(t),renameIT(it,f))
    );
  }
  default T propagateMdf(Mdf mdf, T t){
    assert t!=null;
    if(mdf.isMdf()){ return t; }
    return withMdf(t,mdf);
  }
}