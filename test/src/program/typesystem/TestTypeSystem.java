package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?

  @Test void emptyProgram(){ ok("""
    package test
    """); }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void simpleTypeError(){ fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .fail/0 to return imm test.B[], got imm test.A[].
    """, """
    package test
    A:{ .m: A -> this }
    B:{
      .fail: B -> A.m
    }
    """); }

  @Test void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }
  @Test void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree,FortyTwo{} }
    """); }
  @Test void numbersGenericTypes2aNoMagic(){ fail("""
    In position [###]/Dummy0.fear:6:35
    [E23 methTypeError]
    Expected the method .sum/0 to return imm test.FortyTwo[], got imm test.FortyThree[].
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }

  // TODO: Can we use this to break anything? I think not because .get could not be implemented to do anything bad
  // because it can't capture anything muty if I made an imm Family2 or something.
  @Test void recMdfWeakening() { ok("""
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family2:List[mut Person]{ read .get(): mut Person }
    """); }

  @Test void ref1() { fail("""
    In position [###]/Dummy0.fear:10:42
    [E30 badCapture]
    'mut this' cannot be captured by an imm method in an imm lambda.
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[mdf V,mdf R]):mdf R -> l.in(l.var) }
    Let[V,R]:{ .var:mdf V, .in(v:mdf V):mdf R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: mut UpdateRef[mut X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """); }

  @Test void simpleThis() { ok("""
    package test
    A:{
      .a: C -> B{ this.c }.c,
      .c: C -> {}
      }
    B:{ .c: C }
    C:{ }
    """); }

  @Test void lambdaCapturesThis() { ok("""
    package test
    Let:{ #[V,R](l: mut Let[V, R]): R -> l.in(l.var) }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }
    Void:{}
    Ref[X]:{
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Let#mut Let[X,Void]{ .var -> this.swap(x), .in(_) -> Void },
      }
    """); }

  @Test void callMutFromLent() { ok("""
    package test
    A:{
      .b: lent B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void callMutFromLent2() { ok("""
    package test
    A:{
      .b: lent B -> {},
      .doThing: mut B -> this.b
      }
    B:{}
    """); }
  // the other tests are only parsing due to iso promotion
  @Test void callMutFromLent2a() { fail("""
    In position [###]/Dummy0.fear:4:30
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([])
    were valid:
    (recMdf test.A[]) <: (imm test.A[]): iso test.B[]
    """, """
    package test
    A:{
      read .b: lent B -> {},
      read .doThing: mut B -> this.b
      }
    B:{}
    """); }
  @Test void callMutFromLent3() { ok("""
    package test
    A:{
      .b(a: mut A): lent B -> {},
      .doThing: mut B -> this.b(iso A)
      }
    B:{}
    """); }
  @Test void callMutFromLentFail1() { fail("""
    In position [###]/Dummy0.fear:4:25
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/1[]([[-mut-][test.A[]]{'fear1$ }])
    were valid:
    (imm test.A[], mut test.A[]) <: (imm test.A[], iso test.A[]): iso test.B[]
    """, """
    package test
    A:{
      .b(a: mut A): lent B -> {},
      .doThing: mut B -> this.b({})
      }
    B:{}
    """); }

  @Test void callMutFromIso() { ok("""
    package test
    A:{
      .b: lent B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (imm test.B[]) <: (mut test.B[]): mut test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:24
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type mut test.B[]. The candidates were:
        (imm test.A[]): imm test.B[]
        
    (imm test.B[]) <: (iso test.B[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:24
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
        (imm test.A[]): imm test.B[]
        
    (imm test.B[]) <: (iso test.B[]): lent test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:24
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
        (imm test.A[]): imm test.B[]
    """, """
    package test
    A:{
      .b: imm B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromRead() { fail("""
In position [###]/Dummy0.fear:4:26
[E33 callTypeError]
Type error: None of the following candidates for this method call:
this .b/0[]([]) .foo/0[]([])
were valid:
(read test.B[]) <: (mut test.B[]): mut test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type mut test.B[]. The candidates were:
    (imm test.A[]): read test.B[]
    (imm test.A[]): imm test.B[]

(read test.B[]) <: (iso test.B[]): iso test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (imm test.A[]): read test.B[]
    (imm test.A[]): imm test.B[]

(read test.B[]) <: (iso test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (imm test.A[]): read test.B[]
    (imm test.A[]): imm test.B[]
    """, """
    package test
    A:{
      .b: read B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromRecMdfImm() { fail("""
    In position [###]/Dummy0.fear:4:31
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (recMdf test.B[]) <: (mut test.B[]): mut test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:29
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type mut test.B[]. The candidates were:
        (read test.A[]): recMdf test.B[]
        (imm test.A[]): recMdf test.B[]
        
    (recMdf test.B[]) <: (iso test.B[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:29
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
        (read test.A[]): recMdf test.B[]
        (imm test.A[]): recMdf test.B[]
        
    (recMdf test.B[]) <: (iso test.B[]): lent test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:29
        [E32 noCandidateMeths]
        When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
        (read test.A[]): recMdf test.B[]
        (imm test.A[]): recMdf test.B[]
    """, """
    package test
    A:{
      read .b: recMdf B -> {},
      read .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void lentFromRecMdfLent() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      lent .doThing: lent B -> this.b
      }
    B:{}
    """); }
  // TODO: Do we want to expand Meth-OK to allow this?
  @Disabled
  @Test void mutFromRecMdfLent() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      read .promote(b: lent B): mut B -> b,
      lent .doThing: mut B -> this.promote(this.b)
      }
    B:{}
    """); }
  @Test void CallMutFromRecMdfMut() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      mut .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void recMdfToMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: mut B -> this.b(this),
      }
    B:{}
    """); }
  @Test void readThisIsRecMdf() { ok("""
    package test
    A:{
      read .self: recMdf A -> this,
      }
    """); }
  @Test void readThisIsRead() { ok("""
    package test
    A:{
      read .self: read A -> this,
      }
    """); }
  @Test void bicycle1() { fail("""
    In position [###]/Dummy0.fear:3:49
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .wheel/0[]([]), no candidates for .wheel/0 returned the expected type recMdf test.Wheel[]. The candidates were:
    (read test.Bicycle[]): read test.Wheel[]
    (imm test.Bicycle[]): imm test.Wheel[]
    """, """
    package test
    A:{
      read .wheel(b: read Bicycle): recMdf Wheel -> b.wheel,
      }
    Bicycle:{
      read .wheel: recMdf Wheel -> {}
      }
    Wheel:{}
    """); }
  @Test void bicycle2() { ok("""
    package test
    A:{
      read .wheel(b: read Bicycle): read Wheel[read Bicycle] -> b.wheel,
      }
    Bicycle:{
      read .wheel: recMdf Wheel[recMdf Bicycle] -> {}
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle3() { ok("""
    package test
    A:{
      read .wheel(b: read Bicycle): read Wheel[read Bicycle] -> b.wheel,
      read .accept(b: read Bicycle, w: read Wheel[read Bicycle]): Voodo->
        b.acceptWheel(w),
      }
    Bicycle:{
      read .wheel: recMdf Wheel[recMdf Bicycle] -> {},
      read .acceptWheel(w: recMdf Wheel[recMdf Bicycle]): Voodo -> {},
      }
    Voodo:{}
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle4() { ok("""
    package test
    A[T]:{
      read .wheel1(b: read Bicycle[mdf T]): read Wheel[read T] -> b.wheel,
      read .wheel2(b: read Bicycle[mut T]): read Wheel[read T] -> b.wheel,
      read .wheel3(b: read Bicycle[imm T]): read Wheel[imm T] -> b.wheel,
      }
    Bicycle[T]:{
      read .wheel: recMdf Wheel[recMdf T] -> {},
      }
    Voodo:{}
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle5() { ok("""
    package test
    A[T]:{
      read .wheel1(b: read Bicycle[mdf T]): read T -> b.wheel,
      }
    Bicycle[T]:{
      read .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      read .loop[T]: mdf T -> this.loop,
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle6() { fail("""
    In position [###]/Dummy0.fear:3:53
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .wheel/0[]([]), no candidates for .wheel/0 returned the expected type recMdf T. The candidates were:
    (read test.Bicycle[mdf T]): read T
    (imm test.Bicycle[mdf T]): imm T
    """, """
    package test
    A[T]:{
      read .wheel1(b: read Bicycle[mdf T]): recMdf T -> b.wheel,
      }
    Bicycle[T]:{
      read .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      read .loop[T]: mdf T -> this.loop,
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
      
    """); }
  @Test void box() { ok("""
    package test
    Box:{ #[R](r: mdf R): mut Box[mdf R] -> { r } }
    Box[R]:base.NoMutHyg[mdf R]{ read #: recMdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void boxInnerGens() { ok("""
    package test
    Box:{ #[R](r: mdf R): mut Box[mdf R] -> { r } }
    Box[R]:base.NoMutHyg[mdf R]{ read #: recMdf R }
    BoxF[R]:base.NoMutHyg[mdf R]{ read #: mut F[recMdf R] }
    F[A,B]:{ read #(a: mdf A): mdf B }
    F[A]:{read #:mdf A}
    Usage[A,B]:{ #(b: mut Box[mut F[read A, read B]]): mut F[read A, read B] -> b# }
    Usage2[A,B]:{ read #(b: mut Box[mut F[read A, read B]]): mut F[read A, read B] -> b# }
    // This is okay because adapterOk works in ways that are dark and mysterious
    Usage3[A,B]:{ read #(b: recMdf Box[recMdf F[read A, read B]]): recMdf F[recMdf A, read B] -> b# }
    Usage4[A,B]:{ read #(b: mut Box[mut F[mdf A]]): mut F[mdf A] -> b# }
    Usage5[A,B]:{ read #(b: mut Box[mut F[mdf A]]): mut F[mdf A] -> b# }
    Usage6[A,B]:{ read #(b: mut BoxF[mdf A]): mut F[mdf A] -> b# }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void invalidIsoPromotionWithRecMdf() { ok("""
    package test
    A:{
      read .m():recMdf A->this,
      //mut .break1:iso A->this,//bad and fails obviusly
      //mut .break2:iso A->this.m(),//bad
      read .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void captureRecMdfAsMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: read B -> LetMut#[mut B, read B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:base.NoMutHyg[V]{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void inferCaptureRecMdfAsMut1() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: read B -> LetMut#[mut B, read B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }
  @Test void inferCaptureRecMdfAsMut2() { ok("""
    package test
    A:{
      read .b(a: mut A): recMdf B -> {'b .foo -> b },
      mut .break: read B -> LetMut#[mut B, read B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }

  @Test void breakingEarlyFancyRename() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E23 methTypeError]
    Expected the method .foo/2 to return recMdf test.A[], got read test.A[].
    """, """
    package test
    A:{
      read .foo(a:recMdf A, b:read A):recMdf A -> b
      }
    B:{
      .foo(mutR: mut A, readR: read A): mut A -> mutR.foo(mutR, readR)
      }
    """); }

  @Test void recMdfCallsRecMdf() { ok("""
    package test
    A:{
      read .inner: recMdf A -> {},
      read .outer: recMdf A -> recMdf A.inner,
      }
    """); }
  @Test void recMdfCallsRecMdfa() { ok("""
    package test
    A:{
      read .asRecMdf: recMdf A -> recMdf A{'inner
        read .inner: recMdf A -> inner
        },
      read .inner: recMdf A,
      }
    B:{ #(a: mut A): mut A -> a.inner }
    """); }
  @Test void noCaptureReadInMut() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A:{ mut .prison: read B }
    B:{
      read .break: mut A -> { this }
      }
    """); }
  @Test void noCaptureMdfInMut() { fail("""
    In position [###]/Dummy0.fear:4:29
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      read .break: mut A[B] -> { this }
      }
    """); }
  @Test void noCaptureMdfInMut2() { fail("""
    In position [###]/Dummy0.fear:4:34
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      read .break: mut A[read B] -> { this } // this capture was being allowed because this:mdf B was adapted with read to become this:recMdf B (which can be captured by mut)
      }
    """); }

  @Test void noCaptureMdfInMut3() { fail("""
    In position [###]/Dummy0.fear:4:38
    [E30 badCapture]
    'mdf x' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B[X]:{
      .break(x: mdf X): mut A[mdf X] -> { x }
      }
    """); }
  // TODO: write a test that shows that the error message for this code makes sense:
  /*
      // (Void is the wrong R and this returns Opt[Opt[T]] instead of Opt[T] or the written Void.
        OptDo[T]:OptMatch[T,Void]{
        #(t:T):Void,   //#[R](t:T):R,
        .some(x) -> Opt#this._doRes(this#x, x),
        .none->{},
        ._doRes(y:Void,x:T):T -> Opt#x
        }
   */

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
    A[X]:{ read .m: recMdf X -> this.m }
    B:A[imm Foo]
    C:B
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ read .m(par: mut C) : imm Foo -> par.m  }
    //NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:7:48
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    (read test.B[]): imm test.Foo[]
    (imm test.B[]): imm test.Foo[]
    """, """
    package test
    Foo:{}
    A[X]:{ read .m: recMdf X -> this.m }
    B:A[imm Foo]{}
    CanPass0:{ read .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ read .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ read .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void immToReadCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: read X }
    A:{ read .m[T](par: imm T) : read L[imm T] -> read L[imm T]{.absMeth->par} }
    """); }

  @Test void immCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: imm X }
    A:{ read .m[T](par: mut T) : mut L[mut T] -> mut L[mut T]{.absMeth->par} }
    """); }

  @Test void readMethOnImmLambdaCannotCaptureRead() { fail("""
    In position [###]/Dummy0.fear:4:69
    [E30 badCapture]
    'read par' cannot be captured by a read method in an imm lambda.
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: read X }
    A:{ read .m[T](par: read T) : imm L[imm T] -> imm L[imm T]{.absMeth->par} }
    """);}

  @Test void immReturnsReadAsLent() { fail("""
    In position [###]/Dummy0.fear:4:61
    [E23 methTypeError]
    Expected the method .absMeth/0 to return lent T, got imm T.
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: lent X }
    A:{ read .m[T](par: read T) : lent L[imm T] -> lent L[imm T]{.absMeth->par} }
    """); }

  @Test void noMdfParamAsLent() { fail("""
    In position [###]/Dummy0.fear:4:59
    [E23 methTypeError]
    Expected the method .absMeth/0 to return lent T, got mdf T.
    """, """
    package test
    B:{}
    L[X]:{ mut .absMeth: lent X }
    A:{ read .m[T](par: mdf T): lent L[mut T] -> lent L[mut T]{.absMeth->par} }
    C:{ #: lent L[mut B] -> A{}.m[read B](B) }
    """); }

  @Test void noMutHygRenamedGX1() { ok("""
    package test
    alias base.NoMutHyg as NoMH,
    Person:{}
    
    Foo[X]:NoMH[mdf X]{ read .stuff: recMdf X }
    FooP0[Y]:Foo[mdf Y]{}
    FooP1:{ #(p: read Person): mut Foo[read Person] -> { p } }
    FooP2:{ #(p: read Person): mut FooP0[read Person] -> { p } }
    
    Test:{
      .t1(t: read Person): lent Foo[read Person] -> FooP1#t,
      .t2(t: read Person): lent FooP0[read Person] -> FooP2#t,
      .t2a(t: read Person): lent Foo[read Person] -> FooP2#t,
      }
    
    //Foo[X]:NoMH[X]{stuff[X]}
    //FooP0[Y]:Foo[Y]
    //FooP1:Foo[Person]
    //FooP2:{stuff[Person]}
    //m(x)->FooP1{ x }
    //m(x)->FooP2{ x }
    """,  """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void noMutHygRenamedGX2() { fail("""
    In position [###]/Dummy0.fear:11:52
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    [-imm-][test.FooP1[]]{'fear2$ } #/1[]([t])
    were valid:
    (imm test.FooP1[], read test.Person[]) <: (imm test.FooP1[], imm test.Person[]): iso test.Foo[read test.Person[]]
    """, """
    package test
    alias base.NoMutHyg as NoMH,
    Person:{}
    
    Foo[X]:NoMH[mdf X]{ read .stuff: recMdf X }
    FooP0[Y]:Foo[mdf Y]{}
    FooP1:{ #(p: read Person): mut Foo[read Person] -> { p } }
    FooP2:{ #(p: read Person): mut FooP0[read Person] -> { p } }
    
    Test:{
      .t1(t: read Person): mut Foo[read Person] -> FooP1#t,
      .t2(t: read Person): mut FooP0[read Person] -> FooP2#t,
      .t2a(t: read Person): mut Foo[read Person] -> FooP2#t,
      }
    
    //Foo[X]:NoMH[X]{stuff[X]}
    //FooP0[Y]:Foo[Y]
    //FooP1:Foo[Person]
    //FooP2:{stuff[Person]}
    //m(x)->FooP1{ x }
    //m(x)->FooP2{ x }
    """,  """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, """
    package base
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{}
    """, Base.load("nums.fear")); }

  @Test void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:2
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    s .use/2[imm base.caps.IO[]]([[-imm-][base.caps.IO'[]]{'fear43$ }, [-mut-][base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]]{'fear44$ #/2([io, fear1$]): Sig[mdf=mut,gens=[],ts=[lent base.caps.IO[], lent base.caps.System[imm base.Void[]]],ret=imm base.Void[]] -> fear1$ .return/1[]([[-lent-][base.caps.LentReturnStmt[imm base.Void[]]]{'fear45$ #/0([]): Sig[mdf=lent,gens=[],ts=[],ret=imm base.Void[]] -> io .println/1[]([[-imm-]["Hello, World!"[]]{'fear46$ }])}])}])
    were valid:
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: (lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: (lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: (iso base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: (mut base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ #(_, s) -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent NotTheRootCap, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent IO, lent IO]{
      #(auth: lent IO): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Test void incompatibleGens() { fail("""
    In position [###]/Dummy1.fear:7:12
    [E34 bothTExpectedGens]
    Type error: the generic type lent C cannot be a super-type of any concrete type, like Fear[###]/0.
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ #(_, s) -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent C, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent IO, lent IO]{
      #(auth: lent IO): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Test void incompatibleITs() { fail("""
    In position [###]/Dummy1.fear:7:8
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    cont #/2[]([c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }]), this])
    were valid:
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])?, lent base.caps.System[mdf R]) <: (mut base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]): mdf R
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy1.fear:7:11
        [E33 callTypeError]
        Type error: None of the following candidates for this method call:
        c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])
        were valid:
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps._RootCap[]): lent C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], iso base.caps._RootCap[]): iso C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], mut base.caps._RootCap[]): lent C
        
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], iso C, iso base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], mut C, lent base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear48$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], lent C, mut base.caps.System[mdf R]): mdf R
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ #(_, s) -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """, """
    package base.caps
    alias base.Sealed as Sealed, alias base.Void as Void, alias base.Str as Str,
    // bad version of caps.fear
    LentReturnStmt[R]:{ lent #: mdf R }
    System[R]:{
      lent .use[C](c: CapFactory[lent _RootCap, lent C], cont: mut UseCapCont[C, mdf R]): mdf R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[mdf R]): mdf R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[mdf R]): mdf R }
    CapFactory[C,R]:{
      #(s: lent C): lent R,
      .close(c: lent R): Void,
      }
    IO:{
      lent .print(msg: Str): Void,
      lent .println(msg: Str): Void,
      }
    IO':CapFactory[lent _RootCap, lent IO]{
      #(auth: lent _RootCap): lent IO -> auth,
      .close(c: lent IO): Void -> {},
      }
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear")); }
  @Test void recMdfCannotBeSubtypeOfMdf() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E23 methTypeError]
    Expected the method #/1 to return mdf A, got recMdf A.
    """, """
    package test
    F[A]:{ read #(a:recMdf A):mdf A->a }
    M:{ mut .mutMe: mut M -> this.mutMe } // if this method can be called from M it is broken
    Break:{
      .myF: imm F[mut M] -> {},
      .b1(m: imm M): mut M -> this.myF#m,
      .b2(m: imm M): mut M -> (this.myF#m).mutMe,
      }
    """); }

  // TODO: test lent to mut promotion
  @Test void minimalMatcher() { ok("""
    package test
    //we can have lent matcher with lent cases that can capture all (but mut as lent), and can only return mut as lent :-(
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return mdf R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      lent .get: mdf R
      }
    PreR:{
      read .get: read MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      read .match[R](m: lent Matcher[mdf R]): mdf R -> m.get
      }
    Usage:{
      .direct(preR: read PreR): read MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: read MyRes): read MyRes -> MatcherContainer.match{ r }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void minimalMatcher2() { ok("""
    package test
    //we can have lent matcher with lent cases that can capture all (but mut as lent), and can only return mut as lent :-(
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return mdf R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      mut .get: mdf R
      }
    PreR:{
      mut .get: mut MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      read .match[R](m: mut Matcher[mdf R]): mdf R -> m.get
      }
    Usage:{
      .direct(preR: mut PreR): mut MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: mut MyRes): mut MyRes -> MatcherContainer.match{ r }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void nestedRecMdfExplicitMdf() { ok("""
    package test
    A[X]:{
      read .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      read #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#({}),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  @Test void captureReadAsRecMdf() { fail("""
    In position [###]/Dummy0.fear:7:45
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear0$ } .m/1[read test.B[]]([[-read-][test.B[]]{'fear1$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mut test.B[]. The candidates were:
    (read test.L[read test.B[]]): read test.B[]
    (imm test.L[read test.B[]]): imm test.B[]
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[mdf X]{ read .absMeth: recMdf X }
    A:{ read .m[T](par: read T): mut L[read T] -> mut L[read T]{.absMeth->par} }
    
    Break:{ #(rb: read B): mut B -> (A.m(read B)).absMeth }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void aliasGenericHiding() { fail("""
    [E20 traitNotFound]
    The trait foo.Bar/3 could not be found.
    """, """
    package test
    alias foo.Bar as Baz,
    alias foo.Bar[test.Yolo] as YoloBar,
    Yolo:{}
    Bloop3:YoloBar[Yolo,Yolo]
    """, """
    package foo
    Bar:{}
    Bar[A]:{}
    Bar[A,B]:{}
    """); }

  // Se TestMethsPost5a for the basic ones
  @Test void recMdfIT2() { ok("""
    package test
    A:{ #: mut B -> mut B{ read .argh: mut Foo -> Foo } }
    B:{ read .argh: recMdf Foo }
    Foo:{}
    """); }
}
