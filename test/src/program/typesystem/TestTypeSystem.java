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
  // TODO: .m: mut Box[X] must return lent Box[readOnly Person] if X becomes readOnly X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?

  @Test void emptyProgram(){ ok("""
    package test
    """); }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void undefinedGens() { fail("""
    In position [###]/Dummy0.fear:2:4
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A:{ .foo(x: X): X -> x }
    """); }

  @Test void simpleTypeError(){ fail("""
    In position [###]/Dummy0.fear:4:15
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear1$ } .m/0[]([]), no candidates for .m/0 returned the expected type imm test.B[]. The candidates were:
    (imm test.A[]): imm test.A[]
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
    In position [###]/Dummy0.fear:6:43
    [E54 lambdaTypeError]
    Expected the lambda here to implement imm test.FortyTwo[].
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Test void mdfSubTypingFailure(){ fail("""
    In position [###]/Dummy0.fear:6:46
    [E53 xTypeError]
    Expected 'n' to be imm test.FortyTwo[], got mut test.FortyTwo[].
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ mut .count: N, mut .sum(n: mut FortyTwo): N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum(n) -> n }
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
    In position [###]/Dummy0.fear:4:34
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.B[]") for this method call:
    this .b/0[]([])
    were valid:
    (readOnly test.A[]) <= (imm test.A[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:30
        [E53 xTypeError]
        Expected 'this' to be imm test.A[], got readOnly test.A[].
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
    this .b/1[]([[-mut-][test.A[]]{'fear[###]$ }])
    were valid:
    (imm test.A[], mut test.A[]) <= (imm test.A[], iso test.A[]): iso test.B[]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:4:28
        [E54 lambdaTypeError]
        Expected the lambda here to implement iso test.A[].
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
    [E36 undefinedMethod]
    .foo/0 does not exist in imm test.B[].
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
  @Test void noCallMutFromImmFailLate() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    .foo/0 does not exist in imm test.B[]. The following methods exist on that type: mut .foo/0, mut .ret/0
    """, """
    package test
    A:{
      .b: imm B -> {},
      .doThing: Void -> this.b.foo[].ret[]
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromReadOnly() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    .foo/0 does not exist in readOnly test.B[].
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
  @Test void noCallMutFromRead() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    .foo/0 does not exist in read test.B[].
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
      .getBike: T -> this.getBike,
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
      .getBike: T -> this.getBike,
      }
    """); }
  @Test void bicycle4() { ok("""
    package test
    A[T]:{
      readOnly .wheel1(b: readOnly Bicycle[T]): readOnly Wheel[readOnly T] -> b.wheel,
      readOnly .wheel2(b: readOnly Bicycle[mut T]): readOnly Wheel[readOnly T] -> b.wheel,
      readOnly .wheel3(b: readOnly Bicycle[imm T]): readOnly Wheel[imm T] -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf Wheel[recMdf T] -> {},
      }
    Voodo:{}
    Wheel[T]:{
      .getBike: T -> this.getBike,
      }
    """); }
  @Test void bicycle5() { ok("""
    package test
    A[T]:{
      readOnly .wheel1(b: readOnly Bicycle[T]): readOnly T -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      readOnly .loop[T]: T -> this.loop,
      }
    Wheel[T]:{
      .getBike: T -> this.getBike,
      }
    """); }
  @Test void bicycle6() { fail("""
    In position [###]/Dummy0.fear:3:55
    [E32 noCandidateMeths]
    When attempting to type check the method call: b .wheel/0[]([]), no candidates for .wheel/0 returned the expected type recMdf T. The candidates were:
    (readOnly test.Bicycle[T]): readOnly T
    (imm test.Bicycle[T]): imm T
    """, """
    package test
    A[T]:{
      recMdf .wheel1(b: readOnly Bicycle[T]): recMdf T -> b.wheel,
      }
    Bicycle[T]:{
      recMdf .wheel: recMdf T -> Voodo.loop,
      }
    Voodo:{
      readOnly .loop[T]: T -> this.loop,
      }
    Wheel[T]:{
      .getBike: T -> this.getBike,
      }
      
    """); }
  @Test void box() { ok("""
    package test
    Box:{ recMdf #[R](r: recMdf R): recMdf Box[R] -> { r } }
    Box[R]:{ recMdf #: recMdf R }
    """); }
  @Test void boxMutBounds() { ok("""
    package test
    Box:{#[R:imm,mut](r: R): mut Box[R] -> {r}}
    Box[R]:{mut #: R}
    """); }
  @Test void boxInnerGens() { ok("""
    package test
    Box:{ recMdf #[R](r: recMdf R): recMdf Box[R] -> { r } }
    Box[R]:{ recMdf #: recMdf R }
    BoxF[R]:{ recMdf #: mut F[recMdf R] }
    F[A,B]:{ readOnly #(a: A): B }
    F[A]:{readOnly #:A}
    Usage[A,B]:{ #(b: mut Box[mut F[readOnly A, readOnly B]]): mut F[readOnly A, readOnly B] -> b# }
    Usage2[A,B]:{ readOnly #(b: mut Box[mut F[readOnly A, readOnly B]]): mut F[readOnly A, readOnly B] -> b# }
    // This is okay because adapterOk works in ways that are dark and mysterious
    Usage3[A,B]:{ recMdf #(b: recMdf Box[recMdf F[readOnly A, readOnly B]]): recMdf F[recMdf A, readOnly B] -> b# }
    Usage4[A,B]:{ readOnly #(b: mut Box[mut F[A]]): mut F[A] -> b# }
    Usage5[A,B]:{ readOnly #(b: mut Box[mut F[A]]): mut F[A] -> b# }
    Usage6[A,B]:{ readOnly #(b: mut BoxF[A]): mut F[A] -> b# }
    """, """
    package base
    NoMutHyg[X]:{}
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
    LetMut:{ #[V:read,imm,mut,lent,readOnly,R:read,imm,mut,lent,readOnly](l:mut LetMut[V, R]): R -> l.in(l.var) }
    LetMut[V:read,imm,mut,lent,readOnly,R:read,imm,mut,lent,readOnly]:base.NoMutHyg[V]{ mut .var: V, mut .in(v: V): R }
    """); }

  @Test void breakingEarlyFancyRename() { fail("""
    In position [###]/Dummy0.fear:3:52
    [E53 xTypeError]
    Expected 'b' to be recMdf test.A[], got readOnly test.A[].
    """, """
    package test
    A:{
      recMdf .foo(a:recMdf A, b:readOnly A):recMdf A -> b
      }
    B:{
      .foo(mutR: mut A, readR: readOnly A): mut A -> mutR.foo(mutR, readR)
      }
    """); }

  @Test void noCaptureReadInMut() { fail("""
    In position [###]/Dummy0.fear:4:30
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
    In position [###]/Dummy0.fear:4:33
    [E30 badCapture]
    'readOnly this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: X }
    B:{
      readOnly .break: mut A[B] -> { this }
      }
    """); }
  @Test void noCaptureMdfInMut2() { fail("""
    In position [###]/Dummy0.fear:4:42
    [E30 badCapture]
    'readOnly this' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X:imm,mut,read,readOnly]:{ mut .prison: X }
    B:{
      readOnly .break: mut A[readOnly B] -> { this } // this capture was being allowed because this:B was adapted with readOnly to become this:recB (which can be captured by mut)
      }
    """); }
  @Test void noCaptureMdfInMut4() { fail("""
    In position [###]/Dummy0.fear:4:30
    [E30 badCapture]
    'x' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X:mut,imm,read,lent,readOnly]:{ mut .prison: X }
    B[X:mut,imm,read,lent,readOnly]:{
      .break(x: X): mut A[X] -> { x }
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

  @Test void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E32 noCandidateMeths]
    When attempting to type check the method call: [-imm-][test.A[]]{'fear[###]$ } .m/1[]([[-imm-][test.B[]]{'fear[###]$ }]) .absMeth/0[]([]), no candidates for .absMeth/0 returned the expected type lent test.B[]. The candidates were:
    (lent test.L[imm test.B[]]): imm test.B[]
    (iso test.L[imm test.B[]]): imm test.B[]
    """, """
    package test
    B:{}
    L[X]:{ recMdf .absMeth: recMdf X }
    A:{ readOnly .m(par: imm B) : lent L[imm B] -> lent L[imm B]{.absMeth->par} }
    C:{ #: lent B -> (A.m(B)).absMeth }
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
    In position [###]/Dummy0.fear:4:79
    [E53 xTypeError]
    Expected 'par' to be lent T, got imm T.
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: lent X }
    A:{ readOnly .m[T](par: readOnly T) : lent L[imm T] -> lent L[imm T]{.absMeth->par} }
    """); }

  @Test void noMdfParamAsLent() { fail("""
    In position [###]/Dummy0.fear:4:96
    [E53 xTypeError]
    Expected 'par' to be lent T, got readOnly T.
    """, """
    package test
    B:{}
    L[X:read,mut,readOnly,imm,lent]:{ mut .absMeth: lent X }
    A:{ readOnly .m[T:read,mut,readOnly,imm,lent](par: T): lent L[mut T] -> lent L[mut T]{.absMeth->par} }
    C:{ #: lent L[mut B] -> A{}.m[readOnly B](B) }
    """); }

  @Test void noMutHygRenamedGX1() { ok("""
    package test
    alias base.NoMutHyg as NoMH,
    Person:{}
    
    Foo[X]:NoMH[X]{ rec.stuff: recX }
    FooP0[Y]:Foo[Y]{}
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
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{} Abort:{ ![T]: T -> this! }
    Magic:{ ![T]: T -> this! }
    """, Base.load("nums.fear")); }

  @Disabled // TODO: Figure out better way to load the rest of the base libs
  @Test void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:2
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    s .use/2[imm base.caps.IO[]]([[-imm-][base.caps.IO'[]]{'fear[###]$ }, [-mut-][base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]]{'fear[###]$ #/2([io, fear1$]): Sig[mdf=mut,gens=[],ts=[lent base.caps.IO[], lent base.caps.System[imm base.Void[]]],ret=imm base.Void[]] -> fear1$ .return/1[]([[-lent-][base.caps.LentReturnStmt[imm base.Void[]]]{'fear[###]$ #/0([]): Sig[mdf=lent,gens=[],ts=[],ret=imm base.Void[]] -> io .println/1[]([[-imm-]["Hello, World!"[]]{'fear[###]$ }])}])}])
    were valid:
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <= (lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <= (lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <= (iso base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <= (mut base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], lent base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]): imm base.Void[]
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
    LentReturnStmt[R]:{ lent #: R }
    System[R]:{
      lent .use[C](c: CapFactory[lent NotTheRootCap, lent C], cont: mut UseCapCont[C, R]): R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[R]): R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[R]): R }
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
    LentReturnStmt[R]:{ lent #: R }
    System[R]:{
      lent .use[C](c: CapFactory[lent C, lent C], cont: mut UseCapCont[C, R]): R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[R]): R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[R]): R }
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
    (mut base.caps.UseCapCont[imm C, R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[R]) <= (mut base.caps.UseCapCont[imm C, R], lent C, lent base.caps.System[R]): R
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy1.fear:7:11
        [E33 callTypeError]
        Type error: None of the following candidates for this method call:
        c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])
        were valid:
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <= (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps._RootCap[]): lent C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <= (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], iso base.caps._RootCap[]): iso C
        (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], lent base.caps.NotTheRootCap[]) <= (imm base.caps.CapFactory[lent base.caps._RootCap[], lent C], mut base.caps._RootCap[]): lent C
        
    (mut base.caps.UseCapCont[imm C, R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[R]) <= (iso base.caps.UseCapCont[imm C, R], lent C, lent base.caps.System[R]): R
    (mut base.caps.UseCapCont[imm C, R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[R]) <= (iso base.caps.UseCapCont[imm C, R], iso C, iso base.caps.System[R]): R
    (mut base.caps.UseCapCont[imm C, R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[R]) <= (iso base.caps.UseCapCont[imm C, R], mut C, lent base.caps.System[R]): R
    (mut base.caps.UseCapCont[imm C, R], ?c #/1[]([[-lent-][base.caps.NotTheRootCap[]]{'fear[###]$ }])?, lent base.caps.System[R]) <= (iso base.caps.UseCapCont[imm C, R], lent C, mut base.caps.System[R]): R
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
    LentReturnStmt[R]:{ lent #: R }
    System[R]:{
      lent .use[C](c: CapFactory[lent _RootCap, lent C], cont: mut UseCapCont[C, R]): R ->
        cont#(c#NotTheRootCap, this), // should fail here because NotTheRootCap is not a sub-type of C
      lent .return(ret: lent LentReturnStmt[R]): R -> ret#
      }
        
    NotTheRootCap:{}
    _RootCap:IO{ .println(msg) -> this.println(msg), }
    UseCapCont[C, R]:{ mut #(cap: lent C, self: lent System[R]): R }
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

  // TODO: test lent to mut promotion
  @Test void minimalMatcher() { ok("""
    package test
    //we can have lent matcher with lent cases that can capture all (but mut as lent), and can only return mut as lent :-(
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      lent .get: R
      }
    PreR:{
      readOnly .get: readOnly MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      readOnly .match[R](m: lent Matcher[R]): R -> m.get
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
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      mut .get: R
      }
    PreR:{
      mut .get: mut MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      readOnly .match[R](m: mut Matcher[R]): R -> m.get
      }
    Usage:{
      .direct(preR: mut PreR): mut MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: mut MyRes): mut MyRes -> MatcherContainer.match{ r }
      }
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
      iso .m: iso A[X] -> this,
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
    In [###]/Dummy0.fear:6:39
    [E53 xTypeError]
    Expected 'a' to be mut test.A[], got lent test.A[].
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
    In position [###]/Dummy0.fear:6:39
    [E53 xTypeError]
    Expected 'aa' to be mut test.A[], got lent test.A[].
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
    In position [###]/Dummy0.fear:6:27
    [E53 xTypeError]
    Expected 'a' to be mut test.A[], got lent test.A[].
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
    Abort:{ ![R]: R -> this! }
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
      .if[R](f: mut ThenElse[R]): R,
      ?[R](f: mut ThenElse[R]): R -> this.if(f),
      .look[R](f: readOnly BoolView[R]): R,
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
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    BoolView[R]:{ rec.then: R, rec.else: R, }
    """;
  @Test void recMdfGetForLists1() { ok("""
    package test
    LList[E]:{
      rec.get(i: Nat): recMdf E -> Abort!,
      rec.push(e: recE): recMdf LList[E] -> { .get(i) -> e } // passes
      }
    """, recMdfGetForListsHelpers); }
  @Test void recMdfGetForLists2() { ok("""
    package test
    ThisBox:{ rec#: recFoo }
    Foo:{
      rec.self: recFoo -> this, // passes
      rec.test: recFoo -> recThisBox{ this }# // fails
      }
    """); }
  @Test void recMdfGetForLists3() { ok("""
    package test
    LList[E]:{
      rec.get(i: Nat): recE -> Abort!,
      rec.push(e: recE): recLList[recE] -> { .get(i) -> i.isZero.look(recListGet#(e, this)) },
      }
    ListGet:{ rec#[E](e: recE, t: recLList[E]): recBoolView[recE] -> { .then -> e, .else -> t.get(Z) } }
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
      #[E:imm](h: E, t: List[E]): List[E] -> { .match(m) -> m.elem(h, t) },
      }
    List[E:imm]:{
      .match[R:imm](m: ListMatch[E, R]): R -> m.empty,
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
    ListMatch[E:imm,R:imm]:{ .elem(head: E, tail: List[E]): R, .empty: R }
    
    Opt:{ #[T:imm](x: T): Opt[T] -> { .match(m) -> m.some(x) } }
    Opt[T:imm]:{
      .match[R:imm](m: OptMatch[T, R]): R -> m.none,
      .map[R:imm](f: OptMap[T,R]): Opt[R] -> this.match(f),
      .do(f: OptDo[T]): Opt[T] -> this.match(f),
      .flatMap[R:imm](f: OptFlatMap[T, R]): Opt[R] ->this.match(f),
      ||(alt: T): T -> this.match{ .some(x) -> x, .none -> alt },
      .isEmpty: Bool -> this.match{ .none -> True, .some(_) -> False },
      .isSome: Bool -> this.match{ .none -> False, .some(_) -> True },
      }
    OptMatch[T:imm,R:imm]:{ .some(x:T): R, .none: R }
    OptFlatMap[T:imm,R:imm]:OptMatch[T,Opt[R]]{ .none -> {} }
    OptMap[T:imm,R:imm]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opt#(this#x), .none -> {} }
    OptDo[T:imm]:OptMatch[T,Opt[T]]{
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
    Abort:{ ![R]: R -> this! } // can be optimised to just terminate (goes stuck)
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
    (lent test.Person[]) <= (mut test.Person[]): mut test.Ref[imm test.Name[]]
    (lent test.Person[]) <= (iso test.Person[]): iso test.Ref[imm test.Name[]]
    """, """
    package test
    Person:{ mut .name: mut Ref[Name] }
    Usage:{
      .mutate(p: lent Person): iso Ref[Name] -> p.name,
      }
      
    Ref[X]:{ rec.get: recX, lent .set(x: X): Void }
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
      In position [###]/Dummy1.fear:11:28
      [E18 uncomposableMethods]
      These methods could not be composed.
      conflicts:
      ([###]/Dummy1.fear:17:18) base._FakeCapture[X], .self/0[](): recMdf X
      ([###]/Dummy1.fear:11:45) base.Fear12$[X], .self/0[](): X
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
        #[X](x: X): Void -> this.with(x, Void),
        .with[X,R](_: X, res: R): R -> res,
        }
      Ref:{ #[X:imm,mut](x: X): mut Ref[X] -> this#(x) }
      Ref[X:imm,mut]:Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        mut .swap(x: X): X -> mut _FakeCapture[X]{ x }.prev,
        mut :=(x: X): Void -> Block#(this.swap(x)),
        mut .set(x: X): Void -> this := x,
        mut <-(f: mut UpdateRef[X]): X -> this.swap(f#(this*)),
        mut .update(f: mut UpdateRef[X]): X -> this <- f,
        }
      _FakeCapture[X]:{ recMdf .self: recMdf X, mut .prev: X -> Abort! }
      UpdateRef[X]:{ mut #(x: X): X }
      Abort:{ ![R]: R -> this! }
      Block: {#[P1](a: P1): Void -> Void,}
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
        #[X](x: X): Void -> this.with(x, Void),
        .with[X,R](_: X, res: R): R -> res,
        }
      Ref:{ #[X:imm,mut](x: X): mut Ref[X] -> this#(x) }
      Ref[X:imm,mut]:Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Block#(this.swap(x)),
        mut .set(x: X): Void -> this := x,
        mut <-(f: mut UpdateRef[X]): X -> this.swap(f#(this*)),
        mut .update(f: mut UpdateRef[X]): X -> this <- f,
        }
      UpdateRef[X]:{ mut #(x: X): X }
      Abort:{ ![R]: R -> this! }
      Block: {#[P1](a: P1): Void -> Void,}
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
        #[X](x: X): Void -> this.with(x, Void),
        .with[X,R](_: X, res: R): R -> res,
        }
      Ref:{ #[X](x: X): mut Ref[X] -> this#(x) }
      Ref[X]:NoMutHyg[X],Sealed{
        recMdf *: recMdf X,
        recMdf .get: recMdf X -> this*,
        lent .swap(x: X): X,
        lent :=(x: X): Void -> Block#(this.swap(x)),
        lent .set(x: X): Void -> this := x,
        lent <-(f: mut UpdateRef[X]): X -> this.swap(f#(this*)),
        lent .update(f: mut UpdateRef[X]): X -> this <- f,
        }
      UpdateRef[X]:NoMutHyg[X]{ mut #(x: X): X }
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
    A[X: mut]:{ .a1: X }
    B:A[imm B]
    """); }

  @Test void mutMdfAdapt() { fail("""
    In position [###]/Dummy0.fear:4:101
    [E30 badCapture]
    'par' cannot be captured by a mut method in a recMdf lambda.
    """, """
    package test
    B:{}
    L[X:mut,imm,read,readOnly,lent]:{ iso .absMeth: imm X }
    A:{ recMdf .m[T:mut,imm,read,readOnly,lent](par: T) : recMdf L[lent T] -> recMdf L[lent T]{.absMeth->par} }
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
    Expected the method .absMeth/0 to return T, got readOnly T.
        
    In position [###]/Dummy0.fear:7:36
    [E5 invalidMdfBound]
    The type mut test.B[] is not valid because it's modifier is not in the required bounds. The allowed modifiers are: read.
    """, """
    package test
    B:{}
    L[X]:{ lent .absMeth: X }
    A:{ recMdf .m[T: read](par: T) : lent L[T] -> lent L[T]{.absMeth->par} }
    
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
    'read par' cannot be captured by an imm method in an imm lambda.
    """, """
    package test
    B:{}
    L:{ imm .absMeth: read B }
    A:{ recMdf .m(par: read B) : imm L -> imm L{.absMeth->par} }
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

  @Test void methodOnInlineLambda() { ok("""
    package test
    Foo:{} Bar:{}
    A:{ .foo: Foo -> {} }
    B:{ .bar: Bar -> {} }
    Test2:{ #: Foo -> A,B{}.foo }
    Test1:{ #: Foo -> (B,A{}).foo }
    """); }

  @Test void breaksEvenWithCast() { ok("""
    package test
    Void:{}
    Red[T]:{
      .foo: Void,
      }
    Foo:{}
    DoIt:{
      .m1(red: mut Red[read Foo]): mut Red[Foo] -> red,
      .m2(red: mut Red[Foo]): mut Red[read Foo] -> red,
      }
    """); }

  @Test void readToReadOnlyPromotion1() { ok("""
    package test
    Test:{ #(r: readOnly Box[Foo]): readOnly Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion1ImmRet() { fail("""
    In position [###]/Dummy0.fear:2:39
    [E28 undefinedName]
    The identifier "r" is undefined or cannot be captured.
    """, """
    package test
    Test:{ #(r: readOnly Box[Foo]): Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion1ImmRetRecMdf() { ok("""
    package test
    Test:{ #(r: readOnly Box[Foo]): Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      recMdf .get: recMdf X,
      }
    """); }
  @Test void readToReadOnlyPromotion2() { ok("""
    package test
    Test:{ #(r: read Box[Foo]): readOnly Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion3() { ok("""
    package test
    Test1:{ #(r: readOnly MutyBox): readOnly Foo -> r.rb.get }
    Test2:{ #(r: read MutyBox): read Foo -> r.rb.get }
    MutyBox:{ mut .mb: mut Box[Foo], read .rb: read Box[Foo] }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }

  @Test void inferMultipleTraits1() { ok("""
    package a
    A:{ .foo: A } B:{ .bar: B -> this }
    Test:{ #: B -> A,B{'self .foo -> self } }
    """); }

  @Test void loopingAdaptOk() {ok("""
    package a
    A:B{ .m: Break[A], .me: A, }
    B:{ .m: Break[B], .me: B, }
    C:{}
    Break[X]:{ .b: Break[X] }
    """); }

  @Test void contravarianceBox() { ok("""
    package test
    UInt:{} Str:{}
    Person:{ read .name: Str, read .age: UInt, }
    Student:Person{ read .grades: Box[UInt] }
    Box[T]:{
      mut .get: T,
      read .get: read T,
      imm .get: T,
    }
    
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      .simple(rs: read Student): read Person -> rs,
      .simpleMdf(rs: imm Student): read Person -> rs,
      }
    """); }

  @Test void contravarianceBoxMatcherExtensionMethod() { ok("""
    package test
    UInt:{} Str:{}
    Person:{ read .name: Str, read .age: UInt, }
    Student:Person{ read .grades: Box[UInt] }
    BoxMatcher[T,R]:{ mut #: R }
    BoxExtension[T,R]:{ mut #(self: T): R }
    Box[T]:{
      .match[R](m: mut BoxMatcher[T, R]): R -> m#,
      .extend[R](ext: mut BoxExtension[Box[T], R]): R -> ext#this,
    }
    
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      }
    """); }

  @Test void badGenericPromotionIso() { fail("""
    In position [###]/Dummy0.fear:3:45
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.Beer[Y]") for this method call:
    [-imm-][test.Foo[]]{'fear1$ } .m/1[Y]([y])
    were valid:
    ([E28 undefinedName]) <= (imm test.Foo[], Y): mut test.Beer[Y]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:3:55
        [E28 undefinedName]
        The identifier "y" is undefined or cannot be captured.
        
    ([E28 undefinedName]) <= (imm test.Foo[], iso Y): iso test.Beer[Y]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:3:55
        [E28 undefinedName]
        The identifier "y" is undefined or cannot be captured.
    """, """
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: Y): iso Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: mut Baz): Beer[mut Baz] -> Bar.k(y),
      .ohNo(y: mut Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void badGenericPromotionImm() { fail("""
    In position [###]/Dummy0.fear:3:45
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "imm test.Beer[Y]") for this method call:
    [-imm-][test.Foo[]]{'fear1$ } .m/1[Y]([y])
    were valid:
    (imm test.Foo[], Y) <= (imm test.Foo[], iso Y): iso test.Beer[Y]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:3:55
        [E53 xTypeError]
        Expected y to be iso Y, got Y.
    """, """
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: Y): imm Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: mut Baz): Beer[mut Baz] -> Bar.k(y),
      .ohNo(y: mut Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void okGenericPromotion() { ok("""
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: iso Y): iso Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: iso Baz): Beer[mut Baz] -> Bar.k[mut Baz](y),
      .ohNo(y: iso Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """); }

  @Test void superSimple() { ok("""
    package test
    Test:{
    	.foo[Y](x: Y): Test -> Test{ .foo(hello) -> Test },
    }
    """); }

  @Test void pointColourPoint() { ok("""
    package test
    FPoint:{ #(x: Num, y: Num): Point -> { .x -> x, .y -> y } }
    Point:{
      .x : Num,
      .y: Num,
      .withX(x: Num): Point -> FPoint#(x, this.y),
      .withY(y: Num): Point -> FPoint#(this.x, y),
      }
    ColourPoint:Point{
      .colour: Colour,
      .withX(x: Num): ColourPoint -> FColourPoint#(x, this.y, this.colour),
      .withY(y: Num): ColourPoint -> FColourPoint#(this.x, y, this.colour),
      }
    FColourPoint:{ #(x: Num, y: Num, colour: Colour): ColourPoint -> {
      .x -> x, .y -> y, .colour -> colour,
      }}
    Usage:{ #(cp: ColourPoint): ColourPoint -> cp.withX(Five) }
    """, """
    package test
    Colour:{}
    Num:{}
    Five:Num{}
    """); }
  @Disabled
  @Test void pointColourPointGen() { ok("""
    package test
    FPoint:{ #(x: Num, y: Num): Point -> { 'p
      .x -> x,
      .y -> y,
      .self(s) -> s,
      .withX(x) -> FPoint#(x, p.y),
      .withY(y) -> FPoint#(p.x, y),
      } }
    Point'[Self]:{
      .self(s: Self): Point'[Self],
      .x : Num,
      .y: Num,
      .withX(x: Num): Self,
      .withY(y: Num): Self,
      .withXY(x: Num, y: Num): Self -> this.self(this.withX(x)).withY(y),
      }
    Point:Point'[Point]{}
    ColourPoint'[Self]:Point'[Self]{
      .colour: Colour,
      .withColour(c: Colour): Self,
//      .withPoint(p: p): ColourPoint -> ...
//      .mapPoint(f: F[Point,Point]): ColourPoint
      }
    ColourPoint:ColourPoint'[ColourPoint]{}
    FColourPoint:{ #(x: Num, y: Num, colour: Colour): ColourPoint -> { 'p
      .x -> x, .y -> y, .colour -> colour,
      .self(s) -> s,
      .withX(x) -> FColourPoint#(x, p.y, p.colour),
      .withY(y) -> FColourPoint#(p.x, y, p.colour),
      .withColour(c) -> FColourPoint#(p.x, p.y, c),
      }}
    Usage:{ #(cp: ColourPoint): ColourPoint -> cp.withX(Five) }
    """, """
    package test
    Colour:{}
    Num:{}
    Five:Num{}
    """); }

  @Test void pointColourPointWrapper() { ok("""
    package test
    FPoint:{ #(x: Num, y: Num): Point -> { .x -> x, .y -> y } }
    Point:{
      .x : Num,
      .y: Num,
      .withX(x: Num): Point -> FPoint#(x, this.y),
      .withY(y: Num): Point -> FPoint#(this.x, y),
      .withXY(x: Num, y: Num): Point -> FPoint#(this.x, this.y),
      }
    ColourPoint:{
      .x : Num,
      .y: Num,
      .colour: Colour,
      .withX(x: Num): ColourPoint -> FColourPoint#(x, this.y, this.colour),
      .withY(y: Num): ColourPoint -> FColourPoint#(this.x, y, this.colour),
      .point: Point -> {
        .x -> this.x,
        .y -> this.y,
        .withX(x) -> this.withX(x).point,
        .withY(y) -> this.withY(y).point,
        },
      .withPoint(p: Point): ColourPoint -> this.withX(p.x).withY(p.y),
      .withXY(x: Num, y: Num): Point -> this.point.withXY(x, y),
      .withXYCP(x: Num, y: Num): ColourPoint -> this.withPoint(this.withXY(x, y)),
      }
    FColourPoint:{ #(x: Num, y: Num, colour: Colour): ColourPoint -> {
      .x -> x, .y -> y, .colour -> colour,
      }}
    Usage:{ #(cp: ColourPoint): Point -> cp.point.withX(Five) }
    """, """
    package test
    Colour:{}
    Num:{}
    Five:Num{}
    """); }

  @Test void branchingReturnTypes() { ok("""
    package a
    Opt:{ #[T](x: T): mut Opt[T] -> { .match(m) -> m.some(x) }}
    Opt[T]:{
      mut  .match[R](m: mut OptMatch[T, R]): R -> m.empty,
      read .match[R](m: mut OptMatch[read T, R]): R -> m.empty,
      imm  .match[R](m: mut OptMatch[imm T, R]): R -> m.empty
      }
    OptMatch[T,R]:{ mut .some(x: T): R, mut .empty: R }
    N: {}
    Zero: N{}
    Test:{ .test(opt: Opt[N]): N -> opt.match{
      .some(n) -> n,
      .empty -> Zero,
      }}
    """); }

  @Test void foldAccExplicit() { ok("""
    package test
    Num: { +(other: Num): Num }
    Zero: Num{ +(other) -> other, }
    One: Num{ +(other) -> Abort! }
    List[E]: { .fold[S](acc: S, f: Fold[S, E]): S -> Abort! }
    Fold[S,T]: { #(acc: S, x: T): S }
    
    Break:{ #(l: List[Num]): Num -> l.fold[Num](Zero, Fold[Num, Num]{acc, n -> acc + n}) }
    
    Abort: { ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void foldAccInferred() { ok("""
    package test
    Num: { +(other: Num): Num }
    Zero: Num{ +(other) -> other, }
    One: Num{ +(other) -> Abort! }
    List[E]: { .fold[S](acc: S, f: Fold[S, E]): S -> Abort! }
    Fold[S,T]: { #(acc: S, x: T): S }
    
    Break:{ #(l: List[Num]): Num -> l.fold[Num](Zero, {acc, n -> acc + n}) }
    
    Abort: { ![R:readOnly,lent,read,mut,imm,iso]: R -> this! }
    """); }

  @Test void genericSimplificationSubTyping() { ok("""
    package test
    Default: {} Foo: {}
    A[X,Y]: {}
    B[X]: A[X,Default]
    Break:{ #(b: B[Foo]): A[Foo,Default] -> b }
    """); }

  @Test void noImpossibleLambda() {fail("""
    In position file:///home/nick/Programming/uni/fearless/Dummy0.fear:2:15
    [E61 lambdaImplementsGeneric]
    A lambda may not implement a generic type parameter 'X'
    """, """
    package test
    A[X]: {#: X -> {}}
    """);}
}
