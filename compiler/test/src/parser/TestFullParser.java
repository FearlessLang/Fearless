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
    {foo.Bar/0=Dec[name=foo.Bar/0,gxs=[],lambda=[-mutfoo.Bar[]-][]{}],
    test.Bloop/0=Dec[name=test.Bloop/0,gxs=[],lambda=[-muttest.Bloop[]-][foo.Bar[]]{}]}
    """, """
    package test
    alias foo.Bar as Baz,
    Bloop:Baz{}
    """, """
    package foo
    Bar:{}
    """); }
  @Test void aliasGenericHiding() { ok("""
    {
    test.Bloop4/0=Dec[name=test.Bloop4/0,gxs=[],lambda=[-muttest.Bloop4[]-][foo.Bar[immtest.Yolo[],immtest.Bloop3[]]]{}],
    test.Yolo/0=Dec[name=test.Yolo/0,gxs=[],lambda=[-muttest.Yolo[]-][]{}],
    test.Bloop3/0=Dec[name=test.Bloop3/0,gxs=[],lambda=[-muttest.Bloop3[]-][foo.Bar[immtest.Yolo[],immtest.Yolo[]]]{}],
    foo.Bar/0=Dec[name=foo.Bar/0,gxs=[],lambda=[-mutfoo.Bar[]-][]{}],
    foo.Bar/2=Dec[name=foo.Bar/2,gxs=[A,B],bounds={A=[imm],B=[imm]},lambda=[-mutfoo.Bar[A,B]-][]{}],
    foo.Bar/1=Dec[name=foo.Bar/1,gxs=[A],bounds={A=[imm]},lambda=[-mutfoo.Bar[A]-][]{}],
    test.Bloop2/0=Dec[name=test.Bloop2/0,gxs=[],lambda=[-muttest.Bloop2[]-][foo.Bar[immtest.Yolo[]],foo.Bar[immtest.Yolo[]]]{}],
    test.Bloop1/0=Dec[name=test.Bloop1/0,gxs=[],lambda=[-muttest.Bloop1[]-][foo.Bar[]]{}]}
    """, """
    package test
    alias foo.Bar as Baz,
    alias foo.Bar[test.Yolo] as YoloBar,
    Yolo:{}
    Bloop1:Baz{}
    Bloop2:Baz[Yolo],YoloBar{}
    Bloop3:YoloBar[Yolo]{}
    Bloop4:YoloBar[Bloop3]{}
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
    {pkg1.MyTrue/0=Dec[name=pkg1.MyTrue/0,gxs=[],lambda=[-mutpkg1.MyTrue[]-][base.True[]]{}]}
    """,
    """
    package pkg1
    MyTrue:base.True{}
    """); }
  @Test void testManyDecls(){ ok("""
    {pkg1.My12/0=Dec[name=pkg1.My12/0,gxs=[],lambda=[-mutpkg1.My12[]-][12[]]{}],
    pkg1.MyFalse/0=Dec[name=pkg1.MyFalse/0,gxs=[],lambda=[-mutpkg1.MyFalse[]-][base.False[]]{}],
    pkg2.MyTrue/0=Dec[name=pkg2.MyTrue/0,gxs=[],lambda=[-mutpkg2.MyTrue[]-][base.True[]]{}],
    pkg1.MyTrue/0=Dec[name=pkg1.MyTrue/0,gxs=[],lambda=[-mutpkg1.MyTrue[]-][base.True[]]{}]}
    """,
      """
      package pkg1
      MyTrue:base.True{}
      MyFalse:base.False{}
      """,
      """
      package pkg1
      alias 12 as Twelve,
      My12:Twelve{}
      """,
      """
      package pkg2
      MyTrue:base.True{}
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
    MyTrue:base.True{}
    """,
    """
    package pkg1
    MyFalse:base.False{}
    MyTrue:base.True{}
    """); }

  @Test void baseVoid(){ ok("""
    {base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mutbase.Void[]-][]{}]}
    """, """
    package base
    Void:{}
    """
    );}
  @Test void baseLoopSingleMeth(){ ok("""
    {pkg1.Loop/0=Dec[name=pkg1.Loop/0,gxs=[],lambda=[-mutpkg1.Loop[]-][base.AbstractLoop[]]{[-]([]):[-]->this:infer!/0[-]([]):infer}]}
    """, """
    package pkg1
    alias base.AbstractLoop as AbsLoop,
    Loop:AbsLoop{this!}
    """
  );}
  @Test void baseLoop(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=
      [-mutbase.Loop[]-][]{!/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{!:Void->this!}
    """
  );}
  @Test void baseLoopExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-mutbase.Loop[]-][]{!/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{imm !():imm Void->this!}
    """
  );}
  @Test void baseLoopMoreExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-mutbase.Loop[]-][]{ !/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop[]:{imm ![]():imm Void[]->this!}
    """
  );}
  @Test void baseLoopAbs(){ ok("""
    {base.AbsLoop/0=Dec[name=base.AbsLoop/0,gxs=[],lambda=[-mutbase.AbsLoop[]-][]{ !/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->[-]}]}
    """, """
    package base
    AbsLoop:{!:base.Void}
    """
  );}
  @Test void methWithArgs(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{.foo/1([a]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A): A,}
    """
  );}
  @Test void methWith2Args(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{.foo/2([a,b]):Sig[gens=[],ts=[imm base.A[],imm base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: A): A,}
    """
  );}
  @Test void methWith2ArgsAndMdf(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{.foo/2([a,b]):Sig[gens=[],ts=[imm base.A[],read base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: read A): A,}
    """
    );}
    @Test void methWithGens1(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{.foo/2([a,b]):Sig[gens=[B],bounds={B=[imm]},ts=[imm base.A[],readB],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): A,}
    """
    );}
  @Test void methWithGens2(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{.foo/2([a,b]):
      Sig[gens=[B],bounds={B=[imm]},ts=[imm base.A[],read B],ret=read B]->[-]}]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): read B,}
    """
  );}
  @Test void methWithGensBoundsStar(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{
      .foo/2([a,b]):Sig[gens=[B],bounds={B=[imm,mut,read]},ts=[immbase.A[],B],ret=B]->[-]
      }]}
    """, """
    package base
    A:{.foo[B:*](a: A, b: B): B,}
    """
    );}
  @Test void methWithGensBoundsStarStar(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-mutbase.A[]-][]{
      .foo/2([a,b]):Sig[gens=[B],bounds={B=[imm,iso,mut,mutH,read,readH]},ts=[immbase.A[],B],ret=B]->[-]
    }]}
    """, """
    package base
    A:{.foo[B:**](a: A, b: B): B,}
    """
    );}
  @Test void classWithGensBoundsStar(){ ok("""
    {base.A/1=Dec[name=base.A/1,gxs=[B],bounds={B=[imm,mut,read]},lambda=[-mutbase.A[B]-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immbase.A[],B],ret=B]->[-]
      }]}
    """, """
    package base
    A[B:*]:{.foo(a: A, b: B): B,}
    """
    );}
  @Test void classWithGensBoundsStarStar(){ ok("""
    {base.A/1=Dec[name=base.A/1,gxs=[B],bounds={B=[imm,iso,mut,mutH,read,readH]},lambda=[-mutbase.A[B]-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immbase.A[],B],ret=B]->[-]
      }]}
    """, """
    package base
    A[B:**]:{.foo(a: A, b: B): B,}
    """
    );}
  @Test void failConcreteInGens(){ fail("""
    In position [###]/Dummy0.fear:2:8
    [E3 concreteTypeInFormalParams]
    Trait and method declarations may only have generic type parameters. This concrete type was provided instead:
    base.A[]
    Alternatively, are you attempting to shadow an existing class name?
    """, """
    package base
    A:{.foo[A](a: A, b: A): A}
    """
  );}
  @Test void extendsNewDec(){ ok("""
    {base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-mutbase.HasName[]-][]{.name/0([]):Sig[gens=[],ts=[],ret=imm base.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-mutbase.Dog[]-][base.HasName[]]{}]}
    """, """
    package base
    alias base.String as String,
    HasName:{ .name: String, }
    Dog:HasName{}
    """
  );}
  @Test void multipleExtends(){ ok("""
    {base.HasHunger/0=Dec[name=base.HasHunger/0,gxs=[],lambda=[-mutbase.HasHunger[]-][]{.hunger/0([]):Sig[gens=[],ts=[],ret=immbase.Nat[]]->[-]}],
    base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-mutbase.HasName[]-][]{.name/0([]):Sig[gens=[],ts=[],ret=immbase.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-mutbase.Dog[]-][base.HasHunger[],base.HasName[]]{}]}
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
        lambda=[-mutbase.B[]-][]{#/0([]):Sig[gens=[],ts=[],ret=imm base.natLit.5[]]->[-imm base.A[]-][base.A[]]{}.foo/2[-]([
          [-imm base.natLit.5[]-][base.natLit.5[]]{},[-infer-][]{[-]([lol,fear0$]):[-]->fear0$:infer}
        ]):infer}
      ],base.Cont/2=Dec[
        name=base.Cont/2,
        gxs=[X,R],bounds={R=[imm],X=[imm]},
        lambda=[-mutbase.Cont[X,R]-][]{#/2([x,self]):Sig[gens=[],ts=[X,immbase.A[]],ret=R]->[-]}],
      base.A/0=Dec[
        name=base.A/0,
        gxs=[],
        lambda=[-mutbase.A[]-][]{.foo/2([x,cont]):Sig[gens=[T],bounds={T=[imm]},ts=[T,mutbase.Cont[T,T]],ret=T]->
          cont:infer#/2[-]([x:infer,cont:infer]):infer}]}
    """, """
    package base
    Cont[X,R]:{ mut #(x: X, self: A): R }
    A:{ .foo[T](x: T, cont: mut Cont[T, T]): T -> cont#(x, cont) }
    B:{ #: 5 -> A
      .foo lol=5
      }
    """
  );}
  @Test void equalsSugar2() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],bounds={R=[imm],X=[imm]},lambda=[-muttest.Cont[X,R]-][]{#/2([x,cont]):Sig[gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],bounds={R=[imm]},lambda=[-muttest.ReturnStmt[R]-][]{#/0([]):Sig[gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],bounds={R=[imm]},lambda=[-muttest.Candy[R]-][]{
      .sugar/2([x,cont]):Sig[gens=[X],bounds={X=[imm]},ts=[X,muttest.Cont[X,R]],ret=R]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-muttest.Usage[]-][]{
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-]([[-infer-][]{
              [-]([]):[-]->f:infer.v/0[-]([]):infer
            }]):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-muttest.Foo[]-][]{.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{
    }}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-muttest.Void[]-][]{}]}
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
        .sugar[Foo]f = Foo
        .return{ f.v }
      }
    """); }
  @Test void equalsSugar2a() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],bounds={R=[imm],X=[imm]},lambda=[-muttest.Cont[X,R]-][]{#/2([x,cont]):Sig[gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],bounds={R=[imm]},lambda=[-muttest.ReturnStmt[R]-][]{#/0([]):Sig[gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],bounds={R=[imm]},lambda=[-muttest.Candy[R]-][]{
      .sugar/2([x,cont]):Sig[gens=[X],bounds={X=[imm]},ts=[X,muttest.Cont[X,R]],ret=R]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-muttest.Usage[]-][]{
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-](
              [[-infer-][]{[-]([]):[-]->f:infer.v/0[-]([]):infer}]
              ):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-muttest.Foo[]-][]{.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-muttest.Void[]-][]{}]}
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
      gxs=[V,R],bounds={R=[imm],V=[imm]},
      lambda=[-mutbase.Let[V,R]-][]{
        .var/0([]):Sig[gens=[],ts=[],ret=V]->[-],
        .in/1([v]):Sig[gens=[],ts=[V],ret=R]->[-]
      }],
    base.Ref/1=Dec[
      name=base.Ref/1,
      gxs=[X],bounds={X=[imm]},
      lambda=[-mutbase.Ref[X]-][base.NoMutHyg[X],base.Sealed[]]{
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
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-mutbase.Sealed[]-][]{}],
    base.Ref/0=Dec[name=base.Ref/0,gxs=[],lambda=[-mutbase.Ref[]-][]{#/1([x]):Sig[gens=[X],bounds={X=[imm]},ts=[X],ret=mut base.Ref[X]]->this:infer#/1[-]([x:infer]):infer}],
    base.Let/0=Dec[name=base.Let/0,gxs=[],lambda=[-mutbase.Let[]-][]{#/1([l]):Sig[gens=[V,R],bounds={R=[imm],V=[imm]},ts=[imm base.Let[V,R]],ret=R]->l:infer.in/1[-]([l:infer.var/0[-]([]):infer]):infer}],
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],bounds={X=[imm]},lambda=[-mutbase.NoMutHyg[X]-][]{}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-mutbase.Void[]-][]{}],
    base.UpdateRef/1=Dec[name=base.UpdateRef/1,gxs=[X],bounds={X=[imm]},lambda=[-mutbase.UpdateRef[X]-][]{#/1([x]):Sig[gens=[],ts=[X],ret=X]->[-]}]}
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var: V, .in(v:V): R }
    Ref:{ #[X](x: X): mut Ref[X] -> this#(x) }
    Ref[X] : NoMutHyg [ X ] , Sealed  {
      read * : read/imm X,
      mut .swap(x: X): X,
      mut :=(x: X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[X]): X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: X): X }
    """);}

  @Test void magicIntbers() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-muttest.A[]-][]{.foo/0([]):Sig[gens=[],ts=[],ret=imm base.natLit.5[]]->[-imm base.natLit.5[]-][base.natLit.5[]]{}}]}
    """, """
    package test
    A:{ .foo: 5 -> 5 }
    """); }

  @Test void multiArgInferredWithName() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-muttest.B[]-][test.A[]]{
      .foo/2([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-muttest.A[]-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ .foo(a, b) -> b }
    """); }
  @Test void multiArgInferred1() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-muttest.B[]-][test.A[]]{
      [-]([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-muttest.A[]-][]{
      .foo/2([a,b]):Sig[gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ a, b -> b }
    """); }

  @Test void noModifiersInFormalTypeParams1() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E59 syntaxError]
    extraneous input 'mut' expecting {']', FullCN}
    """, """
    package test
    A[mut B]:{}
    """); }
  @Test void noModifiersInFormalTypeParams2() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[B],bounds={B=[imm]},lambda=[-muttest.A[B]-][]{}]}
    """, """
    package test
    A[B]:{}
    """); }
  @Test void noModifiersInImpls() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E46 noMdfInImplementedT]
    Modifiers are not allowed in declarations or implementation lists: mut A
    """, """
    package test
    A:{}
    B:mut A{}
    """); }

  @Test void Bounds1() { ok("""
    {test.A1/1=Dec[name=test.A1/1,gxs=[B],bounds={B=[imm]},lambda=[-muttest.A1[B]-][]{}],
    test.A2/1=Dec[name=test.A2/1,gxs=[B],bounds={B=[imm,mut]},lambda=[-muttest.A2[B]-][]{}],
    test.A3/2=Dec[name=test.A3/2,gxs=[B,C],bounds={B=[imm,mut],C=[imm]},lambda=[-muttest.A3[B,C]-][]{}],
    test.A4/3=Dec[name=test.A4/3,gxs=[B,C,D],bounds={B=[imm,mut],C=[imm],D=[mutH,readH]},lambda=[-muttest.A4[B,C,D]-][]{}]}
    """, """
    package test
    A1[B: imm]:{}
    A2[B: imm,mut]:{}
    A3[B: imm,mut, C]:{}
    A4[B: imm,mut, C, D: readH,mutH]:{}
    """); }

  @Test void concreteGensImm() { ok("""
    {test.G/1=Dec[name=test.G/1,gxs=[X],bounds={X=[imm]},lambda=[-muttest.G[X]-][]{}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-muttest.B[]-][]{}],
    test.A/1=Dec[name=test.A/1,gxs=[Z],bounds={Z=[imm]},lambda=[-muttest.A[Z]-][test.G[imm test.B[]],test.G[Z]]{}]}
    """, """
    package test
    A[Z]:G[B],G[Z]{}
    B:{}
    G[X]:{}
    """); }
  @Test void ForGensRet() { ok("""
    {test.G/1=Dec[name=test.G/1,gxs=[X],bounds={X=[imm]},lambda=[-muttest.G[X]-][]{}],
    test.B/1=Dec[name=test.B/1,gxs=[Y],bounds={Y=[imm]},lambda=[-muttest.B[Y]-][]{
      .m1/0([]):Sig[gens=[],ts=[],ret=Y]->[-]}]}
    """, """
    package test
    B[Y]:{ .m1: Y }
    G[X]:{}
    """); }

  @Test void namedInline() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-muttest.A[]-][]{#/0([]):Sig[gens=[],ts=[],ret=imm test.B[]]->
      LambdaId[id=test.B/0,gens=[],bounds={}]:[-imm test.B[]-][]{}}]}
    """, """
    package test
    A:{ #: B -> B:{} }
    """); }
  @Test void namedInlineGens() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],bounds={X=[imm]},lambda=[-muttest.A[X]-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=immtest.B[X]]->
        LambdaId[id=test.B/1,gens=[X],bounds={X=[imm]}]:[-imm test.B[X]-][]{
          .m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}}]}
    """, """
    package test
    A[X]:{ #(x: X): B[X] -> B[X]:{ .m1: X -> x } }
    """); }
  @Test void namedInlineGensWithBound() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],bounds={X=[imm]},lambda=[-muttest.A[X]-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=immtest.B[X]]->
        LambdaId[id=test.B/1,gens=[X],bounds={X=[imm]}]:[-immtest.B[X]-][]{
          .m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}}]}
    """, """
    package test
    A[X:imm]:{ #(x: X): B[X] -> B[X:imm]:{ .m1: X -> x } }
    """); }
  @Test void inferNameOnCall() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],bounds={X=[imm]},lambda=[-muttest.A[X]-][]{
      #/1([x]):Sig[gens=[],ts=[X],ret=X]->
        [-imm test.Fear0$[X]-][test.Fear0$[X]]{.m1/0([]):Sig[gens=[],ts=[],ret=X]->x:infer}.m1/0[-]([]):infer}]}
    """, """
    package test
    A[X:imm]: {#(x: X): X -> {.m1: X -> x}.m1}
    """); }

  //NOTE: Failes in main (fix parethesis count?)
  @Test void missingColonTypeDeclaration() { fail("""
    In position [###]/Dummy0.fear:2:3
    [E59 syntaxError]
    no viable alternative at input '{'
    """, """
    package base
    Foo{}
    // I missed a colon :(
    """); }

  @Test void noImplGeneric() {fail("""
    In position [###]/Dummy0.fear:2:0
    [E14 expectedConcreteType]
    A concrete type was expected but the following generic type was given:
    X
    """, """
    package a
    A[X]: X{}
    """);}
  @Test void generciBoundsOk() {ok("""
      {
      test.Beer/0=Dec[name=test.Beer/0,gxs=[],
        lambda=[-muttest.Beer[]-][]{.m/0([]):Sig[gens=[X],
        bounds={X=[mut]},ts=[],ret=immtest.Baz[X]]->
        LambdaId[id=test.Baz/1,gens=[X],bounds={X=[mut]}]
        :[-immtest.Baz[X]-][]{}}],
      test.Foo/1=Dec[name=test.Foo/1,gxs=[X],bounds={X=[mut]},
        lambda=[-muttest.Foo[X]-][]{}],
      test.Bar/0=Dec[name=test.Bar/0,gxs=[],
        lambda=[-muttest.Bar[]-][]{.m/0([]):Sig[gens=[X],
        bounds={X=[mut]},ts=[],ret=immtest.Foo[X]]->[-]}]}
      """, """
      package test
      Foo[X:mut]:{}
      Bar:{.m[X:mut]:Foo[X]}
      Beer:{ .m[X:mut]:Baz[X]->Baz[X:mut]:{} }
      """);}
    @Test void generciBoundsKO1() {fail("""
      In position [###]/Dummy0.fear:3:20
      [E59 syntaxError]
      no viable alternative at input '.m[X:mut]:Foo[X:'
      """, """
      package test
      Foo[X:mut]:{}
      Bar:{.m[X:mut]:Foo[X:mut]}
      Beer:{ .m[X:mut]:Baz[X]->Baz[X:mut]:{} }
      """);}
    @Test void generciBoundsKO2() {fail("""
      In position [###]/Dummy0.fear:4:41
      [E59 syntaxError]
      mismatched input ':' expecting {']', ','}
      """, """
      package test
      Foo[X:mut]:{}
      Bar:{.m[X:mut]:Foo[X]}
      Beer:{ .m[X:mut]:Baz[X]->Baz[X:mut]:Foo[X:mut]{} }
      """);}
}
