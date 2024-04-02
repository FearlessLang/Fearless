package program.typesystem;

import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestReadImm {
  private static final String BOX = """
    package test
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
