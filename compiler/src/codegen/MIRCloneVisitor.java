package codegen;

import utils.Mapper;
import visitors.MIRVisitor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface MIRCloneVisitor extends MIRVisitor<MIR.E> {
  default MIR.Program visitProgram(MIR.Program p) {
    return new MIR.Program(p.p(), p.pkgs().stream().map(this::visitPackage).toList());
  }
  default MIR.Package visitPackage(MIR.Package pkg) {
    return new MIR.Package(
      pkg.name(),
      Mapper.of(res->pkg.defs().forEach((id,def) -> res.put(id, this.visitTypeDef(def)))),
      pkg.funs().stream().map(this::visitFun).toList(),
      pkg.vpfTargets().stream().map(this::visitVPFCallTarget).filter(Optional::isPresent).map(Optional::get).toList()
    );
  }
  default MIR.TypeDef visitTypeDef(MIR.TypeDef def) {
    return new MIR.TypeDef(
      def.name(),
      def.impls().stream().map(this::visitPlain).toList(),
      def.sigs().stream().map(this::visitSig).toList(),
      def.singletonInstance().map(k->this.visitCreateObj(k, true))
    );
  }
  default MIR.Sig visitSig(MIR.Sig sig) {
    return new MIR.Sig(
      sig.name(),
      sig.xs().stream().map(x->(MIR.X)this.visitX(x, true)).toList(),
      this.visitMT(sig.rt())
    );
  }
  default MIR.Meth visitMeth(MIR.Meth meth) {
    return new MIR.Meth(meth.origin(), this.visitSig(meth.sig()), meth.capturesSelf(), meth.captures(), meth.fName());
  }
  default MIR.Fun visitFun(MIR.Fun fun) {
    return new MIR.Fun(
      fun.name(),
      fun.args().stream().map(x->(MIR.X)this.visitX(x, true)).toList(),
      this.visitMT(fun.ret()), fun.body().accept(this, true));
  }
  default Optional<MIR.VPFCallTarget> visitVPFCallTarget(MIR.VPFCallTarget target) {
    var res = visitVPFCall(target.call(), true);
    if (!(res instanceof MIR.VPFCall vpfCall)) {
      return Optional.empty();
    }
    return Optional.of(new MIR.VPFCallTarget(vpfCall));
  }

  default MIR.MT visitMT(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any any -> this.visitAny(any);
      case MIR.MT.Plain plain -> this.visitPlain(plain);
      case MIR.MT.Usual usual -> this.visitUsual(usual);
    };
  }
  default EnumSet<MIR.MCall.CallVariant> visitCallVariant(EnumSet<MIR.MCall.CallVariant> cv) {
    return cv;
  }
  default MIR.MT.Any visitAny(MIR.MT.Any t) {
    return t;
  }
  default MIR.MT.Plain visitPlain(MIR.MT.Plain t) {
    return t;
  }
  default MIR.MT.Usual visitUsual(MIR.MT.Usual t) {
    return t;
  }

  @Override default MIR.CreateObj visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    return new MIR.CreateObj(
      this.visitMT(createObj.t()),
      createObj.selfName(),
      createObj.meths().stream().map(this::visitMeth).toList(),
      createObj.unreachableMs().stream().map(this::visitMeth).toList(),
      Collections.unmodifiableSortedSet(createObj.captures().stream().map(x->(MIR.X)this.visitX(x, checkMagic)).collect(Collectors.toCollection(MIR::createCapturesSet)))
    );
  }

  @Override default MIR.E visitX(MIR.X x, boolean checkMagic) {
    return new MIR.X(x.name(), this.visitMT(x.t()));
  }

  @Override default MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    return new MIR.MCall(
      call.recv().accept(this, checkMagic),
      call.name(),
      call.args().stream().map(e->e.accept(this, checkMagic)).toList(),
      this.visitMT(call.t()),
      this.visitMT(call.originalRet()),
      call.mdf(),
      this.visitCallVariant(call.variant()),
      call.callId(),
      call.captures()
    );
  }

  @Override default MIR.E visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    return new MIR.BoolExpr(
      expr.original().accept(this, checkMagic),
      expr.condition().accept(this, checkMagic),
      expr.then(),
      expr.else_()
    );
  }

  @Override default MIR.E visitVPFCall(MIR.VPFCall vpfCall, boolean checkMagic) {
    return new MIR.VPFCall(
      vpfCall.original(), // todo: not sure if I should transform original here because then it could become any E, skipping doing it for now
      vpfCall.parentFun(),
      MIR.VPFCall.VPFArg.of(-1, vpfCall.recv().accept(this, checkMagic)),
      IntStream.range(0, vpfCall.args().size())
        .mapToObj(i->MIR.VPFCall.VPFArg.of(i, vpfCall.args().get(i).accept(this, checkMagic)))
        .toList()
    );
  }

  @Override default MIR.E visitSpawnVPFArg(MIR.VPFCall.VPFArg.Spawn spawn, boolean checkMagic) {
    return new MIR.VPFCall.VPFArg.Spawn(spawn.i(), spawn.e().accept(this, checkMagic));
  }

  @Override default MIR.E visitPlainVPFArg(MIR.VPFCall.VPFArg.Plain plain, boolean checkMagic) {
    return new MIR.VPFCall.VPFArg.Plain(plain.i(), plain.e().accept(this, checkMagic));
  }

  @Override default MIR.E visitBlockExpr(MIR.Block expr, boolean checkMagic) {
    return new MIR.Block(
      expr.original().accept(this, checkMagic),
      expr.stmts().stream().map(this::visitBlockStmt).toList(),
      this.visitMT(expr.t())
    );
  }
  default MIR.Block.BlockStmt visitBlockStmt(MIR.Block.BlockStmt stmt) {
    return switch (stmt) {
      case MIR.Block.BlockStmt.Do aDo -> new MIR.Block.BlockStmt.Do(aDo.e().accept(this, true));
      case MIR.Block.BlockStmt.If anIf -> new MIR.Block.BlockStmt.If(anIf.e().accept(this, true));
      case MIR.Block.BlockStmt.Let let -> new MIR.Block.BlockStmt.Let(let.name(), let.e().accept(this, true));
      case MIR.Block.BlockStmt.Loop loop -> new MIR.Block.BlockStmt.Loop(loop.e().accept(this, true));
      case MIR.Block.BlockStmt.Return aReturn -> new MIR.Block.BlockStmt.Return(aReturn.e().accept(this, true));
      case MIR.Block.BlockStmt.Throw aThrow -> new MIR.Block.BlockStmt.Throw(aThrow.e().accept(this, true));
      case MIR.Block.BlockStmt.Var var -> new MIR.Block.BlockStmt.Var(var.name(), var.e().accept(this, true));
    };
  }
}
