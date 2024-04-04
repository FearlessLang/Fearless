package program.typesystem;

import ast.E;
import program.Program;
import ast.T;
import failure.CompileError;
import failure.Fail;
import id.Id;
import id.Mdf;
import program.CM;
import utils.Bug;
import utils.Streams;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface GenericBounds {
  static Optional<? extends Supplier<? extends CompileError>> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    return l.its().stream()
      .map(it->validGenericIT(p, xbs, it))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<? extends Supplier<? extends CompileError>> validGenericMeth(Program p, XBs xbs, Mdf recvMdf, Id.IT<T> recvIT, int depth, CM cm, List<T> typeArgs) {
    var gensValid = typeArgs.stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    if (gensValid.isPresent()) { return gensValid; }

    // TODO: throw error if type args.len != cm.sig.len (user code is wrong)
    return Streams.zip(typeArgs, cm.sig().gens())
      .map((t, gx)->{
        var bounds = cm.bounds().getOrDefault(gx, XBs.defaultBounds);
        return validGenericMdf(xbs, bounds.isEmpty() ? XBs.defaultBounds : bounds, t);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<? extends Supplier<? extends CompileError>> validGenericIT(Program p, XBs xbs, Id.IT<T> it) {
    var innerInvalid = it.ts().stream()
      .map(t->validGenericT(p, xbs, t))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
    if (innerInvalid.isPresent()) { return innerInvalid; }

    var dec = p.of(it.name());
    var gxs = switch (dec) {
      case T.Dec d -> d.gxs();
      case astFull.T.Dec d -> d.gxs();
    };
    var boundMap = switch (dec) {
      case T.Dec d -> d.bounds();
      case astFull.T.Dec d -> d.bounds();
    };
    return Streams.zip(it.ts(), gxs)
      .map((t, gx) -> {
        var bounds = boundMap.get(gx);
        return validGenericMdf(xbs, bounds == null || bounds.isEmpty() ? XBs.defaultBounds : bounds, t);
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      .findAny();
  }

  static Optional<? extends Supplier<? extends CompileError>> validGenericT(Program p, XBs xbs, T t) {
    return t.match(
      gx->Optional.empty(),
      it->validGenericIT(p, xbs, it)
    );
  }

  static Optional<Supplier<? extends CompileError>> validGenericMdf(XBs xbs, Set<Mdf> bounds, T t) {
    Supplier<Optional<Supplier<? extends CompileError>>> errMsg = ()->Optional.of(()->Fail.invalidMdfBound(t, bounds.stream().sorted().toList()));
    if (!t.mdf().is(Mdf.mdf, Mdf.recMdf, Mdf.readImm)) {
      return bounds.contains(t.mdf()) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isReadImm()) {
      return bounds.containsAll(Set.of(Mdf.read, Mdf.imm)) ? Optional.empty() : errMsg.get();
    }

    if (t.mdf().isMdf()) {
      var bs = xbs.get(t.gxOrThrow());
      if (!bounds.containsAll(bs)) {
        System.out.println("sgsrg");
      }
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
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.readOnly, Mdf.lent));
          }
          if (!bs.contains(Mdf.mut) && !bs.contains(Mdf.iso) && !bs.contains(Mdf.lent) && bs.contains(Mdf.readOnly)) {
            return bounds.containsAll(Set.of(Mdf.imm, Mdf.readOnly));
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
