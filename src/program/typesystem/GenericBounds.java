package program.typesystem;

import ast.E;
import ast.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Streams;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface GenericBounds {
  static Optional<CompileError> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    return l.its().stream()
      .map(it->validGenericIT(p, xbs, it))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<CompileError> validGenericMeth(Program p, XBs xbs, E.Meth m) {
    throw Bug.todo();
  }

  static Optional<CompileError> validGenericIT(Program p, XBs xbs, Id.IT<T> it) {
    var innerInvalid = it.ts().stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    if (innerInvalid.isPresent()) { return innerInvalid; }

    var dec = p.of(it.name());
    return Streams.zip(it.ts(), dec.gxs())
      .map((t, gx) -> {
        var bounds = dec.bounds().get(gx);
        return validGenericMdf(xbs, bounds.isEmpty() ? XBs.defaultBounds : bounds, t);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<CompileError> validGenericT(Program p, XBs xbs, T t) {
    return t.match(
      gx->Optional.empty(),
      it->validGenericIT(p, xbs, it)
    );
  }

  static Optional<CompileError> validGenericMdf(XBs xbs, Set<Mdf> bounds, T t) {
    Supplier<Optional<CompileError>> errMsg = ()->Optional.of(Fail.invalidMdfBound(t, bounds.stream().sorted().toList()));
    if (!t.mdf().is(Mdf.mdf, Mdf.recMdf)) {
      return bounds.contains(t.mdf()) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isMdf()) {
      var bs = xbs.get(t.gxOrThrow());
      return bounds.containsAll(bs) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isRecMdf()) {
      var isOk = t.match(
        gx->{
          var bs = xbs.get(gx);
          if (bs.contains(Mdf.mut) || bs.contains(Mdf.iso)) {
            return bounds.containsAll(XBs.defaultBounds);
          }
          if (!bs.contains(Mdf.mut) && !bs.contains(Mdf.iso) && bs.contains(Mdf.lent)) {
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.read, Mdf.lent));
          }
          if (!bs.contains(Mdf.mut) && !bs.contains(Mdf.iso) && !bs.contains(Mdf.lent) && bs.contains(Mdf.read)) {
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.read));
          }
          if (bs.size() == 1 && bs.contains(Mdf.imm)) { return bounds.contains(Mdf.imm); }
          throw Bug.unreachable();
        },
        it->bounds.containsAll(XBs.defaultBounds)
      );
      return isOk ? Optional.empty() : errMsg.get();
    }
    throw Bug.unreachable();
  }
}
