package parser;

import failure.CompileError;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Bug;
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
    String res = Parser.parseAll(ps).toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps);
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError | Bug e) {
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
      [-infer-][]{!/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{!:Void->this!}
    """
  );}
  @Test void baseLoopExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-infer-][]{!/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop:{imm !():imm Void->this!}
    """
  );}
  @Test void baseLoopMoreExplicit(){ ok("""
    {base.Loop/0=Dec[name=base.Loop/0,gxs=[],lambda=[-infer-][]{ !/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->this:infer!/0[-]([]):infer}]}
    """, """
    package base
    alias base.Void as Void,
    Loop[]:{imm ![]():imm Void[]->this!}
    """
  );}
  @Test void baseLoopAbs(){ ok("""
    {base.AbsLoop/0=Dec[name=base.AbsLoop/0,gxs=[],lambda=[-infer-][]{ !/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.Void[]]->[-]}]}
    """, """
    package base
    AbsLoop:{!:base.Void}
    """
  );}
  @Test void methWithArgs(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/1([a]):Sig[mdf=imm,gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A): A,}
    """
  );}
  @Test void methWith2Args(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[mdf=imm,gens=[],ts=[imm base.A[],imm base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: A): A,}
    """
  );}
  @Test void methWith2ArgsAndMdf(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[mdf=imm,gens=[],ts=[imm base.A[],read base.A[]],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo(a: A, b: read A): A,}
    """
    );}
    @Test void methWithGens1(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[mdf=imm,gens=[B],ts=[imm base.A[],readB],ret=imm base.A[]]->[-]}]}
    """, """
    package base
    A:{.foo[B](a: A, b: read B): A,}
    """
    );}
  @Test void methWithGens2(){ ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{.foo/2([a,b]):Sig[mdf=imm,gens=[B],ts=[imm base.A[],read B],ret=read B]->[-]}]}
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
    {base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-infer-][]{.name/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm base.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-infer-][base.HasName[]]{}]}
    """, """
    package base
    alias base.String as String,
    HasName:{ .name: String, }
    Dog:HasName
    """
  );}
  @Test void multipleExtends(){ ok("""
    {base.HasHunger/0=Dec[name=base.HasHunger/0,gxs=[],lambda=[-infer-][]{.hunger/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.UInt[]]->[-]}],
    base.HasName/0=Dec[name=base.HasName/0,gxs=[],lambda=[-infer-][]{.name/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immbase.String[]]->[-]}],
    base.Dog/0=Dec[name=base.Dog/0,gxs=[],lambda=[-infer-][base.HasHunger[],base.HasName[]]{}]}
    """, """
    package base
    alias base.UInt as UInt, alias base.String as String,
    HasHunger:{ .hunger: UInt, }
    HasName:{ .name: String, }
    Dog:HasHunger,HasName{}
    """
  );}
  @Test void equalsSugar1(){ ok("""
      {base.B/0=Dec[
        name=base.B/0,
        gxs=[],
        lambda=[-infer-][]{#/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm 5[]]->[-imm base.A[]-][base.A[]]{}.foo/2[-]([
          [-imm 5[]-][5[]]{},[-infer-][]{[-]([lol,fear0$]):[-]->fear0$:infer}
        ]):infer}
      ],base.Cont/2=Dec[
        name=base.Cont/2,
        gxs=[X,R],
        lambda=[-infer-][]{#/2([x,self]):Sig[mdf=mut,gens=[],ts=[mdf X,immbase.A[]],ret=mdf R]->[-]}],
      base.A/0=Dec[
        name=base.A/0,
        gxs=[],
        lambda=[-infer-][]{.foo/2([x,cont]):Sig[mdf=imm,gens=[T],ts=[mdf T,mutbase.Cont[mdf T,mdf T]],ret=mdf T]->
          cont:infer#/2[-]([x:infer,cont:infer]):infer}]}
    """, """
    package base
    Cont[X,R]:{ mut #(x: mdf X, self: A): mdf R }
    A:{ .foo[T](x: mdf T, cont: mut Cont[mdf T, mdf T]): mdf T -> cont#(x, cont) }
    B:{ #: 5 -> A
      .foo (lol=5)
      }
    """
  );}
  @Test void equalsSugar2() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-infer-][]{#/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-infer-][]{#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-infer-][]{
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-infer-][]{
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-]([[-infer-][]{[-]([]):[-]->f:infer.v/0[-]([]):infer}]):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-infer-][]{.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-infer-][]{}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo](f = Foo)
        .return{ f.v }
      }
    """); }
  @Test void equalsSugar2a() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[-infer-][]{#/2([x,cont]):Sig[mdf=mut,gens=[],ts=[mdfX,muttest.Candy[mdfR]],ret=mdfR]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[-infer-][]{#/0([]):Sig[mdf=mut,gens=[],ts=[],ret=mdfR]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[-infer-][]{
      .sugar/2([x,cont]):Sig[mdf=mut,gens=[X],ts=[mdfX,muttest.Cont[mdfX,mdfR]],ret=mdfR]->cont:infer#/2[-]([x:infer,this:infer]):infer,
      .return/1([a]):Sig[mdf=mut,gens=[],ts=[muttest.ReturnStmt[mdfR]],ret=mdfR]->a:infer#/0[-]([]):infer}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[-infer-][]{
      .foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->
        [-imm test.Candy[immtest.Void[]]-][test.Candy[immtest.Void[]]]{}
          .sugar/2[immtest.Foo[]]([[-imm test.Foo[]-][test.Foo[]]{},[-infer-][]{[-]([f,fear0$]):[-]->
            fear0$:infer.return/1[-]([[-infer-][]{[-]([]):[-]->f:infer.v/0[-]([]):infer}]):infer}]):infer}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[-infer-][]{.v/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immtest.Void[]]->[-infer-][]{}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[-infer-][]{}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x: mdf X, cont: mut Candy[mdf R]): mdf R }
    ReturnStmt[R]:{ mut #: mdf R }
    Candy[R]:{
      mut .sugar[X](x: mdf X, cont: mut Cont[mdf X, mdf R]): mdf R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[mdf R]): mdf R -> a#,
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
        .var/0([]):Sig[mdf=imm,gens=[],ts=[],ret=immV]->[-],
        .in/1([v]):Sig[mdf=imm,gens=[],ts=[immV],ret=immR]->[-]
      }],
    base.Ref/1=Dec[
      name=base.Ref/1,
      gxs=[X],
      lambda=[-infer-][base.NoMutHyg[imm X],base.Sealed[]]{
        */0([]):Sig[mdf=read,gens=[],ts=[],ret=recMdfX]->[-],
        .swap/1([x]):Sig[mdf=mut,gens=[],ts=[mdf X],ret=mdfX]->[-],
        :=/1([x]):Sig[mdf=mut,gens=[],ts=[mdf X],ret=imm base.Void[]]->
          [-imm base.Let[]-][base.Let[]]{}#/1[-]([[-infer-][]{
            .var/0([]):[-]->this:infer.swap/1[-]([x:infer]):infer,
            .in/1([fear0$]):[-]->[-imm base.Void[]-][base.Void[]]{}
          }]):infer,
          <-/1([f]):Sig[mdf=mut,gens=[],ts=[imm base.UpdateRef[mdfX]], ret=mdfX]->
            this:infer.swap/1[-]([f:infer#/1[-]([this:infer*/0[-]([]):infer]):infer]):infer
      }],
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-infer-][]{}],
    base.Ref/0=Dec[name=base.Ref/0,gxs=[],lambda=[-infer-][]{#/1([x]):Sig[mdf=imm,gens=[X],ts=[mdfX],ret=mut base.Ref[mdfX]]->this:infer#/1[-]([x:infer]):infer}],
    base.Let/0=Dec[name=base.Let/0,gxs=[],lambda=[-infer-][]{#/1([l]):Sig[mdf=imm,gens=[V,R],ts=[imm base.Let[immV,immR]],ret=immR]->l:infer.in/1[-]([l:infer.var/0[-]([]):infer]):infer}],
    base.NoMutHyg/1=Dec[name=base.NoMutHyg/1,gxs=[X],lambda=[-infer-][]{}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{}],
    base.UpdateRef/1=Dec[name=base.UpdateRef/1,gxs=[X],lambda=[-infer-][]{#/1([x]):Sig[mdf=mut,gens=[],ts=[mdfX],ret=mdfX]->[-]}]}
    """, """
    package base
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: mdf X): mut Ref[mdf X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      read * : recMdf X,
      mut .swap(x: mdf X): mdf X,
      mut :=(x: mdf X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[mdf X]): mdf X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: mdf X): mdf X }
    """);}

  @Test void magicIntbers() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{.foo/0([]):Sig[mdf=imm,gens=[],ts=[],ret=imm5[]]->[-imm 5[]-][5[]]{}}]}
    """, """
    package test
    A:{ .foo: 5 -> 5 }
    """); }

  @Test void multiArgInferredWithName() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-infer-][test.A[]]{
      .foo/2([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .foo/2([a,b]):Sig[mdf=imm,gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ .foo(a, b) -> b }
    """); }
  @Test void multiArgInferred1() { ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[-infer-][test.A[]]{
      [-]([a,b]):[-]->b:infer}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .foo/2([a,b]):Sig[mdf=imm,gens=[],ts=[immtest.A[],immtest.A[]],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{ .foo(a: A, b: A): A }
    B:A{ a, b -> b }
    """); }

  @Test void noModifiersInFormalTypeParams1() { fail("""
    In position [###]/Dummy0.fear:2:2
    [E46 noMdfInFormalParams]
    Modifiers are not allowed in declarations or implementation lists: mutB
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
    Modifiers are not allowed in declarations or implementation lists: mutA
    """, """
    package test
    A:{}
    B:mut A{}
    """); }

  @Test void mdfBounds1() { ok("""
    {test.A1/1=Dec[name=test.A1/1,gxs=[B],bounds={B=[imm]},lambda=[-infer-][]{}],
    test.A2/1=Dec[name=test.A2/1,gxs=[B],bounds={B=[imm,mut]},lambda=[-infer-][]{}],
    test.A3/2=Dec[name=test.A3/2,gxs=[B,C],bounds={B=[imm,mut]},lambda=[-infer-][]{}],
    test.A4/3=Dec[name=test.A4/3,gxs=[B,C,D],bounds={B=[imm,mut],D=[lent,readOnly]},lambda=[-infer-][]{}]}
    """, """
    package test
    A1[B: imm]:{}
    A2[B: imm,mut]:{}
    A3[B: imm,mut, C]:{}
    A4[B: imm,mut, C, D: readOnly,lent]:{}
    """); }

  @Test void multiSigsAbs() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .m1/2([a,b]):Sig[mdf=read,gens=[],ts=[readtest.A[],mutB],ret=isoB],Sig[mdf=mut,gens=[],ts=[muttest.A[],mutB],ret=mutB]->
        [-]}]}
    """, """
    package test
    A:{
      read .m1(a: read A, b: mut B): iso B
      mut .m1(a: mut A, b: mut B): mut B,
      }
    """); }
  @Test void multiSigsImpl() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .m1/2([a,b]):Sig[mdf=read,gens=[],ts=[readtest.A[],mutB],ret=isoB],Sig[mdf=mut,gens=[],ts=[muttest.A[],mutB],ret=mutB]->
        b:infer}]}
    """, """
    package test
    A:{
      read .m1(a: read A, b: mut B): iso B
      mut .m1(a: mut A, b: mut B): mut B -> b,
      }
    """); }
  @Test void multiSigsGen() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[-infer-][]{
      .m1/2([a,b]):Sig[mdf=read,gens=[X],ts=[readtest.A[],mutB],ret=isoB],Sig[mdf=mut,gens=[X],ts=[muttest.A[],mutB],ret=mutB]->
        [-]}]}
    """, """
    package test
    A:{
      read .m1[X](a: read A, b: mut B): iso B
      mut .m1[X](a: mut A, b: mut B): mut B,
      }
    """); }
  @Test void multiSigsGenBadName() { fail("""
    In position [###]/Dummy0.fear:3:2
    [E51 inconsistentMultiSigNames]
    All signatures for a method must have the same name.
    """, """
    package test
    A:{
      read .bad[X](a: read A, b: mut B): iso B
      mut .m1[X](a: mut A, b: mut B): mut B,
      }
    """); }
  @Test void multiSigsIncomplete() { fail("""
    utils.Bug: file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:8:2 mismatched input '.m1' expecting '->'
    file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:8:9 mismatched input 'b' expecting '}'
    file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:8:20 missing ':' at '{'
    file:///Users/nick/Programming/PhD/fearless/Dummy0.fear:8:22 extraneous input ',' expecting {<EOF>, FullCN}
    """, """
    package test
    A:{
      read .m1[X](a: read A, b: mut B): iso B
      mut .m1[X](a: mut A, b: mut B): mut B,
      }
    B:A{
      .m1(a, b)
      .m1(a, b) -> iso B{},
      }
    """); }
}
