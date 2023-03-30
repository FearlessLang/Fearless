package program.typesystem;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import program.Program;
import utils.CoreGenerator;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?

  @Property @Domain(CoreGenerator.class) void notSureYet(@ForAll ast.Program p){
    System.out.println(p);
  }

  @Example void emptyProgram(){ ok("""
    package test
    """); }

  @Example void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Example void simpleTypeError(){ fail("""
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

  @Example void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }
  @Example void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Example void numbersGenericTypes2aNoMagic(){ fail("""
    In position [###]/Dummy0.fear:6:43
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:13) test.FortyThree[], .get/0
    ([###]/Dummy0.fear:3:11) test.FortyTwo[], .get/0
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }

  @Example void noRecMdfWeakening() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    [###]/Dummy0.fear:3:10) test.List[mut test.Person[]], .get/0
    ([###]/Dummy0.fear:4:26) test.Family2[], .get/0
    """, """
    package test
    Person:{}
    List[X]:{ read .get(): recMdf X }
    Family2:List[mut Person]{ read .get(): mut Person }
    """); }

  @Example void ref1() { fail("""
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

  @Example void simpleThis() { ok("""
    package test
    A:{
      .a: C -> B{ this.c }.c,
      .c: C -> {}
      }
    B:{ .c: C }
    C:{ }
    """); }

  @Example void lambdaCapturesThis() { ok("""
    package test
    Let:{ #[V,R](l: mut Let[V, R]): R -> l.in(l.var) }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }
    Void:{}
    Ref[X]:{
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Let#mut Let[X,Void]{ .var -> this.swap(x), .in(_) -> Void },
      }
    """); }

  @Example void callMutFromLent() { ok("""
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
  @Example void callMutFromIso() { ok("""
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
  @Example void noCallMutFromImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (imm test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
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
  @Example void noCallMutFromRead() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (read test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (read test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
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
  @Example void noCallMutFromRecMdfImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (imm test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (imm test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
    """, """
    package test
    A:{
      read .b: recMdf B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void noCallMutFromRecMdfRead() { fail("""
    In position [###]/Dummy0.fear:4:31
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    this .b/0[]([]) .foo/0[]([])
    were valid:
    (recMdf test.B[]) <: TsT[ts=[mut test.B[]], t=mut test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=iso test.B[]]
    (recMdf test.B[]) <: TsT[ts=[iso test.B[]], t=mut test.B[]]
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
  @Example void CallMutFromRecMdfLent() { ok("""
    package test
    A:{
      lent .b: recMdf B -> {},
      lent .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Example void CallMutFromRecMdfMut() { ok("""
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
  @Example void recMdfToMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: mut B -> this.b(this),
      }
    B:{}
    """); }
  @Example void captureRecMdfAsMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: read B -> LetMut#[mut B, read B]{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }
  // TODO: the recMdf here needs to become mut in inference or something
  @Example void inferCaptureRecMdfAsMut() { ok("""
    package test
    A:{
      read .b(a: recMdf A): recMdf B -> {},
      mut .break: read B -> LetMut#{ .var -> this.b(this), .in(b) -> b.foo },
      }
    B:{
      read .foo(): read B
      }
    Void:{}
    LetMut:{ #[V,R](l:mut LetMut[mdf V, mdf R]): mdf R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var: mdf V, mut .in(v: mdf V): mdf R }
    """); }

  @Example void recMdfInSubHyg() { ok("""
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    C:{ #: mut C -> A[C].foo({}) }
    """); }

  @Example void breakingEarlyFancyRename() { fail("""
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

  @Example void recMdfCallsRecMdf() { ok("""
    package test
    A:{
      read .inner: recMdf  A -> this,
      read .outer: recMdf A -> this.inner,
      }
    """); }
  @Example void recMdfCallsRecMdfa() { ok("""
    package test
    A:{
      read .inner: recMdf A -> this
      }
    """); }
  @Example void noCaptureReadInMut() { fail("""
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
  @Example void noCaptureMdfInMut() { fail("""
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
  @Example void noCaptureMdfInMut2() { fail("""
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

  @Example void noCaptureMdfInMut3() { fail("""
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

  @Example void recMdfFluent() { ok("""
    package test
    Let:{
      read #[V,R](l: recMdf Let[mdf V, mdf R]): recMdf Let[mdf V, mdf R],
      read .run[V,R](l: recMdf LetMut[mdf V, mdf R]): mdf R -> l.in(l.var)
      }
    Let[V,R]:{ recMdf .var: mdf V, recMdf .in(v: mdf V): mdf R }
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
}
