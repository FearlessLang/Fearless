package program.typesystem;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestMdfDispatch {
  @Test void inferringShouldWorkForASingleCandidate() { ok("""
    package test
    A:{
      .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  // This fails because in the promotion that we're asking for (mut -> imm) "mut this" cannot be used in gamma
  @Test void inferringShouldFailWhenMultipleCandidates1() { fail("""
    In position [###]/Dummy0.fear:8:9
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mut test.B[] and imm test.A[].
    """, """
    package test
    A:{
      imm .m1: A,
      mut .m1: A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  @Test void inferringShouldWorkWhenMultipleCompatibleCandidates2() { ok("""
    package test
    A:{
      imm .m1: A,
      mut .m1: mut A,
      .m2: A,
      }
    B:A{
      .m1 -> this,
      .m2 -> this.m1,
      }
    """); }

  @Test void inferringShouldWorkWhenMultipleCandidates() { ok("""
    package test
    A:{
      imm .m1: A,
      mut .m1: A,
      .m2: A,
      }
    B:A{
      imm .m1: A -> this,
      .m2: A -> this.m1,
      }
    """); }

  @Test void callingMultiSig() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): read B -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigFail() { fail("""
  In position [###]/Dummy0.fear:8:36
  [E33 callTypeError]
  There is no possible candidate for the method call to .m1/0.
  The receiver's reference capability was read, the method's reference capability was read.
  The expected return types were [mut test.A[]], the method's return type was read test.B[].
  """, """
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: read A): mut A -> a.m1[](),
      mut .aMut(a: mut A): mut A -> a.m1[](),
      }
    """); }
  @Disabled // acceptable regression with the new type system
  @Test void callingMultiSigAmbiguousDiffRet() { ok("""
    package test
    A:{
      read .m1: read B -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): read B -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigAmbiguousSameRet() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: mut A): mut A -> a.m1[](),
      }
    """); }

  @Test void callingMultiSigImmPromotion() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: A): imm A -> a.m1[](),
      }
    """); }
  @Test void callingMultiSigImmDispatch() { ok("""
    package test
    A:{
      read .m1: mut A -> {},
      imm .m1: A -> this,
      mut .m1: mut A -> this,
      }
    B:{}
    Test:{
      read .aRead(a: A): imm A -> a.m1[](),
      }
    """); }

  @Test void optWithImmMatcher() { ok("""
  package base
  Opt:{ #[T:*](x: T): mut Opt[T] -> {
    mut .match[R:*](m: mut OptMatch[T, R]): R -> m.some(x),
    read .match[R:*](m: mut OptMatch[read T, R]): R -> m.some(x),
    }}
  Opt[T:*]:{
    mut  .match[R:*](m: mut OptMatch[T, R]): R -> m.empty,
    read .match[R:*](m: mut OptMatch[read T, R]): R -> m.empty,
    imm  .match[R:*](m: mut OptMatch[T, R]): R -> m.empty,
    read .or(f: mut OptOrElse[read Opt[T]]): read Opt[T] -> this.match[read Opt[T]](mut OptMatch[read T, read Opt[T]]{
      .some(x) -> this,
      .empty -> f#
      }),
    imm .or(f: mut OptOrElse[Opt[T]]): Opt[T] -> this.match[Opt[T]](mut OptMatch[T, Opt[T]]{
        .some(x) -> this,
        .empty -> f#
        }),
      }
    OptMatch[T:*,R:*]:{ mut .some(x: T): R, mut .empty: R }
    OptOrElse[R:*]:{ mut #: R }
  """); }
  @Test void complexOpts() {ok("""
  package test
  Opts: {
    #[T:*](x: T): mut Opt[T] -> {.match(m) -> m.some(x)},
    }
  Opt[T:*]: _Opt[T]{
    .match(m) -> m.empty,
    .map(f)   -> this.match(f),
    ||(f)     -> this.match{.some(x) -> x, .empty -> f#},
    |(f)      -> this.match{.some(x) -> x, .empty -> f},
    !         -> this.match{.some(x) -> x, .empty -> this!},
    }
  _Opt[T:*]: {
    mut  .match[R:*](m: mut OptMatch[T, R]): R,
    read .match[R:*](m: mut OptMatch[read/imm T, R]): R,
    imm  .match[R:*](m: mut OptMatch[imm T, R]): R,
  
    mut  .map[R:*](f: mut OptMap[T, R]):          mut Opt[R],
    read .map[R:*](f: mut OptMap[read/imm T, R]): mut Opt[R],
    imm  .map[R:*](f: mut OptMap[imm T, R]):      mut Opt[R],
  
    mut  ||(default: mut MF[T]):          T,
    read ||(default: mut MF[read/imm T]): read/imm T,
    imm  ||(default: mut MF[imm T]):      imm T,
  
    mut  |(default: T):          T,
    read |(default: read/imm T): read/imm T,
    imm  |(default: imm T):      imm T,
  
    mut  !: T,
    read !: read/imm T,
    imm  !: imm T,
    }
  
  OptMatch[T:*,R:*]:{ mut .some(x: T): R, mut .empty: R }
  OptMap[T:*,R:*]:OptMatch[T, mut Opt[R]]{
    mut #(t: T): R,
    .some(x) -> Opts#(this#x),
    .empty -> {}
    }
  """, """
  package test
  MF[R:read,mut,imm,iso]: { read #: R }
  MF[A:read,mut,imm,iso,R:read,mut,imm,iso]: { mut #(a: A): R }
  MF[A:read,mut,imm,iso, B:read,mut,imm,iso, R:read,mut,imm,iso]: { mut #(a: A, b: B): R }
  MF[A:read,mut,imm,iso, B:read,mut,imm,iso, C:read,mut,imm,iso, R:read,mut,imm,iso]: { mut #(a: A, b: B, c: C): R }
  MF[A:read,mut,imm,iso, B:read,mut,imm,iso, C:read,mut,imm,iso, D:read,mut,imm,iso, R:read,mut,imm,iso]: { mut #(a: A, b: B, c: C, d: D): R }
  """);}
}