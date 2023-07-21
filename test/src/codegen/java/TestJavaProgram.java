package codegen.java;

import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import utils.RunJava;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunJava.Res;

public class TestJavaProgram {
  void ok(Res expected, String entry, String... content) {
    okWithArgs(expected, entry, List.of(), content);
  }
  void okWithArgs(Res expected, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{
      throw err;
    });
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    var res = RunJava.of(new JavaProgram(java).compile(), args).join();
    Assertions.assertEquals(expected, res);
  }

  void fail(String expectedErr, String entry, String... content) {
    failWithArgs(expectedErr, entry, List.of(), content);
  }
  void failWithArgs(String expectedErr, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    try {
      var java = new JavaCodegen(inferred).visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
      var res = RunJava.of(new JavaProgram(java).compile(), args).join();
      Assertions.fail("Did not fail. Got: "+res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void emptyProgram() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> {} }
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, "power level less than 9000", { Void }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (True && True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (True && False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (False && False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (True || True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (True || False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (False || True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (False || False) .str, { Void }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#("hi".len() > 9000u, { Void }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, -123456789 .str, { Void }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, (0 - 2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    alias base.Void as Void,
    Test:Main{ _ -> Assert#(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  @Test void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main{ s -> s
      .use[base.caps.IO](base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Disabled
  @Test void printlnInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main{ s -> s
      .use(base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }
  @Test void print() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .block
      .do{ io.print("Hello") }
      .return{ io.print(", World!") }
      }
    """); }
  @Test void printlnErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ io.printlnErr("Hello, World!") }
      }
    """); }
  @Test void printErr() { ok(new Res("", "Hello, World!", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .block
      .do{ io.printErr("Hello") }
      .return{ io.printErr(", World!") }
      }
    """); }
  @Test void printlnShareLent() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ Usage#io }
      }
    Usage:{
      #(io: lent IO): Void -> io.println("Hello, World!"),
      }
    """); }
  @Test void printlnShareLentCapture() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ lent Usage{ io }# }
      }
    Usage:{
      lent .io: lent IO,
      lent #: Void -> this.io.println("Hello, World!"),
      }
    """); }
  @Test void printlnShareCaptureIsoPod1() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.caps.IsoPod as IsoPod,
    alias base.caps.System as System, alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] _ = IO' // just to have another alias
      .block
      .var[mut IsoPod[System[Void, base.caps.RootCap]]] s' = { IsoPod#[System[Void, base.caps.RootCap]](s.share[Void]) }
      .return{ Usage'#(s'*) # }
      }
    Usage':{ #(s: mut System[Void, base.caps.RootCap]): mut Usage -> { s } }
    Usage:{
      mut .s: mut System[Void, base.caps.RootCap],
      mut #: Void -> this.s
        .use[IO] io = IO'
        .return{ io.println("Hello, World!") },
      }
    """); }
  @Test void printlnShareCaptureIsoPod2() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.caps.IsoPod as IsoPod,
    alias base.caps.System as System, alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] _ = IO' // just to have another alias
      .block
      .var[mut IsoPod[System[Void, base.caps.RootCap]]] s' = { IsoPod#[System[Void, base.caps.RootCap]](s.share[Void]) }
      .return{ Usage'#(s'*) # }
      }
    Usage':{ #(s: lent System[Void, base.caps.RootCap]): lent Usage -> { s } }
    Usage:{
      lent .s: lent System[Void, base.caps.RootCap],
      lent #: Void -> this.s
        .use[IO] io = IO'
        .return{ io.println("Hello, World!") },
      }
    """); }
  @Test void printlnShareCaptureIso() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.caps.IsoPod as IsoPod,
    alias base.caps.System as System, alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use[IO] _ = IO' // just to have another alias
      .block
      .return{ Usage'#(s.share[Void]) # }
      }
    Usage':{ #(s: mut System[Void, base.caps.RootCap]): mut Usage -> { s } }
    Usage:{
      mut .s: mut System[Void, base.caps.RootCap],
      mut #: Void -> this.s
        .use[IO] io = IO'
        .return{ io.println("Hello, World!") },
      }
    """); }

  @Disabled
  @Test void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO' as IO',
    Test:Main{ s -> s
      .use io = IO'
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
    Test:Main{ _ -> Assert#((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Test void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main{ _ -> Assert#((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.var[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Test void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main{ _ -> mut Block[Void]
      .var(n = { Ref#[Int]5 })
      .do{ Assert#(n.swap(6) == 5) }
      .do{ Assert#(n* == 6) }
      .return{{}}
      }
    """); }

  static String cliArgsOrElseGet = """
    package test
    MyApp:Main{ s -> s
      .use[IO] io = IO'
      .use[Env] env = Env'
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) || { (this.errMsg((args.head).isSome)) * },
      .errMsg(retCounter: Bool): mut Ref[Str] -> Do#
        .var res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .var counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Yeet#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Yeet#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
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
    MyApp:Main{ s -> s
      .use[IO] io = IO'
      .use[Env] env = Env'
      .return{ io.println(ImmMain#(env.launchArgs)) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) | (this.errMsg(args.head.isSome)*),
      .errMsg(retCounter: Bool): mut Ref[Str] -> Do#
        .var res = { Ref#[mut Ref[Str]](Ref#[Str]"Sad") }
        .var counter = { Count.int(42) }
        .do{ res* := "mutability!" }
        .do{ Yeet#(counter++) }
        .if{ False }.return{ Ref#[Str]"Short cut" }
        .if{ True }.do{ Yeet#[Int](counter *= 9000) } // MY POWER LEVELS ARE OVER 9000!!!!!!
        .if{ True }.do{ res* := "moar mutability" }
        .if{ retCounter.not }.return{ res* }
        .return{ Ref#(counter*.str) }
      }
    """;
  @Test void cliArgs2a() { okWithArgs(new Res("moar mutability", "", 0), "test.MyApp", List.of(), getCliArgsOrElse, Base.mutBaseAliases); }
  @Test void cliArgs2b() { okWithArgs(new Res("387000", "", 0), "test.MyApp", List.of(
    "hi"
  ), getCliArgsOrElse, Base.mutBaseAliases); }
  @Test void cliArgs2c() { okWithArgs(new Res("bye", "", 0), "test.MyApp", List.of(
    "hi",
    "bye"
  ), getCliArgsOrElse, Base.mutBaseAliases); }

  @Test void findClosestInt() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LList[Int] + 35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LList[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
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
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[Int] closest' = { (ns.get(0u))! }
        .var[mut Ref[Int]] closest = { Ref#(closest') }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#(ns.head!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMut3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.head, ns.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: Opt[Int], t: LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutLList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#(LListMut#[Int]35 + 52 + 84 + 14, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut LListMut[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          h, t -> h.match{
            .none -> {},
            .some(n) -> (target - n).abs < ((target - (closest*)).abs) ? {
              .then -> closest := n,
              .else -> self#(t.head, t.tail)
              }
            }
          }#(ns.tail.head, ns.tail.tail) }
        .return{ closest* }
      }
    Closest':{ mut #(h: mut Opt[Int], t: mut LListMut[Int]): Void }
    """, Base.mutBaseAliases); }
  @Test void findClosestIntMutWithMutList() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[Int] closest = { Closest#((LListMut#[Int]35 + 52 + 84 + 14).list, 49) }
      .return{ Assert#(closest == 52, closest.str, {{}}) }
      }
    Closest:{
      #(ns: mut List[Int], target: Int): Int -> Do#
        .do{ Assert#(ns.isEmpty.not, "empty list :-(", {{}}) }
        .var[mut Ref[Int]] closest = { Ref#((ns.get(0u))!) }
        .do{ mut Closest'{ 'self
          i -> ns.get(i).match{
            .none -> {},
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

  @Test void llistIters() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[LList[Int]] l1 = { LList#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void llistMutItersIterRead() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut LListMut[Int]] l1 = { LListMut#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#((l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140})
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void llistMutItersIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut LListMut[Int]] l1 = { LListMut#[Int]35 + 52 + 84 + 14 }
      .do{ Assert#(l1.head! == (l1.iterMut.next!), "sanity", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#(l1.iterMut
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterRead() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do.hyg
      .var[read List[Int]] l1 = { (LListMut#[Int]35 + 52 + 84 + 14).list }
      .do{ Assert#(l1.look(0u)! == (l1.iter.next!), "sanity", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iter.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#(l1.iter
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .find{n -> n == 84}
                      .isSome,
        "filter", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .count == 2u,
        "count", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .list
                      .len == 2u,
        "toList", {{}})}
      .do{ Assert#(l1.iter
                      .filter{n -> n > 50}
                      .llistMut
                      .len == 2u,
        "toLListMut", {{}})}
      .do{ Assert#(l1.iter
                    .flatMap{n -> (List#(n, n, n)).iter}
                    .map{n -> n * 10}
                    .str({n -> n.str}, ";") == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert#(Sum.int(l1.iter.map{n -> n+0}) == 185, "sum int", {{}})}
      .do{ Assert#(Sum.uint(l1.iter.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .do{ Assert#(Sum.float(l1.iter.map{n -> n.float}) == 185.0, "sum float", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }
  @Test void listIterMut() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut List[Int]] l1 = { (LListMut#[Int]35 + 52 + 84 + 14).list }
      .assert({ l1.get(0u)! == (l1.iterMut.next!) }, "sanity") // okay, time to use this for new tests
      .do{ Assert#((l1.iterMut.find{n -> n > 60})! == 84, "find some", {{}}) }
      .do{ Assert#((l1.iterMut.find{n -> n > 100}).isNone, "find empty", {{}}) }
      .do{ Assert#(l1.iterMut
                      .map{n -> n * 10}
                      .find{n -> n == 140}
                      .isSome,
        "map", {{}})}
      .do{ Assert#(l1.iterMut
                      .filter{n -> n > 50}
                      .find{n -> n == 84}
                      .isSome,
        "filter", {{}})}
      .do{ Assert#(l1.iterMut
                      .filter{n -> n > 50}
                      .count == 2u,
        "count", {{}})}
      .do{ Assert#(l1.iterMut
                      .filter{n -> n > 50}
                      .list.len == 2u,
        "toList", {{}})}
      .do{ Assert#(l1.iterMut
                      .filter{n -> n > 50}
                      .llistMut.len == 2u,
        "toLListMut", {{}})}
      .do{ Assert#(l1.iterMut
                    .flatMap{n -> (List#(n, n, n)).iterMut}
                    .map{n -> n * 10}
                    .str({n -> n.str}, ";") == "350;350;350;520;520;520;840;840;840;140;140;140",
        "flatMap", {{}})}
      .do{ Assert#(Sum.int(l1.iterMut) == 185, "sum int", {{}})}
      .do{ Assert#(Sum.uint(l1.iterMut.map{n -> n.uint}) == 185u, "sum uint", {{}})}
      .do{ Assert#(Sum.float(l1.iterMut.map{n -> n.float}) == 185.0, "sum float", {{}})}
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void absIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert#(5 .abs == 5) }
    """, Base.mutBaseAliases); }
  @Test void absIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert#(0 .abs == 0) }
    """, Base.mutBaseAliases); }
  @Test void absIntNeg() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert#(-5 .abs == 5) }
    """, Base.mutBaseAliases); }

  @Test void absUIntPos() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert#(5u .abs == 5u) }
    """, Base.mutBaseAliases); }
  @Test void absUIntZero() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Assert#(0u .abs == 0u) }
    """, Base.mutBaseAliases); }

  @Test void isoPod1() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .return{ Assert#(Usage#(a*) == 0) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod2() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ a.next(MutThingy'#(Count.int(5))) }
      .return{ Assert#(Usage#(a*) == 5) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }
  @Test void isoPod3() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .var[mut IsoPod[MutThingy]] a = { IsoPod#[MutThingy](MutThingy'#(Count.int(0))) }
      .do{ Yeet#(a.mutate{ mt -> Yeet#(mt.n++) }) }
      .return{ Assert#(Usage#(a*) == 1) }
      }
    Usage:{ #(m: iso MutThingy): Int -> (m.n*) }
    MutThingy:{ mut .n: mut Count[Int] }
    MutThingy':{ #(n: mut Count[Int]): mut MutThingy -> { n }  }
    """, Base.mutBaseAliases); }

  @Test void envFromRootAuth() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> s
      .use[base.caps.IO] io = base.caps.IO'
      .use[base.caps.Env] env = base.caps.Env'
      .return{ io.println(env.launchArgs.iter.str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }
  @Test void envFromIO() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> s
      .use[base.caps.IO] io = base.caps.IO'
      .return{ io.println((base.caps.EnvFromIO.build(io)).launchArgs.iter.str({arg -> arg.str}, " ")) }
      }
    """, Base.mutBaseAliases); }
  @Test void envFromBaseSys() { okWithArgs(new Res("hi bye", "", 0), "test.Test", List.of("hi", "bye"), """
    package test
    Test:Main{ s -> s
      .use[base.caps.IO] io = base.caps.IO'
      .block
      .var[mut System[Str, base.caps.IO]] ioSys = { s.share[Str, base.caps.IO](IO') }
      .return{ io.println(ioSys
        .use[base.caps.Env] env = base.caps.EnvFromIO
        .return{ env.launchArgs.iter.str({arg -> arg.str}, " ") }
        )}
      }
    """, Base.mutBaseAliases); }

  @Test void intExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ io.println(5 ** 5u .str) }
      }
    """, Base.mutBaseAliases); }
  @Test void uintExp() { ok(new Res("3125", "", 0), "test.Test", """
    package test
    Test:Main{ s -> s
      .use[IO] io = IO'
      .return{ io.println(5u ** 5u .str) }
      }
    """, Base.mutBaseAliases); }

  @Test void negativeNums() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .do{ Assert#(-5 == -5, "id", {{}}) }
      .do{ Assert#((-5 - -5) == 0, "subtraction 1", {{}}) }
      .do{ Assert#((-5 - 5) == -10, "subtraction 2", {{}}) }
      .do{ Assert#((-5 + 3) == -2, "addition 1", {{}}) }
      .do{ Assert#((-5 + 7) == 2, "addition 2", {{}}) }
      .do{ Assert#((5 + -7) == -2, "addition 3", {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void floats() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main{ _ -> Do#
      .do{ Assert#(-5.0 == -5.0, "id (neg)", {{}}) }
      .do{ Assert#((-5.0 - -5.0) == 0.0, "subtraction 1", {{}}) }
      .do{ Assert#((-5.0 - 5.0) == -10.0, "subtraction 2", {{}}) }
      .do{ Assert#((-5.0 + 3.0) == -2.0, "addition 1", {{}}) }
      .do{ Assert#((-5.0 + 7.0) == 2.0, "addition 2", {{}}) }
      .do{ Assert#((5.0 + -7.0) == -2.0, "addition 3", {{}}) }
      // pos
      .do{ Assert#(1.0 == 1.0, "id", {{}}) }
      .do{ Assert#(1.0 + 3.5 == 4.5, "addition 1 (pos)", {{}}) }
      .do{ Assert#((1.0).str == "1.0", "str", {{}}) }
      .do{ Assert#((5.0 / 2.0) == 2.5, (5.0 / 2.0).str, {{}}) }
      .return{{}}
      }
    """, Base.mutBaseAliases); }

  @Test void shouldPeekIntoIsoPod() { ok(new Res("""
    peek: help, i'm alive
    consume: help, i'm alive
    """.strip(), "", 0), "test.Test", """
    package test
    Test:Main{ s -> s
      .use[IO] io = IO'
      .block
      .var s1 = { IsoPod#[Str]"help, i'm alive" }
      .do{ PrintMsg#(s.share(IO'), s1) }
      .return{ io.println("consume: " + (s1.consume)) }
      }
    PrintMsg:{
      #(s: mut System[Void, IO], msg: read IsoPod[Str]): Void -> msg.peek{
        .some(str) -> s
          .use[IO] io = base.caps.IOFromIO
          .return{ io.println("peek: " + str) },
        .none -> Void
        }
      }
    """, Base.mutBaseAliases); }
  @Test void shouldPeekIntoIsoPodHyg() { ok(new Res("""
    peek: help, i'm alive
    consume: help, i'm alive
    """.strip(), "", 0), "test.Test", """
    package test
    Test:Main{ s -> s
      .use[IO] io = IO'
      .block
      .var s1 = { IsoPod#[Str]"help, i'm alive" }
      .do{ s1.peekHyg{
        .some(str) -> io.println("peek: " + str),
        .none -> Void
        }}
      .return{ io.println("consume: " + (s1.consume)) }
      }
    """, Base.mutBaseAliases); }

//  @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
//    package test
//    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
//    alias base.Ref as Ref, alias base.Int as Int,
//    Test:Main{ _ -> Assert#((Ref#[Int]5)* == 5, { Void }) }
//    """); }
}
