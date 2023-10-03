package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestMultiSig {
  @Test void shouldAllowDeclaringMultiSigs() { ok("""
    package test
    A[X]:{
      read .get: read X
      mut .get: mdf X,
      }
    """); }
  @Test void shouldAllowImplementingMultiSigs() { ok("""
    package test
    A[X]:{
      read .get: read X
      mut .get: mdf X,
      }
    A:{
      #[X](x: mdf X): mut A[mdf X] -> { x }
      }
    """); }
  @Test void immPromotion() { ok("""
    package test
    A[X]:{
      read .get: read X
      mut .get: mdf X,
      }
    Test[X]:{
      .m1(a: A[mut X]): imm X -> a.get,
      }
    """); }
  @Test void shouldFailWithImpossibleSig() { fail("""
    In position [###]/Dummy0.fear:5:48
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.MutyThing[]") for this method call:
    m .foo/0[]([])
    were valid:
    (read test.MutyThing[]) <: (mut test.MutyThing[]): mut test.MutyThing[]
    (read test.MutyThing[]) <: (iso test.MutyThing[]): iso test.MutyThing[]
    """, """
    package test
    MutyThing:{ mut .foo: mut MutyThing -> this }
    A[X]:{
      read .get(m: read MutyThing): mut MutyThing
      mut .get(m: mut MutyThing): mut MutyThing -> m.foo,
      }
    """); }
  @Test void shouldPassWithPossibleSig() { ok("""
    package test
    MutyThing:{ read .foo: mut MutyThing -> {} }
    A[X]:{
      read .get(m: read MutyThing): mut MutyThing
      mut .get(m: mut MutyThing): mut MutyThing -> m.foo,
      }
    """); }
  @Test void multiSigExpectMut() { ok("""
    package test
    A:{
      mut .get: mut A
      lent .get: readOnly A -> this,
      }
    Test:{ .m1: mut A -> mut A.get }
    """); }
  @Test void multiSigExpectMut2() { ok("""
    package test
    A:{
      lent .get: readOnly A
      mut .get: mut A -> this,
      }
    Test:{ .m1: mut A -> mut A.get }
    """); }

  @Test void simpleList() { ok("""
    package test

    LList:{
      #[E]: mut LList[mdf E] -> {},
      #[E](head: mdf E): mut LList[mdf E] -> this#(head, {}),
        
      #[E](head: mdf E, tail: read LList[mdf E]): mut LList[mdf E]
      #[E](head: mdf E, tail: mut LList[mdf E]): mut LList[mdf E] -> {
        .match(m) -> m.elem(head, tail),
        },
      }
    LList[E]:{
      read .match[R](m: mut LListMatch[read E, mdf R]): mdf R
      mut  .match[R](m: mut LListMatch[mdf E, mdf R]): mdf R -> m.empty,
        
      // I think this is where my multi-sig ordering significance point matters,
      // both are valid but one will have a read tail and one will have a mut tail
//      read +(e: mdf E): mut LList[mdf E]
      mut  +(e: mdf E): mut LList[mdf E] -> this ++ (LList#e),
        
//      read ++(e: read LList[mdf E]): read LList[mdf E]
      mut  ++(l1: mut LList[mdf E]): mut LList[mdf E] -> l1,
        
//      read .pushFront(e: mdf E): read LList[mdf E]
      mut  .pushFront(e: mdf E): mut LList[mdf E] -> LList#(e, this),
      }
    LListMatch[E,R]:{
//      mut .elem(head: read E, tail: read LList[mdf E]): mdf R
      mut .elem(head: mdf E, tail: read LList[mdf E]): mdf R,
        
      mut .empty: mdf R
      }
    """); }
}
