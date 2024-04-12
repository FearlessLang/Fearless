package magic;

import ast.Program;
import ast.T;
import codegen.MIR;
import id.Id;
import id.Mdf;
import program.typesystem.XBs;

import java.util.List;
import java.util.Optional;

public interface MagicImpls<R> {
  static Optional<String> getLiteral(Program p, Id.DecId d) {
    if (isLiteral(d.name())) { return Optional.of(d.name()); }
    var supers = p.superDecIds(d);
    return supers.stream().filter(dec->{
      var name = dec.name();
      return isLiteral(name);
    }).map(Id.DecId::name).findFirst();
  }
  static boolean isLiteral(String name) {
    return Character.isDigit(name.charAt(0)) || isStringLiteral(name) || name.startsWith("-");
  }
  static boolean isStringLiteral(String name) {
    return name.startsWith("\"");
  }

  default Optional<MagicTrait<MIR.E,R>> get(MIR.E e) {
    if (isMagic(Magic.Int, e)) { return Optional.ofNullable(int_(e)); }
    if (isMagic(Magic.UInt, e)) { return Optional.ofNullable(uint(e)); }
    if (isMagic(Magic.Float, e)) { return Optional.ofNullable(float_(e)); }
    if (isMagic(Magic.Str, e)) { return Optional.ofNullable(str(e)); }
    if (isMagic(Magic.Bool, e)) { return Optional.ofNullable(bool(e)); }
    if (isMagic(Magic.Debug, e)) { return Optional.ofNullable(debug(e)); }
    if (isMagic(Magic.RefK, e)) { return Optional.ofNullable(refK(e)); }
    if (isMagic(Magic.IsoPodK, e)) { return Optional.ofNullable(isoPodK(e)); }
    if (isMagic(Magic.Assert, e)) { return Optional.ofNullable(assert_(e)); }
    if (isMagic(Magic.Abort, e)) { return Optional.ofNullable(abort(e)); }
    if (isMagic(Magic.MagicAbort, e)) { return Optional.ofNullable(magicAbort(e)); }
    if (isMagic(Magic.ErrorK, e)) { return Optional.ofNullable(errorK(e)); }
    if (isMagic(Magic.Try, e)) { return Optional.ofNullable(tryCatch(e)); }
    if (isMagic(Magic.CapTry, e)) { return Optional.ofNullable(capTryCatch(e)); }
    if (isMagic(Magic.PipelineParallelSinkK, e)) { return Optional.ofNullable(pipelineParallelSinkK(e)); }
    return Magic.ObjectCaps.stream()
      .filter(target->isMagic(target, e))
      .map(target->Optional.ofNullable(objCap(target, e)))
      .findAny()
      .flatMap(o->o);
  }

  default boolean isMagic(Id.DecId magicDec, MIR.E e) {
    if (e.t().name().isEmpty()) { return false; }
    return isMagic(magicDec, e.t().name().get());
  }
  default boolean isMagic(Id.DecId magicDec, Id.DecId freshName) {
    return p().superDecIds(freshName).contains(magicDec);
//    if (freshName.gen() != magicDec.gen()) { return false; }
//    var gens = Id.GX.standardNames(freshName.gen()).stream().map(gx->new T(Mdf.mdf, gx)).toList();
//    return p().isSubType(XBs.empty(), new T(Mdf.mdf, new Id.IT<>(freshName, gens)), new T(Mdf.mdf, new Id.IT<>(magicDec, gens)));
  }

  MagicTrait<MIR.E,R> int_(MIR.E e);
  MagicTrait<MIR.E,R> uint(MIR.E e);
  MagicTrait<MIR.E,R> float_(MIR.E e);
  MagicTrait<MIR.E,R> str(MIR.E e);
  MagicTrait<MIR.E,R> debug(MIR.E e);
  MagicTrait<MIR.E,R> refK(MIR.E e);
  MagicTrait<MIR.E,R> isoPodK(MIR.E e);
  MagicTrait<MIR.E,R> assert_(MIR.E e);
  default MagicTrait<MIR.E,R> bool(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> abort(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> magicAbort(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> errorK(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> tryCatch(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> capTryCatch(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> pipelineParallelSinkK(MIR.E e) { return null; }
  default MagicTrait<MIR.E,R> objCap(Id.DecId magicTrait, MIR.E e) { return null; }
  default MagicCallable<MIR.E,R> variantCall(MIR.E e) { return null; }
  ast.Program p();

}
