package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.Magic;
import magic.MagicImpls;
import utils.Bug;
import utils.Push;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: also optimise Block#(...)
public class BlockOptimisation implements
  MIRCloneVisitor,
  FlattenChain<MIR.Block.BlockStmt, Class<? extends MIR.Block.BlockStmt>, MIR.Block>
{
  private final MagicImpls<?> magic;
  private Map<MIR.FName, MIR.Fun> funs;
  private Map<MIR.FName, List<MIR.VPFPromotedCall>> newVPFPromotions;
  private MIR.Fun currentFun;
  public BlockOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    this.newVPFPromotions = new HashMap<>();
    this.currentFun = fun;
    if (!(fun.body() instanceof MIR.MCall call)) {
      var res = MIRCloneVisitor.super.visitFun(fun);
      return res.withPromoted(Push.of(res.promoted(), this.newVPFPromotions.getOrDefault(fun.name(), List.of())));
    }
    var isBlock = this.magic.isMagic(Magic.Block, call.recv());
    if (!isBlock) {
      var res = MIRCloneVisitor.super.visitFun(fun);
      return res.withPromoted(Push.of(res.promoted(), this.newVPFPromotions.getOrDefault(fun.name(), List.of())));
    }
    var res = this.visitFluentCall(call, List.of(MIR.Block.BlockStmt.Return.class), Optional.empty())
      .map(fun::withBody)
      .orElse(MIRCloneVisitor.super.visitFun(fun));
    return res.withPromoted(Push.of(res.promoted(), this.newVPFPromotions.getOrDefault(fun.name(), List.of())));
  }

  @Override public Optional<MIR.Block> visitFluentCall(
    MIR.MCall call,
    List<Class<? extends MIR.Block.BlockStmt>> validEndings,
    Optional<MIR.X> self
  ) {
    var stmts = new ArrayDeque<MIR.Block.BlockStmt>();
    var res = flatten(call, stmts, self);
    if (res == FlattenStatus.INVALID) { return Optional.empty(); }
    if (validEndings.stream().noneMatch(c->c.isAssignableFrom(stmts.getLast().getClass()))) { return Optional.empty(); }
    assert res == FlattenStatus.FLATTENED;
    return Optional.of(new MIR.Block(call, Collections.unmodifiableCollection(stmts), call.t()));
  }

  @Override public FlattenStatus flatten(
    MIR.E expr,
    Deque<MIR.Block.BlockStmt> stmts,
    Optional<MIR.X> self
  ) {
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
          var continuation = this.visitFluentCall(
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
        yield flatten(mCall.recv(), stmts, self);
      }
      case MIR.BoolExpr _ -> throw Bug.todo();
      case MIR.X x -> self.filter(x::equals).map(_->FlattenStatus.FLATTENED).orElse(FlattenStatus.INVALID);
      case MIR.VPFArg vpfArg -> flatten(vpfArg.e(), stmts, self);
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
    var bodyF = this.funs.get(m.fName().orElseThrow());
    bodyF.promoted().forEach(this::generateVPFPromotions);
    var body = bodyF.body();
    return Optional.of(body);
  }
  private record VarContinuation(MIR.X var, MIR.X selfVar, MIR.MCall continuationCall) {}
  private Optional<VarContinuation> visitVarContinuation(MIR.E fn) {
    if (!(fn instanceof MIR.CreateObj k)) { return Optional.empty(); }
    if (!this.magic.isMagic(Magic.Continuation, k)) { return Optional.empty(); }
    if (k.meths().size() != 1) { return Optional.empty(); }
    var m = k.meths().getFirst();
    assert m.sig().name().equals(new Id.MethName("#", 2));

    var bodyF = this.funs.get(m.fName().orElseThrow());
    bodyF.promoted().forEach(this::generateVPFPromotions);
    var body = (MIR.MCall) bodyF.body();

    return Optional.of(new VarContinuation(m.sig().xs().getFirst(), m.sig().xs().get(1), body));
  }

  private void generateVPFPromotions(MIR.VPFPromotedCall original) {
    this.newVPFPromotions.put(this.currentFun.name(), Push.of(this.currentFun.promoted(), original));
  }
}
