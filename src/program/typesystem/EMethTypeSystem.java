package program.typesystem;

import ast.E;
import ast.T;
import failure.CompileError;
import failure.Fail;
import failure.Res;
import id.Id;
import id.Id.GX;
import id.Id.MethName;
import id.Mdf;
import program.CM;
import program.TypeRename;
import utils.Bug;
import utils.Mapper;
import utils.Push;
import utils.Streams;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface EMethTypeSystem extends ETypeSystem {
  // TODO: we have to be more permissive first (i.e. mut before read/imm) because e0.accept(v) will work until the final step and then fail, at which point we cannot backtrack.
  List<Mdf> recvPriority = List.of(Mdf.iso, Mdf.mut, Mdf.imm, Mdf.recMdf, Mdf.read, Mdf.lent, Mdf.readOnly);
//  List<Mdf> recvPriority = List.of(Mdf.readOnly, Mdf.imm, Mdf.read, Mdf.recMdf, Mdf.iso, Mdf.lent, Mdf.mut);
//  List<Mdf> inferPriority = recvPriority;
  static List<Mdf> inferPriority(Mdf recvMdf) {
    var base = recvPriority.stream().filter(mdf->mdf != recvMdf).collect(Collectors.toCollection(ArrayList::new));
    return Push.of(recvMdf, base);
  }

  default Optional<Supplier<? extends CompileError>> visitMCall(E.MCall e) {
    var guessType = new GuessIT(p(), g(), xbs(), depth());
    Optional<Supplier<? extends CompileError>> error = Optional.empty();
    for (var expectedRecv : e.receiver().accept(guessType)) {
      var res = visitMCall(e, expectedRecv);
      if (res.isEmpty()) { return res; }
      if (error.isEmpty()) { error = res; }
    }
    return error;
  }
  default Optional<Supplier<? extends CompileError>> visitMCall(E.MCall e, Id.IT<T> recvIT) {
    var guessType = new GuessITX(p(), g(), xbs(), depth());
    var guessFullType = new GuessT(p(), g(), xbs(), depth());
    var recvMdf = e.receiver().accept(guessFullType).stream().map(T::mdf).findFirst().orElse(Mdf.recMdf);
    var optTst=multiMeth(recvIT, recvMdf, e.name(), e.ts());
    if (optTst.isEmpty()) {
      return Optional.of(()->Fail.undefinedMethod(e.name(), new T(recvMdf, recvIT), p().meths(xbs(), Mdf.recMdf, recvIT, depth()).stream()));
    }
    List<TsT> tsts = optTst.stream()
      .filter(this::filterOnRes)
      .toList();

    if (tsts.isEmpty()) {
      return Optional.of(()->Fail.noCandidateMeths(e, expectedT().orElseThrow(), optTst.stream().distinct().toList()).pos(e.pos()));
    }

    List<E> es = Push.of(e.receiver(),e.es());
    var nestedErrors = new ArrayDeque<ArrayList<Supplier<? extends CompileError>>>(tsts.size());
    var methArgCache = IntStream.range(0, es.size()).mapToObj(i_->new HashMap<T, Res>()).toList();
    for (var tst : tsts) {
      var errors = new ArrayList<Supplier<? extends CompileError>>();
      nestedErrors.add(errors);
      if (okAll(es, tst.ts(), errors, methArgCache, guessType)) {
        var recvT = tst.ts().get(0);
        var invalidBounds = GenericBounds.validGenericMeth(p(), xbs(), recvT.mdf(), recvT.itOrThrow(), depth(), tst.original(), e.ts());
        if (invalidBounds.isPresent()) {
          return Optional.of(()->invalidBounds.get().get().pos(e.pos()));
        }

        var expected = expectedT().orElseThrow();
        if (!p().isSubType(xbs(), tst.t(), expected)) {
          return Optional.of(()->Fail.methTypeError(expected, tst.t(), e.name()).pos(e.pos()));
        }
        resolvedCalls().put(e.callId(), tst);
        return Optional.empty();
      }
    }

    return Optional.of(()->{
      var calls1 = tsts.stream()
        .map(tst1->{
          var call = CompletableFuture.supplyAsync(()->Streams.zip(es, tst1.ts()).map((e1,t1)->guessToStr(e1.accept(guessFullType)))
              .collect(Collectors.joining(", ")))
            .completeOnTimeout("<timed out>", 100, TimeUnit.MILLISECONDS)
            .exceptionally(err->switch (err.getCause()) {
              case CompileError ce -> ce.header();
              default -> throw Bug.of(err);
            })
            .join();
          var dependentErrorMsgs = nestedErrors.removeFirst().stream()
            .map(Supplier::get)
            .map(CompileError::toString)
            .collect(Collectors.joining("\n"))
            .indent(4);
          var dependentErrors = !dependentErrorMsgs.isEmpty()
            ? "\n"+"The following errors were found when checking this sub-typing:\n".indent(2)+dependentErrorMsgs
            : "";
          return "("+ String.join(", ", call) +") <= "+tst1+dependentErrors;
        }).toList();
      var calls = String.join("\n", calls1);
      return Fail.callTypeError(e, expectedT(), calls).pos(e.pos());
    });
  }
  default String guessToStr(Set<T> guessedTypes) {
    if (guessedTypes.size() == 1) { return guessedTypes.stream().findFirst().get().toString(); }
    return guessedTypes.toString();
  }
  default boolean filterOnRes(TsT tst){
    if(expectedT().isEmpty()){ return true; }
    return p().isSubType(xbs(), tst.t(), expectedT().get());
  }
  default boolean okAll(List<E> es, List<T> ts, ArrayList<Supplier<? extends CompileError>> errors, List<HashMap<T, Res>> caches, GuessITX guessType) {
    assert es.size() == ts.size() && caches.size() == es.size();
    return IntStream.range(0, es.size())
      .allMatch(i->ok(es.get(i), ts.get(i), errors, caches.get(i), guessType));
  }
  default boolean ok(E e, T t, ArrayList<Supplier<? extends CompileError>> errors, HashMap<T, Res> cache, GuessITX guessType) {
//    var res = cache.computeIfAbsent(t, t_->e.accept(this.withT(Optional.of(t_))));
    // TODO: cache res based on the same visitor hashcode
    /*
     v has program (will stay the same)
     v has the T
     v has a gamma (should stay the same)
     v has a XBs (should stay the same)
     v has a depth (should stay the same)
     */
    var res = e.accept(this.withT(Optional.of(t)));
    if (res.isPresent()) {
      errors.add(res.get());
      return false;
    }
    return true;
//    return e.accept(guessType).stream().anyMatch(exprT->p().tryIsSubType(xbs(), new T(t.mdf(), exprT), t));
//    return p().tryIsSubType(xbs(), res.tOrThrow(), t);
  }

  default List<TsT> multiMeth(Id.IT<T> recIT, Mdf recvMdf, MethName m, List<T> ts) {
    // TODO: throw error (no invoking methods on GXs)
    return p().meths(xbs(), Mdf.recMdf, recIT, depth()).stream()
      .filter(cm->cm.name().nameArityEq(m))
      .sorted(Comparator.comparingInt(cm->recvPriority.indexOf(cm.mdf())))
      .map(cm->{
        Map<GX<T>,T> xsTsMap = Mapper.of(c->Streams.zip(cm.sig().gens(), ts).forEach(c::put));
        var xbs = xbs().addBounds(cm.sig().gens(), cm.sig().bounds());

        var params = Push.of(
          fancyRename(recIT.toString(), new T(cm.mdf(), recIT), recvMdf, xsTsMap, TypeRename.RenameKind.Arg, xbs),
          Streams.zip(cm.xs(), cm.sig().ts())
            .map((xi, ti)->fancyRename(xi+": "+ti.rt().toString(), ti, recvMdf, xsTsMap, TypeRename.RenameKind.Arg, xbs))
            .toList()
        );
        var t = fancyRename(cm.ret().rt().toString(), cm.ret(), recvMdf, xsTsMap, TypeRename.RenameKind.Return, xbs);

        return new TsT(params, t, cm);
      })
      .flatMap(this::allMeth)
      .toList();
  }

  default Stream<TsT> allMeth(TsT tst) {
    return Stream.concat(Stream.of(
      tst,
      tst.renameMdfs(Map.of(
        Mdf.mut, Mdf.iso,
        Mdf.read, Mdf.imm,
        Mdf.mdf, Mdf.iso
      )).renameTsMdfs(Map.of(Mdf.mdf, Mdf.iso)).renameTMdfs(Map.of(Mdf.mdf,Mdf.imm)),
      tst.renameMdfs(Map.of(
        Mdf.readOnly, Mdf.imm,
        Mdf.read, Mdf.imm,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso
      )).renameTsMdfs(Map.of(Mdf.mdf, Mdf.iso)).renameTMdfs(Map.of(Mdf.mdf,Mdf.imm)),
      tst.renameTsMdfs(Map.of(
        Mdf.read, Mdf.readOnly,
        Mdf.lent, Mdf.iso,
        Mdf.mut, Mdf.iso,
        Mdf.mdf, Mdf.iso
      )).renameTMdfs(Map.of(Mdf.mut,Mdf.lent,    Mdf.read,Mdf.readOnly,  Mdf.mdf,Mdf.imm))),
      oneLentToMut(tst.renameTsMdfs(Map.of(Mdf.mdf,Mdf.iso)).renameTMdfs(Map.of(Mdf.mdf,Mdf.imm))).stream())
      .distinct();
  }

  /** This is [MDF, Xs=Ts] (recMdf rewriting for meth calls) */
  default T fancyRename(String x, T t, Mdf mdf0, Map<GX<T>,T> map, TypeRename.RenameKind kind, XBs xbs) {
    assert !mdf0.isMdf();
    Mdf mdf=t.mdf();
    return t.match(
      gx->{
        if(!mdf.isRecMdf()){
          var renamed = map.getOrDefault(gx, t);
          return TypeRename.core(p()).propagateMdf(mdf, renamed);
        }
        var ti = map.get(gx);
        if (ti == null) { return t; }

        return switch (kind) {
          case Return -> ti.withMdf(mdf0.adapt(ti));
          case Arg -> {
//            if (ti.mdf().isMdf()) { yield ti.withMdf(mdf0.adapt(ti)); } // this is probably unsound
            var newMdf = Gamma.xT(x, xbs, mdf0, ti, mdf0).mdf();
            yield ti.withMdf(newMdf);
          }
        };
      },
      it->{
        var newTs = it.ts().stream().map(ti->fancyRename(x, ti, mdf0, map, TypeRename.RenameKind.Arg, xbs)).toList();
        if(!mdf.isRecMdf() && !mdf.isMdf()){ return new T(mdf, it.withTs(newTs)); }
        if(mdf0.isIso()) { return new T(Mdf.mut, it.withTs(newTs)); }
        return new T(mdf0, it.withTs(newTs));
      });
  }
  default List<T> mutToIso(List<T> ts){
    return ts.stream().map(this::mutToIso).toList();
  }
  default T mutToIso(T t){ return t.mdf().isMut() ? t.withMdf(Mdf.iso) : t; }
  default T mutToLent(T t) { return t.mdf().isMut() ? t.withMdf(Mdf.lent) : t; }
  default TsT transformMuts(int i, List<T> ts, T t, CM original){
    var ts0 = IntStream.range(0,ts.size()).mapToObj(j->j==i
      ? ts.get(i).withMdf(Mdf.lent)
      : mutToIso(ts.get(j))
    ).toList();
    return new TsT(ts0, mutToLent(t), original);
  }
  default List<TsT> oneLentToMut(TsT tst){
    var ts = tst.ts();
    var t = tst.t();
    Stream<TsT> r = Stream.of(tst.with(mutToIso(ts), mutToLent(t)));
    var muts = IntStream.range(0,ts.size())
      .filter(i->ts.get(i).mdf().isMut()).boxed().toList();
    Stream<TsT> ps=muts.stream()
      .map(i->transformMuts(i, ts, t, tst.original));
    return Stream.concat(r,ps).toList();
  }

  static boolean containsRecMdf(T t) {
    return t.mdf().isRecMdf() || t.match(
      gx->false,
      it->it.ts().stream().anyMatch(EMethTypeSystem::containsRecMdf)
    );
  }

  record TsT(List<T> ts, T t, CM original){
    public TsT with(List<T> ts, T t) {
      return new TsT(ts, t, original);
    }
    public TsT renameMdfs(Map<Mdf, Mdf> replacements) {
      var ts = renameTsMdfs(replacements).ts();
      var t = renameTMdfs(replacements).t();
      return with(ts, t);
    }
    public TsT renameTsMdfs(Map<Mdf, Mdf> replacements) {
      List<T> ts = ts().stream().map(ti->ti.withMdf(replacements.getOrDefault(ti.mdf(), ti.mdf()))).toList();
      return with(ts, t);
    }
    public TsT renameTMdfs(Map<Mdf, Mdf> replacements) {
      T t = t().withMdf(replacements.getOrDefault(t().mdf(), t().mdf()));
      return with(ts, t);
    }
    @Override public String toString() {
      return "("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
    }
  }
}
