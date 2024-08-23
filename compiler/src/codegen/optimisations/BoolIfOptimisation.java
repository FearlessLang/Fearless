package codegen.optimisations;

import codegen.MIR;
import codegen.MIRCloneVisitor;
import id.Id;
import magic.Magic;
import magic.MagicImpls;
import program.typesystem.XBs;
import utils.Mapper;
import utils.Push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Inlines all boolean `.if` and `?` method calls as ternaries if the ThenElse/1 argument is a literal and does not
 * capture itself. This inlining is shallow but the DevirtualisationOptimisation may extend it.
 */
public class BoolIfOptimisation implements MIRCloneVisitor {
  private final MagicImpls<?> magic;
  private Map<MIR.FName, MIR.Fun> funs;
  private Map<MIR.FName, List<MIR.VPFPromotedCall>> newVPFPromotions;
  private MIR.Fun currentFun;
  public BoolIfOptimisation(MagicImpls<?> magic) {
    this.magic = magic;
  }

  @Override public MIR.Package visitPackage(MIR.Package pkg) {
    this.funs = pkg.funs().stream().collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    return MIRCloneVisitor.super.visitPackage(pkg);
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    if (magic.isMagic(Magic.Bool, call.recv()) && (call.name().equals(new Id.MethName(".if", 1)) || call.name().equals(new Id.MethName("?", 1)))) {
      var res = boolIfOptimisation(call);
      if (res.isPresent()) { return res.get(); }
    }
    return MIRCloneVisitor.super.visitMCall(call, checkMagic);
  }

  @Override public MIR.Fun visitFun(MIR.Fun fun) {
    this.currentFun = fun;
    this.newVPFPromotions = new HashMap<>();
    var transformed = MIRCloneVisitor.super.visitFun(fun);
    return transformed.withPromoted(Push.of(transformed.promoted(), this.newVPFPromotions.getOrDefault(fun.name(), List.of())));
  }

  private Optional<MIR.BoolExpr> boolIfOptimisation(MIR.MCall original) {
    assert this.currentFun != null;

    // We need to make sure that this is a canonical ThenElse literal (i.e. no extra methods and the lambda is made inline here)
    // The restriction about the lambda being created inline here allows us to assume any captures are present
    assert original.args().size() == 1;
    var thenElse_ = original.args().getFirst();
    if (!(thenElse_ instanceof MIR.CreateObj thenElse)) { return Optional.empty(); }
    var thenElseDec = this.magic.p().of(thenElse.t().name().orElseThrow());
    var thenElseMs = this.magic.p().meths(XBs.empty(), thenElse.t().mdf(), thenElseDec.toIT(), 0);
    // just checking size here is fine because it must have .then and .else, so less than 2 is impossible and >2 is not covered by this optimisation
    assert thenElseMs.size() >= 2;
    if (thenElseMs.size() != 2) { return Optional.empty(); }

    var then = thenElse.meths().stream().filter(m->m.sig().name().equals(new Id.MethName(".then", 0))).findFirst().orElseThrow();
    var else_ = thenElse.meths().stream().filter(m->m.sig().name().equals(new Id.MethName(".else", 0))).findFirst().orElseThrow();
    if (then.capturesSelf() || else_.capturesSelf()) {
      return Optional.empty();
    }

    // Bring up any promoted method calls
    var thenF = this.funs.get(then.fName().orElseThrow());
    var elseF = this.funs.get(else_.fName().orElseThrow());
    thenF.promoted().forEach(this::generateVPFPromotions);
    elseF.promoted().forEach(this::generateVPFPromotions);

    // This optimisation is shallow, it will only inline one level of a .if
    // If there is a nested .if, that nested if could also be optimised, but it will not be inlined into this one.
    return Optional.of(new MIR.BoolExpr(original, original.recv(), thenF.name(), elseF.name()));
  }

  private void generateVPFPromotions(MIR.VPFPromotedCall original) {
    this.newVPFPromotions.put(this.currentFun.name(), Push.of(this.currentFun.promoted(), original));
  }
}
