package program.typesystem;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?
  void ok(String... content){ ok(false, content); }
  void ok(boolean loadBase, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
  }
  void fail(String expectedErr, String... content){ fail(false, expectedErr, content); }
  void fail(boolean loadBase, String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    try {
      inferred.typeCheck();
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram(){ ok("""
    package test
    """); }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void baseLib(){ ok(Base.baseLib); }

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

  @Test void numbers1(){ ok( true, """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """); }

  @Test void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, """
    package base
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{}
    """, Base.load("nums.fear")); }

  @Test void numbersSubTyping1(){ ok(true, """
    package test
    alias base.Int as Int,
    A:{ .m(a: 42): Int -> a }
    """); }
  @Test void numbersSubTyping2(){ fail(true, """
    In position [###]/Dummy0.fear:3:4
    [E23 methTypeError]
    Expected the method .m/1 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .m(a: Int): 42 -> a }
    """); }
  @Test void numbersSubTyping3(){ ok(true, """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """); }
  @Test void numbersSubTyping4(){ ok(true, """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Int -> this.a }
    """); }
  @Test void numbersGenericTypes1(){ ok(true, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Int]{ 42 }
    """); }
  @Test void numbersGenericTypes2(){ ok(true, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Int]{ .count -> 56, .sum -> 3001 }
    """); }
  @Test void numbersGenericTypes2a(){ fail(true, """
    In position [###]/Dummy0.fear:4:31
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:43:2) 43[], .float/0
    ([###]/Dummy4.fear:43:2) 42[], .float/0
    """, """
    package test
    alias base.Int as Int,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """); }
  @Test void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Test void numbersGenericTypes2aNoMagic(){ fail("""
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
  @Test void numbersSubTyping5a(){ fail(true, """
    In position [###]/Dummy0.fear:6:5
    [E23 methTypeError]
    Expected the method .b/0 to return imm 42[], got imm base.Int[].
    """, """
    package test
    alias base.Int as Int,
    A:{ .a: Int }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: 42 -> this.a }
    """); }
  @Test void twoInts(){ ok(true, """
    package test
    alias base.Int as Int,
    A:{ .m(a: 56, b: 12): Int -> b+a }
    """); }

  @Test void noRecMdfWeakening() { fail("""
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
  @Test void boolIntRet() { ok(true, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True)?{.then->42,.else->0}
    }
    """); }
  @Test void boolSameRet() { ok(true, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Foo:{}
    Test:Main[Foo]{
      _->False.or(True)?{.then->Foo,.else->Foo}
    }
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

  @Test void numImpls1() { ok(true, """
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """);}

  @Test void numImpls2() { ok(true, """
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """);}

  @Test void numImpls3() { fail(true, """
    In position [###]/Dummy0.fear:5:25
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:63:2) 5[], <=/1
    ([###]/Dummy4.fear:28:2) base.MathOps[imm base.Float[]], <=/1
    """, """
    package test
    alias base.Int as Int, alias base.Float as Float,
    Bar:{
      .nm(n: Float): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

  @Test void numImpl4() { fail(true, """
    In position [###]/Dummy0.fear:5:25
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy4.fear:43:2) 5[], .float/0
    ([###]/Dummy4.fear:43:2) 6[], .float/0
    """, """
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: 6): Int -> 12,
      .check: Int -> this.nm(5)
      }
    """);}

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
  @Test void noCallMutFromRead() { fail("""
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
  @Test void noCallMutFromRecMdfImm() { fail("""
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
  @Test void noCallMutFromRecMdfRead() { fail("""
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
  @Test void CallMutFromRecMdfLent() { ok("""
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
  @Test void captureRecMdfAsMut() { ok("""
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
  @Test void inferCaptureRecMdfAsMut() { ok("""
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

  @Test void incompatibleGens() { fail("""
    In position [###]/Dummy1.fear:7:12
    [E34 bothTExpectedGens]
    Type error: the generic type lent C cannot be a super-type of any concrete type, like Fear71$/0.
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
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
    """, Base.load("lang.fear"), Base.load("strings.fear"), Base.load("nums.fear"), Base.load("bools.fear")); }
  @Test void incompatibleITs() { fail("""
    In position [###]/Dummy1.fear:7:8
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    cont #/2[]([c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }]), this])
    were valid:
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[mut base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], iso C, iso base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], mut C, lent base.caps.System[mdf R]], t=mdf R]
    (mut base.caps.UseCapCont[imm C, mdf R], ?c #/1[]([[-lent-][base.caps._RootCap[], base.caps.NotTheRootCap[]]{'fear1$ }])?, lent base.caps.System[mdf R]) <: TsT[ts=[iso base.caps.UseCapCont[imm C, mdf R], lent C, mut base.caps.System[mdf R]], t=mdf R]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
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
    """, Base.load("lang.fear"), Base.load("strings.fear"), Base.load("nums.fear"), Base.load("bools.fear")); }
  @Test void incompatibleITsDeep() { fail("""
    In position [###]/Dummy0.fear:5:2
    [E33 callTypeError]
    Type error: None of the following candidates for this method call:
    s .use/2[imm base.caps.IO[]]([[-imm-][base.caps.IO'[]]{'fear7$ }, [-mut-][base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]]{'fear8$ #/2([io, fear0$]): Sig[mdf=mut,gens=[],ts=[lent base.caps.IO[], lent base.caps.System[imm base.Void[]]],ret=imm base.Void[]] -> fear0$ .return/1[]([[-lent-][base.caps.LentReturnStmt[imm base.Void[]]]{'fear9$ #/0([]): Sig[mdf=lent,gens=[],ts=[],ret=imm base.Void[]] -> io .println/1[]([[-imm-]["Hello, World!"[]]{'fear10$ }])}])}])
    were valid:
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[lent base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[iso base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    (lent base.caps.System[imm base.Void[]], imm base.caps.IO'[], mut base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]) <: TsT[ts=[mut base.caps.System[imm base.Void[]], imm base.caps.CapFactory[lent base.caps.NotTheRootCap[], imm base.caps.IO[]], iso base.caps.UseCapCont[imm base.caps.IO[], imm base.Void[]]], t=imm base.Void[]]
    """, """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ s -> s
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
    """, Base.load("lang.fear"), Base.load("strings.fear"), Base.load("nums.fear"), Base.load("bools.fear")); }

  @Test void recMdfInSubHyg() { ok(false, """
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ read .argh: recMdf X }
    C:{ #: mut C -> A[C].foo({}) }
    """); }

  @Test void breakingEarlyFancyRename() { fail(false, """
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

  @Test void recMdfCallsRecMdf() { ok(false, """
    package test
    A:{
      read .inner: recMdf  A -> this,
      read .outer: recMdf A -> this.inner,
      }
    """); }
  @Test void recMdfCallsRecMdfa() { ok(false, """
    package test
    A:{
      read .inner: recMdf A -> this
      }
    """); }
  @Test void noCaptureReadInMut() { fail(false, """
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
  @Test void noCaptureMdfInMut() { fail(false, """
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
  @Test void noCaptureMdfInMut2() { fail(false, """
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

  @Test void noCaptureMdfInMut3() { fail(false, """
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

  @Test void recMdfFluent() { ok(false, """
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
