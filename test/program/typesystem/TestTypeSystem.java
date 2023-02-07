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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTypeSystem {
  //  TODO: mut Box[read X] is not valid even after promotion
  // TODO: .m: mut Box[mdf X] must return lent Box[read Person] if mdf X becomes read X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?
  void ok(String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    inferred.typeCheck();
  }
  void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
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

  @Test void baseLib(){ ok( Base.immBaseLib); }

  // TODO: error message is wrong
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

  @Test void numbers1(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping1(){ ok( """
    package test
    alias base.Num as Num,
    A:{ .m(a: 42): Num -> a }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping2(){ fail("""
    In position [###]/Dummy0.fear:3:4
    [E23 methTypeError]
    Expected the method .m/1 to return imm 42[], got imm base.Num[].
    """, """
    package test
    alias base.Num as Num,
    A:{ .m(a: Num): 42 -> a }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping3(){ ok( """
    package test
    alias base.Num as Num,
    A:{ .a: Num }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    """, Base.immBaseLib); }
  @Test void numbersSubTyping4(){ ok( """
    package test
    alias base.Num as Num,
    A:{ .a: Num }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: Num -> this.a }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes1(){ ok( """
    package test
    alias base.Num as Num,
    A[N]:{ .count: N }
    B:A[42]{ 42 }
    C:A[Num]{ 42 }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes2(){ ok( """
    package test
    alias base.Num as Num,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 42 }
    C:A[Num]{ .count -> 56, .sum -> 3001 }
    """, Base.immBaseLib); }
  @Test void numbersGenericTypes2a(){ fail("""
    In position [###]/Dummy0.fear:4:31
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy1.fear:88:4) 43[], <=/1
    ([###]/Dummy1.fear:88:4) 42[], <=/1
    """, """
    package test
    alias base.Num as Num,
    A[N]:{ .count: N, .sum: N }
    B:A[42]{ .count -> 42, .sum -> 43 }
    """, Base.immBaseLib); }
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
  @Test void numbersSubTyping4a(){ fail("""
    In position [###]/Dummy0.fear:6:5
    [E23 methTypeError]
    Expected the method .b/0 to return imm 42[], got imm base.Num[].
    """, """
    package test
    alias base.Num as Num,
    A:{ .a: Num }
    B:A{ .a -> 42 }
    C:A{ .a -> 420 }
    D:B{ .b: 42 -> this.a }
    """, Base.immBaseLib); }
  @Test void twoNums(){ ok( """
    package test
    alias base.Num as Num,
    A:{ .m(a: 56, b: 12): Num -> b+a }
    """, Base.immBaseLib); }

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
