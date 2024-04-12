package codegen.java;

import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicCallable;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static magic.MagicImpls.getLiteral;

public record JavaMagicImpls(JavaCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E,String> int_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->Long.parseLong(lambdaName.replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")").describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.ofNullable(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".uint", 0))) {
          return instantiate().orElseThrow(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate().orElseThrow()+")";
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
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate().orElseThrow()+">>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate().orElseThrow()+"<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate().orElseThrow()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate().orElseThrow()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate().orElseThrow()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate().orElseThrow()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate().orElseThrow()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate().orElseThrow()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate().orElseThrow()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate().orElseThrow()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> uint(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1).replace("_", ""), 10)+"L")
            .orElseGet(()->"((long)"+e.accept(gen, true)+")").describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "UInt");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate().orElseThrow(); // only different at type level
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return "("+"(double)"+instantiate().orElseThrow()+")";
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
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate().orElseThrow()+">>>"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate().orElseThrow()+"<<<"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate().orElseThrow()+"^"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate().orElseThrow()+"&"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate().orElseThrow()+"|"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")>=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")<=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate().orElseThrow()+","+args.getFirst().accept(gen, true)+")==0?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<MIR.E,String> float_(MIR.E e) {
    var name = e.t().name().orElseThrow();
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        var lit = getLiteral(p, name);
        try {
          return lit
            .map(lambdaName->Double.parseDouble(lambdaName.replace("_", ""))+"d")
            .orElseGet(()->"((double)"+e.accept(gen, true)+")").describeConstable();
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lit.orElse(name.toString()), "Float");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of(_call(m, args));
      }
      private String _call(Id.MethName m, List<? extends MIR.E> args) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".uint", 0))) {
          return "("+"(long)"+instantiate().orElseThrow()+")";
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate().orElseThrow();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "rt.Str.fromJavaStr(Double.toString("+instantiate().orElseThrow()+"))";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate().orElseThrow()+" + "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate().orElseThrow()+" - "+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate().orElseThrow()+"*"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate().orElseThrow()+"/"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate().orElseThrow()+"%"+args.getFirst().accept(gen, true); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate().orElseThrow(), args.getFirst().accept(gen, true)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate().orElseThrow()+">"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate().orElseThrow()+"<"+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate().orElseThrow()+">="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate().orElseThrow()+"<="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate().orElseThrow()+"=="+args.getFirst().accept(gen, true)+"?base.True_0._$self:base.False_0._$self)";
        }
        //Float specifics
        if (m.equals(new Id.MethName(".round", 0))) { return "Math.round("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".ceil", 0))) { return "Math.ceil("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".floor", 0))) { return "Math.floor("+instantiate().orElseThrow()+")"; }
        if (m.equals(new Id.MethName(".isNaN", 0))) { return "(Double.isNaN("+instantiate().orElseThrow()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isInfinite", 0))) { return "(Double.isInfinite("+instantiate().orElseThrow()+")?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isPosInfinity", 0))) { return "("+instantiate().orElseThrow()+" == Double.POSITIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(".isNegInfinity", 0))) { return "("+instantiate().orElseThrow()+" == Double.NEGATIVE_INFINITY)?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
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

  @Override public MagicTrait<MIR.E,String> debug(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }

      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.getFirst();
          return Optional.of(String.format("""
            (switch (1) { default -> {
              var x = %s;
              Object xObj = x;
              var strMethod = java.util.Arrays.stream(xObj.getClass().getMethods())
                .filter(meth->
                  (meth.getName().equals("str$read$") || meth.getName().equals("str$readOnly$"))
                  && meth.getReturnType().equals(rt.Str.class)
                  && meth.getParameterCount() == 0)
                .findAny();
              if (strMethod.isPresent()) {
                try {
                  rt.NativeRuntime.println(((rt.Str)strMethod.get().invoke(x)).utf8());
                } catch(java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException err) {
                  System.out.println(x);
                }
              } else {
                System.out.println(x);
              }
              yield x;
            }})
            """, x.accept(gen, true)));
        }
        return Optional.empty();
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
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base.$95MagicRefImpl_1(){
              private Object x = %s;
              public Object get$mut$() { return this.x; }
              public Object get$read$() { return this.x; }
              public Object swap$mut$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
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
          var x = args.getFirst();
          return Optional.of(String.format("""
            new base$46caps.$95MagicIsoPodImpl_1(){
              private Object x = %s;
              private boolean isAlive = true;

              public base.Bool_0 isAlive$readOnly$() { return this.isAlive ? base.True_0._$self : base.False_0._$self; }
              public Object peek$readOnly$(userCode.FProgram.base$46caps.IsoViewer_2 f) { return this.isAlive ? ((base$46caps.IsoViewer_2)f).some$mut$(this.x) : ((base$46caps.IsoViewer_2)f).empty$mut$(); }
              public Object $33$mut$() {
                if (!this.isAlive) {
                  base.Error_0._$self.msg$imm$(rt.Str.fromJavaStr("Cannot consume an empty IsoPod."));
                  return null;
                }
                this.isAlive = false;
                return this.x;
              }
              public base.Void_0 next$mut$(Object x) { this.isAlive = true; this.x = x; return new base.Void_0(){}; }
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
        return Optional.of("rt.Try._$self");
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> capTryCatch(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.of("rt.CapTry._$self");
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> pipelineParallelSinkK(MIR.E e) {
    return new MagicTrait<>() {

      @Override public Optional<String> instantiate() {
        return Optional.of("rt.PipelineParallelFlow.WrappedSinkK._$self");
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<MIR.E,String> objCap(Id.DecId target, MIR.E e) {
    var _this = this;
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.empty();
      }
      @Override public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        ObjCapImpl impl = null;
        if (target == Magic.RootCap) { impl = (ctx, m1, args1) -> null; }
        if (target == Magic.FEnv) { impl = env(); }
        if (target == Magic.FIO) { impl = io(); }
        if (target == Magic.FRandomSeed) { impl = randomSeed(); }
        assert impl != null;

        var res = impl.call(_this, m, args);
        return Optional.ofNullable(res);
      }

      private ObjCapImpl env() {
        return (ctx, m, args)->{
          if (m.equals(new Id.MethName("#", 1)) || m.equals(new Id.MethName(".io", 1))) {
            return """
              new base$46caps.Env_0(){
                public base.LList_1 launchArgs$mut$() { return FAux.LAUNCH_ARGS; }
              }
              """;
          }
          return null;
        };
      }

      private ObjCapImpl io() {
        return (ctx, m, args) ->{
          if (m.equals(new Id.MethName("#", 1))) {
            return "rt.IO._$self";
          }
          return null;
        };
      }

      private ObjCapImpl randomSeed() {
        return (ctx, m, args) ->{
          if (m.equals(new Id.MethName("#", 1))) {
            return "rt.Random.SeedGenerator._$self";
          }
          return null;
        };
      }
    };
  }

  @Override public MagicCallable<MIR.E,String> variantCall(MIR.E e) {
    return (m, args, variants, expectedT)->{
      var call = (MIR.MCall) e;
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

        }
      }

      if (isMagic(Magic.FList, call.recv())) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.mut), ".flow", 0))) {
          if (variants.contains(MIR.MCall.CallVariant.SafeMutSourceFlow)) {
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
          if (variants.contains(MIR.MCall.CallVariant.Standard)) {
            return Optional.empty();
          }
        } else if (m.name().equals(".flow")) {
          if (variants.contains(MIR.MCall.CallVariant.PipelineParallelFlow)) {
            var parFlow = gen.visitMCall(new MIR.MCall(
              new MIR.CreateObj(Mdf.imm, Magic.PipelineParallelFlowK),
              new Id.MethName(Optional.of(Mdf.imm), ".fromOp", 2),
              List.of(
                new MIR.MCall(
                  call.recv(),
                  new Id.MethName(call.name().mdf(), "._flow"+call.mdf(), 0),
                  List.of(),
                  new MIR.MT.Plain(Mdf.mut, Magic.FlowOp),
                  call.mdf(),
                  EnumSet.of(MIR.MCall.CallVariant.Standard)
                ),
                new MIR.CreateObj(Mdf.imm, new Id.DecId("base.Opt", 1)) // TODO: list size
              ),
              MIR.MT.of(new T(Mdf.mut, new Id.IT<>("base.flows.Flow", ((MIR.MT.Usual)call.t()).it().ts()))),
              Mdf.imm,
              variants
            ), true);
            return Optional.of(parFlow);
          }
        }
      }

      if (isMagic(Magic.SafeFlowSource, call.recv())) {
        if (variants.contains(MIR.MCall.CallVariant.PipelineParallelFlow)) {
          var parFlow = gen.visitMCall(new MIR.MCall(
            new MIR.CreateObj(Mdf.imm, Magic.PipelineParallelFlowK),
            new Id.MethName(Optional.of(Mdf.imm), ".fromOp", 2),
            List.of(
              new MIR.MCall(
                new MIR.CreateObj(Mdf.imm, Magic.SafeFlowSource),
                new Id.MethName(Optional.of(Mdf.imm), m.name()+"'", 1),
                args,
                new MIR.MT.Plain(Mdf.mut, Magic.FlowOp),
                Mdf.imm,
                EnumSet.of(MIR.MCall.CallVariant.Standard)
              ),
              new MIR.CreateObj(Mdf.imm, new Id.DecId("base.Opt", 1)) // TODO: list size
            ),
            new MIR.MT.Plain(Mdf.mut, new Id.DecId("base.flows.Flow", 1)),
            Mdf.imm,
            variants
          ), true);
          return Optional.of(parFlow);
        }
      }

      if (isMagic(Magic.PipelineParallelFlowK, call.recv())) {
        return Optional.empty();
      }

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
    var ret = gen.getName(expectedT);
    return switch (ret) {
      default -> "(%s) null".formatted(ret);
      case "Long", "long", "Double", "double" -> "(%s) 0".formatted(ret);
    };
  }
}
