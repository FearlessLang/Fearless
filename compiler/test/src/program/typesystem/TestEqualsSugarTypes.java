package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestEqualsSugarTypes {
  private static final String LET = """
    package test
    Continuation[TT,CC,RR]: {mut #(x: TT, self: CC): RR}
    Let: {#[R]: mut Let[R] -> {}}
    Let[R]: {
      mut .let[T](x: mut MF[T], c: mut Continuation[T,mut Let[R],R]): R
        ->  c#(x#,this),
      mut .in(rv: mut MF[R]): R -> rv#,
      }
    MF[R]: {mut #: R}
    """;
  private static final String NUMS = """
    package test
    Num: {
      .pred: Num,
      +(b: Num): Num,
      *(b: Num): Num,
      .s: Num -> S{this},
      }
    Zero: Num{
      .pred -> Abort!,
      +(b) -> b,
      *(b) -> this,
      }
    S: Num{
      +(b) -> S{this.pred + b},
      *(b) -> b + (b * (this.pred)),
      }
    Abort: { ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """;

  @Test void let() { ok(LET); }
  @Test void letUsageDirectReturn() { ok("""
    package test
    Usage: {#: Num -> Let#
      .let ten = {(Zero.s.s.s.s.s) + (Zero.s.s.s.s.s)}
      .let y = {ten * (Zero.s.s)}
      .in {(y) + (ten)}//returns 30
      }
    """, LET, NUMS); }

  @Test void letUsageGeneric() { ok("""
    package test
    A[T]: {mut .a: T}
    B: {#[T](a: mut A[T]): mut B[T] -> this#a}
    B[T]: {}
    Usage: {#[T](x: T): mut A[T] -> Let#
      .let[mut A[T]] y = {{x}}
      .in {{y.a}}
      }
    """, LET); }
  // TODO: This should pass, that it does not is an inference bug
  @Disabled("03/12/24") @Test void letUsageGenericWrapped() { ok("""
    package test
    A[T]: {mut .a: T}
    B: {#[T](a: mut A[T]): mut B[T] -> this#a}
    B[T]: {}
    Usage: {#[T](x: T): mut B[T] -> B#(Let#
      .let[mut A[T]] y = {{x}}
      .in {{y.a}}
      )}
    """, LET); }
  @Test void letUsageGenericWrappedExplicit() { ok("""
    package test
    A[T]: {mut .a: T}
    B: {#[T](a: mut A[T]): mut B[T] -> this#a}
    B[T]: {}
    Usage: {#[T](x: T): mut B[T] -> B#(Let#[mut A[T]]
      .let[mut A[T]] y = {{x}}
      .in {{y.a}}
      )}
    """, LET); }
}
