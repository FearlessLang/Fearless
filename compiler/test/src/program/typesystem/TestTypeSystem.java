package program.typesystem;

import id.Mdf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static program.typesystem.RunTypeSystem.fail;
import static program.typesystem.RunTypeSystem.ok;

public class TestTypeSystem {
  //  TODO: mut Box[readOnly X] is not valid even after promotion
  // TODO: .m: mut Box[X] must return mutH Box[readOnly Person] if X becomes readH X (same with lent)
  // TODO: Factory of mutBox and immBox, what types do we get?

  @Test void emptyProgram(){ ok("""
    package test
    """); }

  @Test void simpleProgram(){ ok( """
    package test
    A:{ .m: A -> this }
    """); }

  @Test void undefinedGens() { fail("""
    In position [###]/Dummy0.fear:2:4
    [E28 undefinedName]
    The identifier "X" is undefined or cannot be captured.
    """, """
    package test
    A:{ .foo(x: X): X -> x }
    """); }

  @Test void simpleTypeError(){ fail("""
    In position [###]/Dummy0.fear:4:15
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm test.A[] and imm test.B[].
    """, """
    package test
    A:{ .m: A -> this }
    B:{
      .fail: B -> A.m
    }
    """); }

  @Test void subTypingCall(){ ok( """
    package test
    A:{ .m1(a: A): A -> a }
    B:A{}
    C:{ .m2: A -> A.m1(B) }
    """); }
  @Test void numbersGenericTypes2aWorksThanksTo5b(){ ok("""
    package test
    FortyTwo:{}
    FortyThree:{}
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree,FortyTwo{} }
    """); }
  @Test void numbersGenericTypes2aNoMagic(){ fail("""
    In position [###]/Dummy0.fear:6:43
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm test.Fear7$[] and imm test.FortyTwo[].
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ .count: N, .sum: N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum -> FortyThree }
    """); }
  @Test void mdfSubTypingFailure(){ fail("""
    In position [###]/Dummy0.fear:6:46
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mut test.FortyTwo[] and imm test.FortyTwo[].
    """, """
    package test
    Res1:{} Res2:{}
    FortyTwo:{ .get: Res1 -> Res1 }
    FortyThree:{ .get: Res2 -> Res2 }
    A[N]:{ mut .count: N, mut .sum(n: mut FortyTwo): N }
    B:A[FortyTwo]{ .count -> FortyTwo, .sum(n) -> n }
    """); }

  @Test void simpleThis() { ok("""
    package test
    A:{
      .a: C -> B{ this.c }.c,
      .c: C -> {}
      }
    B:{ .c: C }
    C:{ }
    """); }

  @Test void lambdaCapturesThis() { ok("""
    package test
    Let:{ #[V,R](l: mut Let[V, R]): R -> l.in(l.var) }
    Let[V,R]:{ mut .var: V, mut .in(v: V): R }
    Void:{}
    Ref[X]:{
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Let#mut Let[X,Void]{ .var -> this.swap(x), .in(_) -> Void },
      }
    """); }

  @Test void callMutFromLent() { ok("""
    package test
    A:{
      .b: mutH B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void callMutFromLent2() { ok("""
    package test
    A:{
      .b: mutH B -> {},
      .doThing: mut B -> this.b
      }
    B:{}
    """); }
  // the other tests are only passing due to iso promotion
  @Test void callMutFromLent2a() { fail("""
    In position [###]/Dummy0.fear:4:30
    [E33 callTypeError]
    There is no possible candidate for the method call to .b/0.
    The receiver's reference capability was read, the method's reference capability was read.
    The expected return types were [mut test.B[]], the method's return type was mutH test.B[].
    """, """
    package test
    A:{
      read .b: mutH B -> {},
      read .doThing: mut B -> this.b
      }
    B:{}
    """); }
  @Test void callMutFromLent3() { ok("""
    package test
    A:{
      .b(a: mut A): mutH B -> {},
      .doThing: mut B -> this.b(iso A)
      }
    B:{}
    """); }
  @Test void callMutFromLent4() { ok("""
    package test
    A:{
      .b(a: mut A): mutH B -> {},
      .doThing: mut B -> this.b({})
      }
    B:{}
    """); }

  @Test void callMutFromIso() { ok("""
    package test
    A:{
      .b: mutH B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromImm() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    Method <.foo> with 0 args does not exist in <imm test.B[]>
    Did you mean <test.B.foo()>
    
    Other candidates:
    test.A[].b(): imm test.B[]
    test.A[].doThing(): imm test.Void[]
    test.B[].ret(): imm test.Void[]
    """, """
    package test
    A:{
      .b: imm B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromImmFailLate() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    Method <.foo> with 0 args does not exist in <imm test.B[]>
    Did you mean <test.B.foo()>
    
    Other candidates:
    test.A[].b(): imm test.B[]
    test.A[].doThing(): imm test.Void[]
    test.B[].ret(): imm test.Void[]
    """, """
    package test
    A:{
      .b: imm B -> {},
      .doThing: Void -> this.b.foo[].ret[]
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromReadOnly() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    Method <.foo> with 0 args does not exist in <readH test.B[]>
    Did you mean <test.B.foo()>
    
    Other candidates:
    test.A[].b(): readH test.B[]
    test.A[].doThing(): imm test.Void[]
    test.B[].ret(): imm test.Void[]
    """, """
    package test
    A:{
      .b: readH B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void noCallMutFromRead() { fail("""
    In position [###]/Dummy0.fear:4:26
    [E36 undefinedMethod]
    Method <.foo> with 0 args does not exist in <read test.B[]>
    Did you mean <test.B.foo()>
    
    Other candidates:
    test.A[].b(): read test.B[]
    test.A[].doThing(): imm test.Void[]
    test.B[].ret(): imm test.Void[]
    """, """
    package test
    A:{
      .b: read B -> {},
      .doThing: Void -> this.b.foo.ret
      }
    B:{
      mut .foo(): mut B -> this,
      mut .ret(): Void -> {},
      }
    Void:{}
    """); }
  @Test void readThisIsRead() { ok("""
    package test
    A:{
      read .self: readH A -> this,
      }
    """); }
  @Test void box() { ok("""
    package test
    Box:{ #[R](r: R): mut Box[R] -> { r } }
    Box[R]:{
      mut #: R,
      read #: read/imm R,
      }
    """); }
  @Test void boxMutBounds() { ok("""
    package test
    Box:{#[R:imm,mut](r: R): mut Box[R] -> {r}}
    Box[R]:{mut #: R}
    """); }

  @Test void noCaptureMdfInMut() { fail("""
    In position [###]/Dummy0.fear:4:42
    [E30 badCapture]
    'readH prisoner' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X]:{ mut .prison: X }
    B:{
      .break(prisoner: readH B): mut A[B] -> {prisoner},
      }
    """); }
  @Test void noCaptureMdfInMut2() { fail("""
    In position [###]/Dummy0.fear:4:48
    [E30 badCapture]
    'readH prisoner' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X:imm,mut,read,readH]:{ mut .prison: X }
    B:{
      .break(prisoner: readH B): mut A[readH B] -> {prisoner},
      }
    """); }
  @Test void noCaptureMdfInMut4() { fail("""
    In position [###]/Dummy0.fear:4:30
    [E30 badCapture]
    'x' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    A[X:mut,imm,read,mutH,readH]:{ mut .prison: X }
    B[X:mut,imm,read,mutH,readH]:{
      .break(x: X): mut A[X] -> { x }
      }
    """); }
  // TODO: write a test that shows that the error message for this code makes sense:
  /*
      // (Void is the wrong R and this returns Opt[Opt[T]] instead of Opt[T] or the written Void.
        OptDo[T]:OptMatch[T,Void]{
        #(t:T):Void,   //#[R](t:T):R,
        .some(x) -> Opts#this._doRes(this#x, x),
        .none->{},
        ._doRes(y:Void,x:T):T -> Opts#x
        }
   */

  @Test void noCaptureImmAsRecMdfCounterEx() { fail("""
    In position [###]/Dummy0.fear:5:25
    [E33 callTypeError]
    There is no possible candidate for the method call to .absMeth/0.
    The receiver's reference capability was iso, the method's reference capability was read.
    The expected return types were [mutH test.B[]], the method's return type was imm test.B[].
    """, """
    package test
    B:{}
    L[X]:{ read .absMeth: read/imm X }
    A:{ read .m(par: imm B) : mutH L[imm B] -> mut L[imm B]{.absMeth->par} }
    C:{ #: mutH B -> (A.m(B)).absMeth }
    """); }

  @Test void immToReadCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: readH X }
    A:{ read .m[T](par: imm T) : readH L[imm T] -> read L[imm T]{.absMeth->par} }
    """); }

  @Test void immCapture() { ok("""
    package test
    B:{}
    L[X]:{ imm .absMeth: imm X }
    A:{ read .m[T](par: mut T) : mut L[mut T] -> mut L[mut T]{.absMeth->par} }
    """); }

  @Test void numbersNoBase(){ ok( """
    package test
    A:{ .m(a: 42): 42 -> 42 }
    """, """
    package base
    Sealed:{} Stringable:{ .str: Str } Str:{} Bool:{} Abort:{ ![T]: T -> this! }
    Magic:{ ![T]: T -> this! }
    Nat: {}
    _NatInstance: Nat{}
    """); }

  @Disabled
  @Test void minimalMatcher() { ok("""
    package test
    //we can have mutH matcher with mutH cases that can capture all (but mut as lent), and can only return mut as mutH :-(
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      mutH .get: R
      }
    PreR:{
      readH .get: readH MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      readH .match[R](m: mutH Matcher[R]): R -> m.get
      }
    Usage:{
      .direct(preR: readH PreR): readH MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: readH MyRes): readH MyRes -> MatcherContainer.match{ r }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void minimalMatcher2() { ok("""
    package test
    //we can have mutH matcher with mutH cases that can capture all (but mut as lent), and can only return mut as mutH :-(
    //we can have mut matcher with mut cases that can capture mut,imm,iso, can return R
    alias base.NoMutHyg as NoMutHyg,
    Matcher[R]:{ //Look ma, no NoMutHyg
      mut .get: R
      }
    PreR:{
      mut .get: mut MyRes -> {},
      }
    MyRes:{}
    MatcherContainer:{
      read .match[R](m: mut Matcher[R]): R -> m.get
      }
    Usage:{
      .direct(preR: mut PreR): mut MyRes -> MatcherContainer.match{ preR.get },
      .indirect(r: mut MyRes): mut MyRes -> MatcherContainer.match{ r }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void aliasGenericHiding() { fail("""
    In position [###]/Dummy0.fear:5:0
    [E20 traitNotFound]
    The trait foo.Bar/3 could not be found.
    """, """
    package test
    alias foo.Bar as Baz,
    alias foo.Bar[test.Yolo] as YoloBar,
    Yolo:{}
    Bloop3:YoloBar[Yolo,Yolo]
    """, """
    package foo
    Bar:{}
    Bar[A]:{}
    Bar[A,B]:{}
    """); }

  @Test void immMethodOneMutIsoPromotion() { ok("""
    package test
    A:{
      mut .a(a: iso A): iso A -> (B.foo(a)).a1,
      }
    B:{
      .foo(a: iso A): iso Container -> Container'#(a),
      }
    Container':{ #(a: mut A): mut Container -> { .a1 -> a, .a2 -> a } }
    Container:{
      mut .a1: mut A,
      mut .a2: mut A,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiArg1() { fail("""
    In position [###]/Dummy0.fear:6:39
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mutH test.A[] and mut test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: mutH A): iso A -> a,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiArg2() { fail("""
    In position [###]/Dummy0.fear:6:39
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mutH test.A[] and mut test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: mutH A): iso A -> aa,
      }
    """); }
  @Test void immMethodOneMutIsoPromotion_MultiMut() { fail("""
    In position [###]/Dummy0.fear:6:38
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mutH test.A[] and mut test.A[].
    """, """
    package test
    A:{
      mut .a: iso A -> B.foo(this, this),
      }
    B:{
      .foo(a: mut A, aa: mut A): iso A -> a,
      }
    """); }
  @Test void immMethodOneMutIsoPromotionBad() { fail("""
    In position [###]/Dummy0.fear:6:27
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mutH test.A[] and mut test.A[].
    """, """
    package test
    A:{
      mut .a(randomSharedMut: mut A): iso A -> B.foo(this),
      }
    B:{
      .foo(a: mut A): iso A -> a,
      }
    """); }

  @Test void dontFearTheLambdaEx1() { ok("""
    package ex
    alias base.Str as Str,
    
    Stringable:{ .str: Str }
    Bool:Stringable{}
    True:Bool{ .str: "True" -> "True" }
    False:Bool{ .str: Str -> "False" }
    """, """
    package base
    Str:{}
    _StrInstance:Str{}
    """); }

  @Test void panickingPanics() { ok("""
    package test
    Panic:{
      .thoughts: Thoughts
      }
    Panic':{
      #(isAfraid: Bool): Panic -> isAfraid ? {
        .then -> { Fear },
        .else -> { Calm },
        }
      }
    Thoughts:{ .match[R](m: ThoughtMatcher[R]): R }
    ThoughtMatcher[R]:{ .fear: R, .calm: R, }
    Fear:Thoughts{ m -> m.fear }
    Calm:Thoughts{ m -> m.calm }
    
    PanicTriage:{
      .panickingPanics(panics: List[Panic]): List[Panic] -> this._panickingPanics({}, panics),
      ._panickingPanics(acc: List[Panic], panics: List[Panic]): List[Panic] -> panics.head.match{
        .some(panic) -> panic.thoughts.match{
          .fear -> this._panickingPanics(acc + panic, panics.tail),
          .calm -> this._panickingPanics(acc, panics.tail),
          },
        .none -> acc,
        },
      }
    """, """
    package test
    alias base.Str as Str,
    alias base.Void as Void,
    alias base.Nat as Nat,
    alias base.Int as Int,
    Bool:{
      .and(b: Bool): Bool,
      &&(b: Bool): Bool -> this.and(b),
      .or(b: Bool): Bool,
      ||(b: Bool): Bool -> this.or(b),
      .not: Bool,
      ?[R](f: ThenElse[R]): R,
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then()}
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ .then: R, .else: R, }
    
    Cons:{
      #[E:imm](h: E, t: List[E]): List[E] -> { .match(m) -> m.elem(h, t) },
      }
    List[E:imm]:{
      .match[R:imm](m: ListMatch[E, R]): R -> m.empty,
      .isEmpty: Bool -> this.match{ .empty -> True, .elem(_,_) -> False },
      .len: Nat -> this.match{ .empty -> 0, .elem(_,t) -> t.len + 1, },
      ++(l1: List[E]): List[E] -> this.match{
        .empty -> l1,
        .elem(h, t) -> Cons#(h, t ++ l1)
        },
      +(e: E): List[E] -> this ++ (Cons#(e, {})),
      .get(i: Nat) : Opt[E] -> this.match{
        .empty -> {},
        .elem(h, t) -> (i == 0) ? { .then -> Opts#h, .else -> t.get(i - 1) }
        },
      .head: Opt[E] -> this.match{
        .empty -> {},
        .elem(h,_) -> Opts#h,
        },
      .tail: List[E] -> this.match{
        .empty -> {},
        .elem(_,t) -> t,
        },
      }
    ListMatch[E:imm,R:imm]:{ .elem(head: E, tail: List[E]): R, .empty: R }
    
    Opts:{ #[T:imm](x: T): Opt[T] -> { .match(m) -> m.some(x) } }
    Opt[T:imm]:{
      .match[R:imm](m: OptMatch[T, R]): R -> m.none,
      .map[R:imm](f: OptMap[T,R]): Opt[R] -> this.match(f),
      .do(f: OptDo[T]): Opt[T] -> this.match(f),
      .flatMap[R:imm](f: OptFlatMap[T, R]): Opt[R] ->this.match(f),
      ||(alt: T): T -> this.match{ .some(x) -> x, .none -> alt },
      .isEmpty: Bool -> this.match{ .none -> True, .some(_) -> False },
      .isSome: Bool -> this.match{ .none -> False, .some(_) -> True },
      }
    OptMatch[T:imm,R:imm]:{ .some(x:T): R, .none: R }
    OptFlatMap[T:imm,R:imm]:OptMatch[T,Opt[R]]{ .none -> {} }
    OptMap[T:imm,R:imm]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opts#(this#x), .none -> {} }
    OptDo[T:imm]:OptMatch[T,Opt[T]]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opts#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):T -> x
      }
    """, """
    package base
    alias test.Bool as Bool, alias test.True as True, alias test.False as False,
    Str:{}
    _StrInstance:Str{}
    Void:{}
    Abort:{ ![R]: R -> this! } // can be optimised to just terminate (goes stuck)
    """, """
    package base
    Sealed:{}
    Int:Sealed,MathOps[Int],IntOps[Int]{
      .nat: Nat,
      .float: Float,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
    Nat:Sealed,MathOps[Nat],IntOps[Nat]{
      .int: Int,
      .float: Float,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
    Float:Sealed,MathOps[Float]{
      .int: Int,
      .nat: Nat,
      .round: Int,
      .ceil: Int,
      .floor: Int,
      **(n: Float): Float, // pow
      .isNaN: Bool,
      .isInfinity: Bool,
      .isNegInfinity: Bool,
      // not Stringable due to limitations of the Java codegen target
      .str: Str,
      }
        
    MathOps[T]:Sealed{
      +(n: T): T,
      -(n: T): T,
      *(n: T): T,
      /(n: T): T,
      %(n: T): T,
      .abs: T,
        
      // Comparisons
      >(n: T): Bool,
      <(n: T): Bool,
      >=(n: T): Bool,
      <=(n: T): Bool,
      ==(n: T): Bool,
      }
    IntOps[T]:Sealed{
      // bitwise
      >>(n: T): T,
      <<(n: T): T,
      ^(n: T): T,
      &(n: T): T,
      |(n: T): T,
        
      **(n: Nat): T, // pow
      }
        
    // Fake concrete type for all numbers. The real implementation is generated at code-gen.
    _IntInstance:Int{
      .nat -> Abort!,
      .float -> Abort!,
      .str -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
        
      // bitwise
      >>(n) -> Abort!,
      <<(n) -> Abort!,
      ^(n) -> Abort!,
      &(n) -> Abort!,
      |(n) -> Abort!,
        
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    _NatInstance:Nat{
      .int -> Abort!,
      .float -> Abort!,
      .str -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
        
      // bitwise
      >>(n) -> Abort!,
      <<(n) -> Abort!,
      ^(n) -> Abort!,
      &(n) -> Abort!,
      |(n) -> Abort!,
        
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    _FloatInstance:Float{
      .int -> Abort!,
      .nat -> Abort!,
      .str -> Abort!,
      .round -> Abort!,
      .ceil -> Abort!,
      .floor -> Abort!,
      +(n) -> Abort!,
      -(n) -> Abort!,
      *(n) -> Abort!,
      /(n) -> Abort!,
      %(n) -> Abort!,
      **(n) -> Abort!,
      .abs -> Abort!,
      // Comparisons
      >n -> Abort!,
      <n -> Abort!,
      >=n -> Abort!,
      <=n -> Abort!,
      ==n -> Abort!,
      }
    """); }

  @Test void shouldPromoteOneLentToMutToMutH() { ok("""
    package test
    Person:{ mut .name: mut Ref[Name] }
    Usage:{
      .mutate(p: mutH Person): mutH Ref[Name] -> p.name,
      }
    Ref[X]:{
      read .get: read/imm X,
      mut .set(x: X): Void
      }
    Void:{} Name:{}
    """); }

  @Test void shouldNotPromoteOneLentToMutToIso() { fail("""
    In position [###]/Dummy0.fear:4:45
    [E33 callTypeError]
    There is no possible candidate for the method call to .name/0.
    The receiver's reference capability was mutH, the method's reference capability was mut.
    The expected return types were [mut test.Ref[imm test.Name[]]], the method's return type was mut test.Ref[imm test.Name[]].
    """, """
    package test
    Person:{ mut .name: mut Ref[Name] }
    Usage:{
      .mutate(p: mutH Person): iso Ref[Name] -> p.name,
      }
    Ref[X]:{
      read .get: read/imm X,
      mut .set(x: X): Void
      }
    Void:{} Name:{}
    """); }

  @Test void invalidBoundsOnInlineLambda() { fail("""
    In position [###]/Dummy0.fear:3:6
    [E5 invalidMdfBound]
    The type mutH test.Foo[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut, imm.
    """, """
    package test
    A[X: imm, mut]:{}
    Foo:{ .bar: A[mutH Foo] -> A[mutH Foo] }
    """); }

  @Test void mixedLentPromo1b() {
    fail("""
      In position [###]/Dummy0.fear:10:21
      [E5 invalidMdfBound]
      The type mutH base.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut, imm.
      
      In position [###]/Dummy0.fear:3:4
      [E5 invalidMdfBound]
      The type mutH base.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut, imm.
      """, """
      package base
      // should also not pass with `mutH Ref[mutH B]`
      A:{ mut .b: mutH Ref[mutH B] }
      B:{}
      F:{
        .ohNo(b: mutH B): imm A -> F.ohNo'(F.newA, b),
        .ohNo'(a: mut A, b: mutH B): mut A -> F.ohNo''(a, F.break(a, b)),
        .ohNo''(a: mut A, v: Void): mut A -> a,
      
        .works: mut A -> { .b -> Ref#[mutH B]mut B{} },
        .newA: mut A -> F.newA(Ref#[mutH B]mut B{}),
        .newA(b: mut Ref[mutH B]): mut A -> { .b -> b },
        .break(a: mutH A, b: mutH B): Void -> a.b := b,
        }
      """, """
      package base
      Void:{} Sealed:{}
      Yeet:{
        #[X](x: X): Void -> this.with(x, Void),
        .with[X,R](_: X, res: R): R -> res,
        }
      Ref:{ #[X:imm,mut](x: X): mut Ref[X] -> this#(x) }
      Ref[X:imm,mut]:Sealed{
        mut *: X,
        mut .get: X -> this*,
        read *: read/imm X,
        read .get: read/imm X -> this*,
        mut .swap(x: X): X,
        mut :=(x: X): Void -> Block#(this.swap(x)),
        mut .set(x: X): Void -> this := x,
        mut <-(f: mut UpdateRef[X]): X -> this.swap(f#(this*)),
        mut .update(f: mut UpdateRef[X]): X -> this <- f,
        }
      UpdateRef[X]:{ mut #(x: X): X }
      Abort:{ ![R]: R -> this! }
      Block: {#[P1](a: P1): Void -> Void,}
      """);
  }

  @Test void invalidTraitBounds1() { fail("""
    [###]/Dummy0.fear:3:2
    [E5 invalidMdfBound]
    The type imm test.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X: mut]:{}
    B:A[imm B]
    """); }
  @Test void invalidTraitBounds2() { fail("""
    [###]/Dummy0.fear:3:2
    [E5 invalidMdfBound]
    The type imm test.B[] is not valid because its capability is not in the required bounds. The allowed modifiers are: mut.
    """, """
    package test
    A[X: mut]:{ .a1: X }
    B:A[imm B]
    """); }

  @Test void mutMdfAdapt() { fail("""
    In position [###]/Dummy0.fear:4:91
    [E30 badCapture]
    'par' cannot be captured by a mut method in a mut lambda.
    """, """
    package test
    B:{}
    L[X:mut,imm,read,readH,mutH]:{ mut .absMeth: imm X }
    A:{ read .m[T:mut,imm,read,readH,mutH](par: T) : read L[mutH T] -> mut L[mutH T]{.absMeth->par} }
    """); }

  @Test void extraMethInLambda() { ok("""
    package test
    A:{
      .m1: A -> { 'self
        .m1: A -> self.private[], // must provide full signature for private methods
        .private: A -> {}
        }
      }
    """); }
  @Test void extraMethInLambdaGens() { ok("""
    package test
    A:{
      .m1: A -> { 'self
        .m1: A -> self.private[A], // must provide full signature for private methods
        .private[X]: A -> {}
        }
      }
    """); }

  @Test void badIsoCapture() { fail("""
    In position [###]/Dummy0.fear:4:51
    [E30 badCapture]
    'mut par' cannot be captured by a mut method in a iso lambda.
    """, """
    package test
    B:{}
    L:{ mut .absMeth: mut B }
    A:{ read .m(par: mut B) : iso L -> iso L{.absMeth->par} }
    """); }
  @Test void badImmCapture() { fail("""
    In position [###]/Dummy0.fear:4:52
    [E30 badCapture]
    'read par' cannot be captured by an imm method in an imm lambda.
    """, """
    package test
    B:{}
    L:{ imm .absMeth: read B }
    A:{ read .m(par: read B) : imm L -> imm L{.absMeth->par} }
    """); }

  @Test void noIsoMoreThanOnce() { fail("""
    In position [###]/Dummy0.fear:3:63
    [E45 multipleIsoUsage]
    The isolated reference "x1" is used more than once.
    """, """
    package test
    Caps:{} Void:{}
    A:{ .break(x1: iso Caps, x2: iso Caps): Void -> this.break(x1, x1) }
    """); }
  @Test void isoOnce() { ok("""
    package test
    Caps:{} Void:{}
    A:{ .notBreak(x1: iso Caps, x2: iso Caps): Void -> this.notBreak(x1, x2) }
    """); }
  @Test void noIsoMoreThanOnceCCaptured() { fail("""
    In position [###]/Dummy0.fear:4:65
    [E45 multipleIsoUsage]
    The isolated reference "x1" is used more than once.
    """, """
    package test
    Caps:{} Void:{}
    A:{
      .break(x1: iso Caps, x2: iso Caps): Void -> this.break'(x1, B{ x1 }),
      .break'(x1: iso Caps, b: B): Void -> {},
      }
    B:{ .x: Caps }
    """); }
  @Test void noIsoMoreThanOnceCCapturedOk() { ok("""
    package test
    Caps:{} Void:{}
    A:{
      .break(x1: iso Caps, x2: iso Caps): Void -> this.break'(x1, B{ x2 }),
      .break'(x1: iso Caps, b: B): Void -> {},
      }
    B:{ .x: Caps }
    """); }

  @Test void xbsMappingWorks() {
    var expected = Set.of(Mdf.imm, Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", expected);
    assertEquals(Optional.of(expected), xbs.getO("X"));
  }
  @Test void xbsMappingMultipleWorks() {
    var expected = Set.of(Mdf.imm, Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", Set.of(Mdf.mut));
    xbs = xbs.add("Y", expected);
    assertEquals(Optional.of(expected), xbs.getO("Y"));
  }
  @Test void xbsMappingHistoryWorks() {
    var expected = Set.of(Mdf.mut);
    var xbs = XBs.empty();
    xbs = xbs.add("X", expected);
    xbs = xbs.add("Y", Set.of(Mdf.imm, Mdf.mut));
    assertEquals(Optional.of(expected), xbs.getO("X"));
  }

  @Test void methodOnInlineLambda() { ok("""
    package test
    Foo:{} Bar:{}
    A:{ .foo: Foo -> {} }
    B:{ .bar: Bar -> {} }
    Test2:{ #: Foo -> A,B{}.foo }
    Test1:{ #: Foo -> (B,A{}).foo }
    """); }

  @Test void breaksEvenWithCast() { fail("""
    In position [###]/Dummy0.fear:8:47
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between mut test.Red[read test.Foo[]] and mut test.Red[imm test.Foo[]].
    """, """
    package test
    Void:{}
    Red[T]:{
      .foo: Void,
      }
    Foo:{}
    DoIt:{
      .m1(red: mut Red[read Foo]): mut Red[Foo] -> red,
      .m2(red: mut Red[Foo]): mut Red[read Foo] -> red,
      }
    """); }

  @Test void readToReadOnlyPromotion1() { ok("""
    package test
    Test:{ #(r: readH Box[Foo]): readH Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion1ImmRet() { fail("""
    In position [###]/Dummy0.fear:2:37
    [E33 callTypeError]
    There is no possible candidate for the method call to .get/0.
    The receiver's reference capability was readH, the method's reference capability was read.
    The expected return types were [imm test.Foo[]], the method's return type was read test.Foo[].
    """, """
    package test
    Test:{ #(r: readH Box[Foo]): Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion1ImmRetReadImm() { ok("""
    package test
    Test:{ #(r: readH Box[Foo]): Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read/imm X,
      }
    """); }
  @Test void readToReadOnlyPromotion2() { ok("""
    package test
    Test:{ #(r: read Box[Foo]): readH Foo -> r.get }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }
  @Test void readToReadOnlyPromotion3() { ok("""
    package test
    Test1:{ #(r: readH MutyBox): readH Foo -> r.rb.get }
    Test2:{ #(r: read MutyBox): read Foo -> r.rb.get }
    MutyBox:{ mut .mb: mut Box[Foo], read .rb: read Box[Foo] }
    """, """
    package test
    Foo:{}
    Box[X]:{
      mut  .get: X,
      read .get: read X,
      }
    """); }

  @Test void inferMultipleTraits1() { ok("""
    package a
    A:{ .foo: A } B:{ .bar: B -> this }
    Test:{ #: B -> A,B{'self .foo -> self } }
    """); }

  @Test void contravarianceBox() { fail("""
    In position [###]/Dummy0.fear:12:41
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm test.Box[imm test.Student[]] and imm test.Box[imm test.Person[]].
    """, """
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: Box[Nat] }
    Box[T]:{
      mut .get: T,
      read .get: read T,
      imm .get: T,
    }
    
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      .simple(rs: read Student): read Person -> rs,
      .simpleMdf(rs: imm Student): read Person -> rs,
      }
    """); }

  @Test void contravarianceBoxMatcherExtensionMethod() { fail("""
    In position [###]/Dummy0.fear:13:41
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm test.Box[imm test.Student[]] and imm test.Box[imm test.Person[]].
    """, """
    package test
    Nat:{} Str:{}
    Person:{ read .name: Str, read .age: Nat, }
    Student:Person{ read .grades: Box[Nat] }
    BoxMatcher[T,R]:{ mut #: R }
    BoxExtension[T,R]:{ mut #(self: T): R }
    Box[T]:{
      .match[R](m: mut BoxMatcher[T, R]): R -> m#,
      .extend[R](ext: mut BoxExtension[Box[T], R]): R -> ext#this,
    }
    
    Ex:{
      .nums(o: Box[Student]): Box[Person] -> o,
      }
    """); }

  // Error message is from method body promotions
  @Test void badGenericPromotionIso() { fail("""
    In position [###]/Dummy0.fear:3:43
    [E28 undefinedName]
    The identifier "y" is undefined or cannot be captured.
    """, """
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: Y): iso Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: mut Baz): Beer[mut Baz] -> Bar.k(y),
      .ohNo(y: mut Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void badGenericPromotionImm() { fail("""
    In position [###]/Dummy0.fear:3:37
    [E66 invalidMethodArgumentTypes]
    Method .m/1 called in position [###]/Dummy0.fear:3:37 cannot be called with current parameters of types:
    [Y]
    Attempted signatures:
    (iso Y):iso test.Beer[Y] kind: IsoHProm
    (iso Y):iso test.Beer[Y] kind: IsoProm
    """, """
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: Y): imm Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: mut Baz): Beer[mut Baz] -> Bar.k(y),
      .ohNo(y: mut Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void okGenericPromotion() { ok("""
    package test
    Foo:{ .m[X](x: X): mut Beer[X] -> {x} }
    Bar:{ .k[Y](y: iso Y): iso Beer[Y] -> Foo.m[Y](y) }
    Break:{
      .m1(y: iso Baz): Beer[mut Baz] -> Bar.k[mut Baz](y),
      .ohNo(y: iso Baz): imm Baz -> this.m1(y).x,
      }
    """, """
    package test
    Baz:{}
    Beer[X]:{ mut .x: X, read .x: read X }
    Block:{
      #[X:read,mut,imm,iso, R:read,mut,imm,iso](_: X, res: R): R -> res,
      }
    Abort:{ ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }

  @Test void superSimple() { ok("""
    package test
    Test:{
    	.foo[Y](x: Y): Test -> Test{ .foo(hello) -> Test },
    }
    """); }

  @Test void pointColourPoint() { ok("""
    package test
    FPoint:{ #(x: Num, y: Num): Point -> { .x -> x, .y -> y } }
    Point:{
      .x : Num,
      .y: Num,
      .withX(x: Num): Point -> FPoint#(x, this.y),
      .withY(y: Num): Point -> FPoint#(this.x, y),
      }
    ColourPoint:Point{
      .colour: Colour,
      .withX(x: Num): ColourPoint -> FColourPoint#(x, this.y, this.colour),
      .withY(y: Num): ColourPoint -> FColourPoint#(this.x, y, this.colour),
      }
    FColourPoint:{ #(x: Num, y: Num, colour: Colour): ColourPoint -> {
      .x -> x, .y -> y, .colour -> colour,
      }}
    Usage:{ #(cp: ColourPoint): ColourPoint -> cp.withX(Five) }
    """, """
    package test
    Colour:{}
    Num:{}
    Five:Num{}
    """); }

  @Test void pointColourPointWrapper() { ok("""
    package test
    FPoint:{ #(x: Num, y: Num): Point -> { .x -> x, .y -> y } }
    Point:{
      .x : Num,
      .y: Num,
      .withX(x: Num): Point -> FPoint#(x, this.y),
      .withY(y: Num): Point -> FPoint#(this.x, y),
      .withXY(x: Num, y: Num): Point -> FPoint#(this.x, this.y),
      }
    ColourPoint:{
      .x : Num,
      .y: Num,
      .colour: Colour,
      .withX(x: Num): ColourPoint -> FColourPoint#(x, this.y, this.colour),
      .withY(y: Num): ColourPoint -> FColourPoint#(this.x, y, this.colour),
      .point: Point -> {
        .x -> this.x,
        .y -> this.y,
        .withX(x) -> this.withX(x).point,
        .withY(y) -> this.withY(y).point,
        },
      .withPoint(p: Point): ColourPoint -> this.withX(p.x).withY(p.y),
      .withXY(x: Num, y: Num): Point -> this.point.withXY(x, y),
      .withXYCP(x: Num, y: Num): ColourPoint -> this.withPoint(this.withXY(x, y)),
      }
    FColourPoint:{ #(x: Num, y: Num, colour: Colour): ColourPoint -> {
      .x -> x, .y -> y, .colour -> colour,
      }}
    Usage:{ #(cp: ColourPoint): Point -> cp.point.withX(Five) }
    """, """
    package test
    Colour:{}
    Num:{}
    Five:Num{}
    """); }

  @Test void branchingReturnTypes() { ok("""
    package a
    Opt:{ #[T](x: T): mut Opt[T] -> { .match(m) -> m.some(x) }}
    Opt[T]:{
      mut  .match[R](m: mut OptMatch[T, R]): R -> m.empty,
      read .match[R](m: mut OptMatch[read T, R]): R -> m.empty,
      imm  .match[R](m: mut OptMatch[imm T, R]): R -> m.empty
      }
    OptMatch[T,R]:{ mut .some(x: T): R, mut .empty: R }
    N: {}
    Zero: N{}
    Test:{ .test(opt: Opt[N]): N -> opt.match{
      .some(n) -> n,
      .empty -> Zero,
      }}
    """); }

  @Test void genMethMini() { ok("""
    package test
    V: {}
    M: {.m[X](x:X):X}
    MV: {.mv(v:V): V -> M{x->x}.m[V](v)}
    """); }
  
  @Test void foldAccExplicit() { ok("""
    package test
    Num: { +(other: Num): Num }
    Zero: Num{ +(other) -> other, }
    One: Num{ +(other) -> Abort! }
    List[E]: { .fold[S](acc: S, f: Fold[S, E]): S -> Abort! }
    Fold[S,T]: { #(acc: S, x: T): S }
    
    Break:{ #(l: List[Num]): Num 
      -> l.fold[Num](Zero, Fold[Num, Num]{acc, n -> acc + n}) }
    
    Abort: { ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }
  @Test void foldAccInferred() { ok("""
    package test
    Num: { +(other: Num): Num }
    Zero: Num{ +(other) -> other, }
    One: Num{ +(other) -> Abort! }
    List[E]: { .fold[S](acc: S, f: Fold[S, E]): S -> Abort! }
    Fold[S,T]: { #(acc: S, x: T): S }
    
    Break:{ #(l: List[Num]): Num -> l.fold[Num](Zero, {acc, n -> acc + n}) }
    
    Abort: { ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }

  @Test void genericSimplificationSubTyping() { ok("""
    package test
    Default: {} Foo: {}
    A[X,Y]: {}
    B[X]: A[X,Default]
    Break:{ #(b: B[Foo]): A[Foo,Default] -> b }
    """); }

  @Test void noImpossibleLambda() {fail("""
    In position file:///[###]/Dummy0.fear:2:15
    [E62 lambdaImplementsGeneric]
    A lambda may not implement a generic type parameter 'X'
    """, """
    package test
    A[X]: {#: X -> {}}
    """);}

  @Test void inlineLambdaMethodCopying() {ok("""
    package test
    A[E]: {
      mut  .get(v: V): E -> this.get(v),
      read .get(v: V): read/imm E -> this.get(v),
      mut .nest(e: E): mut A[E] -> {
//        .get(v) -> v#e,
        mut  .get(v: V): E -> v#e,
        read .get(v: V): read/imm E -> v#[read/imm E]e,
        },
      }
    V: {#[X](x: X): X -> x}
    """);}

  @Test void sameGenImpl() {ok("""
    package test
    A[X]: {}
    Foo:{} Bar:{}
    B: A[Foo],A[Bar]{}
    Caller: {
      .m1: Void -> CallMe.m1(B),
      .m2: Void -> CallMe.m2(B),
      }
    CallMe: {
      .m1(a: A[Foo]): Void -> {},
      .m2(a: A[Bar]): Void -> {},
      }
    Void: {}
    """);}

  @Test void namedLiteral() {ok("""
    package a
    List[T]:{} Bob:{}
    Bar[X]: {.m(x: X): mut Foo[X] -> mut Foo[X]:{
      mut .get: X -> x
      }}
    CanCall: {#: Bob -> Bar[Bob].m(Bob).get}
    """);}

  @Test void immThisAsImmInReadMethod() { ok("""
    package test
    A: {.m1: imm B -> B: {'self
      imm .foo: B -> self,
      read .bar: B -> self.foo,
      }}
    """); }

  @Test void retTypeSoundness() {fail("""
    In position [###]/Dummy0.fear:4:22
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm test.B[] and imm test.A[].
    """, """
    package test
    A: {}
    B: {}
    ToA: {.m1(b: B): A -> b}
    Call: {#: A -> ToA.m1(B)}
    """);}

  @Test void magicRetTypeSoundness() {fail("""
    In position [###]/Dummy0.fear:4:24
    [E37 noSubTypingRelationship]
    There is no sub-typing relationship between imm base.Str[] and imm test.A[].
    """, """
    package test
    alias base.Str as Str,
    A: {}
    ToA: {.m1(s: Str): A -> s}
    Call: {#: A -> ToA.m1("hello")}
    """, """
    package base
    Str: {}
    _StrInstance: Str{}
    """);}

  @Test void isoLiteral() {fail("""
    In position [###]/Dummy0.fear:4:39
    [E30 badCapture]
    'mut a' cannot be captured by a mut method in a iso lambda.
    """, """
    package test
    A: {}
    B: {}
    Foos: {#(a: mut A): iso Foo -> iso Foo{a}}
    Foo: {mut #: mut A}
    """);}

  @Test void testBoundsGenMethodOk() {ok("""
    package test
    A: {#[X: mut,mutH,readH](x: X, f: mut Foo): mut Foo -> f}
    Expect: {.isoFoo(f: iso Foo): iso Foo -> f} // to ignore flexible method typing
    Good: {#[Y: mutH,readH](y: Y, isoF: iso Foo): iso Foo -> Expect.isoFoo(A#[Y](y, isoF))}
    Concrete: {#(y: mutH Foo, isoF: iso Foo): iso Foo -> Expect.isoFoo(A#[mutH Foo](y, isoF))}
    Foo: {}
    """);}
  @Test void testBoundsGenMethod() {fail("""
    In position [###]/Dummy0.fear:4:75
    [E66 invalidMethodArgumentTypes]
    Method #/2 called in position [###]/Dummy0.fear:4:75 cannot be called with current parameters of types:
    [Y, iso test.Foo[] ()]
    Attempted signatures:
    (iso Y, iso test.Foo[]):iso test.Foo[] kind: IsoHProm
    (iso Y, iso test.Foo[]):iso test.Foo[] kind: IsoProm
    """, """
    package test
    A: {#[X: mut,mutH,readH](x: X, f: mut Foo): mut Foo -> f}
    Expect: {.isoFoo(f: iso Foo): iso Foo -> f} // to ignore flexible method typing
    Bad: {#[Y: mut,mutH,readH](y: Y, isoF: iso Foo): iso Foo -> Expect.isoFoo(A#[Y](y, isoF))}
    Good: {#[Y: mutH,readH](y: Y, isoF: iso Foo): iso Foo -> Expect.isoFoo(A#[Y](y, isoF))}
    Concrete: {#(y: mutH Foo, isoF: iso Foo): iso Foo -> Expect.isoFoo(A#[mutH Foo](y, isoF))}
    Foo: {}
    """);}
}
