package program.typesystem;

import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestReadImm {
  // TODO: uhhhhhh this is copying the body for ALL abstract methods, not just picking one if it has the same name. I mean, it works but uhhhhhh
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;

  @Test void box() { ok(BOX); }

  @Test void canCallReadFromReadImm() { ok("""
    package test
    A: {read .foo: B -> {}}
    B: {}
    Test: {#(a: read/imm A): B -> a.foo}
    """); }

  @Test void boxIsoPromotionExplicit() { ok("""
    package test
    A: {#[S](s: imm S): iso Box[imm S] -> Box#s}
    """, BOX); }
  @Test void boxIsoPromotionBounds() { ok("""
    package test
    A: {#[S:imm](s: S): iso Box[S] -> Box#s}
    """, BOX); }
  @Test void boxIsoPromotionCall() { ok("""
    package test
    B:{#[Y](b: iso Y): mut B -> {}}
    A: {#[S:imm](s: S): mut B -> B#[mut Box[S]](Box#[S]s)}
    """, BOX); }
  @Test void boxIsoPromotionWrong() { fail("""
    In position [###]/Dummy0.fear:2:33
    [E33 callTypeError]
    Type error: None of the following candidates (returning the expected type "mut test.Box[S]") for this method call:
    [-imm-][test.Box[]]{'fear2$ } #/1[S]([s])
    were valid:
    ([E28 undefinedName]) <= (imm test.Box[], S): mut test.Box[S]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:34
        [E28 undefinedName]
        The identifier "s" is undefined or cannot be captured.
    
    ([E28 undefinedName]) <= (imm test.Box[], iso S): iso test.Box[S]
      The following errors were found when checking this sub-typing:
        In position [###]/Dummy0.fear:2:34
        [E28 undefinedName]
        The identifier "s" is undefined or cannot be captured.
    """, """
    package test
    A: {#[S](s: S): iso Box[S] -> Box#s}
    """, BOX); }

  @Provide Arbitrary<Mdf> capturableMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.mut, Mdf.mdf);
  }
  @Provide Arbitrary<Mdf> recvMdf() {
    return Arbitraries.of(Mdf.imm, Mdf.read, Mdf.readImm, Mdf.mut, Mdf.recMdf, Mdf.readOnly, Mdf.lent, Mdf.iso);
  }
  @Property void shouldGetAsIsForMut(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#(box: mut Box[%s B]): %s B -> box.get}
    B: {}
    """.formatted(mdf, mdf), BOX); }
  @Property void shouldGetAsReadForRead(@ForAll("capturableMdf") Mdf mdf) { ok("""
    package test
    A: {#(box: read Box[%s B]): read B -> box.rget}
    B: {}
    """.formatted(mdf), BOX); }

  @Property void shouldGetAsReadOrImmForReadImm(@ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read/imm";
    ok("""
    package test
    A: {#(box: read Box[%s B]): %s B -> box.riget}
    B: {}
    """.formatted(mdf, expected), BOX);
  }
  @Property void shouldGetAsReadOrImmForReadImmArbitraryRecvMdf(@ForAll("recvMdf") Mdf recvMdf, @ForAll("capturableMdf") Mdf mdf) {
    var expected = mdf.isImm() ? "imm" : "read/imm";
    var mMdf = recvMdf.isRecMdf() ? "recMdf" : "imm";
    ok("""
    package test
    A: {%s #(box: %s Box[%s B]): %s B -> box.riget}
    B: {}
    """.formatted(mMdf, recvMdf, mdf, expected), BOX);
  }
}
