package program.typesystem;

import id.Mdf;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.SetArbitrary;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestReadImm {
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {
      .get   -> t,
      .rget  -> t,
      .riget -> t,
      }}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;

  @Test void box() { ok(BOX); }

  @Test void captureAsImmMutGet() { ok("""
    package test
    A: {#(b: imm B): imm B -> Box#b.get }
    B: {}
    """, BOX); }
  @Test void captureAsImmReadImmGet() { ok("""
    package test
    A: {#(b: imm B): imm B -> Box#b.riget }
    B: {}
    """, BOX); }
  @Test void captureAsImmMutGetGen() { ok("""
    package test
    A: {#[B](b: imm B): imm B -> Box#b.get }
    """, BOX); }
  @Test void captureAsImmReadImmGetGen() { ok("""
    package test
    A: {#[B](b: imm B): imm B -> Box#b.riget }
    """, BOX); }

  @Test void immFromReadBox() { ok("""
    package test
    A: {#(b: read Box[imm B]): imm B -> b.riget }
    B: {}
    """, BOX); }
  @Test void immFromReadBoxMethGen() { ok("""
    package test
    A: {#[B](b: read Box[imm B]): imm B -> b.riget }
    """, BOX); }
  @Test void immFromReadBoxTypeGen() { ok("""
    package test
    A[B]: {#(b: read Box[imm B]): imm B -> b.riget }
    """, BOX); }


//  @Test void canCallReadFromReadImm() { ok("""
//    package test
//    A: {read .foo: B -> {}}
//    B: {}
//    Test: {#(a: read/imm A): B -> a.foo}
//    """); }

  @Provide Arbitrary<Mdf> capturableMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.mut, Mdf.mdf);
  }
  @Provide Arbitrary<Mdf> recvMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.mut, Mdf.readH, Mdf.mutH, Mdf.iso);
  }
  @Provide SetArbitrary<Mdf> bounds() {
    return Arbitraries
      .subsetOf(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.mutH, Mdf.read, Mdf.readH)
      .ofMinSize(1);
  }
  @Provide SetArbitrary<Mdf> nonHygBounds() {
    return Arbitraries
      .subsetOf(Mdf.iso, Mdf.imm, Mdf.mut, Mdf.read)
      .ofMinSize(1);
  }

  @Property public void readImmGenSubtypeOfAllNonHyg(@ForAll("bounds") Set<Mdf> bounds) {
    var xbs = bounds.stream().map(Mdf::toString).collect(Collectors.joining(","));
    var code = """
    package a
    A[X:%s]: {#(x: X): read/imm X -> x}
    """.formatted(xbs, xbs);

    if (bounds.contains(Mdf.readH) || bounds.contains(Mdf.mutH)) {
      fail("""
        In position [###]:2:[###]
        [E37 noSubTypingRelationship]
        There is no sub-typing relationship between X and read/imm X.
        """, code);
      return;
    }
    ok(code);
  }
  @Test void readImmGenSubtypeOfMut() {ok("""
    package a
    A[X:mut]: {#(x: X): read/imm X -> x}
    """);}

  @Property void shouldGetAsIsForMut(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#[X](box: mut Box[%s X]): %s X -> box.get}
    """.formatted(mdf, mdf), BOX); }
  @Property void shouldGetAsReadForRead(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#[X](box: read Box[%s X]): read X -> box.rget}
    """.formatted(mdf), BOX); }

  @Property void shouldGetAsReadOrImmForReadImm(@ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read";
    ok("""
    package test
    A: {#[X](box: read Box[%s X]): %s X -> box.riget}
    """.formatted(mdf, expected), BOX);
  }
  @Property void shouldGetAsReadOrImmForReadImmArbitraryRecvMdf(@ForAll("recvMdf") Mdf recvMdf, @ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read";
    if (recvMdf.isHyg() && !mdf.isImm()) { expected = "readH"; }
    ok("""
    package test
    A: {#[X](box: %s Box[%s X]): %s X -> box.riget}
    B: {}
    """.formatted(recvMdf, mdf, expected), BOX);
  }

  @Property void shouldNeverAllowReadToBecomeImm(@ForAll("bounds") Set<Mdf> bounds) {
    var xbs = bounds.stream().map(Mdf::toString).collect(Collectors.joining(","));
    /*
    [E66 invalidMethodArgumentTypes]
      Method .m/1 called in position [###] cannot be called with current parameters of types:
      [read X]
      Attempted signatures:
      [###]
     */
    fail("""
      [###]
      """, """
      package test
      Caster[X:%s]: { .m(bob: read/imm X): read/imm X->bob }
      // could try all permutations of bounds for X
      User: {.user[X:%s](x:read X):imm X->Caster[X].m(x)}
      """.formatted(xbs, xbs));
  }

  @Property void shouldNeverAllowXToBecomeImm(@ForAll("bounds") Set<Mdf> bounds) {
    var xbs = bounds.stream().map(Mdf::toString).collect(Collectors.joining(","));
    var code = """
      package test
      Caster[X:%s]: { .m(bob: read/imm X): read/imm X->bob }
      // could try all permutations of bounds for X
      User: {.user[X:%s](x: X):imm X->Caster[X].m(x)}
      """.formatted(xbs, xbs);
    if (bounds.equals(Set.of(Mdf.iso)) || bounds.equals(Set.of(Mdf.imm)) || bounds.equals(Set.of(Mdf.iso, Mdf.imm))) {
      ok(code);
      return;
    }
    /*
    [E66 invalidMethodArgumentTypes]
      Method .m/1 called in position [###] cannot be called with current parameters of types:
      [read X]
      Attempted signatures:
      [###]
     */
    fail("""
      [###]
      """, code);
  }

  @Property void shouldNeverAllowXToBecomeImm2(@ForAll("bounds") Set<Mdf> bounds) {
    var xbs = bounds.stream().map(Mdf::toString).collect(Collectors.joining(","));
    var code = """
      package test
      A: {
        .m1[X:iso,imm,mut,read](x: read/imm X): read/imm X -> x,
        .m2[X:%s](x: X): imm X -> this.m1[X](x),
        }
      """.formatted(xbs);
    if (bounds.equals(Set.of(Mdf.iso)) || bounds.equals(Set.of(Mdf.imm)) || bounds.equals(Set.of(Mdf.iso, Mdf.imm))) {
      ok(code);
      return;
    }
    fail("""
      [###]
      """, code);
  }

  @Property void shouldNeverAllowXToBecomeImm3(@ForAll("bounds") Set<Mdf> bounds) {
    var xbs = bounds.stream().map(Mdf::toString).collect(Collectors.joining(","));
    var code = """
      package test
      Caster[X:%s]: { .m(bob: read/imm X): read/imm X->bob }
      // could try all permutations of bounds for X
      User: {.user[X:iso,imm,mut,read](x: X):imm X->Caster[X].m(x)}
      """.formatted(xbs);
    fail("""
      [###]
      """, code);
  }

  /* parameter of read X, pass a mdf Y --> becomes read Y */
  @Test void readParamMdfArg() {ok("""
    package test
    A: {.m[X](x: read X): read X -> x}
    B: {.m[Y](y: Y): read Y -> A.m[Y](y)}
    C: {.m[Y](y: read Y): read Y -> A.m[Y](y)}
    """);}
  @Test void readParamMdfArgAIsReadImm() {fail("""
    In position [###]/Dummy0.fear:3:37
    [E66 invalidMethodArgumentTypes]
    Method .m/1 called in position [###]/Dummy0.fear:3:37 cannot be called with current parameters of types:
    [read Y]
    Attempted signatures:
    (imm Y):imm Y kind: IsoHProm
    (imm Y):imm Y kind: IsoProm
    (read/imm Y):read/imm Y kind: Base
    """, """
    package test
    A: {.m[X](x: read/imm X): read/imm X -> x}
    C: {.m[Y](y: read Y): read/imm Y -> A.m[Y](y)}
    D: {.m(foo: read Foo): imm Foo -> C.m[imm Foo](foo)} // unsound
    Foo: {}
    """);}
  @Test void readParamMdfArgAIsReadImmOnTrait() {fail("""
    In position [###]/Dummy0.fear:3:52
    [E66 invalidMethodArgumentTypes]
    Method .m/1 called in position [###]/Dummy0.fear:3:52 cannot be called with current parameters of types:
    [read Y]
    Attempted signatures:
    (imm Y):imm Y kind: IsoHProm
    (imm Y):imm Y kind: IsoProm
    (read/imm Y):read/imm Y kind: Base
    """, """
    package test
    A[X]: {.m(x: read/imm X): read/imm X -> x}
    B[Y]: {.m(y: read Y): read/imm Y -> BrokenA[Y]: A[Y].m(y)}
    C: {.m(foo: read Foo): imm Foo -> BrokenB: B[imm Foo].m(foo)} // unsound
    Foo: {}
    """);}

  @Test void readImmInheritance() {ok("""
    package a
    B[Y:read,imm]:{.m:read/imm Y}
    A1[X:imm]:B[X]{}
    A2[X:imm]:B[X]{.m:read/imm X}
    """); }

  @Test void passReadImmXAround1() {ok("""
    package a
    B[X]: {
      .m1(a: read/imm X): read/imm X -> this.m1(a),
      .m2(a: read/imm X): read/imm X -> a,
      }
    """);}
  @Test void passReadImmXAround2() {ok("""
    package a
    B[X]: {
      .m1(a: read/imm X): read/imm X -> this.m1(a),
      .m2[Y](a: read/imm Y): read/imm Y -> a,
      }
    """);}
}
