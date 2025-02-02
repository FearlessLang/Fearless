package parser;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static program.typesystem.RunTypeSystem.fail;

class TestFullParser {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    var res = Parser.parseAll(ps, new TypeSystemFeatures());
    Err.strCmpFormat(expected,res.toString());
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps, new TypeSystemFeatures());
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void testEmptyPackage(){ ok("""
    {}
    """,
    """
    package pkg1
    """); }
  @Test void testMultiFile(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg1
      """); }
  @Test void testAliasConflictsPackageLocal1(){ ok("""
    {}
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg2
      alias base.True as True,
      """); }
  @Test void failConflictingAliases1(){ fail("""
    [E1 conflictingAlias]
    This alias is in conflict with other aliases in the same package: True
    conflicts:
    ([###]Dummy0.fear:2:0) alias base.True[] as True
    ([###]Dummy1.fear:2:0) alias base.True[] as True
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg1
      alias base.True as True,
      """); }
  @Test void aliasAsDifferentName() { ok("""
    {foo.Bar/0=Dec[name=foo.Bar/0,gxs=[],lambda=[-infer-][]{}],
    test.Bloop/0=Dec[name=test.Bloop/0,gxs=[],lambda=[-infer-][foo.Bar[]]{}]}
    """, """
    package test
    alias foo.Bar as Baz,
    Bloop:Baz
    """, """
    package foo
    Bar:{}
    """); }
  @Test void aliasGenericHiding() { ok("""
    {test.Yolo/0=Dec[name=test.Yolo/0,gxs=[],lambda=[-infer-][]{}],
    test.Bloop3/0=Dec[name=test.Bloop3/0,gxs=[],lambda=[-infer-][foo.Bar[immtest.Yolo[],immtest.Yolo[]]]{}],
    foo.Bar/0=Dec[name=foo.Bar/0,gxs=[],lambda=[-infer-][]{}],foo.Bar/2=Dec[name=foo.Bar/2,gxs=[A,B],lambda=[-infer-][]{}],
    foo.Bar/1=Dec[name=foo.Bar/1,gxs=[A],lambda=[-infer-][]{}],
    test.Bloop2/0=Dec[name=test.Bloop2/0,gxs=[],lambda=[-infer-][foo.Bar[immtest.Yolo[]],foo.Bar[immtest.Yolo[]]]{}],
    test.Bloop1/0=Dec[name=test.Bloop1/0,gxs=[],lambda=[-infer-][foo.Bar[]]{}]}
    """, """
    package test
    alias foo.Bar as Baz,
    alias foo.Bar[test.Yolo] as YoloBar,
    Yolo:{}
    Bloop1:Baz
    Bloop2:Baz[Yolo],YoloBar{}
    Bloop3:YoloBar[Yolo]
    """, """
    package foo
    Bar:{}
    Bar[A]:{}
    Bar[A,B]:{}
    """); }
  @Test void testMultiPackage(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg2
      """,
      """
      package pkg1
      """); }
  @Test void testOneDecl(){ ok("""
    {pkg1.MyTrue/0=Dec[name=pkg1.MyTrue/0,gxs=[],lambda=[-infer-][base.True[]]{}]}
    """,
    """
    package pkg1
    MyTrue:base.True
    """); }
  @Test void testManyDecls(){ ok("""
    {pkg1.My12/0=Dec[name=pkg1.My12/0,gxs=[],lambda=[-infer-][12[]]{}],pkg1.MyFalse/0=Dec[name=pkg1.MyFalse/0,gxs=[],lambda=[-infer-][base.False[]]{}],
    pkg2.MyTrue/0=Dec[name=pkg2.MyTrue/0,gxs=[],lambda=[-infer-][base.True[]]{}],
    pkg1.MyTrue/0=Dec[name=pkg1.MyTrue/0,gxs=[],lambda=[-infer-][base.True[]]{}]}
    """,
      """
      package pkg1
      MyTrue:base.True
      MyFalse:base.False
      """,
      """
      package pkg1
      alias 12 as Twelve,
      My12:Twelve
      """,
      """
      package pkg2
      MyTrue:base.True
      """); }
  @Test void failConflictingDecls1(){ fail("""
      [E2 conflictingDecl]
      This trait declaration is in conflict with other trait declarations in the same package: MyTrue/0
      conflicts:
      ([###]/Dummy0.fear:2:0) MyTrue/0
      ([###]/Dummy1.fear:3:0) MyTrue/0
          """,
    """
    package pkg1
    MyTrue:base.True
    """,
    """
    package pkg1
    MyFalse:base.False
    MyTrue:base.True
    """); }

  @Test void baseVoid(){ ok("""
    {base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{}]}
    """, """
    package base
    Void:{}
    """
    );}
  @Test void baseLoopSingleMeth(){ ok("""
    {pkg1.Loop/0=Dec[name=pkg1.Loop/0,gxs=[],lambda=[-infer-][base.AbstractLoop[]]{[-]([]):[-]->this:infer!/0[-]([]):infer}]}
    """, """
    package pkg1
    alias base.AbstractLoop as AbsLoop,
    Loop:AbsLoop{this!}
    """
  );}
  @Test void baseLoop(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=
      [-infer-][]{!/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{!:Void->this!}
    """
  );}
  @Test void baseLoopExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-infer-][]{!/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{imm !():imm Void->this!}
    """
  );}
  @Test void baseLoopMoreExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-infer-][]{ !/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop[]:{imm ![]():imm Void[]->this!}
    """
  );}
  @Test void baseLoopAbs(){ ok("""
    {base.AbsLoop/0=Dec[name=base.AbsLoop/0,gxs=[],lambda=[-infer-][]{ !/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->[-]}]}
    """, """
    package base
    AbsLoop:{!:base.Void}
    """
  );}
  @Test void methWithArgs(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/1([a]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A): A,}
    """
  );}
  @Test void methWith2Args(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[gens=[],ts=[imm base.A[],imm base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: A): A,}
    """
  );}
  @Test void methWith2ArgsAndMdf(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[gens=[],ts=[imm base.A[],read base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: read A): A,}
    """
    );}
    @Test void methWithGens1(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[gens=[B],ts=[imm base.A[],readB],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): A,}
    """
    );}
  @Test void methWithGens2(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[gens=[B],ts=[imm base.A[],read B],ret=read B]->[-]}]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): read B,}
    """
  );}
  @Test void failConcreteInGens(){ fail("""
    In position [###]/Dummy0.fear:2:7
    [E3 concreteTypeInFormalParams]
    Trait and method declarations may only have generic type parameters. This concrete type was provided instead:
    imm base.A[]
    Alternatively, are you attempting to shadow an existing class name?
    """, """
    package base
    A:{.foo[A](a: A, b: A): A}
    """
  );}
  @Test void extendsNewDec(){ ok("""
    {base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-infer-][]{.name/0([]):Sig[gens=[],ts=[],ret=imm base.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-infer-][base.HasName[]]{}]}
    """, """
    package base
    alias base.String as String,
    HasName:{ .name: String, }
    Dog:HasName
    """
  );}
  @Test void multipleExtends(){ ok("""
    {base.HasHunger/0=Dec[name=base.HasHunger/0,gxs=[],lambda=[-infer-][]{.hunger/0([]):Sig[gens=[],ts=[],ret=immbase.Nat[]]->[-]}],
    base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-infer-][]{.name/0([]):Sig[gens=[],ts=[],ret=immbase.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-infer-][base.HasHunger[],base.HasName[]]{}]}
    """, """
    package base
    alias base.Nat as Nat, alias base.String as String,
    HasHunger:{ .hunger: Nat, }
    HasName:{ .name: String, }
    Dog:HasHunger,HasName{}
    """
  );}
  @Test void equalsSugar1(){ ok("""
      {base.B/0=Dec[
        name=base.B/0,
        gxs=[],
        lambda=[-infer-][]{#/0([]):Sig[gens=[],ts=[],ret=imm 5[]]->[-imm base.A[]-][base.A[]]{}.foo/2[-]([
          [-imm 5[]-][5[]]{},[-infer-][]{[-]([lol,fear0$]):[-]->fear0$:infer}
        ]):infer}
      ],base.Cont/2=Dec[
        name=base.Cont/2,
        gxs=[X,R],
        lambda=[-infer-][]{#/2([x,self]):Sig[gens=[],ts=[X,immbase.A[]],ret=R]->[-]}],
      base.A/0=Dec[
        name=base.A/0,
        gxs=[],
        lambda=[-infer-][]{.foo/2([x,cont]):Sig[gens=[T],ts=[T,mutbase.Cont[T,T]],ret=T]->
          cont:infer#/2[-]([x:infer,cont:infer]):infer}]}
    """, """
    package base
    Cont[X,R]:{ mut #(x: X, self: A): R }
    A:{ .foo[T](x: T, cont: mut Cont[T, T]): T -> cont#(x, cont) }
    B:{ #: 5 -> A
      .foo (lol=5)
      }
    """
  );}
  @Test void equalsSugar2() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-infer-][]{#/2([x,cont]):Sig[gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-infer-][]{#/0([]):Sig[gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-infer-][]{
      .sugar/2([x,cont]):Sig[gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-infer-][]{
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-]([[-infer-][]{[-]([]):[-]->f:infer.v/0[-]([]):infer}]):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-infer-][]{.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-infer-][]{}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: X, cont: mut Candy[R]): R }
    ReturnStmt[R]:{ mut #: R }
    Candy[R]:{
      mut .sugar[X](x: X, cont: mut Cont[X, R]): R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[R]): R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo](f = Foo)
        .return{ f.v }
      }
    """); }
  @Test void equalsSugar2a() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-infer-][]{#/2([x,cont]):Sig[gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-infer-][]{#/0([]):Sig[gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-infer-][]{
      .sugar/2([x,cont]):Sig[gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-infer-][]{
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-]([[-infer-][]{[-]([]):[-]->f:infer.v/0[-]([]):infer}]):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-infer-][]{.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-infer-][]{}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: X, cont: mut Candy[R]): R }
    ReturnStmt[R]:{ mut #: R }
    Candy[R]:{
      mut .sugar[X](x: X, cont: mut Cont[X, R]): R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[R]): R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo] f = Foo
        .return{ f.v }
      }
    """); }

  @Test void refDef() { ok("""
    {base.Let/2=Dec[
      name=base.Let/2,
      gxs=[V,R],
      lambda=[-infer-][]{
        .var/0([]):Sig[gens=[],ts=[],ret=V]->[-],
        .in/1([v]):Sig[gens=[],ts=[V],ret=R]->[-]
      }],
    base.Ref/1=Dec[
      name=base.Ref/1,
      gxs=[X],
      lambda=[-infer-][base.NoMutHyg[X],base.Sealed[]]{
        */0([]):Sig[gens=[],ts=[],ret=read/immX]->[-],
        .swap/1([x]):Sig[gens=[],ts=[X],ret=X]->[-],
        :=/1([x]):Sig[gens=[],ts=[X],ret=imm base.Void[]]->
          [-imm base.Let[]-][base.Let[]]{}#/1[-]([[-infer-][]{
            .var/0([]):[-]->this:infer.swap/1[-]([x:infer]):infer,
            .in/1([fear0$]):[-]->[-imm base.Void[]-][base.Void[]]{}
          }]):infer,
          <-/1([f]):Sig[gens=[],ts=[imm base.UpdateRef[X]], ret=X]->
            this:infer.swap/1[-]([f:infer#/1[-]([this:infer*/0[-]([]):infer]):infer]):infer
      }],
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-infer-][]{}],
    base.Ref/0=Dec[name=base.Ref/0,gxs=[],lambda=[-infer-][]{#/1([x]):Sig[gens=[X],ts=[X],ret=mut base.Ref[X]]->this:infer#/1[-]([x:infer]):infer}],
    base.Let/0=Dec[name=base.Let/0,gxs=[],lambda=[-infer-][]{#/1([l]):Sig[gens=[V,R],ts=[imm base.Let[V,R]],ret=R]->l:infer.in/1[-]([l:infer.var/0[-]([]):infer]):infer}],
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],lambda=[-infer-][]{}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{}],
    base.UpdateRef/1=Dec[name=base.UpdateRef/1,gxs=[X],lambda=[-infer-][]{#/1([x]):Sig[gens=[],ts=[X],ret=X]->[-]}]}
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var: V, .in(v:V): R }
    Ref:{ #[X](x: X): mut Ref[X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : read/imm X,
      mut .swap(x: X): X,
      mut :=(x: X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[X]): X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: X): X }
    """);}

  @Test void magicIntbers() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{.foo/0([]):Sig[gens=[],ts=[],ret=imm5[]]->[-imm 5[]-][5[]]{}}]}
    """, """
    package test
    A:{ .foo: 5 -> 5 }
    """); }

  @Test void multiArgInferredWithName() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-infer-][test.A[]]{
      .foo/2([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ .foo(a, b) -> b }
    """); }
  @Test void multiArgInferred1() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-infer-][test.A[]]{
      [-]([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ a, b -> b }
    """); }

  @Test void noModifiersInFormalTypeParams1() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E46 noMdfInFormalParams]
    Modifiers are not allowed in declarations or implementation lists: mut B
    """, """
    package test
    A[mut B]:{}
    """); }
  @Test void noModifiersInFormalTypeParams2() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[B],lambda=[-infer-][]{}]}
    """, """
    package test
    A[B]:{}
    """); }
  @Test void noModifiersInImpls() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E46 noMdfInFormalParams]
    Modifiers are not allowed in declarations or implementation lists: mut A
    """, """
    package test
    A:{}
    B:mut A{}
    """); }

  @Test void Bounds1() { ok("""
    {test.A1/1=Dec[name=test.A1/1,gxs=[B],bounds={B=[imm]},lambda=[-infer-][]{}],
    test.A2/1=Dec[name=test.A2/1,gxs=[B],bounds={B=[imm,mut]},lambda=[-infer-][]{}],
    test.A3/2=Dec[name=test.A3/2,gxs=[B,C],bounds={B=[imm,mut]},lambda=[-infer-][]{}],
    test.A4/3=Dec[name=test.A4/3,gxs=[B,C,D],bounds={B=[imm,mut],D=[mutH,readH]},lambda=[-infer-][]{}]}
    """, """
    package test
    A1[B: imm]:{}
    A2[B: imm,mut]:{}
    A3[B: imm,mut, C]:{}
    A4[B: imm,mut, C, D: readH,mutH]:{}
    """); }

  @Test void concreteGensImm() { ok("""
    {test.G/1=Dec[name=test.G/1,gxs=[X],lambda=[-infer-][]{}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-infer-][]{}],
    test.A/1=Dec[name=test.A/1,gxs=[Z],lambda=[-infer-][test.G[imm test.B[]],test.G[Z]]{}]}
    """, """
    package test
    A[Z]:G[B],G[Z]{}
    B:{}
    G[X]:{}
    """); }
  @Test void ForGensRet() { ok("""
    {test.G/1=Dec[name=test.G/1,gxs=[X],lambda=[-infer-][]{}],
    test.B/1=Dec[name=test.B/1,gxs=[Y],lambda=[-infer-][]{
      .m1/0([]):Sig[gens=[],ts=[],ret=Y]->[-]}]}
    """, """
    package test
    B[Y]:{ .m1: Y }
    G[X]:{}
    """); }

  @Test void namedInline() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{#/0([]):Sig[gens=[],ts=[],ret=imm test.B[]]->
      LambdaId[id=test.B/0,gens=[],bounds={}]:[-imm test.B[]-][]{}}]}
    """, """
    package test
    A:{ #: B -> B:{} }
    """); }
  @Test void namedInlineGens() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[-infer-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=immtest.B[X]]->
        LambdaId[id=test.B/1,gens=[X],bounds={}]:[-imm test.B[X]-][]{
          .m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}}]}
    """, """
    package test
    A[X]:{ #(x: X): B[X] -> B[X]:{ .m1: X -> x } }
    """); }
  @Test void namedInlineGensWithBound() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],bounds={X=[imm]},lambda=[-infer-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=immtest.B[X]]->
        LambdaId[id=test.B/1,gens=[X],bounds={X=[imm]}]:[-immtest.B[X]-][]{
          .m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}}]}
    """, """
    package test
    A[X:imm]:{ #(x: X): B[X] -> B[X:imm]:{ .m1: X -> x } }
    """); }
  @Test void inferNameOnCall() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],bounds={X=[imm]},lambda=[-infer-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=X]->
        [-imm test.Fear0$[X]-][test.Fear0$[X]]{.m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}.m1/0[-]([]):infer}]}
    """, """
    package test
    A[X:imm]: {#(x: X): X -> {.m1: X -> x}.m1}
    """); }

  @Test void missingColonTypeDeclaration() { fail("""
    In position [###]/Dummy0.fear:2:3
    [E59 syntaxError]
    missing ':' at '{'
    """, """
    package base
    Foo{}
    // I missed a colon :(
    """); }

  @Test void noImplGeneric() {fail("""
    In position [###]/Dummy0.fear:2:6
    [E14 expectedConcreteType]
    A concrete type was expected but the following generic type was given:
    X
    """, """
    package a
    A[X]: X{}
    """);}
}
