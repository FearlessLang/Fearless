package codegen.proto;

import codegen.MIR;
import id.Id;
import id.Mdf;
import org.capnproto.MessageBuilder;
import org.capnproto.Text;
import org.fearlang.mearless.proto.Mearless;
import utils.Bug;
import visitors.MIRVisitor;

import java.io.FileOutputStream;
import java.nio.file.Path;

public class ProtoCodegen implements MIRVisitor<Void> {
  private Object builder = null;

  public void writePackage(MIR.Package pkg) {
    var msg = new MessageBuilder();
    fill(msg.initRoot(Mearless.Package.factory), ()->visitPackage(pkg));
    // obviously this is a bad idea to just chuck a pkg name here, also only works on unix-like:
    try (var out = new FileOutputStream(Path.of(pkg.name()+".fear.pkg.mearless").toFile())) {
      org.capnproto.Serialize.write(out.getChannel(), msg);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitPackage(MIR.Package pkg) {
    var builder = (Mearless.Package.Builder) this.builder;
    builder.setName(pkg.name());
    var defsBuilder = builder.initDefs(pkg.defs().size());
    var defs = pkg.defs().values().toArray(MIR.TypeDef[]::new);
    for (var i = 0; i < defs.length; i++) {
      MIR.TypeDef typeDef = defs[i];
      fill(defsBuilder.get(i), ()->visitTypeDef(typeDef));
    }
    var funsBuilder = builder.initFuns(pkg.funs().size());
    var funs = pkg.funs().toArray(MIR.Fun[]::new);
    for (var i = 0; i < funs.length; i++) {
      MIR.Fun fun = funs[i];
      fill(funsBuilder.get(i), ()->visitFun(fun, true));
    }
  }

  public void visitTypeDef(MIR.TypeDef typeDef) {
    var builder = (Mearless.TypeDef.Builder) this.builder;
    fill(builder.initName(), ()->visitDecId(typeDef.name()));
    var implsBuilder = builder.initImpls(typeDef.impls().size());
    for (var i = 0; i < typeDef.impls().size(); i++) {
      MIR.MT.Plain impl = typeDef.impls().get(i);
      fill(implsBuilder.get(i), ()->visitDecId(impl.id()));
    }
    var sigsBuilder = builder.initSigs(typeDef.sigs().size());
    for (var i = 0; i < typeDef.sigs().size(); i++) {
      MIR.Sig sig = typeDef.sigs().get(i);
      fill(sigsBuilder.get(i), ()->visitSig(sig));
    }
    if (typeDef.singletonInstance().isPresent()) {
      var singletonInstanceBuilder = builder.initSingletonInstance();
      fill(singletonInstanceBuilder.initInstance(), ()->visitCreateObj(typeDef.singletonInstance().get(), true));
    } else {
      builder.initSingletonInstance().setEmpty(null);
    }
  }

  public void visitSig(MIR.Sig sig) {
    var builder = (Mearless.Sig.Builder) this.builder;
    fill(builder.initName(), ()->visitMethName(sig.name()));
    fill(builder.initRt(), ()->visitMT(sig.rt()));
    var xsBuilder = builder.initXs(sig.xs().size());
    for (var i = 0; i < sig.xs().size(); i++) {
      var x = sig.xs().get(i);
      fill(xsBuilder.get(i), ()->visitTypePair(x));
    }
  }
  public void visitMethName(Id.MethName name) {
    var builder = (Mearless.MethName.Builder) this.builder;
    builder.setRc(visitMdf(name.mdf().orElseThrow()));
    builder.setName(name.name());
    builder.setArity(name.num());
    builder.setHash(name.uniqueHash());
  }

  public void visitFun(MIR.Fun fun, boolean checkMagic) {
    var builder = (Mearless.Fun.Builder) this.builder;
    var name = fun.name();
    fill(builder.initName(), ()->visitFName(name));
    var argsBuilder = builder.initArgs(fun.args().size());
    for (var i = 0; i < fun.args().size(); i++) {
      MIR.X arg = fun.args().get(i);
      fill(argsBuilder.get(i), ()->visitTypePair(arg));
    }
    fill(builder.initRet(), ()->visitMT(fun.ret()));
    fill(builder.initBody(), ()->fun.body().accept(this, checkMagic));
  }
  public void visitFName(MIR.FName name) {
    var builder = (Mearless.Fun.Name.Builder) this.builder;
    builder.setHash(name.uniqueHash());
    builder.setRc(visitMdf(name.mdf()));
    fill(builder.initD(), ()->visitDecId(name.d()));
    fill(builder.initM(), ()->visitMethName(name.m()));
  }

  public void visitMeth(MIR.Meth meth) {
    var builder = (Mearless.Meth.Builder) this.builder;
    fill(builder.initOrigin(), ()->visitDecId(meth.origin()));
    fill(builder.initSig(), ()->visitSig(meth.sig()));
    builder.setCapturesSelf(meth.capturesSelf());
    var capturesBuilder = builder.initCaptures(meth.captures().size());
    var captures = meth.captures().toArray(String[]::new);
    for (var i = 0; i < captures.length; i++) {
      String capture = captures[i];
      capturesBuilder.set(i, new Text.Reader(capture));
    }
    if (meth.fName().isPresent()) {
      var fNameBuilder = builder.initFName();
      fill(fNameBuilder.initInstance(), ()->visitFName(meth.fName().get()));
    } else {
      builder.initFName().setEmpty(null);
    }
  }

  @Override public Void visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var builder = (Mearless.E.Builder) this.builder;
    fill(builder.initT(), ()->visitMT(createObj.t()));
    var createObjBuilder = builder.initCreateObj();
    createObjBuilder.setSelfName(createObj.selfName());

    var methsBuilder = createObjBuilder.initMeths(createObj.meths().size());
    var meths = createObj.meths().toArray(MIR.Meth[]::new);
    for (var i = 0; i < meths.length; i++) {
      MIR.Meth meth = meths[i];
      fill(methsBuilder.get(i), ()->visitMeth(meth));
    }

    var capturesBuilder = createObjBuilder.initCaptures(createObj.captures().size());
    var captures = createObj.captures().toArray(MIR.X[]::new);
    for (var i = 0; i < captures.length; i++) {
      MIR.X capture = captures[i];
      fill(capturesBuilder.get(i), ()->visitTypePair(capture));
    }

    return null;
  }

  @Override public Void visitX(MIR.X x, boolean checkMagic) {
    var builder = (Mearless.E.Builder) this.builder;
    fill(builder.initT(), ()->visitMT(x.t()));
    var xBuilder = builder.initX();
    xBuilder.setName(x.name());
    return null;
  }

  @Override public Void visitMCall(MIR.MCall call, boolean checkMagic) {
    var builder = (Mearless.E.Builder) this.builder;
    fill(builder.initT(), ()->visitMT(call.t()));
    var callBuilder = builder.initMCall();
    fill(callBuilder.initRecv(), ()->call.recv().accept(this, checkMagic));
    fill(callBuilder.initName(), ()->visitMethName(call.name()));
    var argsBuilder = callBuilder.initArgs(call.args().size());
    for (var i = 0; i < call.args().size(); i++) {
      MIR.E arg = call.args().get(i);
      fill(argsBuilder.get(i), ()->arg.accept(this, checkMagic));
    }
    fill(callBuilder.initOriginalRet(), ()->visitMT(call.originalRet()));
    callBuilder.setRc(visitMdf(call.mdf()));

    var variantBuilder = callBuilder.initVariant(call.variant().size());
    int i = 0;
    for (MIR.MCall.CallVariant variant : call.variant()) {
      variantBuilder.set(i++, switch (variant) {
        case Standard -> Mearless.E.MCall.Variant.STANDARD;
        case PipelineParallelFlow -> Mearless.E.MCall.Variant.PIPELINE_PARALLEL_FLOW;
        case DataParallelFlow -> Mearless.E.MCall.Variant.DATA_PARALLEL_FLOW;
        case SafeMutSourceFlow -> Mearless.E.MCall.Variant.SAFE_MUT_SOURCE_FLOW;
      });
    }

    return null;
  }

  public void visitTypePair(MIR.X x) {
    var builder = (Mearless.TypePair.Builder) this.builder;
    fill(builder.initT(), ()->visitMT(x.t()));
    builder.setName(x.name());
  }

  private void fill(Object builder, Runnable visitCase) {
    var old = this.builder;
    this.builder = builder;
    visitCase.run();
    this.builder = old;
  }

  private void visitDecId(Id.DecId id) {
    var builder = (Mearless.DecId.Builder) this.builder;
    builder.setName(id.name());
    builder.setGens(id.gen());
    builder.setHash(id.uniqueHash());
  }

  private void visitMT(MIR.MT mt) {
    var builder = (Mearless.T.Builder) this.builder;
    builder.setRc(visitMdf(mt.mdf()));
    switch (mt) {
      case MIR.MT.Any _ -> builder.setAny(null);
      case MIR.MT.Plain plain -> {
        var plainBuilder = builder.initPlain();
        var id = plain.id();
        fill(plainBuilder, ()->visitDecId(id));
      }
      case MIR.MT.Usual usual -> {
        var plainBuilder = builder.initPlain();
        var id = usual.it().name();
        fill(plainBuilder, ()->visitDecId(id));
      }
    }
  }

  private Mearless.RC visitMdf(Mdf mdf) {
    return switch (mdf) {
      case read -> Mearless.RC.READ;
      case mut -> Mearless.RC.MUT;
      case mutH -> Mearless.RC.MUT_H;
      case readH -> Mearless.RC.READ_H;
      case iso -> Mearless.RC.ISO;
      case imm -> Mearless.RC.IMM;
      case readImm -> Mearless.RC.READ_IMM;
      case mdf -> Mearless.RC.GENERIC;
      case recMdf -> throw Bug.unreachable();
    };
  }
}
