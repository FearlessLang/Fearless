package codegen.java;

import failure.CompileError;
import main.CompilerFrontEnd;
import main.Main;
import main.java.LogicMainJava;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Err;
import utils.IoErr;

import java.util.Arrays;
import java.util.List;

import static utils.RunOutput.Res;
import static utils.RunOutput.assertResMatch;

public class TestJavaProgramImm {
  void ok(Res expected, String... content) {
    okWithArgs(expected, List.of(), content);
  }
  void okWithArgs(Res expected, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var logicMain = LogicMainJava.of(TestInputOutputs.programmaticImm(Arrays.asList(content), args), verbosity);
    assertResMatch(logicMain.run(), expected);
  }

  void fail(String expectedErr, String... content) {
    failWithArgs(expectedErr, List.of(), content);
  }
  void failWithArgs(String expectedErr, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    try {
      var logicMain = LogicMainJava.of(TestInputOutputs.programmaticImm(Arrays.asList(content), args), verbosity);
      IoErr.of(()->logicMain.run().inheritIO().start()).onExit().join();
      Assertions.fail("Did not fail");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    Test:Main{ _ -> "" }
    """);}

  @Test void lists() { ok(new Res("2", "", 0), """
    package test
    alias base.Main as Main, alias base.LList as LList, alias base.Nat as Nat,
    Test:Main{_ -> A.m1.get(1).match{.some(n) -> n.str, .none -> base.Abort!}}
    A:{
      .m1: LList[Nat] -> LList[Nat] + 1 + 2 + 3,
      }
    """);}

  @Test void inheritedFnNoSingleton() { ok(new Res("5", "", 0), """
    package test
    alias base.Main as Main, alias base.Nat as Nat,
    A: {.m1: Nat -> 5, .unrelated: Nat,}
    B: A{.m2: Nat -> this.m1, .unrelated -> 123}
    Test: Main{_ -> B.m2.str}
    """);}

  @Test void fib43() { ok(new Res("433494437", "", 0), """
    package test
    alias base.Main as Main, alias base.Nat as Nat,
    Test:Main{ _ -> Fib#(43).str }
    Fib: {
      #(n: Nat): Nat -> n <= 1 ? {
        .then -> n,
        .else -> this#(n - 1) + (this#(n - 2))
        }
      }
    """);}

  @Test void nestedConditional() { ok(new Res("2", "", 0), """
    package test
    alias base.Main as Main, alias base.True as True, alias base.False as False, alias base.Nat as Nat,
    Test:Main {l -> True ?[Nat] {.then -> False ?[Nat] {.then -> 1, .else -> Block#[base.LList[base.Str],Nat](l, 2)}, .else -> 3}.str}
    Block:{#[A:imm,R:imm](_: A, r: R): R -> r}
    """); }

  @Test void assertTrue() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(True, { "" }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, { "" }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { "" }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { "" }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { "" }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && True) .str, { "" }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True && False) .str, { "" }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False && False) .str, { "" }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || True) .str, { "" }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (True || False) .str, { "" }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || True) .str, { "" }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (False || False) .str, { "" }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(420 > 9000, { "" }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!("hi".size > 9000, { "" }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 123456789 .str, { "" }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { "" }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { "" }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    In position [###]/Dummy0.fear:4:31
    [E31 invalidNum]
    The number +9223372036854775808 is not a valid Int
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, +9223372036854775808 .str, { "" }) }
    """);}
  @Test void veryLongLongNatFail() { fail("""
    In position [###]/Dummy0.fear:4:31
    [E31 invalidNum]
    The number 10000000000000000000000 is not a valid Nat
    """, """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, 10000000000000000000000 .str, { "" }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, -123456789 .str, { "" }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { "" }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { "" }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, (+0 - +2) .str, { "" }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main{ _ -> Assert!(False, ((+0 - +2) - +9223372036854775807) .str, { "" }) }
    """);}

  @Test void launchArg() { okWithArgs(new Res("yeet", "", 0), List.of("yeet"), """
    package test
    Test:base.Main{ args -> args.head.match{ .none -> "boo", .some(msg) -> msg } }
    """);}

  @Test void nestedPkgs() { ok(new Res("", "", 0), """
    package test
    Test:base.Main[]{ _ -> test.foo.Bar{ .a -> test.foo.Bar }.str }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    alias base.Str as Str,
    Bar:test.Foo{ .a -> this, .str: Str -> "" }
    """); }

  @Test void gens() { ok(new Res("132", "", 0), """
    package test
    alias base.Nat as Nat, alias base.Str as Str,
    Test:base.Main[]{ _ -> F[Nat,Str]{n -> n.str}#132 }
    F[A,R]:{ #(a: A): R }
    """); }
  @Test void methodGens() { ok(new Res("hi", "", 0), """
    package test
    alias base.Int as Int, alias base.Str as Str,
    Box[T:imm]:{.get: T}
    Box:{#[T:imm](x: T): Box[T] -> {x}}
    Test:base.Main[]{ _ -> Box#"hi".get }
    """); }

  // Based on this it looks like Mearless basically handles anti-shadowing for us
  @Test void shadowingViaReduction() { ok(new Res("3", "", 0), """
    package test
//    Test: base.Main{_ -> (Break# #(L{#(z) -> z, .n -> 4})#(L{#(z) -> z, .n -> 5})).n.str}
    Test: base.Main{_ -> (Break# #(L{#(z) -> z, .n -> 4})).n.str}
    L: {#(l: L): L, .n: base.Nat}
    Break: {#: L -> L{#(x) -> L{#(y) -> x, .n -> 2}, .n -> 1}#(L{#(y) -> y, .n -> 3})}
    """); }

  @Test void blockRet() { ok(new Res("5", "", 0), """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block,
    Test:base.Main{ _ -> Block#
     .return {5 .str}
     }
    """);}
  @Test void blockDoRet() { ok(new Res("5", "", 0), """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main { _ -> Block#
     .do {ForceGen#}
     .return {5 .str}
     }
    ForceGen: {#: Void -> {}}
    """);}
  @Test void blockVarDoRet() { ok(new Res("5", "", 0), """
    package test
    alias base.Nat as Nat, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main { _ -> Block#
     .let[Nat] n = {5}
     .do {ForceGen#}
     .return {n .str}
     }
    ForceGen: {#: Void -> {}}
    """);}

  private static final String PEANO = """
    package test
    alias base.Int as Int,
    Num: {
      .pred: Num,
      +(b: Num): Num,
      *(b: Num): Num,
      .int: Int,
      .succ: Num -> S{this},
      }
    Zero: Num{
      .pred -> this.pred,
      +(b) -> b,
      *(b) -> this,
      .int -> +0,
      }
    S: Num{
      +(b) -> S{this.pred + b},
      *(b) -> b + (b * (this.pred)),
      .int -> this.pred.int + +1,
      }
    """;
  @Test void peanoAdd() { ok(new Res("6", "", 0), """
    package test
    Test: base.Main{_ -> (Zero + (Zero.succ) + (Zero + (Zero.succ)) + (Zero.succ) + (Zero.succ.succ.succ) + Zero).int.str}
    """, PEANO);}
  @Test void peanoMultZero() { ok(new Res("0", "", 0),"""
    package test
    Test: base.Main{_ -> (Zero * (Zero.succ))+(Zero.succ * Zero).int.str}
    """, PEANO);}
  @Test void peanoMultTwo() { ok(new Res("8", "", 0),"""
    package test
    Test: base.Main{_ -> ((Zero.succ.succ.succ.succ) * (Zero.succ.succ)).int.str}
    """, PEANO);}

  @Test void strAssertions() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#("a".assertEq("a"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void strAssertionsFail() { ok(new Res("", """
    Expected: a
    Actual: b
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#("a".assertEq("b"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void strAssertionsFailWithMessage() { ok(new Res("", """
    oh no
    Expected: a
    Actual: b
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#("a".assertEq("b", "oh no"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}


  @Test void intAssertions() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((+5).assertEq(+5))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void intAssertionsFail() { ok(new Res("", """
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((+5).assertEq(+10))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void intAssertionsFailWithMessage() { ok(new Res("", """
    oh no
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((+5).assertEq(+10, "oh no"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}

  @Test void natAssertions() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5).assertEq(5))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void natAssertionsFail() { ok(new Res("", """
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5).assertEq(10))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void natAssertionsFailWithMessage() { ok(new Res("", """
    oh no
    Expected: 5
    Actual: 10
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5).assertEq(10, "oh no"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}

  @Test void floatAssertions() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5.23).assertEq(5.23))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void floatAssertionsFail() { ok(new Res("", """
    Expected: 5.23
    Actual: 5.64
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5.23).assertEq(5.64))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
  @Test void floatAssertionsFailWithMessage() { ok(new Res("", """
    oh no
    Expected: 5.23
    Actual: 5.64
    """, 1), """
    package test
    alias base.Main as Main,
    alias base.Int as Int, alias base.Nat as Nat, alias base.Float as Float,
    alias base.Str as Str,
    
    Test: Main{_ -> Yeet#((5.23).assertEq(5.64, "oh no"))}
    Yeet: {#[X](x: X): Str -> ""}
    """);}
}
