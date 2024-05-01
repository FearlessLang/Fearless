package program;

import failure.CompileError;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSigInference {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = p.inferSignatures();
    Err.strCmpFormat(expected, inferred.toString());
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TypeSystemFeatures.of());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });

    try {
      var inferred = p.inferSignatures();
      Assertions.fail("Did not fail, got:\n" + inferred);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void noInference() { ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=imm base.A[]]->[-imm base.A[]-][base.A[]]{}
    }]}
    """, """
    package base
    A:{ .fullType: A -> A }
    """);}

  @Test void inferOneSig() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
       'this
       .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.A[]]
        ->[-imm base.B[]-][base.B[]]{}}],
     base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{
       'this
       .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.A[]]
         ->[-]}]}
    """, """
    package base
    A:{ .fullType: A }
    B:A{ .fullType->B }
    """);}

  @Test void inferOneSigParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
    'this
    .fullType/1([a]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-imm base.B[]-][base.B[]]{}}],
  base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/1([a]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType(a: A): A }
    B:A{ .fullType a->B }
    """);}

  @Test void inferOneSigGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
      'this
      .fullType/0([]):Sig[gens=[X$0],ts=[],ret=immX$0]->this:infer.fullType/0[-]([]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/0([]):Sig[gens=[X],ts=[],ret=immX]->[-]}]}
    """, """
    package base
    A:{ .fullType[X]: imm X }
    B:A{ .fullType->this.fullType }
    """);}

  @Test void inferOneSigGensClash() { ok("""
    {base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[gens=[X],ts=[],ret=immX]->[-]}],
    base.B/1=Dec[name=base.B/1,gxs=[X],lambda=[-infer-][base.A[]]{'this
      .bla/0([]):Sig[gens=[],ts=[],ret=immX]->[-],
      .fullType/0([]):Sig[gens=[X$0],ts=[],ret=immX$0]->this:infer.fullType/0[-]([]):infer}]}
    """, """
    package base
    A:{ .fullType[X]: imm X }
    B[X]:A{ .bla:imm X, .fullType->this.fullType }
    """);}

  @Test void inferClashingGenMeth() { fail("""
    In position [###]/Dummy0.fear:2:14
    [E18 uncomposableMethods]
    These methods could not be composed.
    conflicts:
    ([###]/Dummy0.fear:3:4) base.B[], .g/0[AA$0, CC$0](): AA$0
    ([###]/Dummy0.fear:4:4) base.C[], .g/0[BB$0](): BB$0
    """, """
    package base
    A[AA,BB]:B,C{ .g -> this.g }
    B:{ .g[AA,CC]: AA }
    C:{ .g[BB]: BB }
    """);}

  @Test void inferOneSigGensAndParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{
      'this
      .fullType/1([y]):Sig[gens=[X$0],ts=[immX$0],ret=immbase.A[]]->[-imm base.B[]-][base.B[]]{}}],
      base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this.fullType/1([x]):Sig[gens=[X],ts=[immX],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType[X](x: imm X): A }
    B:A{ .fullType(y)->B }
    """);}

  @Test void oneParentImplTopDec() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.A[]]->[-imm base.B[]-][base.B[]]{}}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType: A }
    B:A{ B }
    """);}

  @Test void oneParentImplTopDecParams() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/1([b]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->b:infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[gens=[],ts=[immbase.A[]],ret=immbase.A[]]->[-]}]}
    """, """
    package base
    A:{ .fullType(a: A): A }
    B:A{ b -> b }
    """);}

  @Test void oneParentImplGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .fullType/1([x]):Sig[gens=[G$0],ts=[immG$0],ret=immG$0]->x:infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[gens=[G],ts=[immG],ret=immG]->[-]}]}
    """, """
    package base
    A:{ .fullType[G](a: imm G): imm G }
    B:A{ x -> x }
    """);}

  @Test void oneParentImplDecGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[immbase.B[]]]{'this
      .fullType/1([x]):Sig[gens=[],ts=[immbase.B[]],ret=immbase.B[]]->[-imm base.B[]-][base.B[]]{}}],
    base.A/1=Dec[name=base.A/1,gxs=[X],lambda=[-infer-][]{'this
      .fullType/1([a]):Sig[gens=[],ts=[immX],ret=immX]->[-]}]}
    """, """
    package base
    A[X]:{ .fullType(a: imm X): imm X }
    B:A[B]{ x -> B }
    """);}

  @Test void onlyAbsGens() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[immbase.B[]]]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.B[]]->[-imm base.B[]-][base.B[]]{}}],
    base.A/1=Dec[name=base.A/1,gxs=[X],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=immX]->[-]}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .fullType/0([]):Sig[gens=[],ts=[],ret=immbase.Void[]]->[-]}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}]}
    """, """
    package base
    Void:{}
    A:{ .fullType: Void }
    A[X]:{ .fullType: imm X }
    B:A[B]{ B }
    """);}

  @Test void complexInfer() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][]{'this
      .m/0([]):Sig[gens=[X],ts=[],ret=imma.Bi[immX,immY]]->this:infer}],
    a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][a.B[immX]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immX]->[-]}],
    a.Bi/2=Dec[name=a.Bi/2,gxs=[AA,BB],lambda=[-infer-][]{'this}]}
    """, """
    package a
    A[X]:B[imm X]{ .foo:imm X }
    B[Y]:{.m[X]:Bi[imm X, imm Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void complexInfer2() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][]{'this
      .m/0([]):Sig[gens=[X],ts=[],ret=imma.Bi[immX,immY]]->this:infer}],
    a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][a.B[immX]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immX]->[-],
      .m/0([]):Sig[gens=[X$0],ts=[],ret=imma.Bi[imm X$0,imm X$0]]->this:infer}],
    a.Bi/2=Dec[name=a.Bi/2,gxs=[AA,BB],lambda=[-infer-][]{'this}]}
    """, """
    package a
    A[X]:B[imm X]{ .foo:imm X, .m -> this }
    B[Y]:{.m[X]:Bi[imm X, imm Y]->this}
    Bi[AA,BB]:{}
    """); }
  @Test void diamondRename1() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id[]]{'this}],
    a.B/1=Dec[name=a.B/1,gxs=[X],lambda=[-infer-][a.Id[]]{'this}],
    a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[gens=[X$0],ts=[immX$0],ret=immX$0]->a:infer}]}
    """, """
    package a
    Id:{ .id[X](x:imm X):imm X }
    A:Id
    B[X]:Id
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible
  @Test void diamondRenameNotComposable() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][a.Id2[]]{'this}],
    a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id1[]]{'this}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[Z],ts=[immZ],ret=immZ]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[gens=[X$0],ts=[immX$0],ret=immX$0]->a:infer}],
    a.Id1/0=Dec[name=a.Id1/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->[-]}]}
    """, """
    package a
    Id1:{ .id[X](x:imm X):imm X }
    Id2:{ .id[Z](x:imm Z): imm Z }
    A:Id1
    B[Y]:Id2
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible
  @Test void diamondRename2() { ok("""
    {a.B/1=Dec[name=a.B/1,gxs=[Y],lambda=[-infer-][a.Id2[]]{'this}],
    a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][a.Id1[]]{'this}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[],a.B[imma.A[]]]{'this
      .id/1([a]):Sig[gens=[X$0],ts=[immX$0],ret=immX$0]->a:infer}],
    a.Id1/0=Dec[name=a.Id1/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->[-]}]}
    """, """
    package a
    Id1:{ .id[X](x:imm X):imm X }
    Id2:{ .id[X](x:imm X):imm X }
    A:Id1
    B[Y]:Id2
    C:A,B[A]{.id a->a}
    """); }//So, how do we 'accept' that the version with X and the version with X0 are compatible

  @Test void noMethodExists() { fail("""
    In position [###]Dummy0.fear:4:12
    [E19 cannotInferSig]
    Could not infer the signature for .foo/0 in a.Bi/2.
    """, """
    package a
    A[X,Y]:B[X,Y]{ .foo:X }
    B[Y,X]:{.m[Z]:Bi[X,Y]->this.m}
    Bi[AA,BB]:{ .foo -> this.foo }
    """); }

  @Test void multiGens2() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[gens=[X,Y],ts=[immX,immY],ret=immY]->[-]}],
      a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
        .m/2([a1,a2]):Sig[gens=[X$0,Y$0],ts=[immX$0,immY$0],ret=immY$0]->a2:infer}]}
    """, """
    package a
    A:{ .m[X,Y](x:imm X,y:imm Y): imm Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void multiGensOneConcrete() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[gens=[X,Y],ts=[imma.Foo[immX],immY],ret=immY]->[-]}],
    a.Foo/1=Dec[name=a.Foo/1,gxs=[X],lambda=[-infer-][]{'this}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
      .m/2([a1,a2]):Sig[gens=[X$0,Y$0],ts=[imma.Foo[immX$0],immY$0],ret=immY$0]->a2:infer}]}
    """, """
    package a
    Foo[X]:{}
    A:{ .m[X,Y](x:Foo[imm X],y:imm Y): imm Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void multiGensOneConcreteMdfs() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-infer-][]{'this
      .m/2([x,y]):Sig[gens=[X,Y],ts=[muta.Foo[immX],recMdfY],ret=recMdfY]->[-]}],
    a.Foo/1=Dec[name=a.Foo/1,gxs=[X],lambda=[-infer-][]{'this}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[]]{'this
      .m/2([a1,a2]):Sig[gens=[X$0,Y$0],ts=[muta.Foo[immX$0],recMdfY$0],ret=recMdfY$0]->a2:infer}]}
    """, """
    package a
    Foo[X]:{}
    A:{ recMdf .m[X,Y](x:mut Foo[imm X],y:recMdf Y): recMdf Y }
    B:A{ .m(a1,a2) -> a2 }
    """); }
  @Test void sameGens() {
    ok("""
      {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][]{'this
        .g/0([]):Sig[gens=[AA],ts=[],ret=immAA]->[-]}],
        base.C/0=Dec[name=base.C/0,gxs=[],lambda=[-infer-][]{'this.g/0([]):Sig[gens=[BB],ts=[],ret=immBB]->[-]}],
        base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][base.B[],base.C[]]{'this
          .g/0([]):Sig[gens=[AA$0],ts=[],ret=immAA$0]->this:infer.g/0[-]([]):infer}]}
      """, """
      package base
      A:B,C{ .g -> this.g }
      B:{ .g[AA]: imm AA }
      C:{ .g[BB]: imm BB }
      """);
  }

  @Test void abstractNoCandidate() { fail("""
    In position [###]/Dummy0.fear:3:6
    [E22 cannotInferAbsSig]
    Could not infer the signature for the abstract lambda in a.Id2/0. There must be one abstract lambda in the trait.
    """, """
    package a
    Id:{ .id[X](x: X): X }
    Id2:{ x -> x }
    """); }
  @Test void abstractOneArg() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[-infer-][]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->[-]}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[-infer-][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X$0],ts=[immX$0],ret=immX$0]->x:infer}]}
    """, """
    package a
    Id:{ .id[X](x: imm X): imm X }
    Id2:Id{ x -> x }
    """); }
  @Test void adaptGens() { ok("""
    {a.A/1=Dec[name=a.A/1,gxs=[X],lambda=[-infer-][]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=X]->[-],
      .m2/0([]):Sig[gens=[],ts=[],ret=mutX]->[-],
      .m3/0([]):Sig[gens=[],ts=[],ret=isoX]->[-],
      .m4/0([]):Sig[gens=[],ts=[],ret=lentX]->[-],
      .m5/0([]):Sig[gens=[],ts=[],ret=recMdfX]->[-],
      .m7/0([]):Sig[gens=[],ts=[],ret=immX]->[-]}],
    a.C/0=Dec[name=a.C/0,gxs=[],lambda=[-infer-][a.A[muta.B[]]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=muta.B[]]->this:infer.m1/0[-]([]):infer,
      .m2/0([]):Sig[gens=[],ts=[],ret=muta.B[]]->this:infer.m2/0[-]([]):infer,
      .m3/0([]):Sig[gens=[],ts=[],ret=isoa.B[]]->this:infer.m3/0[-]([]):infer,
      .m4/0([]):Sig[gens=[],ts=[],ret=lenta.B[]]->this:infer.m4/0[-]([]):infer,
      .m5/0([]):Sig[gens=[],ts=[],ret=recMdf a.B[]]->this:infer.m5/0[-]([]):infer,
      .m7/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->this:infer.m7/0[-]([]):infer}],
    a.D/1=Dec[name=a.D/1,gxs=[X],lambda=[-infer-][a.A[X]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=X]->this:infer.m1/0[-]([]):infer,
      .m2/0([]):Sig[gens=[],ts=[],ret=mutX]->this:infer.m2/0[-]([]):infer,
      .m3/0([]):Sig[gens=[],ts=[],ret=isoX]->this:infer.m3/0[-]([]):infer,
      .m4/0([]):Sig[gens=[],ts=[],ret=lentX]->this:infer.m4/0[-]([]):infer,
      .m5/0([]):Sig[gens=[],ts=[],ret=recMdfX]->this:infer.m5/0[-]([]):infer,
      .m7/0([]):Sig[gens=[],ts=[],ret=immX]->this:infer.m7/0[-]([]):infer}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[-infer-][a.A[imma.B[]]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->this:infer.m1/0[-]([]):infer,
      .m2/0([]):Sig[gens=[],ts=[],ret=muta.B[]]->this:infer.m2/0[-]([]):infer,
      .m3/0([]):Sig[gens=[],ts=[],ret=isoa.B[]]->this:infer.m3/0[-]([]):infer,
      .m4/0([]):Sig[gens=[],ts=[],ret=lenta.B[]]->this:infer.m4/0[-]([]):infer,
      .m5/0([]):Sig[gens=[],ts=[],ret=imm a.B[]]->this:infer.m5/0[-]([]):infer,
      .m7/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->this:infer.m7/0[-]([]):infer}]}
    """, """
    package a
    A[X]:{ .m1:X,  .m2: mut X, .m3: iso X, .m4: lent X,
            recMdf .m5: recMdf X, .m7: imm X
          }
    B:A[B]{ .m1->this.m1, .m2->this.m2, .m3->this.m3, .m4->this.m4, .m5->this.m5,
            .m7->this.m7,}
    C:A[mut B]{ .m1->this.m1, .m2->this.m2, .m3->this.m3, .m4->this.m4, .m5->this.m5, .m7->this.m7 }
    D[X]:A[X]{ .m1->this.m1, .m2->this.m2, .m3->this.m3, .m4->this.m4, .m5->this.m5,
            .m7->this.m7,
          }
    """); }

  @Test void nullrefDef() { ok("""
    {base.Let/2=Dec[name=base.Let/2,gxs=[V,R],lambda=[-infer-][]{'this
      .var/0([]):Sig[gens=[],ts=[],ret=V]->[-],
      .in/1([v]):Sig[gens=[],ts=[V],ret=R]->[-]}],
      
    base.Ref/1=Dec[name=base.Ref/1,gxs=[X],lambda=[-infer-][base.Sealed[]]{'this
      */0([]):Sig[gens=[],ts=[],ret=recMdfX]->[-],
      .swap/1([x]):Sig[gens=[],ts=[X],ret=X]->[-],
      :=/1([x]):Sig[gens=[],ts=[X],ret=immbase.Void[]]->
        [-imm base.Let[]-][base.Let[]]{}#/1[-]([[-infer-][]{
          .var/0([]):[-]->this:infer.swap/1[-]([x:infer]):infer,
          .in/1([fear0$]):[-]->[-imm base.Void[]-][base.Void[]]{}}]):infer,
      <-/1([f]):Sig[gens=[],ts=[immbase.UpdateRef[X]],ret=X]->
        this:infer.swap/1[-]([f:infer#/1[-]([this:infer*/0[-]([]):infer]):infer]):infer}],
        
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-infer-][]{'this}],
    
    base.Ref/0=Dec[name=base.Ref/0,gxs=[],lambda=[-infer-][]{'this
      #/1([x]):Sig[gens=[X],ts=[X],ret=mutbase.Ref[X]]->this:infer#/1[-]([x:infer]):infer}],
      
    base.Let/0=Dec[name=base.Let/0,gxs=[],lambda=[-infer-][]{'this
      #/1([l]):Sig[gens=[V,R],ts=[immbase.Let[V,R]],ret=R]->
        l:infer.in/1[-]([l:infer.var/0[-]([]):infer]):infer}],
        
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}],
    
    base.UpdateRef/1=Dec[name=base.UpdateRef/1,gxs=[X],lambda=[-infer-][]{'this
      #/1([x]):Sig[gens=[],ts=[X],ret=X]->[-]}]}
    """, """
    package base
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[V,R]):R -> l.in(l.var) }
    Let[V,R]:{ .var:V, .in(v:V):R }
    Ref:{ #[X](x: X): mut Ref[X] -> this#(x) }
    Ref[X]:Sealed{
      recMdf * : recMdf X,
      mut .swap(x: X): X,
      mut :=(x: X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: UpdateRef[X]): X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x: X): X }
    """); }

  @Test void immDelegate() { ok("""
    {base.B/0=Dec[name=base.B/0,gxs=[],lambda=[-infer-][base.A[]]{'this
      .m2/1([k]):Sig[gens=[K$0],ts=[immK$0],ret=immbase.Void[]]->
        this:infer.m1/1[-]([k:infer]):infer}],
    base.A/0=Dec[name=base.A/0,gxs=[],lambda=[-infer-][]{'this
      .m1/1([x]):Sig[gens=[T],ts=[immT],ret=immbase.Void[]]->
        this:infer.m2/1[-]([x:infer]):infer,
      .m2/1([k]):Sig[gens=[K],ts=[immK],ret=immbase.Void[]]->[-]}],
    base.Void/0=Dec[name=base.Void/0,gxs=[],lambda=[-infer-][]{'this}]}
    """, """
    package base
    A:{
      .m1[T](x:imm T):Void->this.m2(x),
      .m2[K](k:imm K):Void
      }
    B:A{ .m2(k)->this.m1(k) }
    Void:{}
    """); }

  @Test void bools() { ok("""
    {base.ThenElse/1=Dec[name=base.ThenElse/1,gxs=[R],lambda=[-infer-][]{'this
      .then/0([]):Sig[gens=[],ts=[],ret=R]->[-],
      .else/0([]):Sig[gens=[],ts=[],ret=R]->[-]}],
    base.Sealed/0=Dec[name=base.Sealed/0,gxs=[],lambda=[-infer-][]{'this}],
    base.Bool/0=Dec[name=base.Bool/0,gxs=[],lambda=[-infer-][base.Sealed[]]{'this
      .and/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->[-],
      .or/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->[-],
      .not/0([]):Sig[gens=[],ts=[],ret=immbase.Bool[]]->[-],
      ?/1([f]):Sig[gens=[R],ts=[mutbase.ThenElse[R]],ret=R]->[-]}],
    base.Str/0=Dec[name=base.Str/0,gxs=[],lambda=[-infer-][]{'this}],
    base.True/0=Dec[name=base.True/0,gxs=[],lambda=[-infer-][base.Bool[]]{'this
      .and/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->b:infer,
      .or/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->this:infer,
      .not/0([]):Sig[gens=[],ts=[],ret=immbase.Bool[]]->[-imm base.False[]-][base.False[]]{},
      ?/1([f]):Sig[gens=[R$0],ts=[mutbase.ThenElse[R$0]],ret=R$0]->f:infer.then/0[-]([]):infer}],
    base.False/0=Dec[name=base.False/0,gxs=[],lambda=[-infer-][base.Bool[]]{'this
      .and/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->this:infer,
      .or/1([b]):Sig[gens=[],ts=[immbase.Bool[]],ret=immbase.Bool[]]->b:infer,
      .not/0([]):Sig[gens=[],ts=[],ret=immbase.Bool[]]->[-imm base.True[]-][base.True[]]{},
      ?/1([f]):Sig[gens=[R$0],ts=[mutbase.ThenElse[R$0]],ret=R$0]->f:infer.else/0[-]([]):infer}]}
    """, """
    package base
    Sealed:{} Str:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """); }
}


//To discuss: what about the implementation of the 2 meth? do we have both or only one now?
//one of the two needs a twist in the cMOf!!