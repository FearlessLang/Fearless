package typing;

import main.CompileError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import utils.Err;
import utils.FromContent;

public class TestMeths {
  void ok(String expected, String type, String ...code){
    var it = new Parser(Parser.dummy, type).parseFullT();
    Program p= FromContent.of(code);
    Err.strCmpFormat(expected, p.meths(it.toAstT().itOrThrow()).toString());
  }
  void fail(String expected, String type, String ...code) {
    var it = new Parser(Parser.dummy, type).parseFullT();
    Program p = FromContent.of(code);
    try {
      var res = p.meths(it.toAstT().itOrThrow());
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
  * A:B{m:Num}
  * B:{m:Num}
  * ----
  * A:B{m:Str} //err
  * B:{m:Num}
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
    uncomposableMethods:18
    Could not compose the methods in the traits a.A[] and a.B[].
    """, "a.B", """
    package a
    Str:{} Num:{}
    A:{ .foo(a:A):Str }
    B:A{ .foo(b:B):Num }
    """); }
  @Test void moreSpecific() { fail("""
    uncomposableMethods:18
    Could not compose the methods in the traits a.B[] and a.A[].
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
    [a.A[],imm.m/0()[][]:imma.Num[]abs]
    """, "a.A", """
    package a
    Num:{}
    A:B { .m: Num }
    B:{ .m: Num }
    """); }
  @Test void t3() { fail("""
    uncomposableMethods:18
    Could not compose the methods in the traits a.B[] and a.A[].
    """, "a.A", """
    package a
    Num:{} Str:{}
    A:B { .m: Str }
    B:{ .m: Num }
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
    uncomposableMethods:18
    Could not compose the methods in the traits a.C[] and a.B[].
    """, "a.A", """
    package a
    A:B,C { }
    B:{ .m: B }
    C:{ .m: C }
    """); }
  @Test void t4c() { fail("""
    uncomposableMethods:18
    Could not compose the methods in the traits a.C[] and a.B[].
    """, "a.A", """
    package a
    A:AA { .m: Num}
    Num:{}
    AA:B,C { }
    B:{ .m: B }
    C:{ .m: C }
    """); }
  @Test void t5() { ok("""
    """, "a.A", """
    package a
    A:B,C,D { .m: A }
    B:{ .m: B }
    C:{ .m: C }
    D:{ .m: D }
    """); }
  @Test void t6() { ok("""
    """, "a.A", """
    package a
    Num:{} Str:{}
    A:B,C,D { .m: B }
    B:D{ .m: D }
    C:D{ .m: D }
    D:{ .m: D }
    """); }
  @Test void t7() { fail("""
    """, "a.A", """
    package a
    Num:{} Str:{}
    A:B,C,D { .m: D }
    B:D{ .m: B }
    C:D{ .m: B }
    D:{ .m: B }
    """); }
  @Test void t8() { ok("""
    """, "a.A", """
    package a
    A:A[A],A[A,A]{ .m(a:A):A }
    A[X]:A[X,X]{ .m(b:A):A }
    A[X,Y]:{ .m(c:A):A[X,Y] }
    """); }
  @Test void t9() { ok("""
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A }
    C:{ .m(c:A): A }
    """); }
  @Test void t10() { ok("""
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A->this }
    C:{ .m(c:A): A }
    """); }
  @Test void t11() { ok("""
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A }
    C:{ .m(c:A): A->this }
    """); }
  @Test void t12() { fail("""
    """, "a.A", """
    package a
    A:B,C{}
    B:{ .m(b:A): A->this }
    C:{ .m(c:A): A->this }
    """); }
  @Test void t13() { ok("""
    """, "a.A", """
    package a
    A:B,C{}
    B:D{ .m(b:A): A }
    C:D{ .m(c:A): A }
    D:{ .m(d:A):A->this }
    """); }
  @Test void t14() { ok("""
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:{ .m: List[X] }
    C[Y]:{ .m: Y }
    List[T]:{}
    """); }
  @Test void t15() { ok("""
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[List[X]]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{.kk:Y}
    List[T]:{}
    """); }
  @Test void t16() { fail("""
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[X]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{.kk:Y}
    List[T]:{}
    """); }
  @Test void t17() { ok("""
    """, "a.A", """
    package a
    A:B[A],C[List[A]]{}
    B[X]:K[X]{.m:List[X]}
    C[Y]:K[Y]{.m:Y}
    K[Y]:{ .kk:A }
    List[T]:{}
    """); }
  @Test void t18() { fail("""
    """, "a.A", """
    package a
    A:B{.m:A}
    B:{.m[X]:A}
    """); }
  @Test void t19() { ok("""
    """, "a.A", """
    package a
    A:B{ .m[X]:A }
    B:{ .m[X]:A->this}
    """); }
  @Test void t20() { ok("""
    """, "a.A", """
    package a
    A[X]:B{ .foo:X }
    B:{.m[X]:A->this}
    """); }
  @Test void t21() { ok("""
    """, "a.A", """
    package a
    A[X]:B[X]{ .foo:X }
    B[Y]:{.m[X]:Bi[X,Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void t22() { ok("""
    """, "a.A", """
    package a
    A:B{ .m:Break[A] }
    B:{.m:Break[B]}
    Break[X]:{}
    """); }
  @Test void t23() { ok("""
    """, "a.A", """
    package a
    A:B{ .m:Break[B] } //pass, note, A/B inverted
    B:{.m:Break[A]}
    Break[X]:{}
    """); }
  @Test void t24() { ok("""
    """, "a.A", """
    package a
    A:B{ .m:Break[A] }
    B:{.m:Break[B]}
    Break[X]:{ .b:X }
    """); }
  @Test void t25() { fail("""
    """, "a.A", """
    package a
    A:B{ .m:Break[B] }
    B:{.m:Break[A]}
    Break[X]:{ .b:X }
    """); }
  @Test void t26() { ok("""
    """, "a.A", """
    package a
    A:B{ .m:Break[A] }//pass? is this the looping one?
    B:{.m:Break[B]}
    Break[X]:{ .b:Break[X] }
    """); }
  @Test void t27() { ok("""
    """, "a.A", """
    package a
    A:B{ .m:Break[B] }//pass? or is this one?
    B:{.m:Break[A]}
    Break[X]:{ .b:Break[X] }
    """); }
}
