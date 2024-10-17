package program.typesystem;

import ast.Program;
import failure.CompileError;
import id.Id;
import id.Mdf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static id.Mdf.*;

public class TestKindingJudgement {
  private static void ok(XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    var actualFull = coreT.accept(new KindingJudgement(p, xbs, expected, false)).get();
    Assertions.assertTrue(actualFull.stream().anyMatch(rcs->rcs.equals(expected)));
    var actualCheckOnly = coreT.accept(new KindingJudgement(p, xbs, expected, true)).get();
    Assertions.assertTrue(actualCheckOnly.stream().anyMatch(rcs->rcs.equals(expected)));
  }
  private static void extract(XBs xbs, String t, Set<Set<Mdf>> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    var actual = new HashSet<>(coreT.accept(new KindingJudgement(p, xbs, false)).get());
    Assertions.assertEquals(expected, actual);
  }
  private static void contains(XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    var actual = new HashSet<>(coreT.accept(new KindingJudgement(p, xbs, false)).get());
    Assertions.assertTrue(actual.contains(expected));
  }
  private static void notContains(XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    var actual = new HashSet<>(coreT.accept(new KindingJudgement(p, xbs, false)).get());
    Assertions.assertFalse(actual.contains(expected));
  }
  private static void fail(String expectedErr, XBs xbs, String t, Set<Mdf> expected, String... content) {
    var fullT = new Parser(Parser.dummy, t).parseFullT();
    var coreT = fullT.toAstT();
    var p = toProgram(content);
    try {
      coreT.accept(new KindingJudgement(p, xbs, expected, false)).get();
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  private static Program toProgram(String... content) {
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck(new ConcurrentHashMap<>());
    return inferred;
  }

  @Test void extractMut() {extract(
    XBs.empty(),
    "mut X",
    Set.of(
      Set.of(mut),
      Set.of(iso, mut),
      Set.of(mut, read),
      Set.of(readH, mut),
      Set.of(mutH, mut),
      Set.of(mut, imm),
      Set.of(mutH, readH, mut),
      Set.of(mutH, mut, imm),
      Set.of(mutH, iso, mut),
      Set.of(mut, read, imm),
      Set.of(readH, mut, read),
      Set.of(iso, mut, imm),
      Set.of(iso, readH, mut),
      Set.of(mutH, mut, read),
      Set.of(iso, mut, read),
      Set.of(readH, mut, imm),
      Set.of(readH, mut, read, imm),
      Set.of(mutH, iso, readH, mut),
      Set.of(iso, mut, read, imm),
      Set.of(mutH, readH, mut, read),
      Set.of(mutH, mut, read, imm),
      Set.of(iso, readH, mut, imm),
      Set.of(mutH, iso, mut, read),
      Set.of(iso, readH, mut, read),
      Set.of(mutH, readH, mut, imm),
      Set.of(mutH, iso, mut, imm),
      Set.of(mutH, iso, readH, mut, read),
      Set.of(iso, readH, mut, read, imm),
      Set.of(mutH, readH, mut, read, imm),
      Set.of(mutH, iso, readH, mut, imm),
      Set.of(mutH, iso, mut, read, imm),
      Set.of(mutH, iso, readH, mut, read, imm)
    ));}
  @Test void isMut() {ok(
    XBs.empty(),
    "mut X",
    Set.of(Mdf.mut)
  );}
  @Test void mutSubsumption() {ok(
    XBs.empty(),
    "mut X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void noMutInExpected() {fail("""
    [E5 invalidMdfBound]
    The type mut X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, imm.
    """,
    XBs.empty(),
    "mut X",
    Set.of(Mdf.imm, Mdf.read)
  );}

  @Test void mutSubsumptionX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void noMutInExpectedX() {fail("""
    [E5 invalidMdfBound]
    The type mut X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut))),
    "mut X",
    Set.of(Mdf.imm, Mdf.read)
  );}
  @Test void multiBoundsXFail() {fail("""
    [E5 invalidMdfBound]
    The type X is not valid because its capability is not in the required bounds. The allowed modifiers are: read, mut, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.iso))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void multiBoundsX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.imm, Mdf.read))),
    "X",
    Set.of(Mdf.mut, Mdf.imm, Mdf.read)
  );}
  @Test void readImm() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.imm, Mdf.read))),
    "read/imm X",
    Set.of(Mdf.read, Mdf.imm)
  );}
  @Test void readImmImmInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm))),
    "read/imm X",
    Set.of(Mdf.imm)
  );}
  @Test void readImmImmAndIsoInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.iso))),
    "read/imm X",
    Set.of(Mdf.imm)
  );}
  @Test void readImmWillOnlyBeImm() {fail("""
    [E5 invalidMdfBound]
    The type read/imm X is not valid because its capability is not in the required bounds. The allowed modifiers are: read.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.iso))),
    "read/imm X",
    Set.of(Mdf.read)
  );}
  @Test void readImmReadInBounds() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.mut, Mdf.read))),
    "read/imm X",
    Set.of(Mdf.read)
  );}

  @Test void immIsoLimitX() {ok(
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm))),
    "X",
    Set.of(Mdf.imm, Mdf.iso)
  );}
  @Test void immIsoLimitXFail() {fail("""
    [E5 invalidMdfBound]
    The type X is not valid because its capability is not in the required bounds. The allowed modifiers are: iso, imm.
    """,
    XBs.empty()
      .addBounds(List.of(new Id.GX<>("X")), Map.of(new Id.GX<>("X"), Set.of(Mdf.imm, Mdf.mut, Mdf.read))),
    "X",
    Set.of(Mdf.imm, Mdf.iso)
  );}

  @Test void literalOk() {ok(
    XBs.empty(),
    "mut a.A[]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A: {}
      """
  );}
  @Test void literalFail() {fail("""
    [E5 invalidMdfBound]
    The type mut a.A[] is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """,
    XBs.empty(),
    "mut a.A[]",
    Set.of(Mdf.imm),
    """
      package a
      A: {}
      """
  );}

  @Test void literalGenOk() {ok(
    XBs.empty(),
    "mut a.A[a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X]: {}
      B: {}
      """
  );}

  @Test void literalImplOk() {ok(
    XBs.empty(),
    "mut a.A[a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X]: Z[X]{}
      Z[X]: {}
      B: {}
      """
  );}
  @Test void literalImplFail1() {fail("""
    [E5 invalidMdfBound]
    The type mut a.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: imm.
    """,
    XBs.empty(),
    "mut a.A[mut a.B[]]",
    Set.of(Mdf.mut, Mdf.read, Mdf.imm),
    """
      package a
      A[X:imm]: Z[X]{}
      Z[X:imm]: {}
      B: {}
      """
  );}

  @Test void shouldExtractReadImmFromReadImmXBoundedToBeMut() {contains(
    XBs.empty().add("X", Set.of(mut)),
    "read/imm X",
    Set.of(
      read, imm
    ));}
  @Test void shouldExtractReadFromReadImmXBoundedToBeMut() {contains(
    XBs.empty().add("X", Set.of(mut)),
    "read/imm X",
    Set.of(
      read
    ));}
  @Test void shouldNotExtractImmFromReadImmXBoundedToBeMut() {notContains(
    XBs.empty().add("X", Set.of(mut)),
    "read/imm X",
    Set.of(
      imm
    ));}

  @Test void shouldExtractReadImmFromReadImmXBoundedToBeImm() {contains(
    XBs.empty().add("X", Set.of(imm)),
    "read/imm X",
    Set.of(
      read, imm
    ));}
  @Test void shouldNotExtractReadFromReadImmXBoundedToBeImm() {notContains(
    XBs.empty().add("X", Set.of(imm)),
    "read/imm X",
    Set.of(
      read
    ));}
  @Test void shouldExtractImmFromReadImmXBoundedToBeImm() {contains(
    XBs.empty().add("X", Set.of(imm)),
    "read/imm X",
    Set.of(
      imm
    ));}

  @Test void shouldExtractReadImmFromReadImmXBoundedToBeIso() {contains(
    XBs.empty().add("X", Set.of(iso)),
    "read/imm X",
    Set.of(
      read, imm
    ));}
  @Test void shouldNotExtractReadFromReadImmXBoundedToBeIso() {notContains(
    XBs.empty().add("X", Set.of(iso)),
    "read/imm X",
    Set.of(
      read
    ));}
  @Test void shouldExtractImmFromReadImmXBoundedToBeIso() {contains(
    XBs.empty().add("X", Set.of(iso)),
    "read/imm X",
    Set.of(
      imm
    ));}

  @Test void shouldExtractReadImmFromReadImmXBoundedToBeImmOrIso() {contains(
    XBs.empty().add("X", Set.of(imm, iso)),
    "read/imm X",
    Set.of(
      read, imm
    ));}
  @Test void shouldNotExtractReadFromReadImmXBoundedToBeImmOrIso() {notContains(
    XBs.empty().add("X", Set.of(imm, iso)),
    "read/imm X",
    Set.of(
      read
    ));}
  @Test void shouldExtractImmFromReadImmXBoundedToBeImmOrIso() {contains(
    XBs.empty().add("X", Set.of(imm, iso)),
    "read/imm X",
    Set.of(
      imm
    ));}

  @Test void shouldExtractReadImmFromReadImmXBoundedToBeMutOrIso() {contains(
    XBs.empty().add("X", Set.of(mut, iso)),
    "read/imm X",
    Set.of(
      read, imm
    ));}
  @Test void shouldNotExtractReadFromReadImmXBoundedToBeMutOrIso() {notContains(
    XBs.empty().add("X", Set.of(mut, iso)),
    "read/imm X",
    Set.of(
      read
    ));}
  @Test void shouldNotExtractImmFromReadImmXBoundedToBeMutOrIso() {notContains(
    XBs.empty().add("X", Set.of(mut, iso)),
    "read/imm X",
    Set.of(
      imm
    ));}
}
