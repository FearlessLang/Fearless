package wellFormedness;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFullWellFormedness {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    var res = new WellFormednessFullShortCircuitVisitor().visitProgram(p);
    var isWellFormed = res.isEmpty();
    assertTrue(isWellFormed, res.map(Object::toString).orElse(""));
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();

    try {
      var p = Parser.parseAll(ps, new TypeSystemFeatures());
      var error = new WellFormednessFullShortCircuitVisitor().visitProgram(p);
      if (error.isEmpty()) { Assertions.fail("Did not fail"); }
      Err.strCmp(expectedErr, error.map(Object::toString).orElseThrow());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void paramsMethCallOk() { ok("""
    package pkg1
    Opt[T]:{}
    A:{
      #[T](x: A[T]): A -> {},
      .foo(): A -> this#[read A]A
      }
    """); }

  @Test void noExplicitThisBlockId() { fail("""
    In position [###]/Dummy0.fear:2:14
    [E6 explicitThis]
    Local variables may not be named 'this'.
    """, """
    package base
    A:{ .m1: A -> {'this} }
    """); }

  @Test void noExplicitThisMethArg() { fail("""
    In position [###]/Dummy0.fear:2:4
    [E6 explicitThis]
    Local variables may not be named 'this'.
    """, """
    package base
    A:{ .foo(this: A): A }
    """); }

  @Test void disjointArgList() { fail("""
    In position [###]/Dummy0.fear:2:4
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: a
    """, """
    package base
    A:{ .foo(a: A, a: A): A }
    """); }

  @Test void disjointMethGens() { fail("""
    In position [###]/Dummy0.fear:2:4
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: T
    """, """
    package base
    A:{ .foo[T,T](a: T, b: T): A }
    """); }

  @Test void disjointDecGens() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E7 conflictingMethParams]
    Parameters on methods must have different names. The following parameters were conflicting: T
    """, """
    package base
    A[T,T]:{ .foo(a: T, b: T): A }
    """); }

  @Test void noShadowingMeths() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E17 conflictingMethNames]
    Methods may not have the same name and number of parameters. The following methods were conflicting: .a/0
    """, """
    package base
    A:{ .a: A, .a: A }
    """); }

  @Test void useUndefinedX() { fail("""
    In position [###]/Dummy0.fear:3:4
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B{ x }.argh }
    B:{ read .argh: read/imm X } // should fail because X is not defined here
    """); }
  @Test void useUndefinedIdent() { fail("""
    In position [###]/Dummy0.fear:2:33
    [E28 undefinedName]
    The identifier "b" is undefined or cannot be captured.
    """, """
    package test
    A[X]:{ .foo(x: X): X -> this.foo(b) }
    """); }

  @Test void noShadowingSelfName(){ fail("""
    In position [###]/Dummy0.fear:4:11
    [E9 shadowingX]
    'unique' is shadowing another variable in scope.
    """, """
    package test
    Foo:{
      .m1(): Foo -> Foo { 'unique
        .m1 -> {'unique}
        }
      }
    """); }

  @Test void noTopLevelSelfName() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E50 namedTopLevelLambda]
    Trait declarations may not have a self-name other than "this".
    """, """
    package test
    A:{ 'self
      .me: A -> self,
      //.meThis: A -> this
      }
    """); }
  @Test void lambdaSelfNameOk() { ok("""
    package test
    A:{
      .me: A -> {'self },
      }
    """); }

  @Test void allowTopLevelDecl() { ok("""
    package test
    FPerson:{ #(name: Str, age: Nat): Person -> Person:{
      .name: Str -> name,
      .age: Nat -> age,
      }}
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void failTopLevelDeclImpl() { fail("""
    In position [###]/Dummy0.fear:6:4
    [E13 implInlineDec]
    Traits declared within expressions cannot be implemented. This lambda has the following invalid implementations: test.Person/0
    """, """
    package test
    FPerson:{ #(name: Str, age: Nat): Person -> Person:{
      .name: Str -> name,
      .age: Nat -> age,
      }}
    Bad:Person{}
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void disjointDecsInline1() { fail("""
    [E55 conflictingDecls]
    Trait names must be unique.
    conflicts:
    ([###]/Dummy0.fear:2:0) test.A/0
    """, """
    package test
    A:{}
    B:{ #: A -> A:{} }
    """); }
  @Test void disjointDecsInline2() { fail("""
    In position [###]/Dummy0.fear:3:14
    [E2 conflictingDecl]
    This trait declaration is in conflict with other trait declarations in the same package: test.A/0
    conflicts:
    ([###]/Dummy0.fear:2:14) test.A/0
    """, """
    package test
    B:{ #: A -> A:{} }
    C:{ #: A -> A:{} }
    """); }
  @Test void disjointDecsInlineCallTwice1() { ok("""
    package test
    B:{ #: A -> A:{} }
    C1:{ #: A -> B# }
    C2:{ #: A -> B# }
    """); }
  @Test void disjointDecsInlineCallTwice2() { ok("""
    package test
    B:{ #: A -> A:{} }
    C1:{ #(b: B): A -> C2#(b#, b#) }
    C2:{ #(a1: A, a2: A): A -> a1 }
    """); }

  @Test void noShadowingOk() { ok("""
    package test
    A: {.m1(a: A): A -> {.m1(b) -> a}}
    """); }
  @Test void noShadowingParam() { fail("""
    In position [###]/Dummy0.fear:2:21
    [E9 shadowingX]
    'a' is shadowing another variable in scope.
    """, """
    package test
    A: {.m1(a: A): A -> {.m1(a) -> a}}
    """); }
//  @Test void noShadowingFixPoint() { fail("""
//    In position [###]/Dummy0.fear:2:21
//    [E9 shadowingX]
//    'a' is shadowing another variable in scope.
//    """, """
//    package test
//    L: {#(l: L): L}
//    Break: {#: L -> L{x -> L{y -> x}}#(L{y -> y})}
//    """); }

  @Test void lambdaImplementingItself() { fail("""
    In position [###]/Dummy0.fear:2:0
    [E8 cyclicImplRelation]
    Implements relations must be acyclic. There is a cycle on the trait test.A/0.
    """, """
    package test
    A: A{}
    """); }

  @Test void noLentLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:22
    [E63 invalidLambdaMdf]
    mutH is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: mutH A -> mutH A}
    """); }
  @Test void noReadOnlyLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:24
    [E63 invalidLambdaMdf]
    readH is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: readH A -> readH A}
    """); }
  @Test void noReadImmLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:33
    [E63 invalidLambdaMdf]
    read/imm is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: Void -> Void.eat(read/imm A)}
    Void: {.eat[X](a: X): Void -> {}}
    """); }

  @Test void noReadImmOnIT() {fail("""
    In position [###]/Dummy0.fear:2:4
    [E11 invalidMdf]
    The modifier 'read/imm' can only be used on generic type variables. 'read/imm' found on type read/imm test.A[]
    """, """
     package test
    A: {#: read/imm A -> this#}
    """);}

  @Test void validPosInt(){ ok("""
    package a
    A: {#: Int -> +5}
    Int: {}
    """); }
  @Test void validNegInt(){ ok("""
    package a
    A: {#: Int -> -5}
    Int: {}
    """); }

  @Test void validPosNat(){ ok("""
    package a
    A: {#: Nat -> 5}
    Nat: {}
    """); }
  @Test void invalidDecimalInt(){ ok("""
    package a
    A: {#: Float -> +5.556}
    Float: {}
    """); }
  @Test void noMultiplePointsInFloat() {fail("""
    In position [###]/Dummy0.fear:2:16
    [E59 syntaxError]
    1.2.4/0 is not a valid type name.
    """, """
    package a
    A: {#: Float -> 1.2.4}
    Float: {}
    """);}
  @Test void validString(){ ok("""
    package a
    A: {#: Str -> "Hello"}
    Str: {}
    """); }

  @Test void noFreeGensFunnelling() { fail("""
    In position [###]/Dummy0.fear:2:52
    [E56 freeGensInLambda]
    The declaration name for a lambda must include all type variables used in the lambda. The declaration name test.Person[] does not include the following type variables: N
    """, """
    package test
    FPerson:{ #[N](name: Str, age: N): Person -> Person:{
      .name: Str -> name,
      .age: N -> age,
      }}
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void nonExistentImplInline() {fail("""
    In position [###]/Dummy0.fear:3:42
    [E28 undefinedName]
    The identifier "Break" is undefined or cannot be captured.
    """, """
    package test
    A[X:mut]: {}
    BreakOuter: {#: BreakInner -> BreakInner: A[imm Break]}
    """);}
  @Test void nonExistentGenInline() {fail("""
    In position [###]/Dummy0.fear:3:49
    [E56 freeGensInLambda]
    The declaration name for a lambda must include all type variables used in the lambda. The declaration name test.BreakInner[] does not include the following type variables: Z
    """, """
    package test
    A[X:mut]: {}
    BreakOuter[Z:mut]: {#: BreakInner -> BreakInner: A[Z]}
    """);}
}
