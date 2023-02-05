package typing;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import utils.Base;
import utils.Err;
import utils.FromContent;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSubTyping {
  void ok(String t1, String t2, boolean res, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    Assertions.assertEquals(res,p.isSubType(ty1, ty2), String.format("t1: %s\nt2: %s", ty1, ty2));
  }

  void fail(String expected, String t1, String t2, String ...code){
    var ty1 = new Parser(Parser.dummy, t1).parseFullT();
    var ty2 = new Parser(Parser.dummy, t2).parseFullT();
    Program p = FromContent.of(code);
    try {
      p.isSubType(ty1, ty2);
      Assertions.fail("Expected failure");
    } catch (CompileError e) {
      Err.strCmp(expected, e.toString());
    }
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
  @Test void loopingAdapt() { fail("""
    [E25 circularSubType]
    There is a cyclical sub-typing relationship between imm a.Break[imm A] and imm a.Break[imm B].
    """, "a.Break[A]", "a.Break[B]", """
    package a
    A:B{ .m: Break[A] }
    B:{ .m: Break[B] }
    Break[X]:{ .b: Break[X] }
    // Break[A]:{ .self: Break[B], .b: Break[A] -> this.self.b } // loop
    """); }

  final String pointEx = """
    package a
    List[T]:{
      read .get: recMdf T
    }
    SortedList[T]:List[mdf T]
    Num:{}
    Point:{ .x: Num, .y: Num }
    ColouredPoint:Point{ .colour: Num }
    """;
  @Test void sortedListOfTExtendsListTOfT() { ok("a.SortedList[a.Num]","a.List[a.Num]",true,pointEx); }
  @Test void sortedListOfTExtendsListTOfTMdf() { ok("a.SortedList[a.Num]","a.List[read a.Num]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfX() { ok("a.SortedList[X]","a.List[X]",true,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot1() { ok("a.SortedList[a.Num]","a.List[X]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot2() { ok("a.SortedList[X]","a.List[a.Num]",false,pointEx); }
  @Test void sortedListOfTExtendsListTOfNot3() { ok("a.SortedList[X]","a.List[a.List[a.Num]]",false,pointEx); }
  @Test void sortedListMixedGens() { ok("a.SortedList[a.ColouredPoint]","a.SortedList[a.Point]",true,pointEx); }
  @Test void inverseSortedListMixedGens() { ok("a.SortedList[a.Point]","a.SortedList[a.ColouredPoint]",false,pointEx); }
}
