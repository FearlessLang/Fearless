package codegen.java.types;

import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import id.Mdf;
import magic.Magic;

import java.util.Map;

import static magic.Magic.getLiteral;

public record Nat(JavaTarget target) implements JavaMagicType {
  @Override public Id.DecId magicDec() { return Magic.Nat; }
  @Override public void instantiate(MIR.MT t, JavaCodegenState state) {
    var lit = getLiteral(target.p().p(), t.name().orElseThrow());
    assert lit.isPresent();
    state.buffer().append(Long.parseUnsignedLong(lit.get().replace("_", ""), 10)).append("L");
//    lit
//      .ifPresentOrElse(
//        l->state.buffer()
//          .append(Long.parseUnsignedLong(l.replace("_", ""), 10))
//          .append("L"),
//        ()->target.withState(state, target_->target_.visitCreateObj(k, true))
//      );
  }
  @Override public void type(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("long");
  }
  @Override public void boxedType(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("Long");
  }

  private static Map<Id.MethName, MethImpl<Nat>> ms = Map.ofEntries(
    Map.entry(new Id.MethName(Mdf.imm, ".nat", 0), (t, call, state, self)->self.instantiate(t, call.recv(), state)),
    Map.entry(new Id.MethName(Mdf.imm, ".int", 0), (t, call, state, self)->self.instantiate(t, call.recv(), state)),
    Map.entry(new Id.MethName(Mdf.imm, ".float", 0), (t, call, state, self)->{
      state.buffer().append("((double)");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(")");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".byte", 0), (t, call, state, self)->{
      state.buffer().append("((byte)");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(")");
    }),
    Map.entry(new Id.MethName(Mdf.read, ".str", 0), (t, call, state, self)->{
      state.buffer().append("rt.Str.fromJavaStr(Long.toUnsignedString(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append("))");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "+", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" + ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".offset", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" + ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, "-", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" - ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, "*", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append("*");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, "/", 1), (t, call, state, self)->{
      state.buffer().append("Long.divideUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "%", 1), (t, call, state, self)->{
      state.buffer().append("Long.remainderUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "**", 1), (t, call, state, self)->{
      state.buffer().append("(switch (1) { default -> { long base = ");
      self.instantiate(t, call.recv(), state);
      state.buffer().append("; long exp = ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append("; long result = 1; while (exp > 0) { if ((exp & 1) == 1) { result *= base; } base *= base; exp >>= 1; } return result; } })");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".abs", 0), (t, call, state, self)->self.instantiate(t, call.recv(), state)),
    Map.entry(new Id.MethName(Mdf.imm, ".shiftRight", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" >>> ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".shiftLeft", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" <<< ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".xor", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append(" ^ ");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".bitwiseAnd", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append("&");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".bitwiseOr", 1), (t, call, state, self)->{
      self.instantiate(t, call.recv(), state);
      state.buffer().append("|");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
    }),
    Map.entry(new Id.MethName(Mdf.imm, ">", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")>0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "<", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")<0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ">=", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")>=0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "<=", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")<=0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "==", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")==0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, "!=", 1), (t, call, state, self)->{
      state.buffer().append("(Long.compareUnsigned(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(")!=0?base.True_0.$self:base.False_0.$self)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".assertEq", 1), (t, call, state, self)->{
      state.buffer().append("base._NatAssertionHelper_0.assertEq$imm$fun(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(",null)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".assertEq", 2), (t, call, state, self)->{
      state.buffer().append("base._NatAssertionHelper_0.assertEq$imm$fun(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(",");
      self.target.withState(state, target_->call.args().get(1).accept(target_, true));
      state.buffer().append(",null)");
    }),
    Map.entry(new Id.MethName(Mdf.imm, ".hash", 1), (t, call, state, self)->{
      self.target.withState(state, target_->call.args().getFirst().accept(target_, true));
      state.buffer().append(".nat$mut(");
      self.instantiate(t, call.recv(), state);
      state.buffer().append(")");
    })
  );
  @Override public void call(MIR.MT t, MIR.MCall call, JavaCodegenState state) {
    var m = ms.get(call.name());
    assert m != null : "No method found for "+call.name();
    m.of(t, call, state, this);
  }
}
