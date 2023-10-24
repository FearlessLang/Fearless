package codegen.java;

import ast.E;
import ast.Program;
import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicTrait;
import program.typesystem.EMethTypeSystem;
import utils.Bug;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static magic.MagicImpls.isLiteral;

public record MagicImpls(JavaCodegen gen, Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> int_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        try {
          return isLiteral(lambdaName) ? Long.parseLong(lambdaName)+"L" : "((long)"+e.accept(gen)+")";
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lambdaName, "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.ofNullable(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.get(0).accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "("+instantiate()+"=="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> uint(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        if (isLiteral(lambdaName)) {
          try {
            long l = Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1));
            return l+"L";
          } catch (NumberFormatException err) {
            throw Fail.invalidNum(lambdaName, "UInt");
          }
        }
        return "((long)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return "Long.divideUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.equals(new Id.MethName("%", 1))) { return "Long.remainderUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
          (switch (1) { default -> {
              long base = %s; long exp = %s; long res = base;
              if (exp == 0) { yield 1L; }
              for(; exp > 1; exp--) { res *= base; }
              yield res;
            }})
          """, instantiate(), args.get(0).accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return instantiate(); } // no-op for unsigned
        if (m.equals(new Id.MethName(">>", 1))) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("<<", 1))) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("^", 1))) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("&", 1))) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("|", 1))) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName(">", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<=0?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")==0?base.True_0._$self:base.False_0._$self)"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> float_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        if (isLiteral(lambdaName)) {
          try {
            double l = Double.parseDouble(lambdaName);
            return l+"d";
          } catch (NumberFormatException err) {
            throw Fail.invalidNum(lambdaName, "Float");
          }
        }
        return "((double)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".int", 0)) || m.equals(new Id.MethName(".uint", 0))) {
          return "("+"(long)"+instantiate()+")";
        }
        if (m.equals(new Id.MethName(".toImm", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return "Double.toString("+instantiate()+")";
        }
        if (m.equals(new Id.MethName("+", 1))) { return instantiate()+" + "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("-", 1))) { return instantiate()+" - "+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("*", 1))) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("/", 1))) { return instantiate()+"/"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("%", 1))) { return instantiate()+"%"+args.get(0).accept(gen); }
        if (m.equals(new Id.MethName("**", 1))) { return String.format("Math.pow(%s, %s)", instantiate(), args.get(0).accept(gen)); }
        if (m.equals(new Id.MethName(".abs", 0))) { return "Math.abs("+instantiate()+")"; }
        if (m.equals(new Id.MethName(">", 1))) { return "("+instantiate()+">"+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<", 1))) { return "("+instantiate()+"<"+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName(">=", 1))) { return "("+instantiate()+">="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("<=", 1))) { return "("+instantiate()+"<="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)"; }
        if (m.equals(new Id.MethName("==", 1))) {
          return "("+instantiate()+"=="+args.get(0).accept(gen)+"?base.True_0._$self:base.False_0._$self)";
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

  @Override public MagicTrait<String> str(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        return isLiteral(lambdaName) ? lambdaName : "((String)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".len", 0))) { return Optional.of(instantiate()+".length()"); }
        if (m.equals(new Id.MethName(".isEmpty", 0))) { return Optional.of("("+instantiate()+".isEmpty()?base.True_0._$self:base.False_0._$self)"); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName(".toImm", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of("("+instantiate()+"+"+args.get(0).accept(gen)+")"); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of("("+instantiate()+".equals("+args.get(0).accept(gen)+")?base.True_0._$self:base.False_0._$self)");
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> debug(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
//        java.util.Arrays.stream(m.getClass().getDeclaredMethods())
//          .filter(meth->
//            meth.getName().equals("str$")
//              && meth.getReturnType().equals(String.class)
//              && meth.getParameterCount() == 0)
//          .findAny().ifPresent(msf -> msf.invoke());
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.get(0);
          return Optional.of(String.format("""
            (switch (1) { default -> {
              var x = %s;
              Object xObj = x;
              var strMethod = java.util.Arrays.stream(xObj.getClass().getMethods())
                .filter(meth->
                  meth.getName().equals("str$read$")
                  && meth.getReturnType().equals(String.class)
                  && meth.getParameterCount() == 0)
                .findAny();
              if (strMethod.isPresent()) {
                try {
                  System.out.println(strMethod.get().invoke(x));
                } catch(java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException err) {
                  System.out.println(x);
                }
              } else {
                System.out.println(x);
              }
              yield x;
            }})
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> refK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1))) {
          var x = args.get(0);
          return Optional.of(String.format("""
            new base.Ref_1(){
              protected Object x = %s;
              public Object get$mut$() { return this.x; }
              public Object get$read$() { return this.x; }
              public Object swap$mut$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
            }
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> isoPodK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.get(0);
          return Optional.of(String.format("""
            new base$46caps.IsoPod_1(){
              protected Object x = %s;
              protected boolean isAlive = true;
              
              public base.Bool_0 isAlive$readOnly$() { return this.isAlive ? base.True_0._$self : base.False_0._$self; }
              public Object peek$readOnly$(Object f) { return this.isAlive ? ((base$46caps.IsoViewer_2)f).some$mut$(this.x) : ((base$46caps.IsoViewer_2)f).empty$mut$(); }
              public Object $33$mut$() {
                if (!this.isAlive) {
                  base.Error_0._$self.str$imm$("Cannot consume an empty IsoPod.");
                  return null;
                }
                this.isAlive = false;
                return this.x;
              }
              public base.Void_0 next$mut$(Object x) { this.isAlive = true; this.x = x; return new base.Void_0(){}; }
            }
            """, x.accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> assert_(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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
            """, args.get(0).accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> abort(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            (switch (1) { default -> {
              System.err.println("Program aborted at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield (Object)null;
            }})
            """);
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> magicAbort(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
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

  @Override public MagicTrait<String> errorK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("!", 1))) {
          return Optional.of("""
            (switch (1) {
              default -> throw new FearlessError(%s);
              case 2 -> 0;
            })
            """.formatted(args.get(0).accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> tryCatch(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("#", 1))) {
          return Optional.of("""
            (switch (1) { default -> {
              try { yield base.Res_0._$self.ok$imm$(%s.$35$read$()); }
              catch(FearlessError _$err) { yield base.Res_0._$self.err$imm$(_$err.info); }
            }})
            """.formatted(args.get(0).accept(gen)));
        }
//        if (m.equals(new Id.MethName("#", 2))) {
//          return Optional.of("""
//            (switch (1) { default -> {
//              try { yield %s.$35$read$(); }
//              catch(FearlessError _$err) { yield %s.$35$mut$(_$err.info); }
//            }})
//            """.formatted(args.get(0).accept(gen), args.get(1).accept(gen)));
//        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> objCap(Id.DecId target, MIR.Lambda l, MIR e) {
    var _this = this;
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        ObjCapImpl impl = null;
        if (target == Magic.RootCap) { impl = (ctx, m1, args1, gamma1) -> null; }
        if (target == Magic.IO) { impl = io(); }
        if (target == Magic.FEnv) { impl = env(); }
        assert impl != null;

        var res = impl.call(_this, m, args, gamma);
        return Optional.ofNullable(res);
      }

      private ObjCapImpl io() {
        return (ctx, m, args, gamma) ->{
          if (m.equals(new Id.MethName(".print", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.out.print(%s);
                yield base.Void_0._$self;
              }})
              """, args.get(0).accept(gen));
          }
          if (m.equals(new Id.MethName(".println", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.out.println(%s);
                yield base.Void_0._$self;
              }})
              """, args.get(0).accept(gen));
          }
          if (m.equals(new Id.MethName(".printErr", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.err.print(%s);
                yield base.Void_0._$self;
              }})
              """, args.get(0).accept(gen));
          }
          if (m.equals(new Id.MethName(".printlnErr", 1))) {
            return String.format("""
              (switch (1) { default -> {
                System.err.println(%s);
                yield base.Void_0._$self;
              }})
              """, args.get(0).accept(gen));
          }
          return null;
        };
      }
      private ObjCapImpl env() {
        return (ctx, m, args, gamma) ->{
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
    };
  }
}
