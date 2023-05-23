package program.typesystem;

import net.jqwik.api.Example;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestRecMdf {
  @Example void shouldCollapseWhenCalled1() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      mut .m2: mut A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1a() { ok("""
    package test
    A:{
      read .m1: recMdf A -> {},
      mut .m2: read A -> this.m1,
      }
    """); }
  @Example void shouldCollapseWhenCalled1b() { fail("""
    In position [###]/Dummy0.fear:4:20
    [E28 undefinedName]
    The identifier "this" is undefined or cannot be captured.
    """, """
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      mut .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1c() { ok("""
    package test
    A:{
      read .m1(_: mut NoPromote): recMdf A,
      imm .m2: imm A -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalled1d() { fail("""
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

  @Example void shouldCollapseWhenCalledGenMut() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, _: mut NoPromote): recMdf X,
      mut .m2(a: mdf X): mdf X -> this.m1(a, mut NoPromote{}),
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenMut2() { ok("""
    package test
    A[X]:{
      read .get: recMdf X -> this.get
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
  @Example void shouldCollapseWhenCalledGenImm() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      imm .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenRead1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: read X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenRead2() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      read .m2: recMdf X -> this.m1{},
      }
    NoPromote:{}
    """); }
  @Example void shouldCollapseWhenCalledGenLent() { fail("""
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
  @Example void shouldCollapseWhenCalledGenIso1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): recMdf X,
      iso .m2: imm X -> this.m1{},
      }
    NoPromote:{}
    """); }
  // should fail because iso can capture imm too
  @Example void shouldCollapseWhenCalledGenIso2() { fail("""
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

  @Example void shouldCollapseWhenCalledNestedGenMut1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      mut .m2: Consumer[mdf X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Example void shouldCollapseWhenCalledNestedGenMut2() { fail("""
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
  @Example void shouldCollapseWhenCalledNestedGenImm1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      imm .m2: Consumer[imm X] -> this.m1{},
      }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Example void shouldCollapseWhenCalledNestedGenSplitImm1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Example void shouldCollapseWhenCalledNestedGenSplitMut1() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[imm X] -> mut A'[imm X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }
  @Example void shouldCollapseWhenCalledNestedGenSplitMut2() { ok("""
    package test
    A[X]:{
      read .m1(_: mut NoPromote): Consumer[recMdf X],
      }
    A'[X]:A[mdf X]{ x -> this.m1(x) }
    B[X]:{ imm .m2: Consumer[mdf X] -> mut A'[mdf X].m1{} }
    Consumer[X]:{ #(x: mdf X): Void }
    NoPromote:{} Void:{}
    """); }

  @Example void shouldApplyRecMdfInTypeParams1a() { ok("""
    package test
    Opt[T]:{ read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none, }
    OptMatch[T,R]:{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """); }
  @Example void shouldApplyRecMdfInTypeParams1b() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    //Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.some(x), } }
    Opt[T]:NoMutHyg[mdf T]{
      read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      }
    OptMatch[T,R]:NoMutHyg[mdf R]{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Example void shouldApplyRecMdfInTypeParams2() { ok("""
    package test
    alias base.NoMutHyg as NoMutHyg,
    //Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { .match(m) -> m.some(x) } }
    Opt:{ #[T](x: mdf T): mut Opt[mdf T] -> { read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.some(x), } }
    Opt[T]:NoMutHyg[mdf T]{
      read .match[R](m: mut OptMatch[recMdf T, mdf R]): mdf R -> m.none,
      read .map[R](f: mut OptMap[recMdf T, mdf R]): mut Opt[mdf R] -> this.match{ .some(x) -> Opt#(f#x), .none -> {} },
      read .do(f: mut OptMap[recMdf T, Void]): mut Opt[recMdf T] -> Yeet.with(this.map(f), this.map{ x -> x }),
      }
    OptMatch[T,R]:NoMutHyg[mdf R]{ mut .some(x: mdf T): mdf R, mut .none: mdf R }
    OptMap[T,R]:{ mut #(x: mdf T): mdf R }
    Yeet:{ .with[X,R](_: mdf X, res: mdf R): mdf R -> res }
    Void:{}
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Example void shouldApplyRecMdfInTypeParams3a() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    """); }
  @Example void shouldApplyRecMdfInTypeParams3b() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: imm A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Example void shouldApplyRecMdfInTypeParams3c() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      #(a: mut A[imm B]): imm B -> a.m1(this, F[imm B]),
      }
    """); }
  @Example void shouldApplyRecMdfInTypeParams3d() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B:{
      mut #(a: mut A[mut B]): mut B -> a.m1(this, F[mut B]),
      }
    """); }
  @Example void shouldApplyRecMdfInTypeParams3e() { ok("""
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
  @Example void shouldApplyRecMdfInTypeParams4a() { ok("""
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
  @Example void shouldApplyRecMdfInTypeParams4aV2() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> this#a,
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#(mut A[mut B[mut C]]{}),
      }
    """); }
  @Example void shouldApplyRecMdfInTypeParams4b() { fail("""
    In position [###]/Dummy0.fear:10:5
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    a .m1/2[]([[-recMdf-][test.B[recMdf Y]]{'fear0$ }, [-imm-][test.F[recMdf test.B[recMdf Y]]]{'fear1$ }])
    were valid:
    (mut test.A[mut test.B[recMdf Y]], recMdf test.B[recMdf Y], imm test.F[recMdf test.B[recMdf Y]]) <: (read test.A[mut test.B[recMdf Y]], iso test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]): iso test.B[recMdf Y]
    (mut test.A[mut test.B[recMdf Y]], recMdf test.B[recMdf Y], imm test.F[recMdf test.B[recMdf Y]]) <: (imm test.A[mut test.B[recMdf Y]], iso test.B[recMdf Y], imm test.F[mut test.B[recMdf Y]]): iso test.B[recMdf Y]
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
  @Example void shouldApplyRecMdfInTypeParams4c() { ok("""
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
}
