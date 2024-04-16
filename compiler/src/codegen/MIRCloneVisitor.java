package codegen;

import utils.Mapper;
import visitors.MIRVisitor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

public interface MIRCloneVisitor extends MIRVisitor<MIR.E> {
  default MIR.Program visitProgram(MIR.Program p) {
    return new MIR.Program(p.p(), p.pkgs().stream().map(this::visitPackage).toList());
  }
  default MIR.Package visitPackage(MIR.Package pkg) {
    return new MIR.Package(
      pkg.name(),
      Mapper.of(res->pkg.defs().forEach((id,def) -> res.put(id, this.visitTypeDef(def)))),
      pkg.funs().stream().map(this::visitFun).toList()
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
      sig.mdf(),
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
      this.visitCallVariant(call.variant())
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
}
