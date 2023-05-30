package magic;

import id.Id;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Magic {
  public static final Id.DecId Main = new Id.DecId("base.Main", 1);
  public static final Id.DecId ImmMain = new Id.DecId("base.Main", 0);
  public static final Id.DecId NoMutHyg = new Id.DecId("base.NoMutHyg", 1);
  public static final Id.DecId Sealed = new Id.DecId("base.Sealed", 0);
  public static final Id.DecId UInt = new Id.DecId("base.UInt", 0);
  public static final Id.DecId Int = new Id.DecId("base.Int", 0);
  public static final Id.DecId Float = new Id.DecId("base.Float", 0);
  public static final Id.DecId Str = new Id.DecId("base.Str", 0);
  public static final Id.DecId RefK = new Id.DecId("base.Ref", 0);
  public static final Id.DecId Assert = new Id.DecId("base.Assert", 0);
  public static final Id.DecId IO = new Id.DecId("base.caps.IO", 0);

  public static Optional<Id.IT<astFull.T>> resolve(String name) {
    var isLiteral  = !name.isEmpty() && MagicImpls.isLiteral(name);
    if(isLiteral){ return Optional.of(new Id.IT<>(name, List.of())); }
    return switch(name){
//      case noMutHygName -> Optional.of(new Id.IT<>(new Id.DecId(noMutHygName, 0), List.of()));
      default -> Optional.empty();
    };
  }

  public static astFull.T.Dec getFullDec(Function<Id.DecId, astFull.T.Dec> resolve, Id.DecId id) {
    var base = _getDec(resolve, id);
    return base.map(b->b.withName(id)).orElse(null);
  }

  public static ast.T.Dec getDec(Function<Id.DecId, ast.T.Dec> resolve, Id.DecId id) {
    var base = _getDec(resolve, id);
    return base.map(b->{
      assert b.lambda().its().size() == 2 : b.lambda(); // instance, kind
      return b.withName(id).withLambda(b.lambda().withITs(List.of(
        new Id.IT<>(id, List.of()),
        b.lambda().its().get(1)
      )));
    }).orElse(null);
  }

  private static <T> Optional<T> _getDec(Function<Id.DecId, T> resolve, Id.DecId id) {
    if ((Character.isDigit(id.name().charAt(0)) || id.name().startsWith("-")) && id.gen() == 0) {
      T baseDec;
      if (id.name().chars().anyMatch(c->c=='.')) {
        baseDec = resolve.apply(new Id.DecId("base._FloatInstance", 0));
      } else if (id.name().endsWith("u")) {
        baseDec = resolve.apply(new Id.DecId("base._UIntInstance", 0));
      } else {
        baseDec = resolve.apply(new Id.DecId("base._IntInstance", 0));
      }
      return Optional.of(baseDec);
    }
    if (id.name().charAt(0) == '"') {
      return Optional.of(resolve.apply(new Id.DecId("base._StrInstance", 0)));
    }
    return Optional.empty();
  }
}
