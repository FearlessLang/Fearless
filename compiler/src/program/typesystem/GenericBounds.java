package program.typesystem;

import ast.E;
import failure.FailOr;
import program.Program;
import ast.T;
import failure.Fail;
import id.Mdf;
import program.CM;
import utils.Streams;

import java.util.List;
import java.util.Set;

public interface GenericBounds {
  Set<Mdf> ALL_RCS = Set.of(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.mutH, Mdf.read, Mdf.readH);

  static FailOr<Void> validGenericLambda(Program p, XBs xbs, E.Lambda l) {
    var res = l.its().stream()
      .map(it->it.accept(new KindingJudgement(p, it.toString(), xbs, ALL_RCS, true)))
      .filter(FailOr::isErr)
      .<FailOr<Void>>map(FailOr::cast)
      .findFirst();
    return res.orElseGet(FailOr::ok);
  }

  static FailOr<Void> validGenericMCall(Program p, XBs xbs, CM cm, List<T> typeArgs) {
    var typeParams = cm.sig().gens();
    if (typeArgs.size() != typeParams.size()) {
      return FailOr.err(()->Fail.genericMismatch(typeArgs, typeParams));
    }
    var res = Streams.zip(typeArgs, typeParams)
      .map((t, gx)->{
        //var bounds = cm.bounds().getOrDefault(gx, XBs.defaultBounds);//Should not be possible any more
        var bounds = cm.bounds().get(gx);
        assert bounds!=null;
        var inference = new KindingJudgement(p, cm.name().toString(), xbs, bounds, true);
        return t.accept(inference);
      })
      .filter(FailOr::isErr)
      .<FailOr<Void>>map(FailOr::cast)
      .findFirst();
    return res.orElseGet(FailOr::ok);
  }
}
