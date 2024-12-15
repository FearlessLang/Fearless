package typing;

import failure.CompileError;
import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import program.typesystem.XBs;
import utils.Err;
import utils.FromContent;

import java.util.Arrays;

public class TestSubTyping {
  void ok(String t1, String t2, boolean res, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    Assertions.assertEquals(res,p.isSubType(XBs.empty(), ty1, ty2), String.format("t1: %s\nt2: %s", ty1, ty2));
  }

  void fail(String expected, String t1, String t2, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    try {
      p.isSubType(XBs.empty(), ty1, ty2);
      Assertions.fail("Expected failure");
    } catch (CompileError e) {
      Err.strCmp(expected, e.toString());
    }
  }

  @Provide Arbitrary<Mdf> syntaxMdfs() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(Mdf::isSyntaxMdf).toList());
  }
  @Property public void mdfIsCommonSupertype(@ForAll("syntaxMdfs") Mdf mdf) {
    ok(mdf+" a.A", "readH a.A", true, "package a\nA:{}");
  }
  @Property public void isoIsCommonSubtype(@ForAll("syntaxMdfs") Mdf mdf) {
    ok("iso a.A", mdf+" a.A", true, "package a\nA:{}");
  }

  @Test void reflSub() { ok("a.A","a.A",true,"package a\nA:{}"); }
  @Test void noDSub() { ok("a.A","a.B",false,"package a\nA:{} B:{}"); }
  @Test void directSub() { ok("a.A","a.B",true,"package a\nA:a.B{} B:{}"); }
  @Test void inverseDirectSub() { ok("a.B","a.A",false,"package a\nA:a.B{} B:{}"); }
  @Test void reflXSub() { ok("X","X",true,"package a\nA:a.B{} B:{}"); }

  @Test void directSubMdf() { ok("a.A","read a.A",true,"package a\n A:{}"); }
  @Test void inverseDirectSubMdf() { ok("read a.A","a.A",false,"package a\n A:{}"); }
  @Test void noSubMdf() { ok(
    "mut a.A","imm a.A",false,
    """
    package a
    A:{}
    """); }
  @Test void inverseTransitiveSub() { ok(
    "a.A","a.C",false,
    """
    package a
    A:{}
    B:A{}
    C:B{}
    """); }
  @Test void transitiveSub() { ok(
    "a.C","a.A",true,
    """
    package a
    A:{} B:A C:B
    """); }
  @Test void transitiveManyStepsSub() { ok(
    "a.E","a.A",true,
    """
    package a
    A:{} B:A C:F,B,G{} D:C E:D F:{} G:{}
    """); }
  @Test void inverseTransitiveManyStepsSub() { ok(
    "a.A","a.E",false,
    """
    package a
    A:{} B:A C:B D:C E:D
    """); }

  // This would fail, but now works because adapt will assume true for a sub-typing relationship if it's part of the
  // relationship it is currently evaluating.
  @Disabled // Adapt subtyping is disabled
  @Test void loopingAdapt() { ok("a.Break[A]", "a.Break[B]", true, """
    package a
    A:B{ .m: Break[A] }
    B:{ .m: Break[B] }
    Break[X]:{ .b: Break[X] }
//     Break[X]:{ .self: Break[B], .b: Break[A] -> this.self.b } // loop
    """); }

  @Disabled // Adapt subtyping is disabled, and this test is questionable-- is this sound??
  @Test void loopingAdapt2() { ok("a.Break[B]", "a.Break[A]", true, """
    package a
    A:B{ .m: Break[A], .me: A, }
    B:{ .m: Break[B], .me: C, }
    C:{}
    Break[X]:{ .b: Break[X] }
//     Break[X]:{ .self: Break[B], .b: Break[A] -> this.self.b } // loop
    """); }

  final String pointEx = """
    package a
    List[T]:{
      read .get: read/imm T
    }
    SortedList[T]:List[T]
    Int:{}
    Point:{ .x: Int, .y: Int }
    ColouredPoint:Point{ .colour: Int }
    """;
  @Test void sortedListOfTExtendsListTOfT() { ok("a.SortedList[a.Int]","a.List[a.Int]",true,pointEx); }
  @Test void sortedListOfTExtendsListTOfTMdfFail() { ok("a.SortedList[a.Int]","a.List[mut a.Int]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfTMdf() { ok("a.SortedList[a.Int]","a.List[read a.Int]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfTMdfReflexive() { ok("a.SortedList[read a.Int]","a.List[a.Int]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfX() { ok("a.SortedList[X]","a.List[X]",true,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot1() { ok("a.SortedList[a.Int]","a.List[X]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot2() { ok("a.SortedList[X]","a.List[a.Int]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot3() { ok("a.SortedList[X]","a.List[a.List[a.Int]]",false,pointEx); }
  @Test void sortedListMixedGens() { ok("a.SortedList[a.ColouredPoint]","a.SortedList[a.Point]",false,pointEx); }
  @Test void inverseSortedListMixedGens() { ok("a.SortedList[a.Point]","a.SortedList[a.ColouredPoint]",false,pointEx); }

  final String pointEx2 = """
    package a
    List[T]:{
      read .get: T
    }
    SortedList[T]:List[T]
    Int:{}
    Point:{ .x: Int, .y: Int }
    ColouredPoint:Point{ .colour: Int }
    """;
  @Test void sortedListOfTExtendsListTOfT2() { ok("a.SortedList[a.Int]","a.List[a.Int]",true,pointEx2); }
  @Test void sortedListOfTExtendsListTOfTMdfFail2() { ok("a.SortedList[a.Int]","a.List[mut a.Int]",false,pointEx2); }
  @Test void sortedListOfTExtendsListTOfTMdf2() { ok("a.SortedList[a.Int]","a.List[read a.Int]",false,pointEx2); }
  @Test void sortedListOfTExtendsListTOfTMdfReflexive2() { ok("a.SortedList[read a.Int]","a.List[a.Int]",false,pointEx2); }
  @Test void sortedListOfTExtendsListTOfX2() { ok("a.SortedList[X]","a.List[X]",true,pointEx2); }
  @Test void sortedListOfTExtendsListTOfNot12() { ok("a.SortedList[a.Int]","a.List[X]",false,pointEx2); }
  @Test void sortedListOfTExtendsListTOfNot22() { ok("a.SortedList[X]","a.List[a.Int]",false,pointEx2); }
  @Test void sortedListOfTExtendsListTOfNot32() { ok("a.SortedList[X]","a.List[a.List[a.Int]]",false,pointEx2); }
  @Test void sortedListMixedGens2() { ok("a.SortedList[a.ColouredPoint]","a.SortedList[a.Point]",false,pointEx2); }
  @Test void inverseSortedListMixedGens2() { ok("a.SortedList[a.Point]","a.SortedList[a.ColouredPoint]",false,pointEx2); }

  @Test void mdfXNotSubtypeOfIT() {ok("X", "read a.Foo", false, """
    package a
    Foo: {}
    """);}
}
