package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

@Disabled // recMdf is disabled
public class TestRecMdf {
  @Test void shouldCollapseWhenCalled1() { ok("""
    package test
    A:{
      recMdf .m1(_: mut NoPromote): recMdf A,
      mut .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1a() { ok("""
    package test
    A:{
      recMdf .m1: recMdf A -> {},
      mut .m2: readH A -> this.m1,
      }
    """); }
  @Test void shouldCollapseWhenCalled1aa() { ok("""
    package test
    A:{
      recMdf .m1: recMdf A -> {},
      mut .m2: readH A -> this.m1,
      }
    """); }
  @Test void shouldCollapseWhenCalled1b() { fail("""
    In position [###]/Dummy0.fear:5:20
    [E28 undefinedName]
    The identifier "this" is undefined or cannot be captured.
    """, """
    package test
    A:{
      // Broken because makes mut this -> imm!
      recMdf .m1(): recMdf A -> this,
      mut .m2: imm A -> this.m1,
      }
    """); }
  @Test void shouldCollapseWhenCalled1bb() { fail("""
    In position [###]/Dummy0.fear:5:20
    [E28 undefinedName]
    The identifier "this" is undefined or cannot be captured.
    """, """
    package test
    A:{
      // Broken because makes mut this -> imm!
      recMdf .m1(_: mut NoPromote): recMdf A -> this,
      mut .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1c() { ok("""
    package test
    A:{
      recMdf .m1(_: mut NoPromote): recMdf A,
      imm .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1d() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut test.A[]. The candidates were:
    (imm test.A[], mut test.NoPromote[]): imm test.A[]
    (imm test.A[], iso test.NoPromote[]): imm test.A[]
    (imm test.A[], mutH test.NoPromote[]): imm test.A[]
    """, """
    package test
    A:{
      recMdf .m1(_: mut NoPromote): recMdf A,
      imm .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalled1e() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/0[]([]), no candidates for .m1/0 returned the expected type mut test.A[]. The candidates were:
    (imm test.A[]): imm test.A[]
    """, """
    package test
    A:{
      recMdf .m1: recMdf A,
      imm .m2: mut A -> this.m1,
      }
    """); }

  @Test void shouldCollapseWhenCalledGenMut() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, _: mut NoPromote): recMdf X,
      mut .m2(a: mdf X): mdf X -> this.m1(a, mut NoPromote{}),
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenMut2() { ok("""
    package test
    A[X:readH,mutH,read,mut,imm]:{
      recMdf .get: recMdf X -> this.loop,
      readH .loop[T:readH,mutH,read,mut,imm]: mdf T -> this.loop,
      }
    B:{
      .m1Mut[Y:readH,mutH,read,mut,imm](a: mut A[mut      Y]): mut Y     -> a.get,
      .m2Mut   (a: mut A[mut Person]): mut Person-> a.get,
      
      .m1Read[Y:readH,mutH,read,mut,imm](a: readH A[readOnly      Y]): readH Y     -> a.get,
      .m2readOnly   (a: readH A[readOnly Person]): readH Person-> a.get,
      
      .m1Lent[Y:readH,mutH,read,mut,imm](a: mutH A[lent      Y]): mutH Y     -> a.get,
      .m2Lent   (a: mutH A[lent Person]): mutH Person-> a.get,
      
      .m1Mdf[Y:readH,mutH,read,mut,imm](a: mut A[mdf      Y]): mdf Y     -> a.get,
      //.m2Mdf   (a: mdf A[mdf Person]): mdf Person-> a.get,
      
      .m1Imm[Y:readH,mutH,read,mut,imm](a: imm A[imm      Y]): imm Y     -> a.get,
      .m1_Imm[Y:readH,mutH,read,mut,imm](a: mut A[imm      Y]): imm Y     -> a.get,
      .m2Imm   (a: imm A[imm Person]): imm Person-> a.get,
      .m2_Imm   (a: mut A[imm Person]): imm Person-> a.get,
      }
    Person:{}
    """); }
  @Test void shouldCollapseWhenCalledGenImm() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      imm .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenRead1() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      readH .m2: readH X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenRead2() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      readH .m2: readH X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Test void shouldCollapseWhenCalledGenIso1ImmPromotion() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      iso .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  // should fail because iso can capture imm too
  @Test void shouldCollapseWhenCalledGenIso2() { fail("""
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .m1/1[]([[-mut-][test.NoPromote[]]{'fear0$ }]), no candidates for .m1/1 returned the expected type mut X. The candidates were:
    (mut test.A[mdf X], mut test.NoPromote[]): mdf X
    (iso test.A[mdf X], iso test.NoPromote[]): mdf X
    (lent test.A[mdf X], iso test.NoPromote[]): mdf X
    (iso test.A[mdf X], mutH test.NoPromote[]): mdf X
    """, """
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      iso .m2: iso X -> this.m1(mut NoPromote{}),
      }
    NoPromote:{}
    """); }

  @Test void shouldCollapseWhenCalledGenIso3() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): recMdf X,
      iso .m2: mdf X -> this.m1(mut NoPromote{}),
      mut .m3: mdf X -> this.m1(mut NoPromote{}),
      }
    NoPromote:{}
    """); }

  @Test void mutAsIso1() { ok("""
    package test
    A:{ #: iso B -> mut B{} }
    B:{}
    """); }
  // TODO: we should make this pass by allowing lambdas to be promoted to iso on creation, and meth calls too?
  @Disabled @Test void mutAsIso2() { ok("""
    package test
    A:{ #(b:iso B): iso B -> mut B{},
        ##:iso B ->this#(mut B{}),
       }
    B:{}
    """); }

  @Test void shouldCollapseWhenCalledNestedGenMut1() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
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
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenImm1() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
      imm .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Test void shouldCollapseWhenCalledNestedGenSplitImm1() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenSplitMut1() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> mut A'[imm X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Test void shouldCollapseWhenCalledNestedGenSplitMut2() { ok("""
    package test
    A[X]:{
      recMdf .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[mdf X] -> mut A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Test void shouldApplyRecMdfInTypeParams1a() { ok("""
    package test
    Opt[T]:{ recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none, }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void shouldApplyRecMdfInTypeParams1bLent() { ok("""
    package test
    Opt:{ #[T:readH,mutH,imm](x: mdf T): mutH Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T:readH,mutH,imm]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T:readH,mutH,imm,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
//  @Test void shouldApplyRecMdfInTypeParams1bRecMdf1() { ok("""
//    package test
//    Opt:{ recMdf #[T](x: recMdf T): recMdf Opt[mdf T] -> { .match(m) -> m.some(x) } }
//    Opt[T]:{
//      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
//      }
//    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
//
//    Foo:{}
//    Usage:{
//      .immOpt(x: imm Foo): imm Opt[imm Foo] -> Opts#x,
//      .mutOpt(x: mut Foo): mut Opt[mut Foo] -> mut Opts#x,
//      .readOnlyOpt(x: readH Foo): readH Opt[readOnly Foo] -> readH Opts#x,
//      .readOpt(x: readH Foo): readH Opt[readOnly Foo] -> readH Opts#x,
//      .lentOpt(x: mutH Foo): mutH Opt[lent Foo] -> mutH Opts#x,
//      //.isoOpt(x: iso Foo): iso Opt[Foo] -> iso Opts#x,
//      recMdf .recMdfOpt(x: recMdf Foo): recMdf Opt[recMdf Foo] -> recMdf Opts#x,
//      .mdfOptMut[X](x: mut X): mut Opt[mut X] -> mut Opts#x,
//      }
//    """); }
  @Test void shouldApplyRecMdfInTypeParams1bRecMdf2() { ok("""
    package test
    Opt:{ recMdf #[T](x: recMdf T): recMdf Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void shouldApplyRecMdfInTypeParams1bBounds() { ok("""
    package test
    Opt:{ #[T:imm,mut](x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T:imm,mut]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T, R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void shouldApplyRecMdfInTypeParams1bBoundsTraitGens() { ok("""
    package test
    FOpt[T:imm,mut]:{ #(x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt[T:imm,mut]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T, R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void boxAndMatcher() { ok("""
    package test
    Opt:{ recMdf #[T](x: recMdf T): recMdf Opt[mdf T] -> {
      recMdf .match[R](m: mut OptMatch[recMdf T, recMdf R]): recMdf R -> m.some(x),
      }}
    Opt[T]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, recMdf R]): recMdf R -> m.none,
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Test void boxAndMatcherDedicated() { ok("""
    package test
    Opt:{ recMdf #[T](x: recMdf T): recMdf Opt[mdf T] -> {
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.some(x),
      }}
    Opt[T]:{
      recMdf .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      recMdf .map[R](f: mut OptMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match(f),
//      recMdf .map[R](f: mut OptMatch[mdf T, mut Opt[mdf R]]): mut Opt[mdf R] -> this.match(f),
      recMdf .flatMap[R](f: mut OptFlatMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match(f),
      }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:OptMatch[mdf T, mut Opt[mdf R]]{
      mut #(x: mdf T): mdf R,
      .some(x) -> mut Opts#(this#x),
      .none -> {},
      }
    OptFlatMap[T,R]:OptMatch[mdf T, mut Opt[mdf R]]{
      mut #(x: mdf T): mut Opt[mdf R],
      .some(x) -> this#x,
      .none -> {},
      }
    Usage:{ #(a: Opt[Foo]): Opt[Bar] -> a.map{_ -> Bar} }
    Foo:{} Bar:{}
    """); }
  @Test void inferRecMdf1() { ok("""
    package test
    Foo[T]:{
      recMdf .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x: mdf T): mdf T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map(mut F[A]{ _->A }) }
    """); }
  @Test void inferRecMdf2() { ok("""
    package test
    Foo[T]:{
      recMdf .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x: mdf T): mdf T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map{ _->A } }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3a() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3b() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: imm A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3c() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: mut A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3d() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      mut #(a: mut A[mut B]): mut B -> a.m1(this, F[mut B]),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams3e() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
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
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] ->
        a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4aV2() { ok("""
    package test
    A[X:readH,mutH,read,mut,imm]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X:readH,mutH,read,mut,imm]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y:readH,mutH,read,mut,imm]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> this.loop,
      readH .loop[R:readH,mutH,read,mut,imm]: mdf R -> this.loop,
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4b() { fail("""
    In position [###]/Dummy0.fear:10:5
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "recMdf test.B[recMdf Y]") for this method call:
    a .m1/2[]([[-recMdf-][test.B[recMdf Y]]{'fear0$ }, [-imm-][test.F[mut test.B[recMdf Y]]]{'fear1$ }])
    were valid:
    (mut test.A[mut test.B[recMdf Y]], recMdf test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]) <= (iso test.A[mut test.B[recMdf Y]], iso test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]): iso test.B[recMdf Y]
    """, """
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    // Fails because if B[Y] is imm, we'll fail because recMdf X will be mut B[..] and recMdf B[..] in the rt here
    // will be imm B[..]
    B[Y]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): recMdf B[recMdf Y] ->
        a.m1(recMdf B[recMdf Y], F[recMdf B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Test void shouldApplyRecMdfInTypeParams4c() { ok("""
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] ->
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
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readH .m(par: imm B) : mutH L[imm B] -> mutH L[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfExample() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readH .m(par: imm B) : mutH L[imm B] -> mutH L[imm B]{.absMeth->par} }
    C:{ #: imm B -> (A.m(B)).absMeth }
    """); }
  @Test void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear1$ } .m/1[]([[-imm-][test.B[]]{'fear2$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mutH test.B[]. The candidates were:
    (lent test.L[imm test.B[]]): imm test.B[]
    (iso test.L[imm test.B[]]): imm test.B[]
    """, """
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readH .m(par: imm B) : mutH L[imm B] -> mutH L[imm B]{.absMeth->par} }
    C:{ #: mutH B -> (A.m(B)).absMeth }
    """); }
  @Test void noCaptureImmAsRecMdfTopLvl1() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    L'[X]:L[imm X]{ recMdf .absMeth: imm X }
    A:{ readH .m(par: imm B) : mutH L[imm B] -> mutH L'[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfTopLvl2() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:7) test.L[mdf X], .absMeth/0[](): recMdf X
    ([###]/Dummy0.fear:4:16) test.L'[mdf X], .absMeth/0[](): imm X
    """, """
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    L'[X]:L[mdf X]{ recMdf .absMeth: imm X }
    A:{ readH .m(par: imm B) : mutH L[imm B] -> mutH L'[imm B]{.absMeth->par} }
    """); }

  @Test void recMdfInheritance() { ok("""
    package test
    Foo:{}
    A[X]:{ recMdf .m: recMdf X -> Loop# }
    B:A[imm Foo]
    C:B
    CanPass0:{ readH .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readH .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ readH .m(par: mut C) : imm Foo -> par.m  }
//    NoCanPass:{ readH .m(par: mut B) : mut Foo -> par.m  }
    Loop:{ #[X]: mdf X -> this# }
    """); }

  @Test void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:8:52
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    (mut test.B[]): imm test.Foo[]
    (iso test.B[]): imm test.Foo[]
    (lent test.B[]): imm test.Foo[]
    """, """
    package test
    Foo:{}
    Loop:{ #[X]: mdf X -> this# }
    A[X]:{ recMdf .m: recMdf X -> Loop# }
    B:A[imm Foo]{}
    CanPass0:{ readH .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readH .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ readH .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void shouldBeAbleToCaptureMutInMutRecMdfSubTypeGeneric() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut B[mut X] -> mut B[mut X]{ x } }
    B[X]:{ recMdf .argh: recMdf X }
    """); }
  @Test void shouldBeAbleToCaptureMutInMutRecMdfSubTypeGenericExplicit() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut B[mut X] -> mut B[mut X]{ recMdf .argh: recMdf X -> x } }
    B[X]:{ recMdf .argh: recMdf X }
    """); }
  @Test void shouldBeAbleToCaptureMutInMutRecMdfSubTypeConcrete() { ok("""
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{ x } }
    B:{ recMdf .argh: recMdf Foo }
    Foo:{}
    """); }
  @Test void shouldBeAbleToCaptureMutInMutRecMdfSubTypeConcreteGeneric() { ok("""
    package test
    A:{ .foo(x: mut Foo): mut B[mut Foo] -> mut B[mut Foo]{ x } }
    B[X]:{ recMdf .argh: recMdf X }
    Foo:{}
    """); }
  @Test void methGensMismatch1() { fail("""
    In position [###]/Dummy0.fear:2:38
    [E23 methTypeError]
    Expected the method .argh/0 to return recMdf X1/0$, got recMdf test.Foo[].
    """, """
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{ x } }
    B:{ recMdf .argh[X]: recMdf X }
    Foo:{}
    """); }
  @Test void methGensMismatch2() { fail("""
    In position [###]/Dummy0.fear:2:38
    [E23 methTypeError]
    Expected the method .argh/0 to return readH X, got mut test.Foo[].
    ""","""
    package test
    A:{ .foo(x: mut Foo): mut B -> mut B{ mut .argh[X]: readH X -> x } }
    B:{ mut .argh[X]: readH X }
    Foo:{}
    """); }
  @Test void methGensMismatch3() { fail("""
    In position [###]/Dummy0.fear:2:39
    [E23 methTypeError]
    Expected the method .argh/0 to return recMdf X', got recMdf X.
    """, """
    package test
    A:{ .foo[X](x: mut X): mut B -> mut B{ recMdf .argh[X']: recMdf X' -> x } }
    B:{ recMdf .argh[X]: recMdf X }
    """); }

  /*
  -----//pass??
AA:{
readH .a(b:recMdf B):recMdf A->recMdf A{
  readH .b():recMdf B ->b
  }
}
A:{ readH .b():recMdf B }
-------//fails
AA:{
readH .a(b:recMdf B):mut A-> mut A{
  readH .b():recMdf B ->b
  }
}
A:{ readH .b():recMdf B }

   */
}
