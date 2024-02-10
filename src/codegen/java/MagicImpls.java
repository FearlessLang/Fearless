package codegen.java;

import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static magic.MagicImpls.getLiteral;

public record MagicImpls(JavaCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E,String> int_(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Long.parseLong(lambdaName.replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.ofNullable(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".uint", 0))) {
          return instantiate(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Long.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> uint(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "UInt");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Long.toUnsignedString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate(); } // no-op for unsigned
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")>0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")<0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")>=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")<=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.getFirst().accept(gen, true)+")==0?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> float_(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }

      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        try {
          return lit
            .map(lambdaName->Double.parseDouble(lambdaName.replace("_", ""))+"d")
            .orElseGet(()->"((double)"+e.accept(gen, true)+")");
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<MIR.E> args) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".uint", 0))) {
          return "("+"(long)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Double.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)";
        }
        //Float specifics
        if (m.equals(new Id.MethName(".round", 0))) { return "Math.round("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".ceil", 0))) { return "Math.ceil("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".floor", 0))) { return "Math.floor("+instantiate()+")"; }
        if (m.equals(new Id.MethName(".isNaN", 0))) { return "(Double.isNaN("+instantiate()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isInfinite", 0))) { return "(Double.isInfinite("+instantiate()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isPosInfinity", 0))) { return "("+instantiate()+" == Double.POSITIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isNegInfinity", 0))) { return "("+instantiate()+" == Double.NEGATIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> str(MIR.E e) {
    var name = e.t().itOrThrow();
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lit = getLiteral(p, name.name());
        return lit.orElseGet(()->"((String)"+e.accept(gen, true)+")");
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName(".size", 0))) { return Optional.of(instantiate()+".length()"); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of("("+instantiate()+".isEmpty()?base.True_0._$self:base.False_0._$self)"); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of("("+instantiate()+"+"+args.getFirst().accept(gen, true)+")"); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of("("+instantiate()+".equals("+args.getFirst().accept(gen, true)+")?base.True_0._$self:base.False_0._$self)");
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return Optional.of("base.$95StrHelpers_0._$self.assertEq$imm$("+instantiate()+", "+args.getFirst().accept(gen, true)+")");
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> debug(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> refK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> isoPodK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> assert_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() {
        return e.t().itOrThrow();
      }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }

      @Override
      public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Assertion failed :(");
              System.exit(1);
              yield null;
            }})
            """);
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            (switch (1) { default -> {
              System.err.println(%s);
              System.exit(1);
              yield null;
            }})
            """, args.getFirst().accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> errorK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> tryCatch(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> pipelineParallelSinkK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> objCap(Id.DecId magicTrait, MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> variantCall(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E,String> abort(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E,String> magicAbort(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return e.t().itOrThrow(); }

      @Override public String instantiate() {
        return e.accept(gen, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("No magic code was found at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield (Object)null;
            }})
            """);
        }
        return Optional.empty();
      }
    };
  }
}
