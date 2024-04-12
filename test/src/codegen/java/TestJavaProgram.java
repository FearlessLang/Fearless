package codegen.java;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import java.util.List;

import static codegen.java.RunJavaProgramTests.*;
import static utils.RunOutput.Res;

public class TestJavaProgram {
  @Test void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    """);}

  @Test void captureTest() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    A:{ #: A -> A{ # -> A { # -> this } }# }
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, "power level less than 9000", { Void }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True && True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True && False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False && False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True || True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (True || False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False || True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (False || False) .str, { Void }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!("hi".size > 9000u, { Void }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, -123456789 .str, { Void }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreInt() { ok(new Res("", "500002", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000 + 2) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreUInt() { ok(new Res("", "500002", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000u + 2u) .str, { Void }) }
    """);}
  @Test void addWithUnderscoreFloat() { ok(new Res("", "500002.6", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5_00_000.5 + 2.1) .str, { Void }) }
    """);}
  @Test void intDivByZero() { ok(new Res("", "Program crashed with: / by zero", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void, alias base.Block as Do,
    Test:Main{ _ -> Do#(5 / 0) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, (0 - 2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert!(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  @Test void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    Test:Main{ s -> Block#
      .let({ base.caps.FIO#s }, { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .return{ io.println("Hello, World!") }
      }
    """); }
  @Test void printlnDeeper() { ok(new Res("IO begets IO", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .return{ Block#
        .let io2 = { base.caps.FIO#s }
        .return{ io2.println("IO begets IO") }
        }
      }
    """); }
  @Test void print() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .do{ io.print("Hello") }
      .return{ io.print(", World!") }
      }
    """); }
  @Test void printlnErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .return{ io.printlnErr("Hello, World!") }
      }
    """); }
  @Test void printErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .do{ io.printErr("Hello") }
      .return{ io.printErr(", World!") }
      }
    """); }
  @Test void printlnShareLent() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .return{ Usage#io }
      }
    Usage:{
      #(io: lent IO): Void -> io.println("Hello, World!"),
      }
    """); }
  @Test void printlnShareLentCapture() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.IO as IO, alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .return{ lent Usage{ io }# }
      }
    Usage:{
      lent .io: lent IO,
      lent #: Void -> this.io.println("Hello, World!"),
      }
    """); }

  @Test void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Block as Block,
    alias base.caps.FIO as FIO,
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Test void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main{ _ -> {} }
    Bloop:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """); }

    @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _ -> Assert!((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Test void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _ -> Assert!((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.let[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Test void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main{ _ -> mut Block[Void]
      .let(n = { Ref#[Int]5 })
      .do{ Assert!(n.swap(6) == 5) }
      .do{ Assert!(n* == 6) }
      .return{{}}
      }
    """); }

  static String cliArgsOrElseGet = """
    package test
    MyApp:Main{ s -> Block#
      .let io = { FIO#s }
      .let env = { FEnv.io(io) }
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u).match{.some(arg) -> arg, .empty -> this.errMsg(args.head.isSome).get},
      .errMsg(retCounter: Bool): mut Ref[Str] -> Block#
        .let res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .let counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Block#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Block#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
        .if{ True }.do{ res* := "moar mutability" }
        .if{ retCounter.not }.return{ res* }
        .return{ Ref#(counter*.str) }
      }
    """;
  @Test void cliArgs1a() { okWithArgs(new Res("moar mutability", "", 0), "test.MyApp", List.of(), cliArgsOrElseGet, Base.mutBaseAliases); }
  @Test void cliArgs1b() { okWithArgs(new Res("387000", "", 0), "test.MyApp", List.of(
    "hi"
  ), cliArgsOrElseGet, Base.mutBaseAliases); }
  @Test void cliArgs1c() { okWithArgs(new Res("bye", "", 0), "test.MyApp", List.of(
    "hi",
    "bye"
  ), cliArgsOrElseGet, Base.mutBaseAliases); }
  String getCliArgsOrElse = """
    package test
    MyApp:Main{ s -> Block#
      .let io = { FIO#s }
      .let env = { FEnv#s }
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) | (this.errMsg(args.head.isSome)*),
      .errMsg(retCounter: Bool): mut Ref[Str] -> Block#
        .let res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .let counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Block#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Block#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
        .if{ True }.do{ res* := "moar mutability" }
        .if{ retCounter.not }.return{ res* }
        .return{ Ref#(counter*.str) }
      }
    """;
  @Disabled
  @Test void cliArgs2a() { okWithArgs(new Res("moar mutability", "", 0), "test.MyApp", List.of(), getCliArgsOrElse, Base.mutBaseAliases); }
  @Disabled
  @Test void cliArgs2b() { okWithArgs(new Res("387000", "", 0), "test.MyApp", List.of(
    "hi"
  ), getCliArgsOrElse, Base.mutBaseAliases); }
  @Disabled
  @Test void cliArgs2c() { okWithArgs(new Res("bye", "", 0), "test.MyApp", List.of(
    "hi",
    "bye"
  ), getCliArgsOrElse, Base.mutBaseAliases); }

  @Test void findClosestInt() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let[Int] closest' = { ns.get(0u)! }
        .let closest = { Ref#[Int](closest') }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let[mut Ref[Int]] closest = { Ref#[Int](ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let[mut Ref[Int]] closest = { Ref#[Int]((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(mut LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let closest = { Ref#[Int](ns.get(0u)!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*[])).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.tail.head, ns.tail.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: mut Opt[Int], t: mut LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(mut LList[Int] + 35 + 52 + 84 + 14 .list, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut List[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let closest = { Ref#[Int](ns.get(0u)!) }
        .do{ mut Closest'{ 'self
          i -> ns.get(i).match{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(i + 1u)
              }
            }
          }#(1u) }
        .return{ closest* }
      }
    Closest':{ mut #(i: UInt): Void }
    """, Base.mutBaseAliases); }

  @Test void LListItersIterImm() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[LList[Int]] l1 = { LList[Int] + 35 + 52 + 84 + 14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 100}).isEmpty, "find empty", {{}}) }
      .do{ Assert!((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void LListItersIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut LList[Int]] l1 = { mut LList[Int] +[] 35 +[] 52 +[] 84 +[] 14 }
      .do{ Assert!(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 100}).isEmpty, "find empty", {{}}) }
      .do{ Assert!(l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.iter.Sum as Sum,
    Test:Main{ _ -> Block#
      .let[mut List[Int]] l1 = { (mut LList[Int] + 35 + 52 + 84 + 14).list }
      .assert({ l1.get(0u)! == (l1.iter.next!) }, "sanity") // okay, time to use this for new tests
      .do{ Assert!((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert!((l1.iter.find{n -> n > 100}).isEmpty, "find empty", {{}}) }
      .do{ Assert!(l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .do{ Assert!(l1.iter
                      .filter{n -> n > 50}
                      .find{n -> n == 84}
                      .isSome,
        "filter", {{}})}
      .do{ Assert!(l1.iter
                      .filter{n -> n > 50}
                      .count == 2u,
        "count", {{}})}
      .do{ Assert!(l1.iter
                      .filter{n -> n > 50}
                      .list.size == 2u,
        "toList", {{}})}
      .do{ Assert!(l1.iter
                      .filter{n -> n > 50}
                      .llist
                      .size == 2u,
        "to mut LList", {{}})}
      .do{ Assert!(l1.iter
                    .flatMap{n -> List#(n, n, n).iter}
                    .map{n -> n * 10}
                    .str({n -> n.str}, ";") == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert!(Sum.int(l1.iter) == 185, "sum int", {{}})}
      .do{ Assert!(Sum.uint(l1.iter.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .do{ Assert!(Sum.float(l1.iter.map{n -> n.float}) == 185.0, "sum float", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void paperExamplePrintIter() { ok(new Res("350,350,350,140,140,140", "", 0), "test.IterFind", """
    package test
    alias base.Int as Int, alias base.Str as Str,
    alias base.List as List, alias base.Block as Block,
    alias base.caps.FIO as FIO, alias base.caps.IO as IO,
    
    IterFind:base.Main{ sys -> Block#
        .let l1 = { List#[Int](35, 52, 84, 14) }
        .assert{l1.iter
          .map{n -> n * 10}
          .find{n -> n == 140}
          .isSome}
        .let[Str] msg = {l1.iter
          .filter{n -> n < 40}
          .flatMap{n -> List#(n, n, n).iter}
          .map{n -> n * 10}
          .str({n -> n.str}, ",")}
        .let io = {FIO#sys}
        .return {io.println(msg)}
        // prints 350,350,350,140,140,140
    }
    """);}
  @Test void paperExamplePrintFlow() { ok(new Res("350,350,350,140,140,140", "", 0), "test.IterFind", """
    package test
    alias base.Int as Int, alias base.Str as Str,
    alias base.List as List, alias base.Block as Block,
    alias base.caps.FIO as FIO, alias base.caps.IO as IO,
    alias base.flows.Flow as Flow,
        
    IterFind:base.Main{ sys -> Block#
        .let l1 = { List#[Int](35, 52, 84, 14) }
        .assert{l1.iter
          .map{n -> n * 10}
          .find{n -> n == 140}
          .isSome}
        .let[Str] msg = {l1.flow
          .filter{n -> n < 40}
          .flatMap{n -> List#(n, n, n).flow}
          .map{n -> n * 10}
          .map{n -> n.str}
          #(Flow.str ",")}
        .let io = {FIO#sys}
        .return {io.println(msg)}
        // prints 350,350,350,140,140,140
    }
    """);}

  @Test void absIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(5 .abs == 5) }
    """, Base.mutBaseAliases); }
  @Test void absIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(0 .abs == 0) }
    """, Base.mutBaseAliases); }
  @Test void absIntNeg() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(-5 .abs == 5) }
    """, Base.mutBaseAliases); }

  @Test void absUIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(5u .abs == 5u) }
    """, Base.mutBaseAliases); }
  @Test void absUIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert!(0u .abs == 0u) }
    """, Base.mutBaseAliases); }

  @Test void isoPod1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .return{ Assert!(Usage#(a!) == 0) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod1Consume() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .return{ Assert!(a.consume{.some(n) -> Usage#n, .empty -> 500} == 0) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ a.next(MutThingy'#(Count.int(5))) }
      .return{ Assert!(Usage#(a!) == 5) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ Block#(a.mutate{ mt -> Block#(mt.n++) }) }
      .return{ Assert!(Usage#(a!) == 1) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPodNoImmFromPeekOk() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .let[Int] ok = { a.peek[Int]{ .some(m) -> m.rn*.toImm + 0, .empty -> base.Abort! } }
      .return{Void}
      }
    MutThingy:{ mut .n: mut Count[Int], read .rn: read Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { .n -> n, .rn -> n }  }
    """, Base.mutBaseAliases); }

  @Test void envFromRootAuth() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let env = { FEnv#s }
      .return{ io.println(env.launchArgs.iter.str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }
  @Test void envFromIO() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .return{ io.println(base.caps.FEnv.io(io).launchArgs.iter.str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }

  @Test void intExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .return{ io.println(5 ** 5u .str) }
      }
    """, Base.mutBaseAliases); }
  @Test void uintExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .return{ io.println(5u ** 5u .str) }
      }
    """, Base.mutBaseAliases); }

  @Test void negativeNums() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!(-5 == -5, "id", {{}}) }
      .do{ Assert!((-5 - -5) == 0, "subtraction 1", {{}}) }
      .do{ Assert!((-5 - 5) == -10, "subtraction 2", {{}}) }
      .do{ Assert!((-5 + 3) == -2, "addition 1", {{}}) }
      .do{ Assert!((-5 + 7) == 2, "addition 2", {{}}) }
      .do{ Assert!((5 + -7) == -2, "addition 3", {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void floats() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .do{ Assert!(-5.0 == -5.0, "id (neg)", {{}}) }
      .do{ Assert!((-5.0 - -5.0) == 0.0, "subtraction 1", {{}}) }
      .do{ Assert!((-5.0 - 5.0) == -10.0, "subtraction 2", {{}}) }
      .do{ Assert!((-5.0 + 3.0) == -2.0, "addition 1", {{}}) }
      .do{ Assert!((-5.0 + 7.0) == 2.0, "addition 2", {{}}) }
      .do{ Assert!((5.0 + -7.0) == -2.0, "addition 3", {{}}) }
      // pos
      .do{ Assert!(1.0 == 1.0, "id", {{}}) }
      .do{ Assert!(1.0 + 3.5 == 4.5, "addition 1 (pos)", {{}}) }
      .do{ Assert!((1.0).str == "1.0", "str", {{}}) }
      .do{ Assert!((5.0 / 2.0) == 2.5, (5.0 / 2.0).str, {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void shouldPeekIntoIsoPod() { ok(new Res("""
    peek: help, i'm alive
    consume: help, i'm alive
    """.strip(), "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let s1 = { IsoPod#[iso Str](iso "help, i'm alive") }
      .do{ PrintMsg#(io, s1) }
      .return{ io.println("consume: " + (s1!)) }
      }
    PrintMsg:{
      #(io: mut IO, msg: read IsoPod[iso Str]): Void -> msg.peek{
        .some(str) -> io.println("peek: " + str),
        .empty -> Void
        }
      }
    """, Base.mutBaseAliases); }

  @Test void shouldReadFullIsoPod() { ok(new Res("hi", "", 0), "test.Test", """
    package test
    Test:Main{ s ->
      Try#[Str]{ Block#
        .let[mut IsoPod[Str]] pod = { IsoPod#[Str] iso "hi" }
        .return{pod!}
        }.resMatch{
          .ok(msg) -> FIO#s.println(msg),
          .err(info) -> FIO#s.printlnErr(info.str),
        }
      }
    """, Base.mutBaseAliases); }
  @Test void shouldFailOnEmptyIsoPod() { ok(new Res("", "Cannot consume an empty IsoPod.", 0), "test.Test", """
    package test
    Test:Main{ s ->
      Try#[Str]{ Block#
        .let[mut IsoPod[Str]] pod = { IsoPod#[Str] iso "hi" }
        .do{ Block#(pod!) }
        .return{pod!}
        }.resMatch{
          .ok(msg) -> FIO#s.println(msg),
          .err(info) -> FIO#s.printlnErr(info.msg),
        }
      }
    """, Base.mutBaseAliases); }

  @Test void optionalMapImm() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Block#
        .let[Opt[Int]] i = { Opt#[Int]16 }
        .let[Opt[Int]] ix10 = { i.map{n -> n * 10} }
        .return{{}}
        }
      """, Base.mutBaseAliases);
  }

  @Test void equality() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Block#
        .let[Shape] s1 = {{ .x -> 5, .y -> 6 }}
        .let[Shape] s1' = {{ .x -> 5, .y -> 6 }}
        .assert({ s1 == s1 }, "shape eq same id")
        .assert({ s1 == s1' }, "shape same structure")
        .let[Shape] s2 = {{ .x -> 7, .y -> 6 }}
        .assert({ s1 != s2 }, "shape neq")
        .return{{}}
        }
      
      Shape:{
        .x: Int, .y: Int,
        ==(other: Shape): Bool -> (this.x == (other.x)) && (this.y == (other.y)),
        !=(other: Shape): Bool -> this == other .not,
        }
      """, Base.mutBaseAliases);
  }
  @Test void equalitySubtyping() {
    ok(new Res("", "", 0), "test.Test", """
      package test
      Test:Main{ _ -> Block#
        .let[Shape] s1 = {{ .x -> 5, .y -> 6 }}
        .let[Square] sq1 = {{ .x -> 5, .y -> 6, .size -> 12 }}
        .let[Square] sq1' = {{ .x -> 5, .y -> 6, .size -> 12 }}
        .assert({ sq1 == sq1' }, "square eq same structure")
        .assert({ s1 == sq1 }, "shape eq")
        .return{{}}
        }
      
      Shape:{
        .x: Int, .y: Int,
        ==(other: Shape): Bool -> (this.x == (other.x)) && (this.y == (other.y)),
        !=(other: Shape): Bool -> this == other .not,
        }
      Square:Shape{
        read .size: Int,
        .eqSq(other: Square): Bool -> this == other && (this.size == (other.size)),
        }
      """, Base.mutBaseAliases);
  }

  @Test void callingMultiSigAmbiguousDiffRet() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> Assert!(False, {{}}),
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): read B -> a.m1[](),
      }
    ToVoid:{ #[I](x: I): Void -> {} }
    """); }
  @Test void callingMultiSigAmbiguousDiffRetMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: read B -> Assert!(False, {{}}),
      mut .m1: mut A -> this,
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    ToVoid:{ #[I](x: I): Void -> {} }
    """); }
  @Test void callingMultiSigAmbiguousSameRet() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    A:{
      read .m1: mut A -> Assert!(False, { mut A }),
      mut .m1: mut A -> {},
      }
    B:{}
    Test:base.Main{
      #(s) -> ToVoid#(this.aRead(mut A)),
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    ToVoid:{ #[I](x: I): Void -> {} }
    """); }
  @Test void optionals1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Void as Void,
    A:{}
    Test:base.Main{
      #(s) -> (base.Opt#Void).match[base.Void](mut base.OptMatch[Void,Void]{ .some(x) -> x, .empty -> {} }),
      }
    """); }
  @Test void canGetImmIntFromImmListOfImmInt() { ok(new Res("", "", 0), "test.Test", """
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 + 34 + 56 }
    Test:Main{ _ -> Block#
      .let myList = { MakeList# }
      .assert({ As[Int]#(myList.head!) == 12 }, myList.head!.str)
      .assert({ As[Int]#(myList.tail.head!) == 34 }, "can get 2nd tail el")
      .assert({ myList.head! == 12 }, "can get head el without cast")
      .assert({ myList.tail.head! == 34 }, "can get 2nd tail el without cast")
      .return{Void}
      }
    """, Base.mutBaseAliases); }
  @Test void canGetImmOptFromImmListOfImmInt() { ok(new Res("", "", 0), "test.Test", """
    package test
    MakeList:{ #: LList[Int] -> LList[Int] + 12 + 34 + 56 }
    Test:Main{ _ -> Block#
      .let myList = { MakeList# }
      .let[Opt[read Int]] opt = { myList.head }
      .let[Int] i1 = { opt! }
      .let[Int] i2 = { myList.head! }
      .return{Void}
      }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMultiMdf() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Block#
      .let[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert!(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Block#
        .do{ Assert!(ns.isEmpty.not, "empty list :-(", {{}}) }
        .let[mut Ref[Int]] closest = { Ref#[Int](ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match[Void] mut base.OptMatch[Int,Void]{
            .empty -> {},
            .some(n) -> (target - n).abs < ((target - (closest*[])).abs) ? {
              .then -> closest := n,
              .else -> self#(
              As[Opt[Int]]#(
                As[Opt[read Int]]#(
                  As[iso Opt[read Int]]#(
                    As[LList[Int]]#(t)
                      .head))),
                t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest*[] }
      }
    Closest':{ mut #(h: Opt[Int], t: LList[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void canCreateMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main{ _ -> {} }
    MutLList:{ #: mut base.LList[base.Int] -> mut base.LList[base.Int] +[] 35 +[] 52 +[] 84 +[] 14 }
    """); }

  @Test void immFromRefImm() { ok(new Res("5", "", 0), "test.Test", """
    package test
    Test:Main{
      #(s) -> FIO#s.println(this.m2.str),
      .m1(r: read RefImm[Int]): Int -> r.get,
      .m2: Int -> this.m1(Ref#[Int]5),
      }
    """, Base.mutBaseAliases); }
  @Test void immFromRefImmRecover() { ok(new Res("5", "", 0), "test.Test", """
    package test
    Test:Main{
      #(s) -> FIO#s.println(this.m2.str),
      .m1(r: read Ref[Int]): Int -> r.getImm!,
      .m2: Int -> this.m1(Ref.ofImm[Int]5),
      }
    """, Base.mutBaseAliases); }
  @Test void immFromRefImmPrimitive() { ok(new Res("5", "", 0), "test.Test", """
    package test
    Test:Main{
      #(s) -> FIO#s.println(this.m2.str),
      .m1(r: read Ref[Int]): Int -> r.get.toImm,
      .m2: Int -> this.m1(Ref#[Int]5),
      }
    """, Base.mutBaseAliases); }
  @Test void updateRefImm() { ok(new Res("12", "", 0), "test.Test", """
    package test
    Test:Main{
      #(s) -> FIO#s.println(this.m2.str),
      .m1(r: mut RefImm[Int]): Int -> Block#
        .do{ r := 12 }
        .let[read RefImm[Int]] rr = { r }
        .return{ rr.get },
      .m2: Int -> this.m1(Ref#[Int]5),
      }
    """, Base.mutBaseAliases); }
  @Test void updateRefImmRecover() { ok(new Res("12", "", 0), "test.Test", """
    package test
    Test:Main{
      #(s) -> FIO#s.println(this.m2.str),
      .m1(r: mut Ref[Int]): Int -> Block#
        .do{ r := 12 }
        .let[read Ref[Int]] rr = { r }
        .return{ rr.getImm! },
      .m2: Int -> this.m1(Ref.ofImm[Int]5),
      }
    """, Base.mutBaseAliases); }

  @Test void llistFilterMultiMdf() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[LList[Int]] l = { LList#[Int] + 12 + 13 + 14 }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: LList[Int]): Str -> l.iter
                                 .filter{n -> n >= (12.5 .round)}
                                 .str({n->n.str}, ", ")
      }
    """, Base.mutBaseAliases);}
  @Test void listFilterMultiMdf() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[List[Int]] l = { List#[Int](12, 13, 14) }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: List[Int]): Str -> l.iter
                                 .filter{n -> n >= (12.5 .round)}
                                 .str({n->n.str}, ", ")
      }
    """, Base.mutBaseAliases);}

  @Test void llistFilterMultiMdfMut() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[mut LList[Int]] l = { LList#[Int] + 12 + 13 + 14 }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: mut LList[Int]): Str -> l.iter
                                      .filter{n -> n >= (12.5 .round)}
                                      .str({n->n.str}, ", ")
      }
    """, Base.mutBaseAliases);}
  @Test void listFilterMultiMdfMut() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[mut List[Int]] l = { List#[Int](12, 13, 14) }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: mut List[Int]): Str -> l.iter
                                     .filter{n -> n >= (12.5 .round)}
                                     .str({n->n.str}, ", ")
      }
    """, Base.mutBaseAliases);}

  @Test void llistFilterMultiMdfRead() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[mut LList[Int]] l = { LList#[Int] + 12 + 13 + 14 }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: read LList[Int]): Str -> l.iter
                                      .filter{n -> n.toImm >= (12.5 .round)}
                                      .str({n -> n.toImm.str}, ", ")
      }
    """, Base.mutBaseAliases);}
  @Test void listFilterMultiMdfRead() { ok(new Res("13, 14", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let io = { FIO#s }
      .let[mut List[Int]] l = { List#[Int](12, 13, 14) }
      .do { io.println(A.m1(l)) }
      .return {{}}
      }
    A:{
      .m1(l: read List[Int]): Str -> l.iter
                                      .filter{n -> n.toImm >= (12.5 .round)}
                                      .str({n -> n.toImm.str}, ", ")
      }
    """, Base.mutBaseAliases);}

  @Test void strMap() { ok(new Res("23\n32\n230\nhi", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .let[mut Ref[mut LinkedLens[Str, Int]]] m = { Ref#[mut LinkedLens[Str, Int]](mut StrMap[Int]) }
      .do{ m := (m*.put("Nick", 23)) }
      .do{ m := (m*.put("Bob", 32)) }
      .do{ io.println(m*.get("Nick")!.str) }
      .do{ io.println(m*.get("Bob")!.str) }
      .assert{ m*.get("nobody").isEmpty }
      .let[mut Ref[mut LinkedLens[Str, Str]]] tm = { Ref#[mut LinkedLens[Str, Str]](m*.map(
        {k, v -> (v * 10).str },
        {k, v -> (v * 10).str },
        {k, v -> (v.toImm * 10).str }
        )) }
      .do{ io.println(tm*.get("Nick")!) }
      .do{ tm := (tm*.put("Nick", "hi")) }
      .do{ io.println(tm*.get("Nick")!) }
      .return{Void}
      }
    """, Base.mutBaseAliases);}
  @Test void strMapImm() { ok(new Res("23\n32\n230\nhi", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .let[mut Ref[LinkedLens[Str, Int]]] m = { Ref#[LinkedLens[Str, Int]](StrMap[Int]) }
      .do{ m := (m*.put("Nick", 23)) }
      .do{ m := (m*.put("Bob", 32)) }
      .do{ io.println(m*.get("Nick")!.str) }
      .do{ io.println(m*.get("Bob")!.str) }
      .assert{ m*.get("nobody").isEmpty }
      .let[mut Ref[LinkedLens[Str, Str]]] tm = { Ref#[LinkedLens[Str, Str]](m*.map(
        {k, v -> (v * 10).str },
        {k, v -> (v.toImm * 10).str }
        )) }
      .do{ io.println(tm*.get("Nick")!) }
      .do{ tm := (tm*.put("Nick", "hi")) }
      .do{ io.println(tm*.get("Nick")!) }
      .return{Void}
      }
    """, Base.mutBaseAliases);}
  @Test void strMapRead() { ok(new Res("23\n32\n230\nhi", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .let[mut Ref[read LinkedLens[Str, Int]]] m = { Ref#[read LinkedLens[Str, Int]]({k1,k2 -> k1 == k2}) }
      .do{ m := (m*.put("Nick", 23)) }
      .do{ m := (m*.put("Bob", 32)) }
      .do{ io.println(m*.get("Nick")!.toImm.str) }
      .do{ io.println(m*.get("Bob")!.toImm.str) }
      .assert{ m*.get("nobody").isEmpty }
      .let[mut Ref[read LinkedLens[Str, Str]]] tm = { Ref#[read LinkedLens[Str, Str]](m*.map(
        {k, v -> (v * 10).str },
        {k, v -> (v.toImm * 10).str }
        )) }
      .do{ io.println(tm*.get("Nick")!.toImm) }
      .do{ tm := (tm*.put("Nick", "hi")) }
      .do{ io.println(tm*.get("Nick")!.toImm) }
      .return{Void}
      }
    """, Base.mutBaseAliases);}

  @Test void lensMap() { ok(new Res("23\n32\n230\nhi", "", 0), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[mut IO] io = { FIO#s }
      .let[mut Ref[Lens[Str, Int]]] m = { Ref#[Lens[Str, Int]]({k1,k2 -> k1 == k2}) }
      .do{ m := (m*.put("Nick", 23)) }
      .do{ m := (m*.put("Bob", 32)) }
      .do{ io.println(m*.get("Nick")!.str) }
      .do{ io.println(m*.get("Bob")!.str) }
      .assert{ m*.get("nobody").isEmpty }
      .let[mut Ref[Lens[Str, Str]]] tm = { Ref#[Lens[Str, Str]](m*.map{k, v -> (v * 10).str }) }
      .do{ io.println(tm*.get("Nick")!) }
      .do{ tm := (tm*.put("Nick", "hi")) }
      .do{ io.println(tm*.get("Nick")!) }
      .return{Void}
      }
    """, Base.mutBaseAliases);}

  @Test void tryCatch1() { ok(new Res("Happy", "", 0), "test.Test", """
    package test
//    Test:Main{s ->
//      FIO#s.println(Try#[Str](
//        {"Happy"},
//        {err->err.str}
//        ))
//      }
    Test:Main{s ->
      FIO#s.println(Try#[Str]{"Happy"}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases);}
  @Test void tryCatch2() { ok(new Res("oof", "", 0), "test.Test", """
    package test
//    Test:Main{s ->
//      FIO#s.println(Try#[Str](
//        {Error.msg("oof")},
//        {err->err.str}
//        ))
//      }
    Test:Main{s ->
      FIO#s.println(Try#[Str]{Error.msg("oof")}.match{ .a(a) -> a, .b(err) -> err.msg })
      }
    """, Base.mutBaseAliases);}
  @Test void error1() { ok(new Res("", "Program crashed with: \"yolo\" ", 1), "test.Test", """
    package test
    Test:Main{s -> Error.msg("yolo") }
    """, Base.mutBaseAliases);}
  @Test void emptyOptErr1() { ok(new Res("", "Program crashed with: \"Opt was empty\"", 1), "test.Test", """
    package test
    Test:Main{s -> Block#(Opt[Str]!) }
    """, Base.mutBaseAliases);}
  @Test void emptyOptErr2() { ok(new Res("", "Opt was empty", 0), "test.Test", """
    package test
    Test:Main{s ->
      Try#{Opt[Str]!}.match{
        .a(_) -> {},
        .b(info) -> FIO#s.printlnErr(info.msg),
        }
      }
    """, Base.mutBaseAliases);}
  @Disabled // TODO: cannot wrap this because try can't capture System right now
  @Test void emptyOptErrWrapped() { ok(new Res("", """
    Exception in thread "main" FearlessError
    	at FProgram$base$Error_0.str$imm$(FProgram.java:[###])
    	at FProgram$base$Opt_1$2.empty$mut$(FProgram.java:[###])
    	at FProgram$base$Opt_1.match$imm$(FProgram.java:[###])
    	at FProgram$base$Opt_1.$33$imm$(FProgram.java:[###])
    	at FProgram$test$Test_0.$35$imm$(FProgram.java:[###])
    	at FProgram.main(FProgram.java:[###])""", 1), "test.Test", """
    package test
    SMain:Main{
      #(s) -> Try#[Void]{this.realMain(s)}.resMatch{
        .ok(void) -> void,
        .err(info) -> FIO#s.printlnErr(info.str)
        },
      .realMain(s: mut System): Void,
      }
    Test:SMain{s -> Block#(Opt[Str]!) }
    """, Base.mutBaseAliases);}

  @Test void optWithExtensionMethodOrElse() { ok(new Res(), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[Int] res = {Opt[Int]
                       #{opt -> opt.match{.some(x) -> x, .empty -> 9001}}}
      .assert{res == 9001}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void optWithExtensionMethodOrElseLib() { ok(new Res(), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[Int] res = {mut Opt[Int]
                       # mut base.OptOrElseExt[Int]{9001}}
      .assert{res == 9001}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void extensionMethodMdfDispatch() { ok(new Res(), "test.Test", """
    package test
    Test:Main{ s -> Block#
      .let[Opt[Int]] res = { Opt[Int]
        #{opt -> opt.match{.some(_) -> opt, .empty -> Opt#[Int]9001}}
        }
      .assert{res! == 9001}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void personFactory() { ok(new Res("Bob", "", 0), "test.Ex", """
    package test
    FPerson:F[Str,UInt,Person]{ name, age -> Person:{
      .name: Str -> name,
      .age: UInt -> age,
      }}
    Ex:Main{
      #(sys) -> FIO#sys.println(this.name(this.create)),
    
      .create: Person -> FPerson#("Bob", 24u),
      .name(p: Person): Str -> p.name,
      }
    """, Base.mutBaseAliases);}

  @Test void codegenCloneMethodBodiesForAbstractMdfOverloadsRead() { ok(new Res(), "test.Ex", """
    package test
    alias base.Void as Void, alias base.Main as Main,
    Ex:Main { _ -> Void }
    
    OhNo: { #: Void -> Fun#{v -> v} }
    Fun: { #(s: read _Sink[Void, Void]): Void -> s#Void }
    _Sink[T,R]: {
      read #(x: T): Void,
      mut  #(x: T): Void,
      }
    """); }

  @Test void codegenCloneMethodBodiesForAbstractMdfOverloadsMut() { ok(new Res(), "test.Ex", """
    package test
    alias base.Void as Void, alias base.Main as Main,
    Ex:Main { _ -> Void }
    
    OhNo: { #: Void -> Fun#{v -> v} }
    Fun: { #(s: mut _Sink[Void, Void]): Void -> s#Void }
    _Sink[T,R]: {
      read #(x: T): Void,
      mut  #(x: T): Void,
      }
    """); }

  @Test void errorKToObj() { ok(new Res("", "Program crashed with: \"whoops\"", 1), "test.Test", """
    package test
    Test: Main{ _ -> A#(Error.msg "whoops") }
    A:{ #(x: Str): Void -> Void }
    """, Base.mutBaseAliases); }

  @Test void noMagicWithManualCapability() { ok(new Res("", "No magic code was found[###]", 1), "test.Test", """
    package test
    Test: Main{_ -> mut FakeIO.println("oh no")}
    FakeIO: IO{
      .print(msg) -> Magic!,
      .println(msg) -> Magic!,
      .printErr(msg) -> Magic!,
      .printlnErr(msg) -> Magic!,
      }
    """, Base.mutBaseAliases); }

  @Test void lazyCall() { ok(new Res("hey", "", 0), "test.Test", """
    package test
    Test: Main{sys -> Block#
      .let[Void] x = {FIO#sys.println("hey")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void lazyCallEarlyExit() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test: Main{sys -> Block#
      .if {True} .return {Void}
      .let[Void] x = {FIO#sys.println("hey")}
      .return {Void}
      }
    """, Base.mutBaseAliases); }
  @Test void eagerCallEarlyExit() { ok(new Res("hey", "", 0), "test.Test", """
    package test
    Test: Main{sys -> Block#
      .if {True} .return {Void}
      .letIso[iso Rez] x = (Block#(base.Debug#[Str]"hey", iso Rez))
      .return {Void}
      }
    Rez: {}
    """, Base.mutBaseAliases); }
}
