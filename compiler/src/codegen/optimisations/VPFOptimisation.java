package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;

import java.util.*;
import java.util.stream.IntStream;

public class VPFOptimisation implements MIRCloneVisitor {
  private MIR.Fun currentFun = null;
  private List<MIR.VPFCallTarget> vpfTargets = new ArrayList<>();
  private Map<MIR.FName, MIR.Fun> inlinedFunsToParents = new HashMap<>();

  @Override public MIR.Program visitProgram(MIR.Program p) {
    var findInlinedFuns = new MIRCloneVisitor(){
      private MIR.Fun currentFun = null;
      @Override public MIR.Fun visitFun(MIR.Fun fun) {
        var inliner = inlinedFunsToParents.get(fun.name());
        if (inliner != null) {
          assert inliner.name() == this.currentFun.name();
          return MIRCloneVisitor.super.visitFun(fun);
        }
        this.currentFun = fun;
        return MIRCloneVisitor.super.visitFun(fun);
      }
      @Override public MIR.E visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
        // This is code generated with the then and else functions inlined,
        // so we need to not go down to that level and stay up here.
        inlinedFunsToParents.put(expr.then(), currentFun);
        inlinedFunsToParents.put(expr.else_(), currentFun);
        return MIRCloneVisitor.super.visitBoolExpr(expr, checkMagic);
      }

      @Override public MIR.Block.BlockStmt visitBlockStmt(MIR.Block.BlockStmt stmt) {
        return MIRCloneVisitor.super.visitBlockStmt(stmt);
      }
    };
    findInlinedFuns.visitProgram(p);
    return MIRCloneVisitor.super.visitProgram(p);
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.vpfTargets = new ArrayList<>(pkg.vpfTargets());
    var res = MIRCloneVisitor.super
      .visitPackage(pkg)
      .withVPFTargets(this.vpfTargets);
    this.vpfTargets = null;
    return res;
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    var inliner = inlinedFunsToParents.get(fun.name());
    this.currentFun = inliner != null ? inliner : fun;
    return MIRCloneVisitor.super.visitFun(fun);
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    assert this.currentFun != null : "currentFun cannot be null because it should be set in visitPackage";
//    assert !this.inlinedFunsToParents.containsKey(this.currentFun.name());
    var processed = MIRCloneVisitor.super.visitMCall(call, checkMagic);
    if (!(processed instanceof MIR.MCall processedCall)) { return processed; }
    if (processedCall.variant().contains(MIR.MCall.CallVariant.VPFParallelisable)) {
      var vpfCall = createVPFCall(processedCall);
      this.vpfTargets.add(new MIR.VPFCallTarget(vpfCall));
      return vpfCall;
    }
    assert !processedCall.variant().contains(MIR.MCall.CallVariant.VPFParallelisable);
    return processed;
  }

  private MIR.VPFCall createVPFCall(MIR.MCall call) {
    // We intentionally don't visit the args or receiver because we don't want to nest VPF calls directly
    // because it's likely not worth spawning in that case. This is not for correctness.
    var recv = MIR.VPFCall.VPFArg.of(-1, nonVPFExpr(call.recv()));
    var args = IntStream.range(0, call.args().size())
      .mapToObj(i -> MIR.VPFCall.VPFArg.of(i, nonVPFExpr(call.args().get(i))))
      .toList();
    return new MIR.VPFCall(call, this.currentFun.name(), recv, args);

//    A deep approach would look like this:
//    var recv = MIR.VPFCall.VPFArg.of(-1, call.recv().accept(this, true));
//    var args = IntStream.range(0, call.args().size())
//      .mapToObj(i -> MIR.VPFCall.VPFArg.of(i, call.args().get(i).accept(this, true)))
//      .toList();
//    return new MIR.VPFCall(call, this.currentFun.name(), recv, args);
  }
  private MIR.E nonVPFExpr(MIR.E expr) {
    return switch (expr) {
      case MIR.VPFCall vpfCall -> vpfCall.original().withVariants(EnumSet.of(MIR.MCall.CallVariant.Standard));
      default -> expr;
    };
  }
}
