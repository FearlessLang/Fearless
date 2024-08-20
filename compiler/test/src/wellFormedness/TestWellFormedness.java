package wellFormedness;

import failure.CompileError;
import id.Mdf;
import main.Main;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestWellFormedness {
  void ok(String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    var res = new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    var isWellFormed = res.isEmpty();
    assertTrue(isWellFormed, res.map(Object::toString).orElse(""));
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    try {
      var error = new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
      if (error.isEmpty()) { Assertions.fail("Did not fail"); }
      Err.strCmp(expectedErr, error.map(Object::toString).orElseThrow());
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void sealedOutsidePkg() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{ .m1: A }
    """, """
    package b
    C:a.A{ .m1: a.A }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNested() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.B/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{ .m1: A }
    """, """
    package b
    C:a.B{ this }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNoOverrides() { fail("""
    In position [###]/Dummy1.fear:2:2
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    C:a.A{}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNoOverridesInline() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    Test: {#: C -> C:a.B{}}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgInline() { fail("""
    In position [###]/Dummy1.fear:2:18
    [E35 sealedCreation]
    The sealed trait a.B/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B: A{.m1: A}
    """, """
    package b
    Test: {#: C -> C: a.B{this}}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgMultiImpl() { fail("""
    In position [###]/Dummy1.fear:2:18
    [E34 conflictingSealedImpl]
    A sealed trait from another package may not be composed with any other traits.
    conflicts:
    ([###]/Dummy0.fear:3:2) a.A/0
    ([###]/Dummy0.fear:4:3) a.B/0
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B: A{.m1: A}
    """, """
    package b
    Test: {#: C -> C: a.A,a.B{}}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgNestedNoOverrides() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    Foo: {#: C -> C:a.B{}}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgInlineExplicit() { fail("""
    In position [###]/Dummy1.fear:4:17
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo }
    B:A{ .a -> {} }
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> a.A{ .a: Foo -> Foo }.a
      }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgInlineInferred() { fail("""
    In position [###]/Dummy1.fear:4:17
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be implemented in a different package (b).
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo }
    B:A{ .a -> {} }
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> a.A{ Foo }.a
      }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgConstructor() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo -> {} }
    A':{ #: A -> {} }
    B:A{}
    Foo:{}
    """, """
    package b
    alias a.A' as A', alias a.Foo as Foo,
    C:{
      .foo(): Foo -> A'#.a
      }
    """, """
    package base
    Sealed:{}
    """); }

  @Test void noSealedMultiOutsideOfPkg() {fail("""
    In position [###]/Dummy1.fear:2:14
    [E34 conflictingSealedImpl]
    A sealed trait from another package may not be composed with any other traits.
    conflicts:
    ([###]/Dummy0.fear:3:3) base.A/0
    ([###]/Dummy1.fear:3:3) a.C/0
    """, """
    package base
    Sealed: {}
    A: Sealed{}
    """, """
    package a
    Foo: {#: C -> base.A,C{}}
    C: {}
    """);}

  // This well formedness requirement doesn't make sense because it only considers boxes (but not functions that do not capture)
//  @Test void noMutHygType1() { fail("""
//    In position [###]/Dummy0.fear:2:7
//    [E40 mutCapturesHyg]
//    The type mut a.A[read a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
//    """, """
//    package a
//    A[X]:{ .no: mut A[read A[X]] }
//    """); }
//  @Test void noMutHygType2() { fail("""
//    In position [###]/Dummy0.fear:2:7
//    [E40 mutCapturesHyg]
//    The type mut a.A[lent a.A[imm X]] is not valid because a mut lambda may not capture hygienic references.
//    """, """
//    package a
//    A[X]:{ .no: mut A[lent A[X]] }
//    """); }
  @Test void noMutHygType3() { ok("""
    package a
    A[X]:{ .no: mut A[X] }
    """); }
  @Test void noMutHygType4() { ok("""
    package a
    A[X]:{ .no: read A[read A[X]] }
    """); }

  @Test void noRecMdfInImplements() { fail("""
    In position [###]/Dummy0.fear:3:5
    [E27 recMdfInImpls]
    Invalid modifier for recMdf Y.
    recMdf may not be used in the list of implemented traits.
    """, """
    package base
    A[X]:{}
    B[Y]:A[recMdf Y]{}
    """); }

  @Test void allowPrivateLambdaUsageWithinPkg() { ok("""
    package base.caps
    Good:{ .ok: mut base.caps._RootCap -> {} }
    """, """
    package base.caps
    _RootCap:{}
    """); }
  @Test void noPrivateLambdaUsageOutsidePkg() { fail("""
    In position [###]/Dummy0.fear:2:41
    [E48 privateTraitImplementation]
    The private trait base.caps._RootCap/0 cannot be implemented outside of its package.
    """, """
    package test
    Evil:{ .break: mut base.caps._RootCap -> {} }
    """, """
    package base.caps
    _RootCap:{}
    """); }
  @Test void allowPrivateMethCallsWithinTrait() { ok("""
    package test
    Good:{
      .foo: Bar -> this._bar,
      ._bar: Bar -> Bar,
      }
    Bar:{}
    """); }
  @Test void allowPrivateMethCallsWithinSubTraits() { ok("""
    package test
    Good:{
      ._bar: Bar -> Bar,
      }
    Good':Good{
      .foo: Bar -> this._bar,
      }
    Bar:{}
    """); }
  @Test void noPrivateMethodCallsOutsideOfTrait() { fail("""
    In position [###]/Dummy0.fear:3:18
    [E47 privateMethCall]
    The private method ._bar/0 cannot be called outside of a lambda that implements it.
    """, """
    package test
    Bad:{
      .foo: Bar -> Bar._bar,
      }
    Bar:{
      ._bar: Bar -> Bar,
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
  @Test void failTopLevelDeclImplInfer() { fail("""
    In position [###]/Dummy0.fear:7:15
    [E13 implInlineDec]
    Traits declared within expressions cannot be implemented. This lambda has the following invalid implementations: test.Person/0
    """, """
    package test
    FPerson:{ #(name: Str, age: Nat): Person -> Person:{
      .name: Str -> name,
      .age: Nat -> age,
      }}
    Bad:{
      #: Person -> {}
      }
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void genericFunnelling() { ok("""
    package test
    FPerson:{ #[N](name: Str, age: N): Person[N] -> Person[N]:{
      .name: Str -> name,
      .age: N -> age,
      }}
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void genericFunnellingFresh() { ok("""
    package test
    Person[N]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: N): Person[N] -> {
      .name: Str -> name,
      .age: N -> age,
      }}
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void genericFunnellingFreshNoBody() { ok("""
    package test
    Person[N]:{ .name: Str, .age: N }
    Person2[N]:Person[N]{ .name -> this.name, .age -> this.age }
    FPerson:{ #[N](name: Str, age: N): Person[N] -> Person2[N] }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void noLentLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:17
    [E63 invalidLambdaMdf]
    mutH is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: mutH A -> {}}
    """); }
}
