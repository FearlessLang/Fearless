package typing;

import failure.CompileError;
import id.Mdf;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import program.typesystem.XBs;
import utils.Err;
import utils.FromContent;

public class TestMethsPost5a {
  void ok(String expected, String type, String ...code){
    var it = new Parser(Parser.dummy, type).parseFullT();
    var p = FromContent.of(code).inferSignatures();
    Err.strCmpFormat(expected, p.meths(XBs.empty(), Mdf.recMdf, it.toAstT().itOrThrow(), 0).toString());
  }
  void fail(String expected, String type, String ...code) {
    var it = new Parser(Parser.dummy, type).parseFullT();
    var p = FromContent.of(code).inferSignatures();
    try {
      var res = p.meths(XBs.empty(), Mdf.recMdf, it.toAstT().itOrThrow(), 0);
      Assertions.fail("Expected failure, got\n" + res);
    } catch (CompileError e) {
      Err.strCmp(expected, e.toString());
    }
  }

  @Test void noMeths() { ok("[]", "a.A", """
    package a
    A:{}
    """); }
  @Test void oneMeth() { ok("""
    [a.A[],imm .foo/1(a)[][imm a.A[]]:imm a.A[]impl]
    """, "a.A", """
    package a
    A:{ .foo(a:A):A->this }
    """); }
  /*
  * A:B {ma}
  * B:{mb}
  * --
  * A:B{m:Int}
  * B:{m:Int}
  * ----
  * A:B{m:Str} //err
  * B:{m:Int}
  *----
  * A:B,C{m:A} //ok
  * B:{m:B}
  * C:{m:C}
  *----
  * A:B,C,D{m:A} //ok
  * B:D{m:B}
  * C:D{m:C}
  * D{m:D}
   *----
   * A:B,C,D{m:B} //ok
   * B:D{m:D}
   * C:D{m:D}
   * D{m:D}
   *----
   * A:B,C,D{m:D} //err
   * B:D{m:B}
   * C:D{m:B}
   * D{m:B}
   *----
   * A:A[A],A[A,A]{m(a:A):A} //ok
   * A[X]:A[X,X]{m(b:A):A}
   * A[X,Y]{m(c:A):A[X,Y]}
   *----
   * A:B,C{} //ok
   * B:{m(b:A):A}
   * C:{m(c:A):A}
   *----
   * A:B,C{} //ok
   * B:{m(b:A):A->this}
   * C:{m(c:A):A}
   *----
   * A:B,C{} //ok
   * B:{m(b:A):A}
   * C:{m(c:A):A->this}
   *----
   * A:B,C{} //err
   * B:{m(b:A):A->this}
   * C:{m(c:A):A->this}
   *----
   * A:B,C{} //ok?? yes but as abstract
   * B:D{m(b:A):A}
   * C:D{m(c:A):A}
   * D:{m(d:A):A->this}
   *----
   * A:B[A],C[List[A]]{} //ok
   * B[X]:{m:List[X]}
   * C[Y]:{m:Y}
   * List[T]:{}
   *----
   * A:B[A],C[List[A]]{} //ok
   * B[X]:K[List[X]]{m:List[X]}
   * C[Y]:K[Y]{m:Y}
   * K[Y]:{kk:Y}
   * List[T]:{}
   *----
   * A:B[A],C[List[A]]{} //fails
   * B[X]:K[X]{m:List[X]}
   * C[Y]:K[Y]{m:Y}
   * K[Y]:{kk:Y}
   * List[T]:{}
   *----
   * A:B[A],C[List[A]]{} //ok
   * B[X]:K[X]{m:List[X]}
   * C[Y]:K[Y]{m:Y}
   * K[Y]:{ kk:A }
   * List[T]:{}
   *
   * ----test two meth with mut as meth modifier can be composed
   *
   * ----test two meths, one with mut and one with lent CAN NOT be composed
   *
   * ------
   * A:B{ m:A }//fails
   * B:{m[X]:A}
   * ------
   * A:B{ m[X]:A }//pass as abs
   * B:{m[X]:A->this}
   * ------
   * A[X]:B{ foo:X }//pass, but how do we name X in m[X] ?
   * B:{m[X]:A->this}
   * ------
   * A[X]:B[X]{ foo:X }//pass, but how do we name X in m[X] ?
   * B[Y]:{m[X]:Bi[X,Y]->this}
   * Bi[A,B]:{}
   * ------
   * A:B{ m:Break[A] }//pass
   * B:{m:Break[B]}
   * Break[X]:{}
   * ------
   * A:B{ m:Break[B] }//pass, note, A/B inverted
   * B:{m:Break[A]}
   * Break[X]:{}
   * ------
   * A:B{ m:Break[A] }//pass
   * B:{m:Break[B]}
   * Break[X]:{ b:X }
   * ------
   * A:B{ m:Break[B] }//fails
   * B:{m:Break[A]}
   * Break[X]:{ b:X }
   * ------
   * A:B{ m:Break[A] }//pass? is this the looping one?
   * B:{m:Break[B]}
   * Break[X]:{ b:Break[X] }
   * ------
   * A:B{ m:Break[B] }//pass? or is this one?
   * B:{m:Break[A]}
   * Break[X]:{ b:Break[X] }
   * */
  @Test void twoMethOneAbs() { ok("""
    [a.A[],imm.foo/1(a)[][imma.A[]]:imm a.A[]impl,a.A[],imm .bar/2(a1,a2)[][imma.A[],read a.A[]]:mut a.A[]abs]
    """, "a.A", """
    package a
    A:{ .foo(a:A):A->this, .bar(a1: A, a2: read A): mut A }
    """); }
  @Test void twoMethBothImpl() { ok("""
    [a.A[],imm .foo/1(a)[][imm a.A[]]:imma.A[]impl,a.A[],imm .bar/2(a1,a2)[][imm a.A[],read a.A[]]:mut a.A[]impl]
    """, "a.A", """
    package a
    A:{ .foo(a:A):A->this, .bar(a1: A, a2: read A): mut A->{} }
    """); }
  @Test void oneAbs() { ok("""
    [a.A[],imm .foo/1(a)[][imm a.A[]]:imm a.A[]abs]
    """, "a.A", """
    package a
    A:{ .foo(a:A):A }
    """); }

  @Test void noOverride() { ok("""
    [a.A[],imm .foo/1(a)[][imm a.A[]]:imm a.A[]abs,
     a.B[],imm .bar/1(b)[][imm a.B[]]:imm a.B[]abs]
    """, "a.B", """
    package a
    A:{ .foo(a:A):A }
    B:A{ .bar(b:B):B }
    """); }
  @Test void sameMeth() { ok("""
    [a.B[],imm.foo/1(b)[][imma.A[]]:imma.A[]abs]
    """, "a.B", """
    package a
    A:{ .foo(a:A):A }
    B:A{ .foo(b:A):A }
    """); }
  @Test void refineRt() { ok("""
    [a.B[],imm.foo/1(b)[][imma.A[]]:imm a.B[]abs]
    """, "a.B", """
    package a
    A:{ .foo(a:A):A }
    B:A{ .foo(b:A):B }
    """); }
  @Test void clashRt() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) a.A[], .foo/1[](imm a.A[]): imm a.Str[]
    ([###]/Dummy0.fear:4:5) a.B[], .foo/1[](imm a.B[]): imm a.Int[]
    """, "a.B", """
    package a
    Str:{} Int:{}
    A:{ .foo(a:A):Str }
    B:A{ .foo(b:B):Int }
    """); }
  @Test void moreSpecific() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) a.B[], .foo/1[](imm a.B[]): imm a.B[]
    ([###]/Dummy0.fear:4:4) a.C[], .foo/1[](imm a.C[]): imm a.C[]
    ([###]/Dummy0.fear:2:7) a.A[], .foo/1[](imm a.A[]): imm a.A[]
    """, "a.A", """
    package a
    A:B,C{ .foo(a:A):A }
    B:{ .foo(b:B):B }
    C:{ .foo(c:C):C }
    """); }

  /* TURBO TEST TIME-- JUST LOADS OF THEM */
  @Test void t1() { ok("""
    [a.A[],imm.m/0()[][]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B { .m: A }
    B:{ .m: B }
    """); }
  @Test void t2() { ok("""
    [a.A[],imm.m/0()[][]:imma.Int[]abs]
    """, "a.A", """
    package a
    Int:{}
    A:B { .m: Int }
    B:{ .m: Int }
    """); }
  @Test void t3() { fail("""
    In position [###]/Dummy0.fear:3:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:4) a.B[], .m/0[](): imm a.Int[]
    ([###]/Dummy0.fear:3:6) a.A[], .m/0[](): imm a.Str[]
    """, "a.A", """
    package a
    Int:{} Str:{}
    A:B { .m: Str }
    B:{ .m: Int }
    """); }
  @Test void t4a() { ok("""
    [a.A[],imm.m/0()[][]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B,C { .m: A }
    B:{ .m: B }
    C:{ .m: C }
    """); }
  @Test void t4b() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:4) a.C[], .m/0[](): imm a.C[]
    ([###]/Dummy0.fear:3:4) a.B[], .m/0[](): imm a.B[]
    """, "a.A", """
    package a
    A:B,C { }
    B:{ .m: B }
    C:{ .m: C }
    """); }
  @Test void t4c() { fail("""
    In position [###]/Dummy0.fear:4:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:6:4) a.C[], .m/0[](): imm a.C[]
    ([###]/Dummy0.fear:5:4) a.B[], .m/0[](): imm a.B[]
    """, "a.A", """
    package a
    A:AA { .m: Int}
    Int:{}
    AA:B,C { }
    B:{ .m: B }
    C:{ .m: C }
    """); }
  @Test void t5() { ok("""
    [a.A[],imm.m/0()[][]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B,C,D { .m: A }
    B:{ .m: B }
    C:{ .m: C }
    D:{ .m: D }
    """); }
  @Test void t6() { ok("""
    [a.A[],imm.m/0()[][]:imma.B[]abs]
    """, "a.A", """
    package a
    Int:{} Str:{}
    A:B,C,D { .m: B }
    B:D{ .m: D }
    C:D{ .m: D }
    D:{ .m: D }
    """); }
  @Test void t7() { fail("""
    In position [###]/Dummy0.fear:3:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:5) a.B[], .m/0[](): imm a.B[]
    ([###]/Dummy0.fear:3:10) a.A[], .m/0[](): imm a.D[]
    """, "a.A", """
    package a
    Int:{} Str:{}
    A:B,C,D { .m: D }
    B:D{ .m: B }
    C:D{ .m: B }
    D:{ .m: B }
    """); }
  @Test void t8() { ok("""
    [a.A[],imm.m/1(a)[][imma.A[]]:imma.A[]abs]
    """, "a.A", """
    package a
    A:A[A],A[A,A]{ .m(a:A):A }
    A[X]:A[X,X]{ .m(b:A):A }
    A[X,Y]:{ .m(c:A):A[X,Y] }
    """); }
  @Test void t9() { ok("""
    [a.B[],imm.m/1(b)[][imma.A[]]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A }
    C:{ .m(c:A): A }
    """); }
  @Test void t10() { ok("""
    [a.B[],imm.m/1(b)[][imma.A[]]:imma.A[]impl]
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A->this }
    C:{ .m(c:A): A }
    """); }
  @Test void t11() { ok("""
    [a.C[],imm.m/1(c)[][imma.A[]]:imma.A[]impl]
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A }
    C:{ .m(c:A): A->this }
    """); }
  @Test void t12() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:4:4) a.C[], .m/1[](imm a.A[]): imm a.A[]
    ([###]/Dummy0.fear:3:4) a.B[], .m/1[](imm a.A[]): imm a.A[]
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A->this }
    C:{ .m(c:A): A->this }
    """); }
  @Test void t13() { ok("""
    [a.B[],imm.m/1(b)[][imma.A[]]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B,C{}
    B:D{ .m(b:A): A }
    C:D{ .m(c:A): A }
    D:{ .m(d:A):A->this }
    """); }
  @Test void t14() { ok("""
    [a.B[imma.A[]],imm.m/0()[][]:imma.List[imma.A[]]abs]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:{ .m: List[X] }
    C[Y]:{ .m: Y }
    List[T]:{}
    """); }
  @Test void t15() { ok("""
    [a.B[imma.A[]],imm.m/0()[][]:imma.List[imma.A[]]abs,
    a.K[imma.List[imma.A[]]],imm.kk/0()[][]:imma.List[imma.A[]]abs]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[List[X]]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{.kk:Y}
    List[T]:{}
    """); }
  @Test void t16() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:5:6) a.K[imm a.List[imm a.A[]]], .kk/0[](): imm a.List[imm a.A[]]
    ([###]/Dummy0.fear:5:6) a.K[imm a.A[]], .kk/0[](): imm a.A[]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[X]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{.kk:Y}
    List[T]:{}
    """); }
  @Test void t17a() { ok("""
    [a.B[imm a.A[]],imm .m/0()[][]:imm a.List[imm a.A[]]abs, a.K[imm a.A[]],imm .kk/0()[][]:imm a.A[]abs]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[X]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{ .kk:A }
    List[T]:{}
    """); }
  @Test void t17b() { fail("""
    In position [###]Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:5:7) a.K[imm a.List[imm a.A[]]], .kk/0[](): imm a.List[imm a.A[]]
    ([###]/Dummy0.fear:5:7) a.K[imm a.A[]], .kk/0[](): imm a.A[]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[X]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{ .kk:Y }
    List[T]:{}
    """); }
  @Test void t17c() { ok("""
    [a.B[imma.A[]],imm.m/0()[][]:imma.List[imma.A[]]abs,
    a.K[imma.List[imma.A[]]],imm.kk/0()[][]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[List[X]]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{ .kk:A }
    List[T]:{}
    """); }
  @Test void t18() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:3) a.B[], .m/0[X0/0$](): imm a.A[]
    ([###]/Dummy0.fear:2:4) a.A[], .m/0[](): imm a.A[]
    """, "a.A", """
    package a
    A:B{.m:A}
    B:{.m[X]:A}
    """); }
  @Test void t19() { ok("""
    [a.A[],imm.m/0()[X0/0$][]:imma.A[]abs]
    """, "a.A", """
    package a
    A:B{ .m[X]:A }
    B:{ .m[X]:A->this}
    """); }
  @Test void t20a() { ok("""
    [a.A[immX],imm.foo/0()[][]:immXabs,a.B[],imm.m/0()[X0/0$][]:imma.A[imma.B[]]impl]
    """, "a.A[X]", """
    package a
    A[X]:B{ .foo:X }
    B:{.m[X]:A[B]->this}
    """); }
  @Test void t20b() { ok("""
    [a.A[imma.A[]],imm.foo/0()[][]:imma.A[]abs,a.B[],imm.m/0()[X0/0$][]:imma.A[imma.B[]]impl]
    """, "a.A[a.A]", """
    package a
    A[X]:B{ .foo:X }
    B:{.m[X]:A[B]->this}
    """); }
  @Test void t20c() { ok("""
    [a.A[imma.B[]],imm.foo/0()[][]:imma.B[]abs,a.B[],imm.m/0()[X0/0$][]:imma.A[imma.B[]]impl]
    """, "a.A[a.B]", """
    package a
    A[X]:B{ .foo:X }
    B:{.m[X]:A[B]->this}
    """); }
  @Test void t21() { ok("""
    [a.A[imm Panic],imm.foo/0()[][]:imm Panic abs,
    a.B[imm Panic],imm.m/0()[X0/0$][]:imma.Bi[immX0/0$,immPanic]impl]
    """, "a.A[Panic]", """
    package a
    A[X]:B[X]{ .foo:X }
    B[Y]:{.m[X]:Bi[X,Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void t22() { ok("""
    [a.A[],imm.m/0()[][]:imma.Break[imma.A[]]abs]
    """, "a.A", """
    package a
    A:B{ .m:Break[A] }
    B:{ .m:Break[B] }
    Break[X]:{}
    """); }
  @Test void t23() { ok("""
    [a.A[],imm.m/0()[][]:imma.Break[imma.B[]]abs]
    """, "a.A", """
    package a
    A:B{ .m:Break[B] } //pass, note, A/B inverted
    B:{.m:Break[A]}
    Break[X]:{}
    """); }
  @Test void t24() { ok("""
    [a.A[],imm.m/0()[][]:imma.Break[imma.A[]]abs]
    """, "a.A", """
    package a
    A:B{ .m:Break[A] }
    B:{.m:Break[B]}
    Break[X]:{ .b:X }
    """); }
  @Test void t25() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) a.B[], .m/0[](): imm a.Break[imm a.A[]]
    ([###]/Dummy0.fear:2:5) a.A[], .m/0[](): imm a.Break[imm a.B[]]
    """, "a.A", """
    package a
    A:B{ .m:Break[B] } // fails because this is a less specific override than B (cannot loosen)
    B:{ .m:Break[A] }
    Break[X]:{ .b:X }
    """); }

  @Test void loopingSupTypes1() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) a.B[], .m/0[](): imm a.Break[imm a.B[]]
    ([###]/Dummy0.fear:2:5) a.A[], .m/0[](): imm a.Break[imm a.A[]]
    """, "a.A", """
    package a
    A:B{ .m: Break[A] }//pass? is this the looping one?
    B:{ .m: Break[B] }
    Break[X]:{ .b: Break[X] }
    """); }
  @Test void loopingSupTypes2() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) a.B[], .m/0[](): imm a.Break[imm a.A[]]
    ([###]/Dummy0.fear:2:5) a.A[], .m/0[](): imm a.Break[imm a.B[]]
    """, "a.A", """
    package a
    A:B{ .m:Break[B] }//pass? or is this one?
    B:{ .m:Break[A] }
    Break[X]:{ .b:Break[X] }
    """); }

  @Test void methGens() { ok("""
    [base.A[],imm.m2/1(k)[X0/0$][immX0/0$]:imm base.Void[]abs,
    base.A[],imm.m1/1(x)[X0/0$][immX0/0$]:imm base.Void[]impl]
    """, "base.A", """
    package base
    A:{
      .m1[T](x:T):Void->this.m2(x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }

  static String boolPkg = """
  package bools
  Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
  Sealed:{}
  True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
  False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
  Fresh1:False,Bool{}
  Fresh2:Bool,False{}
  ThenElse[R]:{ mut .then: R, mut .else: R, }
  """;
  @Test void bool1() { ok("""
    [bools.Bool[],imm.not/0()[][]:immbools.Bool[]abs,bools.Bool[],imm?/1(f)[X0/0$][mutbools.ThenElse[immX0/0$]]:immX0/0$abs,bools.Bool[],imm.or/1(b)[][immbools.Bool[]]:immbools.Bool[]abs,bools.Bool[],imm.and/1(b)[][immbools.Bool[]]:immbools.Bool[]abs]
    """, "bools.Bool", boolPkg); }
  @Test void bool2() { ok("""
    [bools.True[],imm.not/0()[][]:immbools.Bool[]impl,bools.True[],imm?/1(f)[X0/0$][mutbools.ThenElse[immX0/0$]]:immX0/0$impl,bools.True[],imm.or/1(b)[][immbools.Bool[]]:immbools.Bool[]impl,bools.True[],imm.and/1(b)[][immbools.Bool[]]:immbools.Bool[]impl]
    """, "bools.True", boolPkg); }
  @Test void bool3() { ok("""
    [bools.False[],imm.not/0()[][]:immbools.Bool[]impl,bools.False[],imm?/1(f)[X0/0$][mutbools.ThenElse[immX0/0$]]:immX0/0$impl,bools.False[],imm.or/1(b)[][immbools.Bool[]]:immbools.Bool[]impl,bools.False[],imm.and/1(b)[][immbools.Bool[]]:immbools.Bool[]impl]
    """, "bools.False", boolPkg); }
  @Test void bool4() { ok("""
    [bools.False[],imm.not/0()[][]:immbools.Bool[]impl,bools.False[],imm?/1(f)[X0/0$][mutbools.ThenElse[immX0/0$]]:immX0/0$impl,bools.False[],imm.or/1(b)[][immbools.Bool[]]:immbools.Bool[]impl,bools.False[],imm.and/1(b)[][immbools.Bool[]]:immbools.Bool[]impl]
    """, "bools.Fresh1", boolPkg); }
  @Test void bool5() { ok("""
    [bools.False[],imm.not/0()[][]:immbools.Bool[]impl,bools.False[],imm?/1(f)[X0/0$][mutbools.ThenElse[immX0/0$]]:immX0/0$impl,bools.False[],imm.or/1(b)[][immbools.Bool[]]:immbools.Bool[]impl,bools.False[],imm.and/1(b)[][immbools.Bool[]]:immbools.Bool[]impl]
    """, "bools.Fresh2", boolPkg); }
}
