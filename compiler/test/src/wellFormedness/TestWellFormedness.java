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
    The sealed trait a.A/0 cannot be created in a different package (b).
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
    The sealed trait a.A/0 cannot be created in a different package (b).
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
  @Test void sealedOutsidePkgNoOverrides() { ok("""
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
  @Test void sealedOutsidePkgNestedNoOverrides() { ok("""
    package a
    alias base.Sealed as Sealed,
    A:Sealed{}
    B:A{}
    """, """
    package b
    C:a.B{}
    """, """
    package base
    Sealed:{}
    """); }
  @Test void sealedOutsidePkgInlineExplicit() { fail("""
    In position [###]/Dummy1.fear:4:17
    [E35 sealedCreation]
    The sealed trait a.A/0 cannot be created in a different package (b).
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
    The sealed trait a.A/0 cannot be created in a different package (b).
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
    FPerson:{ #(name: Str, age: UInt): Person -> Person:{
      .name: Str -> name,
      .age: UInt -> age,
      }}
    Ex:{
      .create: Person -> FPerson#(Bob, TwentyFour),
      .name(p: Person): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    UInt:{} TwentyFour:UInt{}
    """); }
  @Test void failTopLevelDeclImplInfer() { fail("""
    In position [###]/Dummy0.fear:7:15
    [E13 implInlineDec]
    Traits declared within expressions cannot be implemented. This lambda has the following invalid implementations: test.Person/0
    """, """
    package test
    FPerson:{ #(name: Str, age: UInt): Person -> Person:{
      .name: Str -> name,
      .age: UInt -> age,
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
    UInt:{} TwentyFour:UInt{}
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
    UInt:{} TwentyFour:UInt{}
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
    UInt:{} TwentyFour:UInt{}
    """); }
  @Test void genericFunnellingFreshNoBody() { ok("""
    package test
    Person[N]:{ .name: Str, .age: N }
    Person2[N]:Person[N]{ .name -> this.name, .age -> this.age }
    FPerson:{ #[N](name: Str, age: N): Person[N] -> Person2[N] }
    """, """
    package test
    Str:{} Bob:Str{}
    UInt:{} TwentyFour:UInt{}
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
    UInt:{} TwentyFour:UInt{}
    """); }

  @Test void noLentLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:17
    [E62 invalidLambdaMdf]
    lent is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: lent A -> {}}
    """); }
  @Test void noReadOnlyLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:21
    [E62 invalidLambdaMdf]
    readOnly is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: readOnly A -> {}}
    """); }
  @Test void noReadImmLambdaCreation() { fail("""
    In position [###]/Dummy0.fear:2:21
    [E62 invalidLambdaMdf]
    read/imm is not a valid modifier for a lambda.
    """, """
    package test
    A: {#: read/imm A -> {}}
    """); }
}
