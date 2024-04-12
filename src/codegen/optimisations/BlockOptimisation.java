package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.Magic;
import magic.MagicImpls;
import utils.Bug;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: also optimise Block#(...)
public class BlockOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private Map<MIR.FName, MIR.Fun> funs;
  public BlockOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    if (!(fun.body() instanceof MIR.MCall call)) { return MIRCloneVisitor.super.visitFun(fun); }
    var isBlock = this.magic.isMagic(Magic.Block, call.recv());
    if (!isBlock) {
      return MIRCloneVisitor.super.visitFun(fun);
    }
    return this.visitBlockCall(call, List.of(MIR.Block.BlockStmt.Return.class), Optional.empty())
      .map(fun::withBody)
      .orElse(MIRCloneVisitor.super.visitFun(fun));
  }

  private Optional<MIR.Block> visitBlockCall(MIR.MCall call, List<Class<? extends MIR.Block.BlockStmt>> validEndings, Optional<MIR.X> self) {
    var stmts = new ArrayDeque<MIR.Block.BlockStmt>();
    var res = flattenBlock(call, stmts, self);
    if (res == FlattenStatus.INVALID) { return Optional.empty(); }
    if (validEndings.stream().noneMatch(c->c.isAssignableFrom(stmts.getLast().getClass()))) { return Optional.empty(); }
    assert res == FlattenStatus.FLATTENED;
    return Optional.of(new MIR.Block(call, Collections.unmodifiableCollection(stmts), call.t()));
  }

  private enum FlattenStatus { CONTINUE, INVALID, FLATTENED }
  private FlattenStatus flattenBlock(MIR.E expr, Deque<MIR.Block.BlockStmt> stmts, Optional<MIR.X> self) {
    return switch (expr) {
      case MIR.MCall mCall -> {
        if (mCall.name().equals(new Id.MethName("#", 0)) && this.magic.isMagic(Magic.BlockK, mCall.recv())) {
          yield FlattenStatus.FLATTENED;
        }
        if (mCall.name().equals(new Id.MethName(".return", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) { yield  FlattenStatus.INVALID; }
          stmts.offerFirst(new MIR.Block.BlockStmt.Return(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".do", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) {
            yield FlattenStatus.INVALID;
          }
          stmts.offerFirst(new MIR.Block.BlockStmt.Do(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".loop", 1))) {
          // We intentionally do not inline the block function because, often it is implemented with a Block itself,
          // so leaving it as a different function lets us apply this optimisation to it as well.
          stmts.offerFirst(new MIR.Block.BlockStmt.Loop(mCall.args().getFirst()));
        } else if (mCall.name().equals(new Id.MethName(".if", 1))) {
          var res = this.visitReturn(mCall.args().getFirst());
          if (res.isEmpty()) {
            yield FlattenStatus.INVALID;
          }
          stmts.offerFirst(new MIR.Block.BlockStmt.If(res.get()));
        } else if (mCall.name().equals(new Id.MethName(".let", 2))) {
          var variable = this.visitReturn(mCall.args().getFirst());
          var continuationCall = this.visitVarContinuation(mCall.args().get(1));
          if (variable.isEmpty() || continuationCall.isEmpty()) { yield FlattenStatus.INVALID; }
          var continuation = this.visitBlockCall(
            continuationCall.get().continuationCall(),
            List.of(MIR.Block.BlockStmt.Return.class, MIR.Block.BlockStmt.Do.class), // TODO: and .error when we have that
            Optional.of(continuationCall.get().selfVar())
          );
          if (continuation.isEmpty()) { yield FlattenStatus.INVALID; }
          stmts.offerFirst(new MIR.Block.BlockStmt.Var(continuationCall.get().var().name(), variable.get()));
          continuation.get().stmts().forEach(stmts::offerLast);
        } else {
          // TODO: .error
          // TODO: .assert
          // TODO: .letIso
          yield FlattenStatus.INVALID;
        }
        yield flattenBlock(mCall.recv(), stmts, self);
      }
      case MIR.BoolExpr boolExpr -> throw Bug.todo();
      case MIR.X x -> self.filter(x::equals).map(x_->FlattenStatus.FLATTENED).orElse(FlattenStatus.INVALID);
      case MIR.CreateObj ignored -> FlattenStatus.INVALID;
      case MIR.StaticCall ignored -> throw Bug.unreachable();
      case MIR.Block ignored -> throw Bug.unreachable();
    };
  }

  private Optional<MIR.E> visitReturn(MIR.E fn) {
    if (!(fn instanceof MIR.CreateObj k)) { return Optional.empty(); }
    if (!this.magic.isMagic(Magic.ReturnStmt, k)) { return Optional.empty(); }
    if (k.meths().size() != 1) { return Optional.empty(); }
    var m = k.meths().getFirst();
    assert m.sig().name().equals(new Id.MethName("#", 0));
    var body = this.funs.get(m.fName().orElseThrow()).body();
    return Optional.of(body);
  }
  private record VarContinuation(MIR.X var, MIR.X selfVar, MIR.MCall continuationCall) {}
  private Optional<VarContinuation> visitVarContinuation(MIR.E fn) {
    if (!(fn instanceof MIR.CreateObj k)) { return Optional.empty(); }
    if (!this.magic.isMagic(Magic.VarContinuation, k)) { return Optional.empty(); }
    if (k.meths().size() != 1) { return Optional.empty(); }
    var m = k.meths().getFirst();
    assert m.sig().name().equals(new Id.MethName("#", 2));
    var body = (MIR.MCall) this.funs.get(m.fName().orElseThrow()).body();
    return Optional.of(new VarContinuation(m.sig().xs().getFirst(), m.sig().xs().get(1), body));
  }
}
