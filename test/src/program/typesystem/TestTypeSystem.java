package program.typesystem;

import id.Mdf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystem {
  //  TODO: mut Box[readOnly X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[readOnly Person] if mdf X becomes readOnly X (same with lent)
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
  @Disabled @Test void recMdfWeakening() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family2:List[mut Person]{ recMdf .get(): mut Person }
    // Family2:List[mut Person]{ recMdf .get(): recMdf Person } // works
    """); }

  @Test void ref1() { fail("""
In position [###]/Dummy0.fear:10:31
[E33 callTypeError]
Type error: None of the following candidates (returning the expected type "imm base.Void[]") for this method call:
[-imm-][base.Let[]]{'fear1$ } #/1[mdf X, imm base.Void[]]([[-imm-][base.Let[mdf X, imm base.Void[]]]{'fear2$ .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] -> this .swap/1[]([x]),
.in/1([fear0$]): Sig[mdf=imm,gens=[],ts=[mdf X],ret=imm base.Void[]] -> [-imm-][base.Void[]]{'fear3$ }}])
were valid:
(imm base.Let[], ?[-imm-][base.Let[mdf X, imm base.Void[]]]{'fear2$ .var/0([]): Sig[mdf=imm,gens=[],ts=[],ret=mdf X] -> this .swap/1[]([x]),
.in/1([fear0$]): Sig[mdf=imm,gens=[],ts=[mdf X],ret=imm base.Void[]] -> [-imm-][base.Void[]]{'fear3$ }}?) <: (imm base.Let[], imm base.Let[mdf X, imm base.Void[]]): imm base.Void[]
  The following errors were found when checking this sub-typing:
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
      recMdf * : recMdf X,
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
  // the other tests are only passing due to iso promotion
  @Test void callMutFromLent2a() { fail("""
    In position [###]/Dummy0.fear:4:30
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
    this .b/0[]([])
    were valid:
    (readOnly test.A[]) <: (imm test.A[]): iso test.B[]
    """, """
    package test
    A:{
      readOnly .b: lent B -> {},
      readOnly .doThing: mut B -> this.b
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
    Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
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
Type error: None of the following candidates (returning the expected type "?") for this method call:
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

(imm test.B[]) <: (lent test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type lent test.B[]. The candidates were:
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
Type error: None of the following candidates (returning the expected type "?") for this method call:
this .b/0[]([]) .foo/0[]([])
were valid:
(readOnly test.B[]) <: (mut test.B[]): mut test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type mut test.B[]. The candidates were:
    (imm test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]

(readOnly test.B[]) <: (iso test.B[]): iso test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (imm test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]

(readOnly test.B[]) <: (iso test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (imm test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]

(readOnly test.B[]) <: (lent test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:24
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type lent test.B[]. The candidates were:
    (imm test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]
        """, """
    package test
    A:{
      .b: readOnly B -> {},
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
Type error: None of the following candidates (returning the expected type "?") for this method call:
this .b/0[]([]) .foo/0[]([])
were valid:
(readOnly test.B[]) <: (mut test.B[]): mut test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:29
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type mut test.B[]. The candidates were:
    (readOnly test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]

(readOnly test.B[]) <: (iso test.B[]): iso test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:29
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (readOnly test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]

(readOnly test.B[]) <: (iso test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:29
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type iso test.B[]. The candidates were:
    (readOnly test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]

(readOnly test.B[]) <: (lent test.B[]): lent test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:4:29
    [E32 noCandidateMeths]
    When attempting to type check the method call: this .b/0[]([]), no candidates for .b/0 returned the expected type lent test.B[]. The candidates were:
    (readOnly test.A[]): readOnly test.B[]
    (imm test.A[]): imm test.B[]
    (readOnly test.A[]): readOnly test.B[]
    """, """
    package test
    A:{
      recMdf .b: recMdf B -> {},
      readOnly .doThing: Void -> this.b.foo.ret
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
      recMdf .b: recMdf B -> {},
      lent .doThing: lent B -> this.b
      }
    B:{}
    """); }
  @Test void mutFromRecMdfLent() { fail("""
In position [###]/Dummy0.fear:5:30
[E33 callTypeError]
Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
this .promote/1[]([this .b/0[]([])])
were valid:
(lent test.A[], lent test.B[]) <: (readOnly test.A[], mut test.B[]): mut test.B[]
  The following errors were found when checking this sub-typing:
    In position [###]/Dummy0.fear:5:43
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
    this .b/0[]([])
    were valid:
    (lent test.A[]) <: (iso test.A[]): iso test.B[]

(lent test.A[], lent test.B[]) <: (imm test.A[], iso test.B[]): iso test.B[]
    """, """
    package test
    A:{
      recMdf .b: recMdf B -> {},
      readOnly .promote(b: mut B): mut B -> b,
      lent .doThing: mut B -> this.promote(this.b)
      }
    B:{}
    """); }
  @Test void CallMutFromRecMdfMut() { ok("""
    package test
    A:{
      recMdf .b: recMdf B -> {},
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
      recMdf .b(a: recMdf A): recMdf B -> {},
      mut .break: mut B -> this.b(this),
      }
    B:{}
    """); }
  @Test void recMdfThisIsRecMdf() { ok("""
    package test
    A:{
      recMdf .self: recMdf A -> this,
      }
    """); }
  @Test void readThisIsNotRecMdf() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E26 recMdfInNonRecMdf]
    Invalid modifier for recMdf test.A[].
    recMdf may only be used in recMdf methods. The method .self/0 has the readOnly modifier.
    """, """
    package test
    A:{
      readOnly .self: recMdf A -> this,
      }
    """); }
  @Test void readThisIsRead() { ok("""
    package test
    A:{
      readOnly .self: readOnly A -> this,
      }
    """); }
  @Test void bicycle1() { fail("""
    In position [###]/Dummy0.fear:3:51
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .wheel/0[]([]), no candidates for .wheel/0 returned the expected type recMdf test.Wheel[]. The candidates were:
    (readOnly test.Bicycle[]): readOnly test.Wheel[]
    (imm test.Bicycle[]): imm test.Wheel[]
    """, """
    package test
    A:{
      recMdf .wheel(b: readOnly Bicycle): recMdf Wheel -> b.wheel,
      }
    Bicycle:{
      recMdf .wheel: recMdf Wheel -> {}
      }
    Wheel:{}
    """); }
  @Test void bicycle2() { ok("""
    package test
    A:{
      readOnly .wheel(b: readOnly Bicycle): readOnly Wheel[readOnly Bicycle] -> b.wheel,
      }
    Bicycle:{
      recMdf .wheel: recMdf Wheel[recMdf Bicycle] -> {}
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle3() { ok("""
    package test
    A:{
      readOnly .wheel(b: readOnly Bicycle): readOnly Wheel[readOnly Bicycle] -> b.wheel,
      readOnly .accept(b: readOnly Bicycle, w: readOnly Wheel[readOnly Bicycle]): Voodo->
        b.acceptWheel(w),
      }
    Bicycle:{
      recMdf .wheel: recMdf Wheel[recMdf Bicycle] -> {},
      recMdf .acceptWheel(w: recMdf Wheel[recMdf Bicycle]): Voodo -> {},
      }
    Voodo:{}
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle4() { ok("""
    package test
    A[T]:{
      readOnly .wheel1(b: readOnly Bicycle[mdf T]): readOnly Wheel[readOnly T] -> b.wheel,
      readOnly .wheel2(b: readOnly Bicycle[mut T]): readOnly Wheel[readOnly T] -> b.wheel,
      readOnly .wheel3(b: readOnly Bicycle[imm T]): readOnly Wheel[imm T] -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf Wheel[recMdf T] -> {},
      }
    Voodo:{}
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle5() { ok("""
    package test
    A[T]:{
      readOnly .wheel1(b: readOnly Bicycle[mdf T]): readOnly T -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      readOnly .loop[T]: mdf T -> this.loop,
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
    """); }
  @Test void bicycle6() { fail("""
    In position [###]/Dummy0.fear:3:55
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .wheel/0[]([]), no candidates for .wheel/0 returned the expected type recMdf T. The candidates were:
    (readOnly test.Bicycle[mdf T]): readOnly T
    (imm test.Bicycle[mdf T]): imm T
    """, """
    package test
    A[T]:{
      recMdf .wheel1(b: readOnly Bicycle[mdf T]): recMdf T -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      readOnly .loop[T]: mdf T -> this.loop,
      }
    Wheel[T]:{
      .getBike: mdf T -> this.getBike,
      }
      
    """); }
  @Test void box() { ok("""
    package test
    Box:{ recMdf #[R](r: recMdf R): recMdf Box[mdf R] -> { r } }
    Box[R]:{ recMdf #: recMdf R }
    """); }
  @Test void boxMutBounds() { ok("""
    package test
    Box:{ #[R: imm, mut](r: mdf R): mut Box[mdf R] -> { r } }
    Box[R]:{ recMdf #: recMdf R }
    """); }
  @Test void boxInnerGens() { ok("""
    package test
    Box:{ recMdf #[R](r: recMdf R): recMdf Box[mdf R] -> { r } }
    Box[R]:{ recMdf #: recMdf R }
    BoxF[R]:{ recMdf #: mut F[recMdf R] }
    F[A,B]:{ readOnly #(a: mdf A): mdf B }
    F[A]:{readOnly #:mdf A}
    Usage[A,B]:{ #(b: mut Box[mut F[readOnly A, readOnly B]]): mut F[readOnly A, readOnly B] -> b# }
    Usage2[A,B]:{ readOnly #(b: mut Box[mut F[readOnly A, readOnly B]]): mut F[readOnly A, readOnly B] -> b# }
    // This is okay because adapterOk works in ways that are dark and mysterious
    Usage3[A,B]:{ recMdf #(b: recMdf Box[recMdf F[readOnly A, readOnly B]]): recMdf F[recMdf A, readOnly B] -> b# }
    Usage4[A,B]:{ readOnly #(b: mut Box[mut F[mdf A]]): mut F[mdf A] -> b# }
    Usage5[A,B]:{ readOnly #(b: mut Box[mut F[mdf A]]): mut F[mdf A] -> b# }
    Usage6[A,B]:{ readOnly #(b: mut BoxF[mdf A]): mut F[mdf A] -> b# }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void invalidIsoPromotionWithRecMdf1() { ok("""
    package test
    A:{
      recMdf .m():recMdf A->this,
      //mut .break1:iso A->this,//bad and fails obviusly
      //mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void invalidIsoPromotionWithRecMdf2() { fail("""
    In position [###]/Dummy0.fear:4:2
    [E23 methTypeError]
    Expected the method .break1/0 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      recMdf .m():recMdf A->this,
      mut .break1:iso A->this,//bad and fails obviusly
      //mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void invalidIsoPromotionWithRecMdf3() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.A[]") for this method call:
    this .m/0[]([])
    were valid:
    (lent test.A[]) <: (iso test.A[]): iso test.A[]
    """, """
    package test
    A:{
      recMdf .m():recMdf A->this,
      //mut .break1:iso A->this,//bad and fails obviusly
      mut .break2:iso A->this.m(),//bad
      recMdf .noBreak:recMdf A->this.m(),//should pass
      }
    """); }
  @Test void captureRecMdfAsMut() { ok("""
    package test
    A:{
      recMdf .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: readOnly B -> LetMut#[mut B, readOnly B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      readOnly .foo(): readOnly B
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
      recMdf .b(a: recMdf A): recMdf B -> {'b .foo -> b },
      mut .break: readOnly B -> LetMut#[mut B, readOnly B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      readOnly .foo(): readOnly B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }
  @Test void inferCaptureRecMdfAsMut2() { ok("""
    package test
    A:{
      recMdf .b(a: mut A): recMdf B -> {'b .foo -> b },
      mut .break: readOnly B -> LetMut#[mut B, readOnly B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      readOnly .foo(): readOnly B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }

  @Test void breakingEarlyFancyRename() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E23 methTypeError]
    Expected the method .foo/2 to return recMdf test.A[], got readOnly test.A[].
    """, """
    package test
    A:{
      recMdf .foo(a:recMdf A, b:readOnly A):recMdf A -> b
      }
    B:{
      .foo(mutR: mut A, readR: readOnly A): mut A -> mutR.foo(mutR, readR)
      }
    """); }

  @Test void recMdfCallsRecMdf() { ok("""
    package test
    A:{
      recMdf .inner: recMdf A -> {},
      recMdf .outer: recMdf A -> recMdf A.inner,
      }
    """); }
  @Test void recMdfCallsRecMdfA() { ok("""
    package test
    A:{
      recMdf .asRecMdf: recMdf A -> recMdf A{'inner
        recMdf .inner: recMdf A -> inner
        },
      recMdf .inner: recMdf A,
      }
    B:{ #(a: mut A): mut A -> a.inner }
    """); }
  @Test void noCaptureReadInMut() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E30 badCapture]
    'readOnly this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A:{ mut .prison: readOnly B }
    B:{
      readOnly .break: mut A -> { this }
      }
    """); }
  @Test void noCaptureMdfInMut() { fail("""
    In position [###]/Dummy0.fear:4:29
    [E30 badCapture]
    'readOnly this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      readOnly .break: mut A[B] -> { this }
      }
    """); }
  @Test void noCaptureMdfInMut2() { fail("""
    In position [###]/Dummy0.fear:4:34
    [E30 badCapture]
    'readOnly this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      readOnly .break: mut A[readOnly B] -> { this } // this capture was being allowed because this:mdf B was adapted with readOnly to become this:recMdf B (which can be captured by mut)
      }
    """); }
  @Test void noCaptureMdfInMut3() { fail("""
    In position [###]/Dummy0.fear:4:36
    [E30 badCapture]
    'recMdf this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: mdf X }
    B:{
      recMdf .break: mut A[readOnly B] -> { this } // this capture was being allowed because this:mdf B was adapted with readOnly to become this:recMdf B (which can be captured by mut)
      }
    """); }

  @Test void noCaptureMdfInMut4() { fail("""
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
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    """); }
  @Test void noCaptureImmAsRecMdfExample() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: imm B -> (A.m(B)).absMeth }
    """); }
  @Test void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear1$ } .m/1[]([[-imm-][test.B[]]{'fear2$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type lent test.B[]. The candidates were:
    (lent test.L[imm test.B[]]): imm test.B[]
    (iso test.L[imm test.B[]]): imm test.B[]
    """, """
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: lent B -> (A.m(B)).absMeth }
    """); }
  @Test void okCaptureImmAsRecMdfTopLvl1() { ok("""
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    L'[X]:L[imm X]{ recMdf .absMeth: imm X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
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
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L'[imm B]{.absMeth->par} }
    """); }

  @Test void recMdfInheritance() { ok("""
    package test
    Foo:{}
    A[X]:{ recMdf .m: recMdf X -> this.m }
    B:A[imm Foo]
    C:B
    CanPass0:{ readOnly .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readOnly .m(par: mut B) : imm Foo -> par.m  }
    CanPass2:{ readOnly .m(par: mut C) : imm Foo -> par.m  }
    //NoCanPass:{ readOnly .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void recMdfInheritanceFail() { fail("""
    In position [###]/Dummy0.fear:7:48
    [E32 noCandidateMeths]
    When attempting to type check the method call: par .m/0[]([]), no candidates for .m/0 returned the expected type mut test.Foo[]. The candidates were:
    (mut test.B[]): imm test.Foo[]
    (iso test.B[]): imm test.Foo[]
    (lent test.B[]): imm test.Foo[]
    """, """
    package test
    Foo:{}
    A[X]:{ recMdf .m: recMdf X -> this.m }
    B:A[imm Foo]{}
    CanPass0:{ readOnly .m(par: mut A[imm Foo]) : imm Foo -> par.m  }
    CanPass1:{ readOnly .m(par: mut B) : imm Foo -> par.m  }
    NoCanPass:{ readOnly .m(par: mut B) : mut Foo -> par.m  }
    """); }

  @Test void immToReadCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: readOnly X }
    A:{ readOnly .m[T](par: imm T) : readOnly L[imm T] -> readOnly L[imm T]{.absMeth->par} }
    """); }

  @Test void immCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: imm X }
    A:{ readOnly .m[T](par: mut T) : mut L[mut T] -> mut L[mut T]{.absMeth->par} }
    """); }

  @Test void readMethOnImmLambdaCannotCaptureRead() { fail("""
    In position [###]/Dummy0.fear:4:69
    [E30 badCapture]
    'readOnly par' cannot be captured by a readOnly method in an imm lambda.
    """, """
    package test
    B:{}
    L[X]:{ readOnly .absMeth: readOnly X }
    A:{ readOnly .m[T](par: readOnly T) : imm L[imm T] -> imm L[imm T]{.absMeth->par} }
    """);}

  @Test void immReturnsReadAsLent() { fail("""
    In position [###]/Dummy0.fear:4:61
    [E23 methTypeError]
    Expected the method .absMeth/0 to return lent T, got imm T.
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: lent X }
    A:{ readOnly .m[T](par: readOnly T) : lent L[imm T] -> lent L[imm T]{.absMeth->par} }
    """); }

  @Test void noMdfParamAsLent() { fail("""
    In position [###]/Dummy0.fear:4:90
    [E23 methTypeError]
    Expected the method .absMeth/0 to return lent T, got readOnly T.
    """, """
    package test
    B:{}
    L[X:read,mut,readOnly,imm,lent]:{ mut .absMeth: lent X }
    A:{ readOnly .m[T:read,mut,readOnly,imm,lent](par: mdf T): lent L[mut T] -> lent L[mut T]{.absMeth->par} }
    C:{ #: lent L[mut B] -> A{}.m[readOnly B](B) }
    """); }

  @Test void noMutHygRenamedGX1() { ok("""
    package test
    alias base.NoMutHyg as NoMH,
    Person:{}
    
    Foo[X]:NoMH[mdf X]{ recMdf .stuff: recMdf X }
    FooP0[Y]:Foo[mdf Y]{}
    FooP1:{ #(p: readOnly Person): lent Foo[readOnly Person] -> { p } }
    FooP2:{ #(p: readOnly Person): lent FooP0[readOnly Person] -> { p } }
    
    Test:{
      .t1(t: readOnly Person): lent Foo[readOnly Person] -> FooP1#t,
      .t2(t: readOnly Person): lent FooP0[readOnly Person] -> FooP2#t,
      .t2a(t: readOnly Person): lent Foo[readOnly Person] -> FooP2#t,
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
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{} Abort:{ ![T]: mdf T -> this! }
    """, Base.load("nums.fear")); }

  @Disabled // TODO: Figure out better way to load the rest of the base libs
  @Test void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:2
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    s .use/2[imm base.caps.IO[]]([[-imm-][base.caps.IO'[]]{'fear[###]$ }, [-mut-][base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]]{'fear[###]$ #/2([io, fear1$]): Sig[mdf=mut,gens=[],ts=[lent base.caps.IO[], lent base.caps.System[imm base.Void[]]],ret=imm base.Void[]] -> fear1$ .return/1[]([[-lent-][base.caps.LentReturnStmt[imm base.Void[]]]{'fear[###]$ #/0([]): Sig[mdf=lent,gens=[],ts=[],ret=imm base.Void[]] -> io .println/1[]([[-imm-]["Hello, World!"[]]{'fear[###]$ }])}])}])
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear"), Base.load("block.fear"), Base.load("ref.fear"), Base.load("iter.fear")); }
  @Disabled // TODO: Figure out better way to load the rest of the base libs
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear"), Base.load("iter.fear"), Base.load("block.fear"), Base.load("ref.fear")); }
  @Disabled // TODO: Figure out better way to load the rest of the base libs
  @Test void incompatibleITs() { fail("""
    In position [###]/Dummy1.fear:7:8
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    cont #/2[]([c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }]), this])
    were valid:
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: (mut base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]): mdf R
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy1.fear:7:11
        [E33 callTypeError]
        Type error: None of the following candidates for this method call:
        c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])
        were valid:
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps._RootCap[]): lent C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], iso base.caps._RootCap[]): iso C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <: (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], mut base.caps._RootCap[]): lent C
        
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], iso C, iso base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], mut C, lent base.caps.System[mdf R]): mdf R
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[mdf R]) <: (iso base.caps.UseCapCont[imm C, mdf R], lent C, mut base.caps.System[mdf R]): mdf R
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
    """, Base.load("lang.fear"), Base.load("bools.fear"), Base.load("nums.fear"), Base.load("strings.fear"), Base.load("optionals.fear"), Base.load("lists.fear"), Base.load("iter.fear"), Base.load("block.fear"), Base.load("ref.fear")); }
  @Test void recMdfCannotBeSubtypeOfMdf1() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E23 methTypeError]
    Expected the method #/1 to return mdf A, got recMdf A.
    Try writing the signature for #/1 explicitly if it needs to return a recMdf type.
    """, """
    package test
    F[A]:{ recMdf #(a:recMdf A):mdf A->a }
    M:{ mut .mutMe: mut M -> this.mutMe } // if this method can be called from M it is broken
    Break:{
      .myF: imm F[mut M] -> {},
      .b1(m: imm M): mut M -> this.myF#m,
      .b2(m: imm M): mut M -> (this.myF#m).mutMe,
      }
    """); }
  @Test void recMdfCannotBeSubtypeOfMdf2() { fail("""
    In position [###]/Dummy0.fear:2:7
    [E30 badCapture]
    'mut test.M[]' cannot be captured by an imm method in an imm lambda.
    """, """
    package test
    F[A]:{ recMdf #(a:recMdf A):recMdf A->a }
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
      readOnly .get: readOnly MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      readOnly .match[R](m: lent Matcher[mdf R]): mdf R -> m.get
      }
    Usage:{
      .direct(preR: readOnly PreR): readOnly MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: readOnly MyRes): readOnly MyRes -> MatcherContainer.match{ r }
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
      readOnly .match[R](m: mut Matcher[mdf R]): mdf R -> m.get
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
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x: mdf X): mdf X -> x, }
    B[Y]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#({}),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  @Test void captureReadAsRecMdfLent() { fail("""
    In position [###]/Dummy0.fear:7:45
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear0$ } .m/1[readOnly test.B[]]([[-read-][test.B[]]{'fear1$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mut test.B[]. The candidates were:
    (lent test.L[readOnly test.B[]]): readOnly test.B[]
    (iso test.L[readOnly test.B[]]): imm test.B[]
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[mdf X]{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m[T](par: readOnly T): lent L[readOnly T] -> lent L[readOnly T]{.absMeth->par} }
    
    Break:{ #(rb: readOnly B): mut B -> (A.m(readOnly B)).absMeth }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void captureReadAsRecMdfRead() { fail("""
    In position [###]/Dummy0.fear:7:45
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear0$ } .m/1[readOnly test.B[]]([[-read-][test.B[]]{'fear1$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type mut test.B[]. The candidates were:
    (readOnly test.L[readOnly test.B[]]): readOnly test.B[]
    (imm test.L[readOnly test.B[]]): imm test.B[]
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    B:{}
    L[X]:NoMutHyg[mdf X]{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m[T](par: readOnly T): readOnly L[readOnly T] -> readOnly L[readOnly T]{.absMeth->par} }
    
    Break:{ #(rb: readOnly B): mut B -> (A.m(readOnly B)).absMeth }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void aliasGenericHiding() { fail("""
    In position [###]/Dummy0.fear:5:0
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

  @Test void shouldKeepIsoThisAsIso() { ok("""
    package test
    A[X]:{
      iso .m: iso A[mdf X] -> this,
      }
    """); }

  @Test void immMethodOneMutIsoPromotion() { ok("""
    package test
    A:{
      mut .a(a: iso A): iso A -> (B.foo(a)).a1,
      }
    B:{
      .foo(a: iso A): iso Container -> Container'#(a),
      }
    Container':{ #(a: mut A): mut Container -> { .a1 -> a, .a2 -> a } }
    Container:{
      mut .a1: mut A,
      mut .a2: mut A,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiArg1() { fail("""
    In [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .foo/2 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: lent A): iso A -> a,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiArg2() { fail("""
    In [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .foo/2 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: lent A): iso A -> aa,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiMut() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .foo/2 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: mut A): iso A -> a,
      }
    """); }
  @Test void immMethodOneMutIsoPromotionBad() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .foo/1 to return mut test.A[], got lent test.A[].
    """, """
    package test
    A:{
      mut .a(randomSharedMut: mut A): iso A -> B.foo(this),
      }
    B:{
      .foo(a: mut A): iso A -> a,
      }
    """); }

  private static final String noMutHyg = """
    package base
    NoMutHyg[X]:{}
    """;
  private static final String recMdfGetForListsHelpers = """
    package test
    Abort:{ ![R]: mdf R -> this! }
    Nat:{
      .pred: Nat,
      .succ: S -> { .pred -> this },
      .isZero: Bool,
      }
    Z:Nat{ .pred -> Abort!, .isZero -> True }
    S:Nat{ .isZero -> False, }

    Bool:{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      .if[R](f: mut ThenElse[mdf R]): mdf R,
      ?[R](f: mut ThenElse[mdf R]): mdf R -> this.if(f),
      .look[R](f: readOnly BoolView[mdf R]): mdf R,
      }
    True:Bool{
      .and(b) -> b,
      .or(b) -> this,
      .not -> False,
      .if(f) -> f.then(),
      .look(f) -> f.then(),
      }
    False:Bool{
      .and(b) -> this,
      .or(b) -> b,
      .not -> True,
      .if(f) -> f.else(),
      .look(f) -> f.else(),
      }
    ThenElse[R]:{ mut .then: mdf R, mut .else: mdf R, }
    BoolView[R]:{ recMdf .then: mdf R, recMdf .else: mdf R, }
    """;
  @Test void recMdfGetForLists1() { ok("""
    package test
    LList[E]:{
      recMdf .get(i: Nat): recMdf E -> Abort!,
      recMdf .push(e: recMdf E): recMdf LList[mdf E] -> { .get(i) -> e } // passes
      }
    """, recMdfGetForListsHelpers); }
  @Test void recMdfGetForLists2() { ok("""
    package test
    ThisBox:{ recMdf #: recMdf Foo }
    Foo:{
      recMdf .self: recMdf Foo -> this, // passes
      recMdf .test: recMdf Foo -> recMdf ThisBox{ this }# // fails
      }
    """); }
  @Test void recMdfGetForLists3() { ok("""
    package test
    LList[E]:{
      recMdf .get(i: Nat): recMdf E -> Abort!,
      recMdf .push(e: recMdf E): recMdf LList[recMdf E] -> { .get(i) -> i.isZero.look(recMdf ListGet#(e, this)) },
      }
    ListGet:{ recMdf #[E](e: recMdf E, t: recMdf LList[mdf E]): recMdf BoolView[recMdf E] -> { .then -> e, .else -> t.get(Z) } }
    """, recMdfGetForListsHelpers); }

  @Test void dontFearTheLambdaEx1() { ok("""
    package ex
    alias base.Str as Str,
    
    Stringable:{ .str: Str }
    Bool:Stringable{}
    True:Bool{ .str: "True" -> "True" }
    False:Bool{ .str: Str -> "False" }
    """, """
    package base
    Str:{}
    _StrInstance:Str{}
    """); }

  @Test void panickingPanics() { ok("""
    package test
    Panic:{
      .thoughts: Thoughts
      }
    Panic':{
      #(isAfraid: Bool): Panic -> isAfraid ? {
        .then -> { Fear },
        .else -> { Calm },
        }
      }
    Thoughts:{ .match[R](m: ThoughtMatcher[R]): R }
    ThoughtMatcher[R]:{ .fear: R, .calm: R, }
    Fear:Thoughts{ m -> m.fear }
    Calm:Thoughts{ m -> m.calm }
    
    PanicTriage:{
      .panickingPanics(panics: List[Panic]): List[Panic] -> this._panickingPanics({}, panics),
      ._panickingPanics(acc: List[Panic], panics: List[Panic]): List[Panic] -> panics.head.match{
        .some(panic) -> panic.thoughts.match{
          .fear -> this._panickingPanics(acc + panic, panics.tail),
          .calm -> this._panickingPanics(acc, panics.tail),
          },
        .none -> acc,
        },
      }
    """, """
    package test
    alias base.Str as Str,
    alias base.Void as Void,
    alias base.UInt as UInt,
    alias base.Int as Int,
    Bool:{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      ?[R](f: ThenElse[R]): R,
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then()}
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ .then: R, .else: R, }
    
    Cons:{
      #[E](h: E, t: List[E]): List[E] -> { .match(m) -> m.elem(h, t) },
      }
    List[E]:{
      .match[R](m: ListMatch[E, R]): R -> m.empty,
      .isEmpty: Bool -> this.match{ .empty -> True, .elem(_,_) -> False },
      .len: UInt -> this.match{ .empty -> 0u, .elem(_,t) -> t.len + 1u, },
      ++(l1: List[E]): List[E] -> this.match{
        .empty -> l1,
        .elem(h, t) -> Cons#(h, t ++ l1)
        },
      +(e: E): List[E] -> this ++ (Cons#(e, {})),
      .get(i: UInt) : Opt[E] -> this.match{
        .empty -> {},
        .elem(h, t) -> (i == 0u) ? { .then -> Opt#h, .else -> t.get(i - 1u) }
        },
      .head: Opt[E] -> this.match{
        .empty -> {},
        .elem(h,_) -> Opt#h,
        },
      .tail: List[E] -> this.match{
        .empty -> {},
        .elem(_,t) -> t,
        },
      }
    ListMatch[E,R]:{ .elem(head: E, tail: List[E]): R, .empty: R }
    
    Opt:{ #[T](x: T): Opt[T] -> { .match(m) -> m.some(x) } }
    Opt[T]:{
      .match[R](m: OptMatch[T, R]): R -> m.none,
      .map[R](f: OptMap[T,R]): Opt[R] -> this.match(f),
      .do(f: OptDo[T]): Opt[T] -> this.match(f),
      .flatMap[R](f: OptFlatMap[T, R]): Opt[R] ->this.match(f),
      ||(alt: T): T -> this.match{ .some(x) -> x, .none -> alt },
      .isNone: Bool -> this.match{ .none -> True, .some(_) -> False },
      .isSome: Bool -> this.match{ .none -> False, .some(_) -> True },
      }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none -> {} }
    OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opt#(this#x), .none -> {} }
    OptDo[T]:OptMatch[T,Opt[T]]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opt#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):T -> x
      }
    """, """
    package base
    alias test.Bool as Bool, alias test.True as True, alias test.False as False,
    Str:{}
    _StrInstance:Str{}
    Void:{}
    Abort:{ ![R]: mdf R -> this! } // can be optimised to just terminate (goes stuck)
    """, """
    package base
    Sealed:{}
    Int:Sealed,MathOps[Int],IntOps[Int]{
      readOnly .uint: UInt,
      readOnly .float: Float,
      // not Stringable due to limitations of the Java codegen target
      readOnly .str: Str,
      }
    UInt:Sealed,MathOps[UInt],IntOps[UInt]{
      readOnly .int: Int,
      readOnly .float: Float,
      // not Stringable due to limitations of the Java codegen target
      readOnly .str: Str,
      }
    Float:Sealed,MathOps[Float]{
      readOnly .int: Int,
      readOnly .uint: UInt,
      readOnly .round: Int,
      readOnly .ceil: Int,
      readOnly .floor: Int,
      readOnly **(n: readOnly Float): Float, // pow
      readOnly .isNaN: Bool,
      readOnly .isInfinity: Bool,
      readOnly .isNegInfinity: Bool,
      // not Stringable due to limitations of the Java codegen target
      readOnly .str: Str,
      }
        
    MathOps[T]:Sealed{
      readOnly +(n: readOnly T): T,
      readOnly -(n: readOnly T): T,
      readOnly *(n: readOnly T): T,
      readOnly /(n: readOnly T): T,
      readOnly %(n: readOnly T): T,
      readOnly .abs: T,
        
      // Comparisons
      readOnly >(n: readOnly T): Bool,
      readOnly <(n: readOnly T): Bool,
      readOnly >=(n: readOnly T): Bool,
      readOnly <=(n: readOnly T): Bool,
      readOnly ==(n: readOnly T): Bool,
      }
    IntOps[T]:Sealed{
      // bitwise
      readOnly >>(n: readOnly T): T,
      readOnly <<(n: readOnly T): T,
      readOnly ^(n: readOnly T): T,
      readOnly &(n: readOnly T): T,
      readOnly |(n: readOnly T): T,
        
      readOnly **(n: readOnly UInt): T, // pow
      }
        
    // Fake concrete type for all numbers. The real implementation is generated at code-gen.
    _IntInstance:Int{
      .uint -> Abort!,
      .float -> Abort!,
      .str -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
        
      // bitwise
      >>(n) -> Abort!,
      <<(n) -> Abort!,
      ^(n) -> Abort!,
      &(n) -> Abort!,
      |(n) -> Abort!,
        
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    _UIntInstance:UInt{
      .int -> Abort!,
      .float -> Abort!,
      .str -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
        
      // bitwise
      >>(n) -> Abort!,
      <<(n) -> Abort!,
      ^(n) -> Abort!,
      &(n) -> Abort!,
      |(n) -> Abort!,
        
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    _FloatInstance:Float{
      .int -> Abort!,
      .uint -> Abort!,
      .str -> Abort!,
      .round -> Abort!,
      .ceil -> Abort!,
      .floor -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    """); }

  @Test void readRecvMakesMutPromotion() { ok("""
    package test
    A:{ readOnly .newB: mut B -> mut B }
    B:{}
    C:{ .promote(b: iso B): B -> b }
    Test:{ #: B -> C.promote(A.newB) }
    """); }
  @Test void readMethOnLentPromotion() { ok("""
    package test
    A:{ readOnly .newB: mut B -> mut B }
    B:{}
    C:{ .promote(b: iso B): B -> b }
    Test:{ #(a: lent A): B -> C.promote(a.newB) }
    """); }
  @Test void readMethOnMutPromotion() { ok("""
    package test
    A:{ readOnly .newB: mut B -> mut B }
    B:{}
    C:{ .promote(b: iso B): B -> b }
    Test:{ #(a: mut A): B -> C.promote(a.newB) }
    """); }

  @Test void shouldNotPromoteOneLentToMutToIso() { fail("""
    In position [###]/Dummy0.fear:4:45
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.Ref[imm test.Name[]]") for this method call:
    p .name/0[]([])
    were valid:
    (lent test.Person[]) <: (mut test.Person[]): mut test.Ref[imm test.Name[]]
    (lent test.Person[]) <: (iso test.Person[]): iso test.Ref[imm test.Name[]]
    """, """
    package test
    Person:{ mut .name: mut Ref[Name] }
    Usage:{
      .mutate(p: lent Person): iso Ref[Name] -> p.name,
      }
      
    Ref[X]:{ recMdf .get: recMdf X, lent .set(x: mdf X): Void }
    Void:{} Name:{}
    """); }

  @Test void invalidBoundsOnInlineLambda() { fail("""
    In position [###]/Dummy0.fear:3:6
    [E5 invalidMdfBound]
    The type lent test.Foo[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
    """, """
    package test
    A[X: imm, mut]:{}
    Foo:{ .bar: A[lent Foo] -> A[lent Foo] }
    """); }

  @Test void mixedLentPromo1a() {
    fail("""
      In position [###]/Dummy0.fear:3:4
      [E5 invalidMdfBound]
      The type lent base.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
            
      In position [###]/Dummy0.fear:10:21
      [E5 invalidMdfBound]
      The type lent base.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
      """, """
      package base
      // should also not pass with `lent Ref[lent B]`
      A:{ lent .b: lent Ref[lent B] }
      B:{}
      F:{
        .ohNo(b: lent B): imm A -> F.ohNo'(F.newA, b),
        .ohNo'(a: mut A, b: lent B): mut A -> F.ohNo''(a, F.break(a, b)),
        .ohNo''(a: mut A, v: Void): mut A -> a,
        
        .works: mut A -> { .b -> Ref#[lent B]{} },
        .newA: mut A -> F.newA(Ref#[lent B]{}),
        .newA(b: mut Ref[lent B]): mut A -> { .b -> b },
        .break(a: lent A, b: lent B): Void -> a.b := b,
        }
      """, """
      package base
      Void:{} Sealed:{}
      Yeet:{
        #[X](x: mdf X): Void -> this.with(x, Void),
        .with[X,R](_: mdf X, res: mdf R): mdf R -> res,
        }
      Ref:{ #[X:imm,mut](x: mdf X): mut Ref[mdf X] -> this#(x) }
      Ref[X:imm,mut]:Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        mut .swap(x: mdf X): mdf X -> mut _FakeCapture[mdf X]{ x }.prev,
        mut :=(x: mdf X): Void -> Yeet#(this.swap(x)),
        mut .set(x: mdf X): Void -> this := x,
        mut <-(f: mut UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
        mut .update(f: mut UpdateRef[mdf X]): mdf X -> this <- f,
        }
      _FakeCapture[X]:{ recMdf .self: recMdf X, mut .prev: mdf X -> Abort! }
      UpdateRef[X]:{ mut #(x: mdf X): mdf X }
      Abort:{ ![R]: mdf R -> this! }
      """);
  }
  @Test void mixedLentPromo1b() {
    fail("""
      In position [###]/Dummy0.fear:3:4
      [E5 invalidMdfBound]
      The type lent base.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
            
      In position [###]/Dummy0.fear:10:21
      [E5 invalidMdfBound]
      The type lent base.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
      """, """
      package base
      // should also not pass with `lent Ref[lent B]`
      A:{ lent .b: lent Ref[lent B] }
      B:{}
      F:{
        .ohNo(b: lent B): imm A -> F.ohNo'(F.newA, b),
        .ohNo'(a: mut A, b: lent B): mut A -> F.ohNo''(a, F.break(a, b)),
        .ohNo''(a: mut A, v: Void): mut A -> a,
        
        .works: mut A -> { .b -> Ref#[lent B]{} },
        .newA: mut A -> F.newA(Ref#[lent B]{}),
        .newA(b: mut Ref[lent B]): mut A -> { .b -> b },
        .break(a: lent A, b: lent B): Void -> a.b := b,
        }
      """, """
      package base
      Void:{} Sealed:{}
      Yeet:{
        #[X](x: mdf X): Void -> this.with(x, Void),
        .with[X,R](_: mdf X, res: mdf R): mdf R -> res,
        }
      Ref:{ #[X:imm,mut](x: mdf X): mut Ref[mdf X] -> this#(x) }
      Ref[X:imm,mut]:Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        mut .swap(x: mdf X): mdf X,
        mut :=(x: mdf X): Void -> Yeet#(this.swap(x)),
        mut .set(x: mdf X): Void -> this := x,
        mut <-(f: mut UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
        mut .update(f: mut UpdateRef[mdf X]): mdf X -> this <- f,
        }
      UpdateRef[X]:{ mut #(x: mdf X): mdf X }
      Abort:{ ![R]: mdf R -> this! }
      """);
  }

  @Disabled
  @Test void mixedLentPromo2() {
    fail("""
      """, """
      package base
      A:{ lent .b: lent Ref[lent B] }
      B:{}
      F:{
        .break(a: lent A, b: lent B): Void -> a.b := b,
        }
      """, """
      package base
      Void:{} NoMutHyg[X]:{} Sealed:{}
      Yeet:{
        #[X](x: mdf X): Void -> this.with(x, Void),
        .with[X,R](_: mdf X, res: mdf R): mdf R -> res,
        }
      Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
      Ref[X]:NoMutHyg[mdf X],Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        lent .swap(x: mdf X): mdf X,
        lent :=(x: mdf X): Void -> Yeet#(this.swap(x)),
        lent .set(x: mdf X): Void -> this := x,
        lent <-(f: mut UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
        lent .update(f: mut UpdateRef[mdf X]): mdf X -> this <- f,
        }
      UpdateRef[X]:NoMutHyg[mdf X]{ mut #(x: mdf X): mdf X }
      """);
  }

  @Test void invalidTraitBounds1() { fail("""
    [###]/Dummy0.fear:3:2
    [E5 invalidMdfBound]
    The type imm test.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X: mut]:{}
    B:A[imm B]
    """); }
  @Test void invalidTraitBounds2() { fail("""
    [###]/Dummy0.fear:3:2
    [E5 invalidMdfBound]
    The type imm test.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X: mut]:{ .a1: mdf X }
    B:A[imm B]
    """); }

  @Test void mutMdfAdapt() { fail("""
    In position [###]/Dummy0.fear:4:78
    [E30 badCapture]
    'mdf par' cannot be captured by a mut method in a recMdf lambda.
    """, """
    package test
    B:{}
    L[X]:{ iso .absMeth: imm X }
    A:{ recMdf .m[T](par: mdf T) : recMdf L[lent T] -> recMdf L[lent T]{.absMeth->par} }
    """); }

  @Test void extraMethInLambda() { ok("""
    package test
    A:{
      .m1: A -> { 'self
        .m1: A -> self.private[], // must provide full signature for private methods
        .private: A -> {}
        }
      }
    """); }
  @Test void extraMethInLambdaGens() { ok("""
    package test
    A:{
      .m1: A -> { 'self
        .m1: A -> self.private[A], // must provide full signature for private methods
        .private[X]: A -> {}
        }
      }
    """); }

  @Test void lentCannotAdaptWithMut() { fail("""
    In position [###]/Dummy0.fear:4:68
    [E23 methTypeError]
    Expected the method .absMeth/0 to return mdf T, got readOnly T.
        
    In position [###]/Dummy0.fear:7:36
    [E5 invalidMdfBound]
    The type mut test.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: read.
    """, """
    package test
    B:{}
    L[X]:{ lent .absMeth: mdf X }
    A:{ recMdf .m[T: read](par: mdf T) : lent L[mdf T] -> lent L[mdf T]{.absMeth->par} }
    
    C:{
      .m1(b: mut B) : lent L[mut B] -> A.m(b),
      .m2(b: lent L[mut B]): mut B -> b.absMeth,
      }
    """); }

  @Test void adaptRecMdfMutBreak() { fail("""
    In position [###]/Dummy0.fear:8:39
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .get/0[]([]), no candidates for .get/0 returned the expected type mut test.Person[]. The candidates were:
    (imm test.BoxMutP[]): imm test.Person[]
    """, """
    package test
    Person:{}
    Box[X]:{ recMdf .get: recMdf X }
    BoxMutP:Box[mut Person]{}
    F:{
      #(p:mut Person):mut BoxMutP->mut BoxMutP{ recMdf .get: recMdf Person -> p },
      .break():imm BoxMutP->this#({}),
      .breakMe(b:imm BoxMutP):mut Person->b.get,
      }
    """); }
  @Test void adaptRecMdfMutOk() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family:List[mut Person]{}
    """); }

  @Test void badIsoCapture() { fail("""
    In position [###]/Dummy0.fear:4:53
    [E30 badCapture]
    'mut par' cannot be captured by a mut method in a iso lambda.
    """, """
    package test
    B:{}
    L:{ iso .absMeth: mut B }
    A:{ recMdf .m(par: mut B) : iso L -> iso L{.absMeth->par} }
    """); }
  @Test void badImmCapture() { fail("""
    In position [###]/Dummy0.fear:4:54
    [E30 badCapture]
    'readOnly par' cannot be captured by an imm method in an imm lambda.
    """, """
    package test
    B:{}
    L:{ imm .absMeth: readOnly B }
    A:{ recMdf .m(par: readOnly B) : imm L -> imm L{.absMeth->par} }
    """); }

  // TODO: interesting tests that look into what should stay as recMdf in propagateMdf
  @Disabled
  @Test void recMdfRenameOnITReturnType() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf test.Person[], got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm Foo -> imm Foo{
      recMdf .first: recMdf Person -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnITReturnType2() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf test.Person[], got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm Foo -> { bob } }
    """); }
  @Disabled
  @Test void recMdfRenameOnITReturnTypeMGens() { fail("""
    In position [###]/Dummy0.fear:6:2
    [E23 methTypeError]
    Expected the method .first/0 to return recMdf Y, got imm test.Person[].
    """, """
    package test
    Person:{}
    //List[X]:{ recMdf .first: recMdf X }
    Foo[X]:{ recMdf .first: recMdf X }
    Bar:{ .bar[Y](bob: imm Person): imm Foo[mdf Y] -> imm Foo[mdf Y]{
      recMdf .first: recMdf Y -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnGXReturnType() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .first: recMdf X }
    //Foo:{ recMdf .first: recMdf Person }
    Bar:{ .bar(bob: imm Person): imm List[Person] -> imm List[imm Person]{
      recMdf .first: imm Person -> bob,
      }}
    """); }
  @Disabled
  @Test void recMdfRenameOnGXReturnTypeInfer() { ok("""
    package test
    Person:{}
    List[X]:{ recMdf .first: recMdf X }
    Bar:{ .bar[Y](bob: mdf Y): imm List[mdf Y] -> { bob } }
    """); }

  @Test void noIsoMoreThanOnce() { fail("""
    In position [###]/Dummy0.fear:3:63
    [E45 multipleIsoUsage]
    The isolated reference "x1" is used more than once.
    """, """
    package test
    Caps:{} Void:{}
    A:{ .break(x1: iso Caps, x2: iso Caps): Void -> this.break(x1, x1) }
    """); }
  @Test void isoOnce() { ok("""
    package test
    Caps:{} Void:{}
    A:{ .notBreak(x1: iso Caps, x2: iso Caps): Void -> this.notBreak(x1, x2) }
    """); }
  @Test void noIsoMoreThanOnceCCaptured() { fail("""
    In position [###]/Dummy0.fear:4:65
    [E45 multipleIsoUsage]
    The isolated reference "x1" is used more than once.
    """, """
    package test
    Caps:{} Void:{}
    A:{
      .break(x1: iso Caps, x2: iso Caps): Void -> this.break'(x1, B{ x1 }),
      .break'(x1: iso Caps, b: B): Void -> {},
      }
    B:{ .x: Caps }
    """); }
  @Test void noIsoMoreThanOnceCCapturedOk() { ok("""
    package test
    Caps:{} Void:{}
    A:{
      .break(x1: iso Caps, x2: iso Caps): Void -> this.break'(x1, B{ x2 }),
      .break'(x1: iso Caps, b: B): Void -> {},
      }
    B:{ .x: Caps }
    """); }

  final String blockSrc = """
    package test
    ReturnStmt[R]:{ mut #: mdf R }
    Condition:{ mut #: Bool }
    VarContinuation[X,R:mut,imm]:{ mut #(x: mdf X, self: mut Block[mdf R]): mdf R }
    Do:{
      #[R:mut,imm]: mut Block[mdf R] -> {},
//      .hyg[R]: mut BlockHyg[mdf R] -> {},
      }
    Block[R:mut,imm]:{
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      mut .do(r: mut ReturnStmt[Void]): mut Block[mdf R] -> this._do(r#),
        mut ._do(v: Void): mut Block[mdf R] -> this,
      mut .var[X](x: mut ReturnStmt[mdf X], cont: mut VarContinuation[mdf X, mdf R]): mdf R -> cont#(x#, this),
      mut .if(p: mut Condition): mut BlockIf[mdf R] -> p# ? { 'cond
        .then -> { 't
          .return(a) -> _DecidedBlock#(a#),
          .do(r) -> t._do[](r#),
            mut ._do(v: Void): mut Block[mdf R] -> this,
          },
        .else -> { 'f
          .return(_) -> this,
          .do(_) -> this,
          },
        },
      }
    BlockIf[R:mut,imm]:{
      mut .return(a: mut ReturnStmt[mdf R]): mut Block[mdf R],
      mut .do(r: mut ReturnStmt[Void]): mut Block[mdf R],
      }
    _DecidedBlock:{
      #[R: imm, mut](res: mdf R): mut Block[mdf R] -> { 'self
        .return(_) -> res,
        .do(_) -> self,
        .var(_, _) -> res,
        }
      }
    
//    ReturnStmtHyg[R]:{ lent #: mdf R }
//    ConditionHyg:{ lent #: Bool }
//    VarContinuationHyg[X,R]:{ lent #(x: mdf X, self: lent BlockHyg[mdf R]): mdf R }
//    BlockHyg[R]:{
//      lent .return(a: mut ReturnStmtHyg[mdf R]): mdf R -> a#,
//      lent .do(r: lent ReturnStmtHyg[Void]): lent BlockHyg[mdf R] -> this._do(r#),
//        lent ._do(v: Void): lent BlockHyg[mdf R] -> this,
//      lent .var[X](x: lent ReturnStmtHyg[mdf X], cont: lent VarContinuationHyg[mdf X, mdf R]): mdf R -> cont#(x#, this),
//      lent .if(p: lent ConditionHyg): lent BlockIfHyg[mdf R] -> p#.look(lent BoolView[lent BlockIfHyg[mdf R]]{ 'cond
//        .then -> { 't
//          .return(a) -> _DecidedBlockHyg#(a#),
//          .do(r) -> t._do[](r#),
//            lent ._do(v: Void): lent BlockHyg[mdf R] -> this,
//          },
//        .else -> { 'f
//          .return(_) -> this,
//          .do(_) -> this,
//          },
//        }),
//      }
//    BlockIfHyg[R]:{
//      lent .return(a: lent ReturnStmtHyg[mdf R]): lent BlockHyg[mdf R],
//      lent .do(r: lent ReturnStmtHyg[Void]): lent BlockHyg[mdf R],
//      }
//    _DecidedBlockHyg:{
//      #[R](res: mdf R): lent BlockHyg[mdf R] -> { 'self
//        .return(_) -> res,
//        .do(_) -> self,
//        .var(_, _) -> res,
//        }
//      }
    
    Void:{} Abort:{ ![R]: mdf R -> this! }
    Bool:{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .xor(b: Bool): Bool,
      ^(b: Bool): Bool -> this.xor(b),
      .not: Bool,
      .if[R](f: mut ThenElse[mdf R]): mdf R,
      ?[R](f: mut ThenElse[mdf R]): mdf R -> this.if(f),
      .ifHyg[R](f: lent ThenElse[mdf R]): mdf R -> this.if(f),
      recMdf .look[R](f: readOnly BoolView[mdf R]): mdf R,
      }
    True:Bool{
      .and(b) -> b,
      .or(b) -> this,
      .xor(b) -> b.not,
      .not -> False,
      .if(f) -> f.then(),
      .look(f) -> f.then(),
      }
    False:Bool{
      .and(b) -> this,
      .or(b) -> b,
      .xor(b) -> b,
      .not -> True,
      .if(f) -> f.else(),
      .look(f) -> f.else(),
      }
    ThenElse[R]:{ mut .then: mdf R, mut .else: mdf R, }
    BoolView[R]:{ recMdf .then: mdf R, recMdf .else: mdf R, }
    """;
  @Test void recMdfBlock() { ok("""
    package test
    A:AorB{} B:AorB{} AorB:{}
    Usage:{
      .m1: Void -> Do#.return{Void},
      .m2: mut Void -> Do#.return{mut Void},
      .m3: mut AorB -> Do#[mut AorB]
        .if{True}.return{mut A}
        .return{mut B},
      .m6: mut AorB -> Do#[mut AorB]
        .if{True}.return{Do#
          .var[mut A] a = { mut A }
          .return{ a }
          }
        .return{mut B},
      }
    """, blockSrc); }
  @Test void recMdfBlockLent() { fail("""
    In position [###]/Dummy0.fear:4:22
    [E5 invalidMdfBound]
    The type lent test.AorB[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: mut, imm.
    """, """
    package test
    A:AorB{} B:AorB{} AorB:{}
    Usage:{
      .m1: lent AorB -> Do#[lent AorB]
        .if{True}.return{Do#
          .var[lent A] a = lent ReturnStmt[lent A]{ lent A }
          .return lent ReturnStmt[lent A]{ a }
          }
        .return{lent B},
      }
    """, blockSrc); }

  @Test void xbsMappingWorks() {
    var expected = Set.of(Mdf.imm, Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", expected);
    assertEquals(Optional.of(expected), xbs.getO("X"));
  }
  @Test void xbsMappingMultipleWorks() {
    var expected = Set.of(Mdf.imm, Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", Set.of(Mdf.mut));
    xbs = xbs.add("Y", expected);
    assertEquals(Optional.of(expected), xbs.getO("Y"));
  }
  @Test void xbsMappingHistoryWorks() {
    var expected = Set.of(Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", expected);
    xbs = xbs.add("Y", Set.of(Mdf.imm, Mdf.mut));
    assertEquals(Optional.of(expected), xbs.getO("X"));
  }

  @Test void unsoundHygRecMdf() { fail("""
    In position [###]/Dummy0.fear:5:8
    [E30 badCapture]
    'readOnly x: X1/0$' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> mut Box#foo }
    """); }
  @Test void soundHygRecMdf() { ok("""
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): readOnly Box[readOnly Foo] -> readOnly Box#foo }
    """); }
  @Test void unsoundHygRecMdfCapture() { fail("""
    In position [###]/Dummy0.fear:3:10
    [E30 badCapture]
    'readOnly test.Foo[]' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    FBox[T]:{ recMdf #(x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> mut FBox[readOnly Foo]#foo }
    """); }
  @Test void soundHygRecMdfCapture() { ok("""
    package test
    Foo:{}
    FBox[T]:{ recMdf #(x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{ recMdf .get: recMdf T }
    Break:{ #(foo: readOnly Foo): readOnly Box[readOnly Foo] -> readOnly FBox[readOnly Foo]#foo }
    """); }
  @Test void unsoundHygRecMdfIndirect() { fail("""
    In position [###]/Dummy0.fear:8:14
    [E30 badCapture]
    'mdf x: X1/0$' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{
      recMdf .get: recMdf T,
      recMdf .clone(c: mut BoxClone[recMdf T]): mut Box[recMdf T] -> c.task(this.get),
      }
    BoxClone[T]:{ mut .task(x: mdf T): mut Box[mdf T] -> mut Box#x }
    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> (readOnly Box#foo).clone(mut BoxClone[readOnly Foo]) }
    """); }
  @Test void soundHygRecMdfIndirect() { ok("""
    package test
    Foo:{}
    Box:{ recMdf #[T](x: recMdf T): recMdf Box[recMdf T] -> {x} }
    Box[T]:{
      recMdf .get: recMdf T,
      recMdf .clone(c: mut BoxClone[recMdf T]): mut Box[recMdf T] -> c.task(this.get),
      }
    BoxClone[T:imm,mut]:{ mut .task(x: mdf T): mut Box[mdf T] -> mut Box#x }
//    Break:{ #(foo: readOnly Foo): mut Box[readOnly Foo] -> (readOnly Box#foo).clone(mut BoxClone[mut Foo]) }
    """); }
}
