package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestCallSitePromotions {
  @Test void oneMutHToMutRecv() {ok("""
    package test
    A: {
      mut .m1(): mut A -> this,
      .call(a: mutH A): mutH A -> a.m1,
      }
    """);}
  @Test void oneMutHToMutRecvMustHygRet() {fail("""
    In position [###]/Dummy0.fear:4:30
    [E33 callTypeError]
    There is no possible candidate for the method call to .m1/0.
    The receiver's reference capability was mutH, the method's reference capability was mut.
    The expected return types were [mut test.A[]], the method's return type was mut test.A[].
    """, """
    package test
    A: {
      mut .m1(): mut A -> this,
      .call(a: mutH A): mut A -> a.m1,
      }
    """);}

  @Test void oneMutHToMutMultiArgs1() {ok("""
    package test
    A: {
      .m1(a1: imm A, a2: iso A, a3: mut A, a4: mut A): mut A -> a3,
      .call(a: mutH A): mutH A -> this.m1(imm A, iso A, a, iso A),
      }
    """);}
  @Test void oneMutHToMutMultiArgs2() {ok("""
    package test
    A: {
      .m1(a1: imm A, a2: iso A, a3: mut A, a4: mut A): mut A -> a3,
      .call(a: mutH A): mutH A -> this.m1(imm A, iso A, iso A, a),
      }
    """);}
  @Test void oneMutHToMutMultiArgsOnlyOnce() {fail("""
    In position [###]/Dummy0.fear:4:34
    [E66 invalidMethodArgumentTypes]
    Method .m1/4 called in position [###]/Dummy0.fear:4:34 cannot be called with current parameters of types:
    [imm test.Fear1$[] (test.A/0), iso test.Fear3$[] (test.A/0), mutH test.A[] (), mutH test.A[] ()]
    Attempted signatures:
    (imm test.A[], iso test.A[], iso test.A[], iso test.A[]):iso test.A[] kind: IsoHProm
    (imm test.A[], iso test.A[], iso test.A[], iso test.A[]):iso test.A[] kind: IsoProm
    (imm test.A[], iso test.A[], mut test.A[], mut test.A[]):mut test.A[] kind: Base
    (imm test.A[], iso test.A[], iso test.A[], iso test.A[]):mutH test.A[] kind: ReadHProm
    (imm test.A[], iso test.A[], mutH test.A[], iso test.A[]):mutH test.A[] kind: MutHPromPar(2)
    (imm test.A[], iso test.A[], iso test.A[], mutH test.A[]):mutH test.A[] kind: MutHPromPar(3)
    """, """
    package test
    A: {
      .m1(a1: imm A, a2: iso A, a3: mut A, a4: mut A): mut A -> a3,
      .call(a: mutH A): mutH A -> this.m1(imm A, iso A, a, a),
      }
    """);}
  @Test void oneMutHToMutMultiArgsOnlyOnceRecv() {fail("""
    In position [###]/Dummy0.fear:4:31
    [E66 invalidMethodArgumentTypes]
    Method .m1/4 called in position [###]/Dummy0.fear:4:31 cannot be called with current parameters of types:
    [imm test.Fear1$[] (test.A/0), iso test.Fear3$[] (test.A/0), mutH test.A[] (), iso test.Fear5$[] (test.A/0)]
    Attempted signatures:
    (imm test.A[], iso test.A[], iso test.A[], iso test.A[]):mutH test.A[] kind: MutHPromRec
    """, """
    package test
    A: {
      mut .m1(a1: imm A, a2: iso A, a3: mut A, a4: mut A): mut A -> this,
      .call(a: mutH A): mutH A -> a.m1(imm A, iso A, a, iso A),
      }
    """);}

  @Test void multiArgsBase() {ok("""
    package test
    A: {
      .m1(a1: imm A, a2: iso A, a3: mut A, a4: mut A): mut A -> a3,
      .call(a: mutH A): mut A -> this.m1(imm A, iso A, mut A, mut A),
      }
    """);}
}
