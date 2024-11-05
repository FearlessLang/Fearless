package codegen.proto;

import codegen.MIRInjectionVisitor;
import codegen.go.GoCodegen;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.TsT;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestProtoCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.immBaseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var gen = new ProtoCodegen();
    mir.pkgs().forEach(gen::writePackage);
//    var res = new GoCodegen(mir).visitProgram(new Id.DecId(entry, 0));
//    Err.strCmp(expected, res.toString());
  }

  @Test void capturing() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    FPerson: { #(age: Num): mut Person -> mut Person: {
      read .age: Num -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.age,
      }
    Num: {}
    FortyTwo: Num
    """);}

  @Test void stuck() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    Usage: {
      #: Num -> this#,
      }
    Num: {}
    FortyTwo: Num
    """);}

  @Test void capturingDeep() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    Person: {
      read .age: Num,
      mut .wrap: mut Person -> {'self
       .age -> this.age.plus1,
       .wrap -> {'topLevelWrapped
         .age -> self.age.plus1,
         }
       },
      }
    FPerson: { #(age: Num): mut Person -> {'original
      .age -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.wrap.age,
      }
    Num: {
      .plus1: Num,
      }
    FortyTwo: Num{ .plus1 -> FortyThree }
    FortyThree: Num{ .plus1 -> FortyFour }
    FortyFour: Num{ .plus1 -> this.plus1 }
    """);}

  @Test void helloWorld() { ok("""
    """, "fake.Fake", true, """
    package test
    alias base.Main as Main,
    Test: Main{_ -> "Hello, World!"}
    """); }

  @Test void fib42() { ok("""
    """, "fake.Fake", false, """
    package test
    alias base.Main as Main, alias base.Nat as Nat,
    Test: Main{ _ -> Fib#(10).str }
    Fib: {
      #(n: Nat): Nat -> n <= 1 ? {
        .then -> n,
        .else -> this#(n - 1) + (this#(n - 2))
        }
      }
    """, """
    package base
    Main: {#(args: LList[Str]): Str}
    LList[E]: {}
    Str: {}
    Magic:{ ![R:imm]: R -> this! }
    Sealed:{}
    """, """
    package base
    
    Int: Sealed,_MathOps[imm Int],_IntOps[imm Int]{}
    Nat: Sealed,_MathOps[imm Nat],_IntOps[imm Nat]{}
    Float: Sealed,_MathOps[imm Float]{
      .round: Int,
      .ceil: Int,
      .floor: Int,
      **(n: Float): Float, // pow
      .isNaN: Bool,
      .isInfinite: Bool,
      .isPosInfinity: Bool,
      .isNegInfinity: Bool,
      }
    
    _MathOps[T]: Sealed{
      read .int: Int,
      read .nat: Nat,
      read .float: Float,
      read .str: Str,
      +(n: T): T,
      -(n: T): T,
      *(n: T): T,
      /(n: T): T,
      %(n: T): T,
      .abs: T,
    
      // Comparisons
      >(n: T): Bool,
      <(n: T): Bool,
      >=(n: T): Bool,
      <=(n: T): Bool,
      ==(n: T): Bool,
      }
    _IntOps[T]: Sealed{
      // bitwise
      .shiftLeft(n: T): T,
      .shiftRight(n: T): T,
      .xor(n: T): T,
      .bitwiseAnd(n: T): T,
      .bitwiseOr(n: T): T,
    
      **(n: Nat): T, // pow
      }
    
    // Fake concrete type for all numbers. The real implementation is generated at code-gen.
    _IntInstance: Int{
      .int -> Magic!,
      .nat -> Magic!,
      .float -> Magic!,
      .str -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
    
      // bitwise
      .shiftLeft(n) -> Magic!,
      .shiftRight(n) -> Magic!,
      .xor(n) -> Magic!,
      .bitwiseAnd(n) -> Magic!,
      .bitwiseOr(n) -> Magic!,
    
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    _NatInstance: Nat{
      .int -> Magic!,
      .nat -> Magic!,
      .float -> Magic!,
      .str -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
    
      // bitwise
      .shiftLeft(n) -> Magic!,
      .shiftRight(n) -> Magic!,
      .xor(n) -> Magic!,
      .bitwiseAnd(n) -> Magic!,
      .bitwiseOr(n) -> Magic!,
    
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    _FloatInstance: Float{
      .int -> Magic!,
      .nat -> Magic!,
      .float -> Magic!,
      .str -> Magic!,
      .round -> Magic!,
      .ceil -> Magic!,
      .floor -> Magic!,
      .isNaN -> Magic!,
      .isInfinite -> Magic!,
      .isPosInfinity -> Magic!,
      .isNegInfinity -> Magic!,
      +(n) -> Magic!,
      -(n) -> Magic!,
      *(n) -> Magic!,
      /(n) -> Magic!,
      %(n) -> Magic!,
      **(n) -> Magic!,
      .abs -> Magic!,
      // Comparisons
      >n -> Magic!,
      <n -> Magic!,
      >=n -> Magic!,
      <=n -> Magic!,
      ==n -> Magic!,
      }
    """, """
    package base
    _StrInstance: Str{}
    Bool: Sealed{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      ?[R:imm](f: ThenElse[R]): R,
      read .str: Str,
      }
    True: Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then(), .str -> "True" }
    False: Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else(), .str -> "False" }
    ThenElse[R:imm]: { .then: R, .else: R, }
    """); }
}
