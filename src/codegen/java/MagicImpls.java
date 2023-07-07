package codegen.java;

import ast.Program;
import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import magic.Magic;
import magic.MagicTrait;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static magic.MagicImpls.isLiteral;

public record MagicImpls(JavaCodegen gen, Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> int_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        try {
          return isLiteral(lambdaName) ? Long.parseLong(lambdaName)+"L" : "((long)"+e.accept(gen)+")";
        } catch (NumberFormatException ignored) {
          throw Fail.invalidNum(lambdaName, "Int");
        }
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private String _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        // _NumInstance
        if (m.name().equals(".uint")) {
          return instantiate(); // only different at type level
        }
        if (m.name().equals(".str")) {
          return "Long.toString("+instantiate()+")";
        }
        if (m.name().equals("+")) { return instantiate()+"+"+args.get(0).accept(gen); }
        if (m.name().equals("-")) { return instantiate()+"-"+args.get(0).accept(gen); }
        if (m.name().equals("*")) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.name().equals("/")) { return instantiate()+"/"+args.get(0).accept(gen); }
        if (m.name().equals("%")) { return instantiate()+"%"+args.get(0).accept(gen); }
        if (m.name().equals("**")) {
          // TODO: implement this iteratively in fearless
          throw Bug.todo();
        }
        if (m.name().equals(".abs")) { return "Math.abs("+instantiate()+")"; }
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return "("+instantiate()+">"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("<")) { return "("+instantiate()+"<"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals(">=")) { return "("+instantiate()+">="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("<=")) { return "("+instantiate()+"<="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("==")) { return "("+instantiate()+"=="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){})"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> uint(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
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
        if (m.name().equals(".int")) {
          return instantiate(); // only different at type level
        }
        if (m.name().equals(".str")) {
          return "Long.toUnsignedString("+instantiate()+")";
        }
        if (m.name().equals("+")) { return instantiate()+"+"+args.get(0).accept(gen); }
        if (m.name().equals("-")) { return instantiate()+"-"+args.get(0).accept(gen); }
        if (m.name().equals("*")) { return instantiate()+"*"+args.get(0).accept(gen); }
        if (m.name().equals("/")) { return "Long.divideUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.name().equals("%")) { return "Long.remainderUnsigned("+instantiate()+","+args.get(0).accept(gen)+")"; }
        if (m.name().equals("**")) {
          // TODO: implement this iteratively in fearless
          throw Bug.todo();
        }
        if (m.name().equals(".abs")) { return instantiate(); } // no-op for unsigned
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>0?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("<")) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<0?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals(">=")) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>=0?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("<=")) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<=0?new base.True_0(){}:new base.False_0(){})"; }
        if (m.name().equals("==")) { return "(Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")==0?new base.True_0(){}:new base.False_0(){})"; }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> float_(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<String> str(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        return isLiteral(lambdaName) ? lambdaName : "((String)"+e.accept(gen)+")";
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".len", 0))) { return Optional.of(instantiate()+".length()"); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of("("+instantiate()+"+"+args.get(0).accept(gen)+")"); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of("("+instantiate()+".equals("+args.get(0).accept(gen)+")?new base.True_0(){}:new base.False_0(){})");
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> refK(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("#", 1))) {
          var x = args.get(0);
          return Optional.of(String.format("""
            new base.Ref_1(){
              protected Object x = %s;
              public Object $42$() { return this.x; }
              public Object swap$(Object x$) { var x1 = this.x; this.x = x$; return x1; }
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
      @Override public MIR.Lambda instance() { return l; }
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
              
              public base.Bool_0 isAlive$() { return this.isAlive ? new base.True_0(){} : new base.False_0(){}; }
              public Object peek$() { return this.consume$(); }
              public Object consume$() {
                if (!this.isAlive) {
                  throw new RuntimeException("Cannot consume an empty IsoPod.");
                }
                this.isAlive = false;
                return this.x;
              }
              public base.Void_0 next$(Object x) { this.isAlive = true; this.x = x; return new base.Void_0(){}; }
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
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("._fail", 0))) {
          return Optional.of("""
            switch (1) { default -> {
              System.err.println("Assertion failed :(");
              System.exit(1);
              yield null;
            }}
            """);
        }
        if (m.equals(new Id.MethName("._fail", 1))) {
          return Optional.of(String.format("""
            switch (1) { default -> {
              System.err.println(%s);
              System.exit(1);
              yield null;
            }}
            """, args.get(0).accept(gen)));
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> abort(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName("!", 0))) {
          return Optional.of("""
            switch (1) { default -> {
              System.err.println("Program aborted at:\\n"+java.util.Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining("\\n")));
              System.exit(1);
              yield null;
            }}
            """);
        }
        return Optional.empty();
      }
    };
  }

  @Override public MagicTrait<String> objCap(Id.DecId target, MIR.Lambda l, MIR e) {
    var _this = this;
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String instantiate() {
        return gen.visitLambda(l, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        ObjCapImpl impl = null;
        if (target == Magic.RootCap) { impl = rootCap(); }
        if (target == Magic.Env) { impl = env(); }
        assert impl != null;

        var res = impl.call(_this, m, args, gamma);
        return Optional.ofNullable(res);
      }

      private ObjCapImpl rootCap() {
        return (ctx, m, args, gamma) ->{
          if (m.equals(new Id.MethName(".print", 1))) {
            return String.format("""
              switch (1) { default -> {
                System.out.print(%s);
                yield new base.Void_0(){};
              }}
              """, args.get(0).accept(gen));
          }
          if (m.equals(new Id.MethName(".println", 1))) {
            return String.format("""
              switch (1) { default -> {
                System.out.println(%s);
                yield new base.Void_0(){};
              }}
              """, args.get(0).accept(gen));
          }
          return null;
        };
      }
      private ObjCapImpl env() {
        return (ctx, m, args, gamma) ->{
          if (m.equals(new Id.MethName(".launchArgs", 0))) {
            return "FAux.LAUNCH_ARGS";
          }
          return null;
        };
      }
    };
  }
}
