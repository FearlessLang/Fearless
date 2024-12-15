package program.typesystem;

import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestBounds {
  private static final String BOX = """
    package test
    Box: {#[T](t: T): mut Box[T] -> {
      .get -> t,
      .rget -> t,
      .riget -> t,
      }}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """;
  @Test void boxIsoPromotionBounds() { ok("""
    package test
    A: {#[S:imm](s: S): iso Box[S] -> Box#s}
    """, BOX); }
  @Test void boxIsoPromotionCall() { ok("""
    package test
    B:{#[Y](b: iso Y): mut B -> {}}
    A: {#[S:imm](s: S): mut B -> B#[mut Box[S]](Box#[S]s)}
    """, BOX); }

  @Test void invalidTraitBoundsTopLevel() {fail("""
    In position [###]/Dummy0.fear:3:7
    [E5 invalidMdfBound]
    The type imm test.Break[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X:mut]: {}
    Break: A[imm Break]
    """);}
  @Test void invalidTraitBoundsInline() {fail("""
    In position [###]/Dummy0.fear:3:42
    [E5 invalidMdfBound]
    The type imm test.Break[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X:mut]: {}
    BreakOuter: {#: BreakInner -> BreakInner: A[imm Break]}
    Break: {}
    """);}
  @Test void invalidTraitBoundsTopLevelValid() {ok("""
    package test
    A[X:mut]: {}
    Break: A[mut Break]
    """);}
  @Test void invalidTraitBoundsInlineValid() {ok("""
    package test
    A[X:mut]: {}
    BreakOuter: {#: BreakInner -> BreakInner: A[mut Break]}
    Break: {}
    """);}

  @Test void invalidTraitBoundsMethodCall() {fail("""
    In position [###]/Dummy0.fear:3:25
    [E5 invalidMdfBound]
    The type imm test.Break[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A: {#[X: mut](x: X): X -> x}
    Break: {#: imm Break -> A#(imm Break)}
    """);}
  @Test void invalidTraitBoundsMethodCallValid() {ok("""
    package test
    A: {#[X: mut](x: X): X -> x}
    Break: {#: mut Break -> A#(mut Break)}
    """);}

  @Test void invalidBounds() {fail("""
    In position [###]/Dummy0.fear:3:4
    [E5 invalidMdfBound]
    The type Y is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package a
    Foo[X: mut]: {}
    A: {.bar[Y:mut,read,imm]: imm Foo[Y] -> imm Foo[Y]}
    """);}
  @Test void validBounds() {ok("""
    package a
    Foo[X: mut,read]: {}
    A: {.bar[Y:mut]: imm Foo[Y] -> imm Foo[Y]}
    """);}
  @Test void readImmSubsumption() {ok("""
    package a
    Foo[X: read,imm]: {}
    A: {.bar[Y:mut,read,imm]: imm Foo[read/imm Y] -> imm Foo[read/imm Y]}
    """);}
}
