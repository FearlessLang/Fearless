package codegen.go;

import codegen.MIR;
import failure.Fail;
import id.Id;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static magic.Magic.getLiteral;

public record GoMagicImpls(PackageCodegen gen, ast.Program p) implements magic.MagicImpls<GoMagicImpls.Res> {
  public record Res(String output, Set<String> imports) {
    public Res(String output) { this(output, Set.of()); }
    public Optional<Res> opt() { return Optional.of(this); }
  }
  @Override public MagicTrait<MIR.E, Res> int_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<Res> instantiate() {
        var lit = getLiteral(p, name);
        return new Res(lit
          .map(lambdaName->"int64("+(lambdaName.startsWith("+") ? lambdaName.substring(1) : lambdaName)+")")
          .map(lambdaName->"int64("+Long.parseLong(lambdaName.replace("_", ""), 10)+"L)")
          .orElseGet(()->"((long)"+e.accept(gen, true)+")")).opt();
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private Res _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".nat", 0))) {
          return new Res("uint64("+instantiate().orElseThrow()+")");
        }
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return new Res("float64("+instantiate().orElseThrow().output()+")");
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return new Res("strconv.FormatInt("+instantiate().orElseThrow().output()+", 10)", Set.of("strconv"));
        }
        if (m.equals(new Id.MethName("+", 1))) { return new Res(instantiate().orElseThrow().output()+" + "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("-", 1))) { return new Res(instantiate().orElseThrow().output()+" - "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("*", 1))) { return new Res(instantiate().orElseThrow().output()+"*"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("/", 1))) { return new Res(instantiate().orElseThrow().output()+"/"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("%", 1))) { return new Res(instantiate().orElseThrow().output()+"%"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("**", 1))) { return new Res(String.format("baseφrtφPow(%s, %s)", instantiate().orElseThrow().output(), args.getFirst().accept(gen, true)), Set.of()); }
        if (m.equals(new Id.MethName(".abs", 0))) { return new Res("baseφrtφAbs("+instantiate().orElseThrow().output()+")", Set.of("rt/rt/baseφrt")); }
        if (m.equals(new Id.MethName(".shiftRight", 1))) { return new Res(instantiate().orElseThrow().output()+">>"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".shiftLeft", 1))) { return new Res(instantiate().orElseThrow().output()+"<<"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".xor", 1))) { return new Res(instantiate().orElseThrow().output()+"^"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseAnd", 1))) { return new Res(instantiate().orElseThrow().output()+"&"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseOr", 1))) { return new Res(instantiate().orElseThrow().output()+"|"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(">", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+">"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<", 1))) { new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"<"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName(">=", 1))) { new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+">="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<=", 1))) { new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"<="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("==", 1))) { new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"=="+args.getFirst().accept(gen, true)+")", Set.of()); }
        throw Bug.unreachable();
      }
    };
  }

  // TODO: golang computes constant arithmetic at compile-time and will throw for invalid Nats in that case, which is annoyingly different behaviour than Java
  @Override public MagicTrait<MIR.E, Res> nat(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<Res> instantiate() {
        var lit = getLiteral(p, name);
        return new Res(lit
          .map(lambdaName->"uint64("+Long.parseUnsignedLong(lambdaName.replace("_", ""), 10)+")")
          .orElseGet(()->"uint64("+e.accept(gen, true)+")")).opt();
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private Res _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return new Res("uint64("+instantiate().orElseThrow().output()+")");
        }
        if (m.equals(new Id.MethName(".nat", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return new Res("float64("+instantiate().orElseThrow().output()+")");
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return new Res("strconv.FormatUint("+instantiate().orElseThrow().output()+", 10)", Set.of("strconv"));
        }
        if (m.equals(new Id.MethName("+", 1))) { return new Res(instantiate().orElseThrow().output()+" + "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("-", 1))) { return new Res(instantiate().orElseThrow().output()+" - "+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("*", 1))) { return new Res(instantiate().orElseThrow().output()+"*"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("/", 1))) { return new Res(instantiate().orElseThrow().output()+"/"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("%", 1))) { return new Res(instantiate().orElseThrow().output()+"%"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName("**", 1))) { return new Res(String.format("baseφrtφPow(%s, %s)", instantiate().orElseThrow().output(), args.getFirst().accept(gen, true)), Set.of()); }
        if (m.equals(new Id.MethName(".abs", 0))) { return new Res("baseφrtφAbs("+instantiate().orElseThrow().output()+")", Set.of("rt/rt/baseφrt")); }
        if (m.equals(new Id.MethName(".shiftRight", 1))) { return new Res(instantiate().orElseThrow().output()+">>"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".shiftLeft", 1))) { return new Res(instantiate().orElseThrow().output()+"<<"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".xor", 1))) { return new Res(instantiate().orElseThrow().output()+"^"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseAnd", 1))) { return new Res(instantiate().orElseThrow().output()+"&"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseOr", 1))) { return new Res(instantiate().orElseThrow().output()+"|"+args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(">", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+">"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"<"+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName(">=", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+">="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("<=", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"<="+args.getFirst().accept(gen, true)+")", Set.of()); }
        if (m.equals(new Id.MethName("==", 1))) { return new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+"=="+args.getFirst().accept(gen, true)+")", Set.of()); }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> float_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<Res> instantiate() {
        var lit = getLiteral(p, name);
        return new Res(lit
          .map(lambdaName->"float64("+Double.parseDouble(lambdaName.replace("_", ""))+")")
          .orElseGet(()->"float64("+e.accept(gen, true)+")")).opt();
      }

      @Override public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        throw Bug.todo();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> byte_(MIR.E e) {
    throw Bug.todo("Byte magic for Go");
  }

  @Override public MagicTrait<MIR.E, Res> str(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<Res> instantiate() {
        var lit = getLiteral(p, name);
        return new Res(lit.orElseGet(()->"string("+e.accept(gen, true)+")")).opt();
      }

      @Override
      public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(".size", 0))) { return Optional.of(new Res("uint64(len("+Optional.of(instantiate().orElseThrow().output()+"))"))); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of(new Res("baseφrtφConvertBool(len("+instantiate().orElseThrow().output()+") == 0)", Set.of())); }
        if (m.equals(new Id.MethName(".str", 0))) { return instantiate(); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return instantiate(); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of(new Res("("+instantiate().orElseThrow().output()+"+"+args.getFirst().accept(gen, true)+")")); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of(new Res("baseφrtφConvertBool("+instantiate().orElseThrow().output()+" == "+args.getFirst().accept(gen, true)+")", Set.of()));
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
//          return Optional.of("base._StrHelpers_0.$self.assertEq$imm("+instantiate()+", "+args.getFirst().accept(gen, true)+")");
          throw Bug.todo(); // TODO
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> asciiStr(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, Res> bool(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<Res> instantiate() {
        return Optional.empty();
      }

      @Override
      public Optional<Res> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        // inline .if for performance reasons, it's 274% faster than calling the .if method
        // we can inline this further (i.e. the .then/.else expressions themselves) in the future for a bigger
        // improvement but we will need to make that optimisation more restrictive (ThenElse/1 is an open type)
        if (m.equals(new Id.MethName("?", 1)) || m.equals(new Id.MethName(".if", 1))) {
          var ret = gen.getName(expectedT);
          return Optional.of(new Res("""
            (func () %s {
              var thenElse = %s
              if %s == (φbaseφTrue_0Impl{}) {
                return thenElse.φthen_0_immφ().(%s)
              } else {
                return thenElse.φelse_0_immφ().(%s)
              }
            })()
            """.formatted(ret, args.getFirst().accept(gen, true), e.accept(gen, true), ret, ret)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, Res> debug(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> refK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> isoPodK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> assert_(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> cheapHash(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicCallable<MIR.E, Res> variantCall(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, Res> regexK(MIR.E e) {
    throw Bug.todo();
  }
}
