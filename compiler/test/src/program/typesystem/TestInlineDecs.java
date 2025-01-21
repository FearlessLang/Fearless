package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestInlineDecs {
  @Test void personFactory() { ok("""
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
  @Test void genericPerson() { ok("""
    package test
    Person[N]:{ .name: Str, .age: imm N }
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> {
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void genericPersonInline() { ok("""
    package test
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> Person[N]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void inlineBoundsForwarding() { ok("""
    package test
    //Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: imm N): Person[N] -> Person[N: imm]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void inlineBoundsForwardingMethodSig() { fail("""
    In position [###]/Dummy0.fear:3:67
    [E57 invalidLambdaNameMdfBounds]
    This lambda is missing/has an incompatible set of bounds for its type parameters:
      N: imm
    """, """
    package test
    //Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: imm N): Person[N] -> Person[N]:{
      .name: Str -> name,
      .age: imm N -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#(Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> {
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicitBreak() { fail("""
    In position [###]/Dummy0.fear:3:10
    [E5 invalidMdfBound]
    The type N is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """, """
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[N] -> {
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit2() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> Person[imm N]{
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }
  @Test void boundsForwardingImplicit3() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N: imm](name: Str, age: N): Person[N] -> Person[N]{
      .name -> name,
      .age -> age,
      }}
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void boundsForwardingExplicit() { ok("""
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> Fresh[N]:Person[imm N]{
      .name -> name,
      .age -> age,
      }}
    Break:{ #: Fresh[mut Nat], }
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void boundsForwardingExplicit2() { fail("""
    In position [###]/Dummy0.fear:3:65
    [E5 invalidMdfBound]
    The type N is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """, """
    package test
    Person[N: imm]:{ .name: Str, .age: N }
    FPerson:{ #[N](name: Str, age: imm N): Person[imm N] -> Fresh[N]:Person[N]{
      .name -> name,
      .age -> age,
      }}
    Break:{ #: Fresh[mut Nat], }
    Ex:{
      .create: Person[Nat] -> FPerson#[Nat](Bob, TwentyFour),
      .name(p: Person[Nat]): Str -> p.name,
      }
    """, """
    package test
    Str:{} Bob:Str{}
    Nat:{} TwentyFour:Nat{}
    """); }

  @Test void inlineCallUnbounded() { ok("""
    package test
    alias base.F as F,
    
    Stack[T]: _Stack[T]{
      .match(m) -> m.empty,
      read .process[R](f: F[read/imm T, R]): mut Stack[R] -> {'comp
        #(current: read Stack[T], acc: mut Stack[R]): mut Stack[R] -> current.match{
          .empty -> acc,
          .elem(top, tail) -> comp#(tail, acc + ( f#(top) ))
          },
        }#(this,{}),
      mut +(e: T): mut Stack[T] -> {
        .match(m) -> m.elem(e, this),
        },
      }
    _Stack[T]: {
      mut  .match[R](m: mut StackMatch[T,R]): R,
      read .match[R](m: mut StackMatchRead[T,R]): R,
      }
    StackMatch[T, R]: {
      mut .empty: R,
      mut .elem(top: T, tail: mut Stack[T]): R
      }
    StackMatchRead[T, R]: {
      mut .empty: R,
      mut .elem(top: read/imm T, tail: read Stack[T]): R
      }
    """, """
    package test
    alias base.Str as Str,
    Plate: {
      .id: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate
    Clean: Plate
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    """, """
    package base
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]: { read #(a: A): R }
    Str: {}
    """); }

  @Test void inlineCallBounded() { ok("""
    package test
    alias base.F as F,
    
    Stack[T:imm]: {
      .match[R](m: StackMatch[T,R]): R -> m.empty,
      .process[R:imm](f: F[T, R]): Stack[R] -> {'comp
        #(current: Stack[T], acc: Stack[R]): Stack[R] -> current.match{
          .empty -> acc,
          .elem(top, tail) -> comp#(tail, acc + ( f#(top) ))
          },
        }#(this,{}),
      +(e: T): Stack[T] -> {
        .match(m) -> m.elem(e, this),
        },
      }
    StackMatch[T:imm, R]: {
      .empty: R,
      .elem(top:T, tail: Stack[T]): R
    }
    """, """
    package test
    alias base.Str as Str,
    Plate: {
      .id: Str,
      .clean: Clean -> {this.id},
      }
    Dirty: Plate
    Clean: Plate
    Example: {#(ds: Stack[Dirty]): Stack[Clean] -> ds.process{dirty -> dirty.clean}}
    """, """
    package base
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]: { read #(a: A): R }
    Str: {}
    """); }

  @Test void shouldRejectRCInInlineDecl() { fail("""
    In position [###]/Dummy0.fear:2:46
    [E46 noMdfInFormalParams]
    Modifiers are not allowed in declarations or implementation lists: mut X
    """, """
    package test
    A: {.m[X:mut,read]: mut Foo[mut X] -> mut Foo[mut X]: {}}
    """);}

  @Test void shouldRejectInvalidFunnel() { fail("""
    In position [###]/Dummy0.fear:2:54
    [E57 invalidLambdaNameMdfBounds]
    This lambda is missing/has an incompatible set of bounds for its type parameters:
      X: mut, read
    """, """
    package test
    A: {.m[X:mut,read]: mut Foo[mut X] -> mut Foo[X:mut]: {}}
    """);}
  @Test void shouldRejectValidFunnelBecauseNotMut() { fail("""
    In position [###]/Dummy0.fear:2:59
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between iso test.Foo[X] and mut test.Foo[mut X].
    """, """
    package test
    A: {.m[X:mut,read]: mut Foo[mut X] -> mut Foo[X:mut,read]: {}}
    """);}

  @Test void diffOrderFunnelling() {ok("""
    package test
    Foo: {.m[X:imm,Y:imm](x: X, y: Y): Box[X,Y] ->
      Anon[Y:imm, X:imm]: Box[X,Y]{.x -> x, .y -> y}
      }
    Box[X:imm,Y:imm]: {.x: X, .y: Y}
    """);}
  @Test void diffOrderFunnellingMustOnlyUseInScope() {fail("""
    In position [###]/Dummy0.fear:4:22
    [E28 undefinedName]
    The identifier "Z" is undefined or cannot be captured.
    """, """
    package test
    Foo: {.m[X:imm,Y:imm](x: X, y: Y): Box[X,Y] ->
      Box[X,Y]{'b
        .x -> x, .y -> y, .z: Z -> b.z}
      }
    Box[X:imm,Y:imm]: {.x: X, .y: Y}
    """);}

  @Test void extraFunnelling() {fail("""
    In position [###]/Dummy0.fear:4:22
    [E28 undefinedName]
    The identifier "Z" is undefined or cannot be captured.
    """, """
    package test
    Foo: {.m[X:imm,Y:imm](x: X, y: Y): Box[X,Y] ->
      Anon[Y:imm, X:imm, Z:imm]: Box[X,Y]{'b
        .x -> x, .y -> y, .z: Z -> b.z}
      }
    Box[X:imm,Y:imm]: {.x: X, .y: Y}
    """);}
}
