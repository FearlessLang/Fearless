package ast;

import id.Id;
import id.Id.DecId;
import id.Mdf;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import files.HasPos;
import files.Pos;
import visitors.TypeVisitor;

public record T(Mdf mdf, Id.RT<T> rt) implements Id.Ty {
  @Override public String toString(){
    if (mdf.isMdf()) { return rt.toString(); }
    return mdf+" "+rt;
  }
  public T{ assert mdf!=null && rt!=null; }
  public <R> R match(Function<Id.GX<T>,R>gx, Function<Id.IT<T>,R>it){ return rt.match(gx, it); }
  public Id.IT<T> itOrThrow() { return this.match(gx->{ throw Bug.unreachable(); }, it->it); }
  public Id.GX<T> gxOrThrow() {
    return match(gx->gx, it->{ throw Bug.of("Expected GX, got IT: "+it); });
  }
  public boolean isIt() { return this.match(gx->false, it->true); }
  public boolean isGX() { return this.match(gx->true, it->false); }
  public boolean isMdfX() { return this.match(gx->this.mdf().isMdf(), it->false); }
  public astFull.T toAstFullT() {
    return this.match(
      gx->new astFull.T(mdf(), new Id.GX<>(gx.name())),
      it->{
        var ts = it.ts().stream().map(T::toAstFullT).toList();
        return new astFull.T(mdf(), new Id.IT<>(it.name(), ts));
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

  public <R> R accept(TypeVisitor<T, R> visitor) {
    return this.match(gx->switch (mdf) {
      case mdf -> visitor.visitX(gx);
      case readImm -> visitor.visitReadImm(gx);
      case iso,imm,mut,mutH,read,readH -> visitor.visitRCX(mdf, gx);
      case recMdf -> throw Bug.unreachable();
    }, it->visitor.visitLiteral(mdf, it));
  }
  public record Dec(E.Lambda lambda) implements HasPos, Id.Dec {
    public DecId name() { return lambda.id().id(); }
    public List<Id.GX<T>> gxs(){ return lambda.id().gens(); }
    public Map<Id.GX<T>, Set<Mdf>> bounds(){ return lambda.id().bounds(); }
    public Optional<Pos> pos(){ return lambda.pos(); }

    public Dec withLambda(ast.E.Lambda lambda){ return new Dec(lambda); }
    public Id.IT<T> toIT(){
      return new Id.IT<>(
        this.lambda().id().id(),
        this.lambda().id().gens().stream()
          .map(gx->new T(Mdf.mdf, gx)).toList()
        );
    }
  }
}