package ast;

import failure.CompileError;
import files.HasPos;
import files.Pos;
import id.Id;
import id.Id.DecId;
import id.Mdf;
import utils.Bug;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record T(Mdf mdf, Id.RT<T> rt) implements failure.Res, Id.Ty {
  public <R> R resMatch(Function<T,R> ok, Function<CompileError,R> err){ return ok.apply(this); }
  @Override public String toString(){
    if (mdf.isMdf()) { return rt.toString(); }
    return mdf+" "+rt;
  }
  public T{
    assert mdf!=null && rt!=null;
  }
  public <R> R match(Function<Id.GX<T>,R>gx, Function<Id.IT<T>,R>it){ return rt.match(gx, it); }
  public Id.IT<T> itOrThrow() { return this.match(gx->{ throw Bug.unreachable(); }, it->it); }
  public Id.GX<T> gxOrThrow() {
    return match(gx->gx, it->{ throw Bug.of("Expected GX, got IT"); });
  }
  public boolean isIt() { return this.match(gx->false, it->true); }
  public boolean isGX() { return this.match(gx->true, it->false); }
  public boolean isMdfX() { return this.match(gx->this.mdf().isMdf(), it->false); }
  public astFull.T toAstFullT() {
    return this.match(
      gx->new astFull.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(T::toAstFullT).toList();
        return new astFull.T(mdf(), new Id.IT<>(it.id(), ts));
      });
  }
  public Stream<Id.GX<T>> deepGXs() {
    return rt().match(Stream::of, it->it.ts().stream().flatMap(T::deepGXs));
  }

  public Stream<T> flatten() {
    return this.match(gx->Stream.of(this), it->
      Stream.concat(Stream.of(this), it.ts().stream().flatMap(T::flatten))
    );
  }

  public T withMdf(Mdf mdf){ return new T(mdf,rt); }

  public record Dec(DecId id, List<Id.GX<T>> gxs, Map<Id.GX<T>, Set<Mdf>> bounds, E.Lambda lambda, Optional<Pos> pos) implements HasPos, Id.Dec {
    public Dec{ assert gxs.size() == id.gen() && lambda!=null; }
    public ast.T.Dec withName(Id.DecId name) { return new ast.T.Dec(name,gxs,bounds,lambda,pos); }
    public ast.T.Dec withSelfName(String selfName) { return new ast.T.Dec(id,gxs,bounds,lambda.withSelfName(selfName),pos); }
    public ast.T.Dec withLambda(ast.E.Lambda lambda) { return new ast.T.Dec(id,gxs,bounds,lambda,pos); }

    public Id.IT<T> toIT(){
      return new Id.IT<>(//AstFull.T || Ast.T
        this.id(),
        this.gxs().stream().map(gx->new T(Mdf.mdf, new Id.GX<>(gx.name()))).toList()
      );
    }
    @Override public String toString() {
      if (bounds.values().stream().mapToLong(Collection::size).sum() == 0) {
        return "Dec[name="+ id +",gxs=["+gxs.stream().map(Id.GX::toString).collect(Collectors.joining(","))+"],lambda="+lambda+"]";
      }
      var boundsStr = bounds.entrySet().stream()
        .sorted(Comparator.comparing(a->a.getKey().name()))
        .map(kv->kv.getKey()+"="+kv.getValue().stream().sorted(Comparator.comparing(Enum::toString)).toList())
        .collect(Collectors.joining(","));
      return "Dec[name="+ id +",gxs=["+gxs.stream().map(Id.GX::toString).collect(Collectors.joining(","))+"],bounds={"+boundsStr+"},lambda="+lambda+"]";
    }
  }
}