package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.MagicImpls;
import program.typesystem.XBs;
import utils.Bug;
import utils.OneOr;
import utils.Streams;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This optimisation replaces `newLambda.methodCall` with that method's Fun (i.e. a static function call) if
 * the lambda/call is non-magical, and the lambda does not capture itself.
 * This could be extended, in the future, to support method call receivers and X receivers if they return a final lambda.
 */
public class DevirtualisationOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private boolean hasActed = true;
  private Map<MIR.FName, MIR.Fun> funs;
  private MIR.Program p;
  public DevirtualisationOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Program visitProgram(MIR.Program p) {
    this.p = p;
    this.funs = p.pkgs().stream().flatMap(pkg->pkg.funs().stream()).collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitProgram(p);
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
//    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    if (!call.variant().contains(MIR.MCall.CallVariant.Standard)) { return call; }
    if (!(call.recv() instanceof MIR.MCall) && !(call.recv() instanceof MIR.CreateObj)) { return call; }
    if (call.recv() instanceof MIR.MCall recvCall) { return call.withRecv(this.visitMCall(recvCall, checkMagic)); }

    var recvK = (MIR.CreateObj) call.recv();
    if (this.magic.get(call.recv()).isPresent()) { return call; }

    var singletonInstance = this.p.of(recvK.concreteT().id()).singletonInstance();
    var ms = singletonInstance.map(MIR.CreateObj::meths).orElse(recvK.meths());

    var meth = ms.stream()
      .filter(m->m.fName().isPresent())
      .filter(m->m.sig().name().withMdf(Optional.of(m.sig().mdf()))
        .equals(call.name().withMdf(Optional.of(call.mdf()))))
      .findFirst()
      .orElseThrow();
    if (meth.capturesSelf()) { return call; }
    var fName = meth.fName().orElseThrow();
    var fun = this.funs.get(fName);
    // TODO: there's an issue with a missing cast and generic return type combo here (on Error.msg inlining Info...)
    Optional<MIR.MT> cast = !call.t().equals(call.originalRet()) ? Optional.of(call.t()) : Optional.empty();
    var args = Stream.concat(call.args().stream(), fun.args().stream().skip(call.args().size())).toList();
    // we can easily inline the static function if there are no args
    if (args.isEmpty()) {
      return fun.body();
    }

    return new MIR.StaticCall(call, fName, args, cast);
  }
}
