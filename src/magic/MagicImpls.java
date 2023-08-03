package magic;

import ast.E;
import ast.Program;
import ast.T;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Push;
import visitors.MIRVisitor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MagicImpls<R> {
  static boolean isLiteral(String name) {
    return Character.isDigit(name.charAt(0)) || name.startsWith("\"") || name.startsWith("-");
  }

  default Optional<MagicTrait<R>> get(MIR e) {
    return e.accept(new LambdaVisitor(p())).flatMap(l->{
      if (isMagic(Magic.Int, l)) { return Optional.ofNullable(int_(l, e)); }
      if (isMagic(Magic.UInt, l)) { return Optional.ofNullable(uint(l, e)); }
      if (isMagic(Magic.Float, l)) { return Optional.ofNullable(float_(l, e)); }
      if (isMagic(Magic.Str, l)) { return Optional.ofNullable(str(l, e)); }
      if (isMagic(Magic.RefK, l)) { return Optional.ofNullable(refK(l, e)); }
      if (isMagic(Magic.IsoPodK, l)) { return Optional.ofNullable(isoPodK(l, e)); }
      if (isMagic(Magic.Assert, l)) { return Optional.ofNullable(assert_(l, e)); }
      if (isMagic(Magic.Abort, l)) { return Optional.ofNullable(abort(l, e)); }
      return Magic.ObjectCaps.stream()
        .filter(target->isMagic(target, l))
        .map(target->Optional.ofNullable(objCap(target, l, e)))
        .findAny()
        .flatMap(o->o);
    });
  }

  default boolean isMagic(Id.DecId magicDec, MIR.Lambda l) {
    var name = l.freshName().name();
    if (l.freshName().gen() != 0) { return false; }
    if (!name.startsWith("base.") && Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }

    var p = p();
    try {
      p().of(l.freshName());
    } catch (CompileError e) {
      p = p.withDec(new T.Dec(l.freshName(), List.of(), new E.Lambda(
        l.mdf(),
        Push.of(l.its(), new Id.IT<>(l.freshName(), List.of())),
        l.selfName(),
        List.of(),
        Optional.empty()
      ), Optional.empty()));
    }
    return p.isSubType(new T(l.mdf(), new Id.IT<>(l.freshName(), List.of())), new T(l.mdf(), new Id.IT<>(magicDec, List.of())));
  }
  default boolean isMagic(Id.DecId magicDec, Id.DecId freshName) {
    var name = freshName.name();
    if (freshName.gen() != 0) { return false; }
    if (!name.startsWith("base.") && Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    return p().isSubType(new T(Mdf.mdf, new Id.IT<>(freshName, List.of())), new T(Mdf.mdf, new Id.IT<>(magicDec, List.of())));
  }

  MagicTrait<R> int_(MIR.Lambda l, MIR e);
  MagicTrait<R> uint(MIR.Lambda l, MIR e);
  MagicTrait<R> float_(MIR.Lambda l, MIR e);
  MagicTrait<R> str(MIR.Lambda l, MIR e);
  MagicTrait<R> refK(MIR.Lambda l, MIR e);
  MagicTrait<R> isoPodK(MIR.Lambda l, MIR e);
  MagicTrait<R> assert_(MIR.Lambda l, MIR e);
  default MagicTrait<R> abort(MIR.Lambda l, MIR e) { return  null; }
  MagicTrait<R> objCap(Id.DecId magicTrait, MIR.Lambda l, MIR e);
  ast.Program p();

  record LambdaVisitor(Program p) implements MIRVisitor<Optional<MIR.Lambda>> {
    public Optional<MIR.Lambda> visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitTrait(String pkg, MIR.Trait trait) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitMeth(MIR.Meth meth, String selfName, boolean concrete) { throw Bug.unreachable(); }
    public Optional<MIR.Lambda> visitX(MIR.X x, boolean _ignored) {
      T.Dec ret; try { ret = p().of(x.t().itOrThrow().name()); }
      catch (CompileError e) {
        // no magic multi-impls
        return Optional.empty();
      }
      try {
        return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
      } catch (MIRInjectionVisitor.NotInGammaException e) {
        return Optional.empty();
      }
    }
    public Optional<MIR.Lambda> visitMCall(MIR.MCall mCall, boolean _ignored) {
      T.Dec ret; try { ret = p().of(mCall.t().itOrThrow().name()); }
      catch (CompileError e) {
        // no magic multi-impls
        return Optional.empty();
      }
      try {
        return Optional.of(new MIRInjectionVisitor(p()).visitLambda("base", ret.lambda(), Map.of()));
      } catch (MIRInjectionVisitor.NotInGammaException e) {
        return Optional.empty();
      }
    }
    public Optional<MIR.Lambda> visitLambda(MIR.Lambda newL, boolean _ignored) { return Optional.of(newL); }
  }
}
