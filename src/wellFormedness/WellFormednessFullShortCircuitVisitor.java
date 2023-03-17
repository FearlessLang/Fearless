package wellFormedness;

import astFull.E;
import astFull.T;
import files.HasPos;
import id.Id;
import id.Mdf;
import magic.Magic;
import failure.CompileError;
import failure.Fail;
import astFull.Program;
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
.var[A](fx:mut F[mdf A],f:mut F[mdf A,R]):mdf R
Evil:Main{
  #(s) -> Block
//    .var[iso Person] p = {Person'#23} // nvm this is an error because we cannot have iso generics. This implies no iso local vars
    .isoVar p = {Person'#23} // nvm this is an error because we cannot have iso generics. This implies no iso local vars
    .var[mut Ref[mut Person]] r = {Ref#Person'#0}
    .mutVar r = {Ref#Person'#0}//mut ref of mut Person
    .isoVar a = {}
    .var[imm Person]{ a.multiGet(p, r) } // I now have an imm and mut refs to the same person
    ...
 */
public class WellFormednessFullShortCircuitVisitor extends FullShortCircuitVisitorWithEnv<CompileError> {
  @Override public Optional<CompileError> visitMCall(E.MCall e) {
    return e.ts().flatMap(this::noIsoParams)
      .or(()->super.visitMCall(e))
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
      .or(()->hasNonDisjointMs(e))
      .or(()->super.visitLambda(e))
      .map(err->err.pos(e.posOrUnknown()));
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
      .map(err->err.pos(e.posOrUnknown()))
      .or(()->super.visitMeth(e));
  }
  @Override public Optional<CompileError> visitT(T t){
    return mdfOnlyOnGX(t)
      .or(()->super.visitT(t));
  }
  @Override public Optional<CompileError> visitIT(Id.IT<T> t) {
    return noIsoParams(t.ts())
      .or(()->super.visitIT(t));
  }

  @Override
  public Optional<CompileError> visitDec(T.Dec d) {
    return noMutHygValid(d).map(err->err.pos(d.posOrUnknown()))
      .or(()->hasNonDisjointXs(d.gxs().stream().map(Id.GX::name).toList(), d))
      .or(()->super.visitDec(d));
  }

  @Override public Optional<CompileError> visitProgram(Program p){
    return noCyclicImplRelations(p)
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

  private Optional<CompileError> noIsoParams(List<T> genArgs) {
    return genArgs.stream()
      .flatMap(T::flatten)
      .dropWhile(t->t.mdf() != Mdf.iso)
      .map(Fail::isoInTypeArgs)
      .findFirst();
  }
  private Optional<CompileError> mdfOnlyOnGX(T t) {
    if(t.isInfer() || !t.mdf().isMdf()){ return Optional.empty(); }
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
    var all = new ArrayList<>(xs);
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
    for(var key:p.ds().keySet()){
      var ks = p.superDecIds(key);
      if(ks.contains(key)){ return Optional.of(Fail.cyclicImplRelation(key)); }
    }
    return Optional.empty();
  }

  private Optional<CompileError> noMutHygValid(T.Dec dec) {
    return dec.lambda().its().stream()
      .filter(it->it.name().equals(Magic.NoMutHyg))
      .flatMap(it->it.ts().stream())
      .<Optional<CompileError>>map(t->t.match(
          gx->dec.gxs().contains(gx) ? Optional.empty() : Optional.of(Fail.invalidNoMutHyg(t)),
          it->Optional.of(Fail.concreteInNoMutHyg(t))
      ))
      .dropWhile(Optional::isEmpty)
      .findFirst()
      .flatMap(o->o);
  }

  private Optional<CompileError> validMethMdf(E.Meth e) {
    return e.sig().flatMap(m->{
      var ismMdfOrRecMdf = m.mdf().isMdf() || m.mdf().isRecMdf();
      if (!ismMdfOrRecMdf) { return Optional.empty(); }
      return Optional.of(Fail.invalidMethMdf(e.sig().get(), e.name().get()));
    });
  }
}

/*
//TODO: move it in a better place
For the standard library
we need an input format

precompiled Opt
package base
Opt[X]{
  .match(..):T->this
  }

HasId:{  .same[X](mut X x, read X y):Bool Native["java{ return x==y; }"]

Native:{ .get[T](name: Str): T }
 */