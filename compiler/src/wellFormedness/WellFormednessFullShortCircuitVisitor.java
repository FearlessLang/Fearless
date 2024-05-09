package wellFormedness;

import astFull.E;
import astFull.Program;
import astFull.T;
import failure.CompileError;
import failure.Fail;
import files.HasPos;
import id.Id;
import id.Mdf;
import magic.Magic;
import visitors.FullShortCircuitVisitor;
import visitors.FullShortCircuitVisitorWithEnv;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: Sealed and _C/_m restrictions
/*
  ✅Actual generic parameters can not be iso //checked in Alias, Lambda, mCall, and Meth
  ✅No explicitly declared this
  ✅Method and type parameter lists are disjoint
  ✅No shadowing:done x in meth decl, x in lambda, done Xs in meth decl for other Xs in scope
  ✅(done in package) Disjoint class names //overloading on generic arity
  ✅Disjoint method names //overloading on parameter arity
  ✅‘recMdf’ is not used as a the modifier for a method
  ✅'mdf' only in front of X, NOTE: this also excludes 'mdf' for meth or lambda modifier
  'mdf/recMdf' never as method modifier // TODO: This happens after inference
  recMdf only for read/lent methods
    recMdf on mut //no because pointless
    recMdf on imm //no because pointless/broken
    recMdf on iso //no because pointless
    recMdf on mdf or recMdf//no because not well formed already
    imm break(obj:recMdf X,a:imm A):Void->a.foo(obj)
    TODO: we should reundestand why. For example if I have a mut method returning recMdf Foo
    TODO: would this return a capsule Foo when called on a capsule receiver?
  ✅X in base.NoMutHyg[X] is a generic parameter on the interface implementing it
  ✅(done in parser) If the lambda type is inferred then at least "{}" must be present
  ✅(done in parser) In a B a SM of form ()->e is ill formed (but ok as an SM)
  ✅Implements relation is acyclic //ok to implement same trait with different generics
    that is, forall n,C C[mdf X1..mdf Xn] notin supertypes(Ds, C[mdf X1..mdf Xn]) where X1..Xn are fresh.
    Declarations being used are present
 */

/*
recMdf on mut methods:
A:{
  read .multiGet(hmm: recMdf Person, anotherRef: mut Ref[recMdf Person]): recMdf Person -> Block
    .do{ anotherRef := hmm }
    .return{ hmm }
}

IsoF[A,R]:{ #(a:iso A):mdf R }
Iso[R]:{ #:iso R }
.isoVar[A](fx:mut Iso[A],f:mut IsoF[A,mdf R]):mdf R
.mutVar[A](fx:mut F[mut A],f:mut F[mut A,mdf R]):mdf R
.let[A](fx:mut F[mdf A],f:mut F[mdf A,R]):mdf R
Evil:Main{
  #(s) -> Block
//    .let[iso Person] p = {Person'#23} // nvm this is an error because we cannot have iso generics. This implies no iso local vars
    .isoVar p = {Person'#23} // nvm this is an error because we cannot have iso generics. This implies no iso local vars
    .let[mut Ref[mut Person]] r = {Ref#Person'#0}
    .mutVar r = {Ref#Person'#0}//mut ref of mut Person
    .isoVar a = {}
    .let[imm Person]{ a.multiGet(p, r) } // I now have an imm and mut refs to the same person
    ...
 */

public class WellFormednessFullShortCircuitVisitor extends FullShortCircuitVisitorWithEnv<CompileError> {
  private Program p;

  @Override public Optional<CompileError> visitMCall(E.MCall e) {
    return super.visitMCall(e)
      .map(err->err.pos(e.posOrUnknown()));
  }

  @Override public Optional<CompileError> visitAlias(T.Alias a) {
    return super.visitAlias(a)
      .map(err->err.pos(a.posOrUnknown()));
  }

  @Override public Optional<CompileError> visitLambda(E.Lambda e) {
    return Optional.ofNullable(e.selfName())
      .flatMap(x->
        noExplicitThis(List.of(x))
        .or(()->noShadowingVar(List.of(x)))
      )
      .or(()->validLambdaMdf(e))
      .or(()->noImplInlineDec(e))
      .or(()->hasNonDisjointMs(e))
      .or(()->super.visitLambda(e))
      .map(err->err.parentPos(e.pos()));
  }

  @Override public Optional<CompileError> visitMeth(E.Meth e) {
    return hasNonDisjointXs(e.xs(), e)
      .or(()->noExplicitThis(e.xs()))
      .or(()->noShadowingVar(e.xs()))
      .or(()->hasNonDisjointXs(
        e.sig()
          .map(s->s.gens().stream().map(Id.GX::name).toList())
          .orElse(List.of()),
        e))
      .or(()->noShadowingGX(e.sig().map(E.Sig::gens).orElse(List.of())))
      .or(()->validMethMdf(e))
      .or(()->e.sig().flatMap(s->e.name().flatMap(name->noRecMdfInNonRecMdf(s, name))).map(err->err.pos(e.pos())))
      .or(()->super.visitMeth(e))
      .map(err->err.parentPos(e.pos()));
  }
  @Override public Optional<CompileError> visitT(T t){
    return GXOnlyRCs(t)
      .or(()->super.visitT(t));
  }
  @Override public Optional<CompileError> visitIT(Id.IT<T> t) {
    return noInvalidLiterals(t)
      .or(()->super.visitIT(t));
  }

  @Override
  public Optional<CompileError> visitDec(T.Dec d) {
    return hasNonDisjointXs(d.gxs().stream().map(Id.GX::name).toList(), d)
      .or(()->noSelfNameOnTopLevelDec(d.lambda()))
      .or(()->super.visitDec(d))
      .map(err->err.parentPos(d.pos()));
  }

  @Override public Optional<CompileError> visitProgram(Program p){
    this.p = p;
    return noCyclicImplRelations(p)
      .or(()->disjointDecls(p))
      .or(()->super.visitProgram(p));
  }

  @Override
  public Optional<CompileError> visitGX(Id.GX<T> t) {
    if (env.has(t)) { return super.visitGX(t); }
    return Optional.of(Fail.undefinedName(t.name()));
  }

  @Override
  public Optional<CompileError> visitX(E.X e) {
    if (env.has(e)) { return super.visitX(e); }
    return Optional.of(Fail.undefinedName(e.name()));
  }
//  private boolean hasUndeclaredXs(List<Id.IT<T>> its) {
//    return its.stream()
//      .flatMap(it->it.ts().stream())
//      .allMatch(t->t.match(gx->env.has(gx), it->hasUndeclaredXs(List.of(it))));
//  }
//  private boolean hasUndeclaredXs(List<Id.IT<T>> its) {
//    return its.stream()
//      .flatMap(it->it.ts().stream())
//      .allMatch(t->t.match(gx->env.has(gx), it->hasUndeclaredXs(List.of(it))));
//  }

  private Optional<CompileError> GXOnlyRCs(T t) {
    if(t.isInfer() || !t.mdf().is(Mdf.mdf, Mdf.readImm)){ return Optional.empty(); }
    return t.match(gx->Optional.empty(), it->Optional.of(Fail.invalidMdf(t)));
  }
  private Optional<CompileError> noExplicitThis(List<String> xs) {
    return xs.stream().anyMatch(x->x.equals("this")) ? Optional.of(Fail.explicitThis()) : Optional.empty();
  }
  private Optional<CompileError> noShadowingVar(List<String> xs) {
    return xs.stream().filter(x->this.env.has(x)).findFirst().map(Fail::shadowingX);
  }
  private Optional<CompileError> noShadowingGX(List<Id.GX<T>> xs) {
    return xs.stream().filter(x->this.env.has(x)).findFirst().map(x->Fail.shadowingGX(x.name()));
  }
  private Optional<CompileError> hasNonDisjointMs(E.Lambda e) {
    return hasNonDisjointAux(
      e.meths(),
      e,
      m->m.name().map(Object::toString).orElse("<unnamed>/"+m.xs().size()),
      Fail::conflictingMethNames
    );
  }
  private Optional<CompileError> hasNonDisjointXs(List<String> xs, E.Meth e) {
    return hasNonDisjointAux(xs,e,x->x,Fail::conflictingMethParams);
  }
  private Optional<CompileError> hasNonDisjointXs(List<String> xs, T.Dec d) {
    return hasNonDisjointAux(xs,d,x->x,Fail::conflictingMethParams);
  }

  private <TT> Optional<CompileError> hasNonDisjointAux(List<TT> xs, HasPos e, Function<TT,String> toS, Function<List<String>, CompileError> errF) {
    var all = xs.stream().filter(x->!x.equals("_")).collect(Collectors.toCollection(ArrayList::new));
    xs.stream().distinct().forEach(all::remove);
    if (all.isEmpty()) {
      return Optional.empty();
    }
    var conflicts = all.stream()
      .collect(Collectors.groupingBy(toS))
      .keySet().stream()
      .toList();
    return Optional.of(errF.apply(conflicts).pos(e.posOrUnknown()));
  }
  private Optional<CompileError> noCyclicImplRelations(Program p) {
    for(var entry:p.ds().entrySet()){
      var key = entry.getKey();
      var ks = p.superDecIds(key);
      if(ks.contains(key)){ return Optional.of(Fail.cyclicImplRelation(key).pos(entry.getValue().pos())); }
    }
    return Optional.empty();
  }

  private Optional<CompileError> validMethMdf(E.Meth e) {
    return e.sig().flatMap(sig->e.mdf().flatMap(mdf->{
      if (!mdf.is(Mdf.mdf, Mdf.readImm)) { return Optional.empty(); }
      return Optional.of(Fail.invalidMethMdf(e.sig().get(), e.name().orElseThrow()));
    }));
  }

  private Optional<CompileError> validLambdaMdf(E.Lambda e) {
    return e.mdf().flatMap(mdf->{
      if (mdf.is(Mdf.readImm, Mdf.lent, Mdf.readOnly)) { return Optional.of(Fail.invalidLambdaMdf(mdf)); }
      return Optional.empty();
    });
  }

  private Optional<CompileError> noRecMdfInNonRecMdf(E.Sig s, Id.MethName name) {
    var mdf = name.mdf().orElseThrow();
    if (mdf.isRecMdf()) { return Optional.empty(); }
    return new FullShortCircuitVisitor<CompileError>(){
      @Override public Optional<CompileError> visitT(T t) {
        if (t.mdf().isRecMdf()) {
          return Optional.of(Fail.recMdfInNonRecMdf(mdf, name, t).pos(s.pos()));
        }
        return FullShortCircuitVisitor.super.visitT(t);
      }
    }.visitSig(s);
  }

  private Optional<CompileError> noSelfNameOnTopLevelDec(E.Lambda e) {
    if (e.selfName() == null) { return Optional.empty(); }
    return Optional.of(Fail.namedTopLevelLambda().pos(e.pos()));
  }

  private Optional<CompileError> noImplInlineDec(E.Lambda e) {
    if (e.its().stream().noneMatch(it->p.isInlineDec(it.name()) && !e.id().id().equals(it.name()))) {
      return Optional.empty();
    }
    return Optional.of(Fail.implInlineDec(
      e.its().stream().map(Id.IT::name).filter(d->p.isInlineDec(d) && !e.id().id().equals(d)).toList()
    ));
  }

  private Optional<CompileError> noInvalidLiterals(Id.IT<T> it) {
    if (!Magic.isLiteral(it.name().name())) { return Optional.empty(); }
    return Magic.validateLiteral(it.name());
  }

  private Optional<CompileError> disjointDecls(Program p) {
    var inline = p.inlineDs().keySet();
    var topLevel = p.ds().keySet();

    if (Collections.disjoint(inline, topLevel)) {
      return Optional.empty();
    }

    var allConflicts = new HashSet<>(inline);
    allConflicts.retainAll(topLevel);
    var conflicts = allConflicts.stream()
      .map(p::of)
      .map(d->new Fail.Conflict(d.posOrUnknown(), d.name().toString()))
      .toList();
    return Optional.of(Fail.conflictingDecls(conflicts));
  }
}
