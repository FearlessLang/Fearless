package codegen.java;

import ast.Program;
import ast.T;
import codegen.MIR;
import failure.Fail;
import id.Id;
import magic.MagicTrait;
import utils.Bug;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static magic.MagicImpls.isLiteral;

public record MagicImpls(JavaCodegen gen, Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<String> int_(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String type() {
        return "Long";
      }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        try {
          return isLiteral(lambdaName) ? Long.parseLong(lambdaName)+"L" : e.accept(gen, false);
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
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return instantiate()+">"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<")) { return instantiate()+"<"+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals(">=")) { return instantiate()+">="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<=")) { return instantiate()+"<="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("==")) { return instantiate()+"=="+args.get(0).accept(gen)+"?new base.True_0(){}:new base.False_0(){}"; }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<String> uint(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public String type() {
        return "Long";
      }
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
        return e.accept(gen);
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
        if (m.name().equals(">>")) { return instantiate()+">>"+args.get(0).accept(gen); }
        if (m.name().equals("<<")) { return instantiate()+"<<"+args.get(0).accept(gen); }
        if (m.name().equals("^")) { return instantiate()+"^"+args.get(0).accept(gen); }
        if (m.name().equals("&")) { return instantiate()+"&"+args.get(0).accept(gen); }
        if (m.name().equals("|")) { return instantiate()+"|"+args.get(0).accept(gen); }
        if (m.name().equals(">")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals(">=")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")>=0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("<=")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")<=0?new base.True_0(){}:new base.False_0(){}"; }
        if (m.name().equals("==")) { return "Long.compareUnsigned("+instantiate()+","+args.get(0).accept(gen)+")==0?new base.True_0(){}:new base.False_0(){}"; }
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
      @Override public String type() {
        return "String";
      }
      @Override public String instantiate() {
        var lambdaName = name().name().name();
        return isLiteral(lambdaName) ? lambdaName : e.accept(gen, false);
      }
      @Override public Optional<String> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        if (m.equals(new Id.MethName(".len", 0))) {
          return Optional.of(instantiate()+".length()");
        }
        throw Bug.unreachable();
      }
    };
  }
  @Override public MagicTrait<String> refK(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }
  @Override public MagicTrait<String> assert_(MIR.Lambda l, MIR e) {
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return l.t().itOrThrow(); }
      @Override public MIR.Lambda instance() { return l; }

      @Override public String type() {
        return "Assert_0";
      }
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
}
