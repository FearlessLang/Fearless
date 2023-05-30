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
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
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
    Void:{}
    Test:Main[Void]{ _, _ -> {} }
    """);}

  @Test void assertTrue() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(True, { Void }) }
    """);}
  @Test void assertFalse() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, { Void }) }
    """);}
  @Test void assertFalseMsg() { ok(new Res("", "power level less than 9000", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, "power level less than 9000", { Void }) }
    """);}

  @Test void falseToStr() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, Foo.bs(False), { Void }) }
    Foo:{ .bs(b: base.Bool): base.Str -> b.str }
    """);}
  @Test void trueToStr() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, Foo.bs(True), { Void }) }
    Foo:{ .bs(s: base.Stringable): base.Str -> s.str }
    """);}

  @Test void binaryAnd1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True && True) .str, { Void }) }
    """);}
  @Test void binaryAnd2() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True && False) .str, { Void }) }
    """);}
  @Test void binaryAnd3() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False && False) .str, { Void }) }
    """);}
  @Test void binaryOr1() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True || True) .str, { Void }) }
    """);}
  @Test void binaryOr2() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (True || False) .str, { Void }) }
    """);}
  @Test void binaryOr3() { ok(new Res("", "True", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False || True) .str, { Void }) }
    """);}
  @Test void binaryOr4() { ok(new Res("", "False", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (False || False) .str, { Void }) }
    """);}

  @Test void conditionals1() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(420 > 9000, { Void }) }
    """);}
  @Test void conditionals2() { ok(new Res("", "Assertion failed :(", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#("hi".len() > 9000u, { Void }) }
    """);}

  @Test void longToStr() { ok(new Res("", "123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 123456789 .str, { Void }) }
    """);}
  @Test void longLongToStr() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775807 .str, { Void }) }
    """);}

  @Test void veryLongLongToStr() { ok(new Res("", "9223372036854775808", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775808u .str, { Void }) }
    """);}
  @Test void veryLongLongIntFail() { fail("""
    [E31 invalidNum]
    The number 9223372036854775808 is not a valid Int
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 9223372036854775808 .str, { Void }) }
    """);}
  @Test void veryLongLongUIntFail() { fail("""
    [E31 invalidNum]
    The number 10000000000000000000000u is not a valid UInt
    """, "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, 10000000000000000000000u .str, { Void }) }
    """);}
  @Test void negativeToStr() { ok(new Res("", "-123456789", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, -123456789 .str, { Void }) }
    """);}

  @Test void addition() { ok(new Res("", "7", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (5 + 2) .str, { Void }) }
    """);}
  @Test void subtraction() { ok(new Res("", "3", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (5 - 2) .str, { Void }) }
    """);}
  @Test void subtractionNeg() { ok(new Res("", "-2", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, (0 - 2) .str, { Void }) }
    """);}
  @Test void subtractionUnderflow() { ok(new Res("", "9223372036854775807", 1), "test.Test", """
    package test
    alias base.Main as Main, alias base.Assert as Assert, alias base.True as True, alias base.False as False,
    Void:{}
    Test:Main[Void]{ _, _ -> Assert#(False, ((0 - 2) - 9223372036854775807) .str, { Void }) }
    """);}

  @Test void println() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main[Void]{ _, s -> s
      .use[base.caps.IO](base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Disabled
  @Test void printlnInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    Test:Main[Void]{ _, s -> s
      .use(base.caps.IO', { io, s' -> s'.return{ io.println "Hello, World!" } })
      }
    """);}
  @Test void printlnSugar() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ _, s -> s
      .use[IO] io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }
  @Test void printlnSugarMultiCap() { ok(new Res("yeet\nHello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO as IO, alias base.caps.IO' as IO',
    Test:Main[Void]{ _, s -> s
      .use[IO] io1 = IO'
      .use[IO] io2 = IO'
      .do{ io2.println("yeet") }
      .return{ io1.println("Hello, World!") }
      }
    """); }

  @Disabled
  @Test void printlnSugarInferUse() { ok(new Res("Hello, World!", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    alias base.caps.IO' as IO',
    Test:Main[Void]{ _, s -> s
      .use io = IO'
      .return{ io.println("Hello, World!") }
      }
    """); }

  @Test void nestedPkgs() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:base.Main[test.foo.Bar]{ _, _ -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """); }

    @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main[Void]{ _, _ -> Assert#((GetRef#5)* == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  @Test void ref2() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
    alias base.Ref as Ref, alias base.Int as Int,
    Test:Main[Void]{ _, _ -> Assert#((GetRef#5).swap(6) == 5, { Void }) }
    GetRef:{ #(n: Int): mut Ref[Int] -> Ref#n }
    """); }
  // TODO: loops if we give a broken value like `.var[mut Ref[Int]](n = Ref#5)` (not a ReturnStmt)
  @Test void ref3() { ok(new Res("", "", 0), "test.Test", """
    package test
    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert, alias base.Block as Block,
    alias base.Ref as Ref, alias base.Int as Int, alias base.ReturnStmt as ReturnStmt,
    Test:Main[Void]{ _, _ -> mut Block[Void]
      .var(n = { Ref#[Int]5 })
      .do{ Assert#(n.swap(6) == 5) }
      .do{ Assert#(n* == 6) }
      .return{{}}
      }
    """); }

  static String cliArgsOrElseGet = """
    package test
    MyApp:Main[Void]{ args, s -> s
      .use[IO] io = IO'
      .return{ io.println(ImmMain#args) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) || mut Box[Str]{ (this.errMsg((args.head).isSome)) * },
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
    MyApp:Main[Void]{ args, s -> s
      .use[IO] io = IO'
      .return{ io.println(ImmMain#args) }
      }
    ImmMain:{
      #(args: LList[Str]): Str -> args.get(1u) | (this.errMsg((args.head).isSome)) *,
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

//  @Test void ref1() { ok(new Res("", "", 0), "test.Test", """
//    package test
//    alias base.Main as Main, alias base.Void as Void, alias base.Assert as Assert,
//    alias base.Ref as Ref, alias base.Int as Int,
//    Test:Main[Void]{ _, _ -> Assert#((Ref#[Int]5)* == 5, { Void }) }
//    """); }
}
