package codegen.optimisations.interpreter;

import ast.Program;
import codegen.MIR;
import id.Id;
import id.Mdf;
import magic.LiteralKind;
import magic.Magic;
import magic.MagicImpls;
import magic.MagicTrait;
import utils.Bug;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public final class InterpreterMagicImpl implements MagicImpls<MIR.E> {
  private final MIR.Program program;
  private final MearlessInterpreter interpreter;
  public InterpreterMagicImpl(MIR.Program program, MearlessInterpreter interpreter) {
    this.program = program;
    this.interpreter = interpreter;
  }

  @Override public MagicTrait<MIR.E, MIR.E> int_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> nat(MIR.E e) {
    return new MagicTrait<>() {
      @Override public Optional<MIR.E> instantiate() {
        return Optional.of(e);
      }
      @Override
      public Optional<MIR.E> call(Id.MethName m, List<? extends MIR.E> args, EnumSet<MIR.MCall.CallVariant> variants, MIR.MT expectedT) {
        if (m.equals(new Id.MethName(".int", 0))) {
          return instantiate(); // only different at type level
        }
        if (m.equals(new Id.MethName(".nat", 0))) {
          return instantiate();
        }
        if (m.equals(new Id.MethName("+", 1))) {
          var rhs = args.getFirst().accept(interpreter, true);
          var lhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), e.t().name().orElseThrow()).orElseThrow(), 10);
          var rhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), rhs.t().name().orElseThrow()).orElseThrow(), 10);
          var res = LiteralKind.classify(Long.toUnsignedString(lhs_ + rhs_)).orElseThrow().toDecId();
          return Optional.of(new MIR.CreateObj(Mdf.imm, res));
        }
        if (m.equals(new Id.MethName("-", 1))) {
          var rhs = args.getFirst().accept(interpreter, true);
          var lhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), e.t().name().orElseThrow()).orElseThrow(), 10);
          var rhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), rhs.t().name().orElseThrow()).orElseThrow(), 10);
          var res = LiteralKind.classify(Long.toUnsignedString(lhs_ - rhs_)).orElseThrow().toDecId();
          return Optional.of(new MIR.CreateObj(Mdf.imm, res));
        }
        if (m.equals(new Id.MethName("<=", 1))) {
          var rhs = args.getFirst().accept(interpreter, true);
          var lhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), e.t().name().orElseThrow()).orElseThrow(), 10);
          var rhs_ = Long.parseUnsignedLong(Magic.getLiteral(p(), rhs.t().name().orElseThrow()).orElseThrow(), 10);
          var isLt = Long.compareUnsigned(lhs_, rhs_) <= 0 ? new Id.DecId("base.True", 0) : new Id.DecId("base.False", 0);
          return Optional.of(new MIR.CreateObj(Mdf.imm, isLt));
        }
        throw Bug.todo();
      }
    };
  }

  @Override public MagicTrait<MIR.E, MIR.E> float_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> byte_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> str(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> asciiStr(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> debug(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> refK(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> isoPodK(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> assert_(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> cheapHash(MIR.E e) {
    throw Bug.todo();
  }

  @Override public MagicTrait<MIR.E, MIR.E> regexK(MIR.E e) {
    throw Bug.todo();
  }

  @Override public Program p() {
    return this.program.p();
  }
}
