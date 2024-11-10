package codegen.java;

import codegen.FlowSelector;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicCallable;
import magic.MagicImpls;
import magic.MagicTrait;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static magic.Magic.getLiteral;

public record JavaMagicImpls(
    MIRVisitor<String> gen,
    Function<MIR.MT,String> getTName,
    ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E,String> int_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        return lit
          .map(lambdaName->Long.parseLong(lambdaName.replace("_", ""), 10)+"L")
          .orElseGet(()->"((long)"+e.accept(gen, true)+")").describeConstable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.ofNullable(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".nat", 0))) {
          return instantiate().orElseThrow(); // only different at type level
        }
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".byte", 0))) {
          return "("+"(byte)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "rt.Str.fromJavaStr(Long.toString("+instantiate().orElseThrow()+"))";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate().orElseThrow()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate().orElseThrow()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate().orElseThrow()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate().orElseThrow()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate().orElseThrow(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".shiftRight", 1))) { return instantiate().orElseThrow()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".shiftLeft", 1))) { return instantiate().orElseThrow()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".xor", 1))) { return instantiate().orElseThrow()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".bitwiseAnd", 1))) { return instantiate().orElseThrow()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".bitwiseOr", 1))) { return instantiate().orElseThrow()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate().orElseThrow()+">"+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate().orElseThrow()+"<"+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate().orElseThrow()+">="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate().orElseThrow()+"<="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate().orElseThrow()+"=="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return "base._IntAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+", "+args.getFirst().accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".assertEq", 2))) {
          return "base._IntAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+", "+args.get(1).accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".hash", 1))) {
          return args.getFirst().accept(gen, true) + ".int$mut(" + instantiate().orElseThrow() + ")";
        }
        throw Bug.of("Expected magic to exist for: "+m);
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> nat(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        return lit
          .map(lambdaName->Long.parseUnsignedLong(lambdaName.replace("_", ""), 10)+"L")
          .orElseGet(()->"((long)"+e.accept(gen, true)+")").describeConstable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate().orElseThrow(); // only different at type level
        }
        if (m.equals(new Id.MethName(".nat", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".byte", 0))) {
          return "("+"(byte)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "rt.Str.fromJavaStr(Long.toUnsignedString("+instantiate().orElseThrow()+"))";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate().orElseThrow()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate().orElseThrow()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate().orElseThrow(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate().orElseThrow(); } // no-op for unsigned
        if (m.equals(new Id.MethName(".shiftRight", 1))) { return instantiate().orElseThrow()+">>>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".shiftLeft", 1))) { return instantiate().orElseThrow()+"<<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".xor", 1))) { return instantiate().orElseThrow()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".bitwiseAnd", 1))) { return instantiate().orElseThrow()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(".bitwiseOr", 1))) { return instantiate().orElseThrow()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>=0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<=0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")==0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return "base._NatAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+", "+args.getFirst().accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".assertEq", 2))) {
          return "base._NatAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+", "+args.get(1).accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".hash", 1))) {
          return args.getFirst().accept(gen, true) + ".int$mut(" + instantiate().orElseThrow() + ")";
        }
        if (m.equals(new Id.MethName(".offset", 1))) {
          return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true);
        }
        throw Bug.of("Expected magic to exist for: "+m);
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> float_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        return lit
          .map(lambdaName->Double.parseDouble(lambdaName.replace("_", ""))+"d")
          .orElseGet(()->"((double)"+e.accept(gen, true)+")").describeConstable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".nat", 0))) {
          return "("+"(long)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".byte", 0))) {
          return "("+"(byte)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "rt.Str.fromTrustedUtf8(rt.Str.wrap(rt.NativeRuntime.floatToStr("+instantiate().orElseThrow()+")))";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate().orElseThrow()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate().orElseThrow()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate().orElseThrow()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate().orElseThrow()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate().orElseThrow(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate().orElseThrow()+">"+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate().orElseThrow()+"<"+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate().orElseThrow()+">="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate().orElseThrow()+"<="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate().orElseThrow()+"=="+args.getFirst().accept(gen, true)+"?base.True_0.$self:base.False_0.$self)";
        }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return "base._FloatAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+", "+args.getFirst().accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".assertEq", 2))) {
          return "base._FloatAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+", "+args.get(1).accept(gen, true)+", null)";
        }
        //Float specifics
        if (m.equals(new Id.MethName(".round", 0))) { return "Math.round("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".ceil", 0))) { return "Math.ceil("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".floor", 0))) { return "Math.floor("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".isNaN", 0))) { return "(Double.isNaN("+instantiate().orElseThrow()+")?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".isInfinite", 0))) { return "(Double.isInfinite("+instantiate().orElseThrow()+")?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".isPosInfinity", 0))) { return "("+instantiate().orElseThrow()+" == Double.POSITIVE_INFINITY)?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".isNegInfinity", 0))) { return "("+instantiate().orElseThrow()+" == Double.NEGATIVE_INFINITY)?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".hash", 1))) {
          return args.getFirst().accept(gen, true) + ".int$mut(" + instantiate().orElseThrow() + ")";
        }
        throw Bug.of("Expected magic to exist for: "+m);
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> byte_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        return lit
          .map(lambdaName->"((byte)"+Long.parseUnsignedLong(lambdaName.replace("_", ""), 10)+")")
          .orElseGet(()->"((byte)"+e.accept(gen, true)+")").describeConstable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return "("+"(long)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".nat", 0))) {
          return "("+"(long)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".byte", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "rt.Str.fromJavaStr(Integer.toString(Byte.toUnsignedInt(%s)))".formatted(instantiate().orElseThrow());
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate().orElseThrow()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate().orElseThrow()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              byte base = %s; long exp = %s; byte res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate().orElseThrow(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate().orElseThrow(); } // no-op for unsigned
        if (m.equals(new Id.MethName(".shiftRight", 1))) { return byteToInt(instantiate().orElseThrow())+">>"+byteToInt(args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".shiftLeft", 1))) { return byteToInt(instantiate().orElseThrow())+"<<"+byteToInt(args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".xor", 1))) { return byteToInt(instantiate().orElseThrow())+"^"+byteToInt(args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseAnd", 1))) { return byteToInt(instantiate().orElseThrow())+"&"+byteToInt(args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".bitwiseOr", 1))) { return byteToInt(instantiate().orElseThrow())+"|"+byteToInt(args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Byte.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Byte.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Byte.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>=0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Byte.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<=0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Byte.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")==0?base.True_0.$self:base.False_0.$self)"; }
        if (m.equals(new Id.MethName(".assertEq", 1))) {
          return "base._ByteAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+", "+args.getFirst().accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".assertEq", 2))) {
          return "base._ByteAssertionHelper_0.assertEq$imm$fun("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+", "+args.get(1).accept(gen, true)+", null)";
        }
        if (m.equals(new Id.MethName(".hash", 1))) {
          return args.getFirst().accept(gen, true) + ".byte$mut(" + instantiate().orElseThrow() + ")";
        }
        if (m.equals(new Id.MethName(".offset", 1))) {
          return "((byte)"+byteToInt(instantiate().orElseThrow())+" + "+args.getFirst().accept(gen, true)+")";
        }
        throw Bug.of("Expected magic to exist for: "+m);
      }
      private String byteToInt(String raw) {
        return "Byte.toUnsignedInt(%s)".formatted(raw);
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> str(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        throw Bug.unreachable();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> utf8(MIR.E e) {
    return "rt.UTF8.$self"::describeConstable;
  }

  @Override public MagicTrait<MIR.E, String> utf16(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(".fromCodePoint", 1))) {
          return "rt.Str.fromJavaStr(new String(new int[]{(int)(long)%s}, 0, 1))"
            .formatted(args.getFirst().accept(gen, true))
            .describeConstable();
        }
        if (m.equals(new Id.MethName(".fromSurrogatePair", 2))) {
          return "rt.Str.fromJavaStr(new String(new int[]{(int)(long)%s, (int)(long)%s}, 0, 2))"
            .formatted(args.get(0).accept(gen, true), args.get(1).accept(gen, true))
            .describeConstable();
        }
        return MagicTrait.super.call(m, args, variants, expectedT);
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> asciiStr(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        throw Bug.todo();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> debug(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.of("rt.Debug.$self");
      }

      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> cheapHash(MIR.E e) {
    return "new rt.CheapHash()"::describeConstable;
  }

  @Override public MagicTrait<MIR.E, String> regexK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() { return Optional.empty(); }
      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          return Optional.of("new rt.Regex(%s)".formatted(args.getFirst().accept(gen, true)));
        }
        return MagicTrait.super.call(m, args, variants, expectedT);
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> refK(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }

      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          MIR.E x = args.getFirst();
          return Optional.of(String.format("""
            new base._MagicVarImpl_1(){
              private Object x$ = %s;
              public Object get$mut() { return this.x$; }
              public Object get$read() { return this.x$; }
              public Object swap$mut(Object x$) {
                var y$ = this.x$; this.x$ = x$; return y$;
                }
            }
            """, x.accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> isoPodK(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }

      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          MIR.E x = args.getFirst();
          return Optional.of(String.format("""
            new base.caps._MagicIsoPodImpl_1(){
              private Object x = %s;
              private boolean isAlive = true;

              public base.Bool_0 isAlive$read() { return this.isAlive ? base.True_0.$self : base.False_0.$self; }
              public Object peek$read(base.caps.IsoViewer_2 f) { return this.isAlive ? ((base.caps.IsoViewer_2)f).some$mut(this.x) : ((base.caps.IsoViewer_2)f).empty$mut(); }
              public Object $exclamation$mut() {
                if (!this.isAlive) {
                  base.Error_0.$self.msg$imm(rt.Str.fromJavaStr("Cannot consume an empty IsoPod."));
                  return null;
                }
                this.isAlive = false;
                return this.x;
              }
              public base.Void_0 next$mut(Object x) { this.isAlive = true; this.x = x; return new base.Void_0(){}; }
            }
            """, x.accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> assert_(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Assertion failed :(");
              System.exit(1);
              yield %s;
            }})
            """.formatted(getJavaRet(expectedT)));
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            (switch (1) { default -> {
              rt.NativeRuntime.printlnErr(%s.utf8());
              System.exit(1);
              yield %s;
            }})
            """, args.getFirst().accept(gen, true), getJavaRet(expectedT)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> errorK(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName("!", 1))) {
          return Optional.of("rt.Error.throwFearlessError(%s)".formatted(args.getFirst().accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> tryCatch(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.of("rt.Try.$self");
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> capTryCatchK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() { return Optional.empty(); }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.read), "#", 1))) {
          return Optional.of("new rt.CapTry(%s)".formatted(args.getFirst().accept(gen, true)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> listK(MIR.E e) {
    return ()->Optional.of("rt.ListK.$self");
  }

  @Override public MagicTrait<MIR.E, String> mapK(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {return Optional.empty();}
      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), ".hashMap", 2))) {
          return "new rt.LinkedHashMap(%s,%s)"
            .formatted(args.getFirst().accept(gen, true), args.get(1).accept(gen, true))
            .describeConstable();
        }
        return MagicTrait.super.call(m, args, variants, expectedT);
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> flowRange(MIR.E e) {
    return ()->Optional.of("rt.flows.Range.$self");
  }

  @Override public MagicTrait<MIR.E,String> pipelineParallelSinkK(MIR.E e) {
    return ()->Optional.of("rt.flows.pipelineParallel.PipelineParallelFlow.WrappedSinkK.$self");
  }

  @Override public MagicTrait<MIR.E, String> dataParallelFlowK(MIR.E e) {
    return ()->Optional.of("rt.flows.dataParallel.DataParallelFlowK.$self");
  }

  @Override public MagicTrait<MIR.E,String> objCap(Id.DecId target, MIR.E e) {
    JavaMagicImpls _this = this;
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        ObjCapImpl impl = null;
        if (target == Magic.RootCap) { impl = (ctx, m1, args1) -> null; }
        if (target == Magic.FEnv) { impl = env(); }
        if (target == Magic.UnrestrictedIO) { impl = io(); }
        if (target == Magic.FRandomSeed) { impl = randomSeed(); }
        assert impl != null;

        String res = impl.call(_this, m, args);
        return Optional.ofNullable(res);
      }

      private ObjCapImpl env() {
        return (ctx, m, args)->{
          if (m.equals(new Id.MethName("#", 1)) || m.equals(new Id.MethName(".io", 1))) {
            return """
              new base.caps.Env_0(){
                public base.LList_1 launchArgs$mut() { return base.FearlessMain.FAux.LAUNCH_ARGS; }
              }
              """;
          }
          return null;
        };
      }

      private ObjCapImpl io() {
        return (ctx, m, args) ->{
          if (m.equals(new Id.MethName("#", 1))) {
            return "rt.IO.$self";
          }
          return null;
        };
      }

      private ObjCapImpl randomSeed() {
        return (ctx, m, args) ->{
          if (m.equals(new Id.MethName("#", 1))) {
            return "rt.Random.SeedGenerator.$self";
          }
          return null;
        };
      }
    };
  }

  @Override public MagicCallable<MIR.E,String> variantCall(MIR.E e) {
    return (m, args, variants, expectedT)->{
      var call = (MIR.MCall) e;
      assert call.variant() == variants; // TODO: if this holds remove the variants param here
      var parallelConstr = FlowSelector.bestParallelConstr(call);

      if (isMagic(Magic.FlowK, call.recv())) {
        if (m.name().equals("#")) {
          var listKCall = new MIR.MCall(
            new MIR.CreateObj(Mdf.imm, Magic.ListK),
            new Id.MethName(Optional.of(Mdf.imm), "#", call.args().size()),
            call.args(),
            new MIR.MT.Plain(Mdf.mut, Magic.FList),
            Mdf.imm,
            EnumSet.of(MIR.MCall.CallVariant.Standard)
          );
          var listFlowCall = new MIR.MCall(
            listKCall,
            new Id.MethName(Optional.of(Mdf.mut), ".flow", 0),
            List.of(),
            call.t(),
            Mdf.mut,
            variants
          );
          return Optional.of(gen.visitMCall(listFlowCall, true));
        }
        if (m.name().equals(".range")) {
          assert parallelConstr.isPresent();
          return "rt.flows.FlowCreator.fromFlow(%s, %s)".formatted(
//            gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, new Id.DecId("base.flows._SeqFlow", 0)), true),
//            gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, Magic.PipelineParallelFlowK), true),
            gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, Magic.DataParallelFlowK), true),
            call.withVariants(EnumSet.of(MIR.MCall.CallVariant.Standard)).accept(gen, true)
          ).describeConstable();
        }
      }

      if (variants.contains(MIR.MCall.CallVariant.SafeMutSourceFlow)) {
        if (isMagic(Magic.FList, call.recv())) {
          var newVariants = EnumSet.copyOf(variants);
          newVariants.remove(MIR.MCall.CallVariant.SafeMutSourceFlow);
          return Optional.of(gen.visitMCall(new MIR.MCall(
            new MIR.CreateObj(Mdf.imm, Magic.SafeFlowSource),
            new Id.MethName(Optional.of(Mdf.imm), ".fromList", 1),
            List.of(call.recv()),
            call.t(),
            Mdf.imm,
            newVariants
          ), true));
        }
      }

      if (m.equals(new Id.MethName(".flow", 0)) || isMagic(Magic.SafeFlowSource, call.recv())) {
        if (parallelConstr.isPresent()) {
          var flowMethName = StringIds.$self.getMName(call.mdf(), call.name());
          var argList = args.stream()
            .map(arg->arg.accept(gen, true))
            .collect(Collectors.joining(", "));
          return "rt.flows.FlowCreator.fromFlow(%s, %s.%s(%s))".formatted(
            gen.visitCreateObj(new MIR.CreateObj(Mdf.imm, parallelConstr.get()), true),
            call.recv().accept(gen, true),
            flowMethName,
            argList
          ).describeConstable();
        }
      }

//      if (isMagic(Magic.SafeFlowSource, call.recv())) {
//        if (parallelConstr.isPresent()) {
//          String parFlow = gen.visitMCall(new MIR.MCall(
//            new MIR.CreateObj(Mdf.imm, parallelConstr.get()),
//            new Id.MethName(Optional.of(Mdf.imm), ".fromOp", 2),
//            List.of(
//              new MIR.MCall(
//                new MIR.CreateObj(Mdf.imm, Magic.SafeFlowSource),
//                new Id.MethName(Optional.of(Mdf.imm), m.name()+"'", 1),
//                args,
//                new MIR.MT.Plain(Mdf.mut, Magic.FlowOp),
//                Mdf.imm,
//                EnumSet.of(MIR.MCall.CallVariant.Standard)
//              ),
//              new MIR.CreateObj(Mdf.imm, new Id.DecId("base.Opt", 1)) // TODO: list size
//            ),
//            new MIR.MT.Plain(Mdf.mut, new Id.DecId("base.flows.Flow", 1)),
//            Mdf.imm,
//            variants
//          ), true);
//          return Optional.of(parFlow);
//        }
//      }

      System.err.println("Warning: No magic handler found for: "+e+"\nFalling back to Fearless implementation.");
      return Optional.empty();
    };
  }

  @Override public MagicTrait<MIR.E,String> abort(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Program aborted at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield %s;
            }})
            """.formatted(getJavaRet(expectedT)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> magicAbort(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("No magic code was found at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield %s;
            }})
            """.formatted(getJavaRet(expectedT)));
        }
        return Optional.empty();
      }
    };
  }

  private String getJavaRet(MIR.MT expectedT) {
    String ret = getTName.apply(expectedT);
    return switch (ret) {
      case "Long", "long", "Double", "double" -> "(%s) 0".formatted(ret);
      default -> "(%s) null".formatted(ret);
    };
  }
}
