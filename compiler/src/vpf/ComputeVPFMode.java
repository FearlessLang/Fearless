package vpf;

import ast.E;
import ast.T;
import failure.FailOr;
import id.Mdf;
import program.typesystem.ETypeSystem;
import program.typesystem.Gamma;
import program.typesystem.TraitTypeSystem;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface ComputeVPFMode {
  /// Can a method call be parallelised via VPF?
  /// Parallelisation of a method call can happen if there are two or more arguments (including the receiver) and either:
  /// A. All arguments can be type-checked in an environment where all mut and mutH bindings are weakened to read and
  /// readH respectively.
  /// B. All arguments except one can be type-checked in an environment where all mut, mutH, read, and readH bindings are
  /// not present in Gamma. (This is currently disabled because it is incompatible with flows)
  @SuppressWarnings("preview")
  static VPFCallMode of(ETypeSystem ts, E.MCall call) {
    // Sadly, the flow runtime (which is somewhat magical) can break assumptions around
    // nothing else running that could mutate a thing a read or readH is pointing to in A,
    // so if we have both VPF and flows running we have to make sure that VPF is not parallelising
    // any code that the flow runtime could mutate in parallel.
    if (TraitTypeSystem.pkg.get().equals("base.flows")) {
      return VPFCallMode.Sequential;
    }

    // must have at least 2 calls (incl. receiver)
    var subCalls = Stream.concat(Stream.of(call.receiver()), call.es().stream())
      .filter(e -> e instanceof E.MCall)
      .limit(2)
      .count();
    if (subCalls != 2) {
      return VPFCallMode.Sequential;
    }

    var canPromoteNoMut = canPromoteNoMut(ts, call);
    if (canPromoteNoMut == VPFCallMode.Parallel) {
      return canPromoteNoMut;
    }
//    TODO: this breaks flows in weird and racy ways:
//    var canPromoteOnlyOneLikeMut = canPromoteOnlyOneLikeMut(ts, call);
//    if (canPromoteOnlyOneLikeMut == VPFCallMode.Parallel) {
//      return canPromoteOnlyOneLikeMut;
//    }
    return VPFCallMode.Sequential;
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
    return isPromotable ? VPFCallMode.Parallel : VPFCallMode.Sequential;
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
        return VPFCallMode.Parallel;
      }
    }
    return VPFCallMode.Sequential;
  }
}
