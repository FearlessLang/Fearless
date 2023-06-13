package program.typesystem;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import static utils.Base.load;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestRecMdf {
  @Test void shouldCollapseWhenCalled1() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      mut .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1a() { ok("""
    package test
    A:{
      read .m1: recMdf A -> {},
      mut .m2: read A -> this.m1,
      }
    """); }
  @Test void shouldCollapseWhenCalled1b() { fail("""
    In position [###]/Dummy0.fear:5:20
    [E28 undefinedName]
    The identifier "this" is undefined or cannot be captured.
    """, """
    package test
    A:{
      // Broken because with or without NoPromote, makes mut this -> imm!
      read .m1(_: mut NoPromote): recMdf A -> this,
      mut .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1c() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      imm .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1d() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut test.A[]. The candidates were:
    (read test.A[], mut test.NoPromote[]): imm test.A[]
    (read test.A[], iso test.NoPromote[]): imm test.A[]
    (imm test.A[], iso test.NoPromote[]): imm test.A[]
    """, """
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      imm .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }

  @Test void shouldCollapseWhenCalledGenMut() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, _: mut NoPromote): recMdf X,
      mut .m2(a: mdf X): mdf X -> this.m1(a, mut NoPromote{}),
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenMut2() { ok("""
    package test
    A[X]:{
      read .get: recMdf X -> this.loop,
      read .loop[T]: mdf T -> this.loop,
      }
    B:{
      .m1Mut[Y](a: mut A[mut      Y]): mut Y     -> a.get,
      .m2Mut   (a: mut A[mut Person]): mut Person-> a.get,
      
      .m1Read[Y](a: read A[read      Y]): read Y     -> a.get,
      .m2Read   (a: read A[read Person]): read Person-> a.get,
      
      .m1Lent[Y](a: lent A[lent      Y]): lent Y     -> a.get,
      .m2Lent   (a: lent A[lent Person]): lent Person-> a.get,
      
      .m1Mdf[Y](a: mut A[mdf      Y]): mdf Y     -> a.get,
      //.m2Mdf   (a: mdf A[mdf Person]): mdf Person-> a.get,
      
      .m1Imm[Y](a: imm A[imm      Y]): imm Y     -> a.get,
      .m1_Imm[Y](a: mut A[imm      Y]): imm Y     -> a.get,
      .m2Imm   (a: imm A[imm Person]): imm Person-> a.get,
      .m2_Imm   (a: mut A[imm Person]): imm Person-> a.get,
      }
    Person:{}
    """); }
  @Test void shouldCollapseWhenCalledGenImm() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      imm .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenRead1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: read X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenRead2() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: read X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenLent() { fail("""
    In position [###]/Dummy0.fear:4:28
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type recMdf X. The candidates were:
    (read test.A[mdf X], mut test.NoPromote[]): mdf X
    (read test.A[mdf X], iso test.NoPromote[]): mdf X
    (imm test.A[mdf X], iso test.NoPromote[]): mdf X
    """, """
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      lent .m2: recMdf X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenIso1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      iso .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  // should fail because iso can capture imm too
  @Test void shouldCollapseWhenCalledGenIso2() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut X. The candidates were:
    (read test.A[mdf X], mut test.NoPromote[]): imm X
    (read test.A[mdf X], iso test.NoPromote[]): imm X
    (imm test.A[mdf X], iso test.NoPromote[]): imm X
    """, """
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      iso .m2: iso X -> this.m1{},
      }
    NoPromote:{}
    """); }

  @Test void shouldCollapseWhenCalledNestedGenMut1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[mdf X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenMut2() { fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .m2/0 to return imm test.Consumer[imm X], got imm test.Consumer[mdf X].
    """, """
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenImm1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      imm .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Test void shouldCollapseWhenCalledNestedGenSplitImm1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenSplitMut1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> mut A'[imm X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenSplitMut2() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[mdf X] -> mut A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Test void shouldApplyRecMdfInTypeParams1a() { ok("""
    package test
    Opt[T]:{ read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none, }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void shouldApplyRecMdfInTypeParams1b() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T]:NoMutHyg[mdf T]{
      read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T,R]:NoMutHyg[mdf R]{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void boxAndMatcher() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
      read .match[R](m: mut OptMatch[recMdf T, recMdf R]): recMdf R -> m.some(x),
      }}
    Opt[T]:NoMutHyg[mdf T]{
      read .match[R](m: mut OptMatch[recMdf T, recMdf R]): recMdf R -> m.none,
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void boxAndMatcherWithMapMut() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
      mut .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.some(x),
      }}
    Opt[T]:NoMutHyg[mdf T]{
      mut .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.none,
      mut .map[R](f: mut OptMap[mdf T, mdf R]): mut Opt[mdf R] -> this.match({
        .some(x) -> Opt#(f#x),
        .none -> {}
        })
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:{ mut #(x: mdf T): mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void boxAndMatcherWithMapLent() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
      lent .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.some(x),
      }}
    Opt[T]:NoMutHyg[mdf T]{
      lent .match[R](m: mut OptMatch[mdf T, mdf R]): mdf R -> m.none,
      lent .map[R](f: mut OptMap[mdf T, mdf R]): mut Opt[mdf R] -> this.match({
        .some(x) -> Opt#(f#x),
        .none -> {}
        })
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:{ mut #(x: mdf T): mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void boxAndMatcherDedicated() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> {
      read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.some(x),
      }}
    Opt[T]:NoMutHyg[mdf T]{
      read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      read .map[R](f: mut OptMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match(f),
//      read .map[R](f: mut OptMatch[mdf T, mut Opt[mdf R]]): mut Opt[mdf R] -> this.match(f),
      read .flatMap[R](f: mut OptFlatMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match(f),
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:OptMatch[mdf T, mut Opt[mdf R]]{
      mut #(x: mdf T): mdf R,
      .some(x) -> Opt#(this#x),
      .none -> {},
      }
    OptFlatMap[T,R]:OptMatch[mdf T, mut Opt[mdf R]]{
      mut #(x: mdf T): mut Opt[mdf R],
      .some(x) -> this#x,
      .none -> {},
      }
    Usage:{ #(a: Opt[Foo]): Opt[Bar] -> a.map{_ -> Bar} }
    Foo:{} Bar:{}
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void inferRecMdf1() { ok("""
    package test
    Foo[T]:{
      read .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x: mdf T): mdf T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map(F[A]{ _->A }) }
    """); }
  @Test void inferRecMdf2() { ok("""
    package test
    Foo[T]:{
      read .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x: mdf T): mdf T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map{ _->A } }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3a() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3b() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: imm A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3c() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: mut A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3d() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      mut #(a: mut A[mut B]): mut B -> a.m1(this, F[mut B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3e() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: mut A[mut B]): mut B -> a.m1(mut B, F[mut B]),
      }
    """); }
  // TODO: This is failing because our check that disables fancyRename is shallow
  @Test void shouldApplyRecMdfInTypeParams4a() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] ->
        a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4aV2() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> this.loop,
      read .loop[R]: mdf R -> this.loop,
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4b() { fail("""
    In position [###]/Dummy0.fear:10:5
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    a .m1/2[]([[-recMdf-][test.B[recMdf Y]]{'fear0$ }, [-imm-][test.F[mut test.B[recMdf Y]]]{'fear1$ }])
    were valid:
    (mut test.A[mut test.B[recMdf Y]], recMdf test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]) <: (read test.A[mut test.B[recMdf Y]], iso test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]): iso test.B[recMdf Y]
    (mut test.A[mut test.B[recMdf Y]], recMdf test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]) <: (imm test.A[mut test.B[recMdf Y]], iso test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]): iso test.B[recMdf Y]
    """, """
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    // Fails because if B[Y] is imm, we'll fail because recMdf X will be mut B[..] and recMdf B[..] in the rt here
    // will be imm B[..]
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): recMdf B[recMdf Y] ->
        a.m1(recMdf B[recMdf Y], F[recMdf B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4c() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] ->
        a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      .ii(b: mut B[imm C]): imm B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  // These are okay because recMdf X where MDF X = imm X becomes imm X.
  // this method always returns imm X in this case.
  @Test void noCaptureImmAsRecMdf() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfExample() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: imm B -> (A.m(B)).absMeth }
    """); }
  @Test void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear1$ } .m/1[]([[-imm-][test.B[]]{'fear2$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type lent test.B[]. The candidates were:
    (read test.L[imm test.B[]]): imm test.B[]
    (imm test.L[imm test.B[]]): imm test.B[]
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: lent B -> (A.m(B)).absMeth }
    """); }
  @Test void noCaptureImmAsRecMdfTopLvl1() { ok("""
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    L'[X]:L[imm X]{ read .absMeth: imm X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfTopLvl2() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:7) test.L[mdf FearX0$], .absMeth/0[](): mdf FearX0$
    ([###]/Dummy0.fear:4:16) test.L'[mdf FearX0$], .absMeth/0[](): imm FearX0$
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: recMdf X }
    L'[X]:L[mdf X]{ read .absMeth: imm X }
    A:{ read .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }

  @Test void recMdfInheritance() { ok("""
    package test
    Foo:{}
    A[X]:{ read .m: recMdf X -> Loop# }
    B:A[imm Foo]
    C:B
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ read .m(par: mut C) : imm Foo -> par.m  }
//    NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    Loop:{ #[X]: mdf X -> this# }
    """); }

  @Test void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:8:48
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    (read test.B[]): imm test.Foo[]
    (imm test.B[]): imm test.Foo[]
    """, """
    package test
    Foo:{}
    Loop:{ #[X]: mdf X -> this# }
    A[X]:{ read .m: recMdf X -> Loop# }
    B:A[imm Foo]{}
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void recMdfInSubHyg1() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void recMdfInSubHyg1NotInline() { ok("""
    package test
    B:{ #[X](x: mut X): mut B[mut X] -> { x } }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void recMdfInSubHyg2() { ok("""
    package test
    A:{ .foo(x: mut Foo): mut Foo -> mut B{ x }.argh }
    B:{ read .argh: recMdf Foo }
    Foo:{}
    """); }
  @Test void recMdfInSubHyg3() { fail("""
    In position [###]/Dummy0.fear:2:39
    [E23 methTypeError]
    Expected the method .argh/0 to return recMdf X1/0$, got mut X.
    """, """
    package test
    A:{ .foo[X](x: mut X): mut X -> mut B{ x }.argh[mut X] }
    B:{ read .argh[X]: recMdf X }
    """); }
  @Test void recMdfInSubHyg3b() { ok("""
    package test
    A:{ .foo[X](x: mut X): mut X -> mut B{ read .argh[X']: recMdf X' -> x }.argh[mut X] }
    B:{ read .argh[X]: recMdf X }
    """); }

  // TODO: give these all sane names
  @Test void recMdfInSubHyg1a() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut B[mut X] -> mut B[mut X]{ x } }
    B[X]:{ read .argh: recMdf X }
    """); }
  @Test void recMdfInSubHyg2a() { ok("""
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{
      read .argh: mut Foo -> x
      }}
    B:{ read .argh: recMdf Foo }
    Foo:{}
    """); }
  @Test void recMdfInSubHyg2b() { ok("""
    package test
    A:{ .foo(x: mut Foo): mut B[mut Foo] -> mut B[mut Foo]{ x } }
    B[X]:{ read .argh: recMdf X }
    Foo:{}
    """); }
  @Test void recMdfInSubHyg3d() { fail("""
    In position [###]/Dummy0.fear:2:38
    [E23 methTypeError]
    Expected the method .argh/0 to return recMdf X1/0$, got recMdf test.Foo[].
    """, """
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{ x } }
    B:{ read .argh[X]: recMdf X }
    Foo:{}
    """); }
  @Test void recMdfInSubHyg3e() { fail("""
    In position [###]/Dummy0.fear:2:38
    [E23 methTypeError]
    Expected the method .argh/0 to return read X, got mut test.Foo[].
    ""","""
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{ mut .argh[X]: read X -> x } }
    B:{ mut .argh[X]: read X }
    Foo:{}
    """); }
  @Test void recMdfInSubHyg3a() { ok("""
    package test
    A:{ .foo[X](x: mut X): mut B -> mut B{ x } }
    B:{ read .argh[X]: recMdf X }
    """); }
  @Test void recMdfInSubHyg3c() { ok("""
    package test
    A:{ .foo[X](x: mut X): mut B -> mut B{ read .argh[X']: recMdf X' -> x } }
    B:{ read .argh[X]: recMdf X }
    """); }

  /*
  -----//pass??
AA:{
read .a(b:recMdf B):recMdf A->recMdf A{
  read .b():recMdf B ->b
  }
}
A:{ read .b():recMdf B }
-------//fails
AA:{
read .a(b:recMdf B):mut A-> mut A{
  read .b():recMdf B ->b
  }
}
A:{ read .b():recMdf B }

   */
}
