package codegen.go;

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

import static codegen.go.GoCodegen.Output;

public record MagicImpls(GoCodegen gen, Program p) implements magic.MagicImpls<Output> {
  @Override public MagicTrait<Output> int_(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<Output> uint(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public Output instantiate() {
        var lambdaName = name().name().name();
        if (magic.MagicImpls.isLiteral(lambdaName)) {
          try {
            long l = Long.parseUnsignedLong(lambdaName.substring(0, lambdaName.length()-1));
            return new Output("uint64("+l+")");
          } catch (NumberFormatException err) {
            throw Fail.invalidNum(lambdaName, "UInt");
          }
        }
        var res = e.accept(gen);
        return new Output(res.relative()+".(uint64)", res.global());
      }
      @Override public Optional<Output> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        return Optional.of(_call(m, args, gamma));
      }
      private Output _call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        var argOut = args.stream().map(arg->arg.accept(gen)).toList();
        var globalArgOut = argOut.stream().reduce(Output::merge).orElseGet(Output::new);
        var instance = instantiate().relative();
        // _NumInstance
        if (m.equals(new Id.MethName(".int", 0))) {
          return new Output("int64("+instance+")", globalArgOut.global());
        }
        if (m.equals(new Id.MethName(".float", 0))) {
          return new Output("float64("+instance+")", globalArgOut.global());
        }
        if (m.equals(new Id.MethName(".str", 0))) {
          return new Output("strconv.FormatUint("+instance+", 10)", (globalArgOut).global());
        }
        if (m.equals(new Id.MethName("+", 1))) { return new Output(instance+" + "+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("-", 1))) { return new Output(instance+" - "+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("*", 1))) { return new Output(instance+"*"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("/", 1))) { return new Output(instance+"/"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("%", 1))) { return new Output(instance+"%"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("**", 1))) {
          // TODO
          throw Bug.todo();
        }
//        if (m.equals(new Id.MethName("**", 1))) { return String.format("""
//          switch (1) { default -> {
//              long base = %s; long exp = %s; long res = base;
//              if (exp == 0) { yield 1L; }
//              for(; exp > 1; exp--) { res *= base; }
//              yield res;
//            }}
//          """, instantiate(), argOut.get(0).relative()); }
        if (m.equals(new Id.MethName(".abs", 0))) { return new Output(instance, globalArgOut.global()); } // no-op for unsigned
        if (m.equals(new Id.MethName(">>", 1))) { return new Output(instance+">>"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("<<", 1))) { return new Output(instance+"<<"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("^", 1))) { return new Output(instance+"^"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("&", 1))) { return new Output(instance+"&"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName("|", 1))) { return new Output(instance+"|"+argOut.get(0).relative(), globalArgOut.global()); }
        if (m.equals(new Id.MethName(">", 1))) { return new Output("func() baseφBool_0 { if "+instance+" > "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut.global()); }
        if (m.equals(new Id.MethName("<", 1))) { return new Output("func() baseφBool_0 { if "+instance+" < "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut.global()); }
        if (m.equals(new Id.MethName(">=", 1))) { return new Output("func() baseφBool_0 { if "+instance+" >= "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut.global()); }
        if (m.equals(new Id.MethName("<=", 1))) { return new Output("func() baseφBool_0 { if "+instance+" <= "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut.global()); }
        if (m.equals(new Id.MethName("==", 1))) { return new Output("func() baseφBool_0 { if "+instance+" == "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut.global()); }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<Output> float_(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<Output> str(MIR.Lambda l, MIR e) {
    var name = new Id.IT<T>(l.freshName().name(), List.of());
    return new MagicTrait<>() {
      @Override public Id.IT<T> name() { return name; }
      @Override public MIR.Lambda instance() { return l; }
      @Override public Output instantiate() {
        var lambdaName = name().name().name();
        if (magic.MagicImpls.isLiteral(lambdaName)) {
          return new Output(lambdaName);
        }
        var res = e.accept(gen);
        return new Output(res.relative()+".(string)", res.global());
      }
      @Override public Optional<Output> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma) {
        var argOut = args.stream().map(arg->arg.accept(gen)).toList();
        var globalArgOut = argOut.stream().reduce(Output::merge).orElseGet(Output::new).global();
        var instance = instantiate().relative();
        if (m.equals(new Id.MethName(".len", 0))) { return Optional.of(new Output("len("+instance+")", globalArgOut)); }
        if (m.equals(new Id.MethName(".str", 0))) { return Optional.of(instantiate()); }
        if (m.equals(new Id.MethName("+", 1))) { return Optional.of(new Output("("+instance+"+"+argOut.get(0).relative()+")", globalArgOut)); }
        if (m.equals(new Id.MethName("==", 1))) {
          return Optional.of(new Output("func() baseφBool_0 { if "+instance+" == "+argOut.get(0).relative()+" { return baseφTrue_0Impl{} } else { return baseφFalse_0Impl{} } }()", globalArgOut));
        }
        throw Bug.unreachable();
      }
    };
  }

  @Override public MagicTrait<Output> refK(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<Output> isoPodK(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<Output> assert_(MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<Output> objCap(Id.DecId magicTrait, MIR.Lambda l, MIR e) {
    throw Bug.todo();
  }
}
