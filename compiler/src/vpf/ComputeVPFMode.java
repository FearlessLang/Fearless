package vpf;

import ast.E;
import ast.T;
import failure.FailOr;
import id.Mdf;
import program.typesystem.ETypeSystem;
import program.typesystem.Gamma;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface ComputeVPFMode {
  /**
   * Can a method call be parallelised via VPF?
   * Parallelisation of a method call can happen if there are two or more arguments (including the receiver) and either:
   * A. All arguments can be type-checked in an environment where all mut and mutH bindings are weakened to read and
   * readH respectively.
   * B. All arguments except one can be type-checked in an environment where all mut, mutH, read, and readH bindings are
   * not present in Gamma.
   */
  static VPFCallMode of(ETypeSystem ts, E.MCall call) {
    // must have at least 2 calls (incl. receiver)
    var subCalls = Stream.concat(Stream.of(call.receiver()), call.es().stream())
      .filter(e -> e instanceof E.MCall)
      .limit(2)
      .count();
    if (subCalls != 2) {
      return VPFCallMode.SEQUENTIAL;
    }

    var canPromoteNoMut = canPromoteNoMut(ts, call);
    if (canPromoteNoMut == VPFCallMode.PARALLEL) {
      return canPromoteNoMut;
    }
    var canPromoteOnlyOneLikeMut = canPromoteOnlyOneLikeMut(ts, call);
    if (canPromoteOnlyOneLikeMut == VPFCallMode.PARALLEL) {
      return canPromoteOnlyOneLikeMut;
    }
    return VPFCallMode.SEQUENTIAL;
  }

  private static VPFCallMode canPromoteNoMut(ETypeSystem ts, E.MCall call) {
    var g = ts.g();
    var weakenedGamma = new Gamma(){
      @Override public FailOr<Optional<T>> getO(String s) {
        return g.getO(s).map(optT->optT
          .filter(t->t.mdf() != Mdf.mdf)
          .map(t->switch (t.mdf()) {
            case mut -> t.withMdf(Mdf.read);
            case mutH -> t.withMdf(Mdf.readH);
            default -> t;
          })
        );
      }
      @Override public String toStr() {
        return "canPromoteNoMut:"+g.toStr();
      }
      @Override public List<String> dom() {
        return g.dom();
      }
    };

    ETypeSystem stricterTs = ts.withGamma(weakenedGamma);
    var isPromotable = call.es().stream()
      .allMatch(arg -> arg.accept(stricterTs).isRes());
    return isPromotable ? VPFCallMode.PARALLEL : VPFCallMode.SEQUENTIAL;
  }

  private static VPFCallMode canPromoteOnlyOneLikeMut(ETypeSystem ts, E.MCall call) {
    var g = ts.g();
    var weakenedGamma = new Gamma(){
      @Override public FailOr<Optional<T>> getO(String s) {
        return g.getO(s).map(optT->optT
          .filter(t->t.mdf() != Mdf.mdf && !t.mdf().isLikeMut())
        );
      }
      @Override public String toStr() {
        return "canPromoteOnlyOneLikeMut:"+g.toStr();
      }
      @Override public List<String> dom() {
        return g.dom();
      }
    };

    ETypeSystem stricterTs = ts.withGamma(weakenedGamma);

    for (int i = 0; i < call.es().size(); ++i) {
      final int excludedArg = i;
      var allOk = IntStream.range(0, call.es().size())
        .filter(j -> j != excludedArg)
        .mapToObj(j -> call.es().get(j).accept(stricterTs))
        .allMatch(FailOr::isRes);
      // we don't need to check excludedArg because we only check for a VPF
      // promotion after the base method call has already passed type checking
      if (allOk) {
        return VPFCallMode.PARALLEL;
      }
    }
    return VPFCallMode.SEQUENTIAL;
  }
}
