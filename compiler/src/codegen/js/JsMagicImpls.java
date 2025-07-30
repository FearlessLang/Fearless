package codegen.js;

import codegen.MIR;
import id.Id;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public record JsMagicImpls(JsCodegen gen, ast.Program p) implements magic.MagicImpls<String> {
  @Override public MagicTrait<MIR.E, String> int_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        System.out.println("int_instantiate: "+e.toString());
        return Optional.of("\""+e.toString()+"\"");
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
//        return Optional.of("\""+e.toString()+"."+m+"("+args+")"+"\"");
        String self = e.accept(gen, true);  // LHS
        String arg = args.isEmpty() ? "" : args.get(0).accept(gen, true);  // RHS
        System.out.printf("int_call: method=%s, self=%s, arg=%s%n", m.name(), self, arg);

        if (m.equals(new Id.MethName("+", 1))) return Optional.of(self + " + " + arg);
        if (m.equals(new Id.MethName("*", 1))) return Optional.of(self + " * " + arg);
        if (m.equals(new Id.MethName("-", 1))) return Optional.of(self + " - " + arg);
        if (m.equals(new Id.MethName("/", 1))) return Optional.of(self + " / " + arg);
        if (m.equals(new Id.MethName("%", 1))) return Optional.of(self + " % " + arg);
        if (m.equals(new Id.MethName("==", 1))) return Optional.of(self + " === " + arg);
        if (m.equals(new Id.MethName("!=", 1))) return Optional.of(self + " !== " + arg);
        if (m.equals(new Id.MethName(">", 1))) return Optional.of(self + " > " + arg);
        if (m.equals(new Id.MethName("<", 1))) return Optional.of(self + " < " + arg);
        if (m.equals(new Id.MethName(">=", 1))) return Optional.of(self + " >= " + arg);
        if (m.equals(new Id.MethName("<=", 1))) return Optional.of(self + " <= " + arg);
        if (m.equals(new Id.MethName(".str", 0))) {
          return Optional.of("rt.Str.fromJavaStr(" + self + ".toString())");
        }
        return Optional.empty();
      }
    };
  }

  // sign-extension "myNumber >>> 0" to get a 32-bit unsigned integer from a JS double
  @Override public MagicTrait<MIR.E, String> nat(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.of("\""+e.toString()+"\"");
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of("\""+e.toString()+"."+m+"("+args+")"+"\"");
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> float_(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.of("\""+e.toString()+"\"");
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of("\""+e.toString()+"."+m+"("+args+")"+"\"");
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> byte_(MIR.E e) {
    throw Bug.todo("Byte magic for JS");
  }

  @Override public MagicTrait<MIR.E, String> str(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<String> instantiate() {
        return Optional.of("\""+e.toString()+"\"");
      }

      @Override
      public Optional<String> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        return Optional.of("\""+e.toString()+"."+m+"("+args+")"+"\"");
      }
    };
  }

  @Override public MagicTrait<MIR.E, String> asciiStr(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> debug(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> refK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> isoPodK(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> assert_(MIR.E e) {
    return null;
  }

  @Override public MagicTrait<MIR.E, String> cheapHash(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, String> regexK(MIR.E e) {
    throw Bug.todo();
  }
}
