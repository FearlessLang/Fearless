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

public class BlockOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private Map<MIR.FName, MIR.Fun> funs;
  private boolean isEffectiveStatement = false;
  public BlockOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    this.isEffectiveStatement = true;
    if (!(fun.body() instanceof MIR.MCall call)) { return MIRCloneVisitor.super.visitFun(fun); }
    System.out.println(call.recv());
    var isBlock = this.magic.isMagic(Magic.Block, call.recv());
    if (!isBlock) {
      return MIRCloneVisitor.super.visitFun(fun);
    }
    return this.visitBlockCall(call).map(fun::withBody).orElse(MIRCloneVisitor.super.visitFun(fun));
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    this.isEffectiveStatement = false;
    return MIRCloneVisitor.super.visitMCall(call, checkMagic);
  }

  @Override public MIR.CreateObj visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    this.isEffectiveStatement = false;
    return MIRCloneVisitor.super.visitCreateObj(createObj, checkMagic);
  }

  @Override public MIR.E visitX(MIR.X x, boolean checkMagic) {
    this.isEffectiveStatement = false;
    return MIRCloneVisitor.super.visitX(x, checkMagic);
  }

  @Override public MIR.E visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    this.isEffectiveStatement = false;
    return MIRCloneVisitor.super.visitBoolExpr(expr, checkMagic);
  }

  private Optional<MIR.E> visitBlockCall(MIR.MCall call) {
    var stmts = new ArrayDeque<MIR.Block.BlockStmt>();
    var res = flattenBlock(call, stmts);
    if (res == FlattenStatus.INVALID) { return Optional.empty(); }
    assert res == FlattenStatus.FLATTENED;
    return Optional.of(new MIR.Block(call, Collections.unmodifiableCollection(stmts)));
  }

  private enum FlattenStatus { CONTINUE, INVALID, FLATTENED }
  private FlattenStatus flattenBlock(MIR.E expr, Deque<MIR.Block.BlockStmt> stmts) {
    return switch (expr) {
      case MIR.MCall mCall -> {
        if (mCall.name().equals(new Id.MethName("#", 0)) && this.magic.isMagic(Magic.BlockK, mCall.recv())) {
          yield FlattenStatus.FLATTENED;
        }
        throw Bug.todo();
      }
      case MIR.BoolExpr boolExpr -> throw Bug.todo();
      case MIR.CreateObj ignored -> FlattenStatus.INVALID;
      case MIR.X ignored -> FlattenStatus.INVALID;
      case MIR.Block ignored -> throw Bug.unreachable();
    };
  }
}
