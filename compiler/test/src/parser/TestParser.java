package parser;

import failure.CompileError;
import main.Main;
//import net.jqwik.api.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Bug;
import utils.Err;

import java.util.Optional;

class TestParser {
  void ok(String expected, String content){
    Main.resetAll();
    Parser.debugTokensPrint(content);
    String res = new Parser(Parser.dummy,content)
      .parseFullE("dummy",Bug::err,s->Optional.empty())
      .toString();
    Err.strCmpFormat(expected,res);
  }
  void same(String content1, String content2){
    Main.resetAll();
    String res1 = new Parser(Parser.dummy,content1).parseFullE("dummy",Bug::err,s->Optional.empty()).toString();
    Main.resetAll();
    String res2 = new Parser(Parser.dummy,content2).parseFullE("dummy",Bug::err,s->Optional.empty()).toString();
    Err.strCmpFormat(res1,res2);
  }
  void fail(String expectedErr,String content){
    Main.resetAll();
    var b=new StringBuffer();
    try {
      var res=new Parser(Parser.dummy,content)
        .parseFullE("dummy",s->{b.append(s);return null;},s->Optional.empty());
      if (res == null) { return; }
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void testMCall(){ ok("""
    x:infer.m/0[-]([]):infer
    """,
    "x.m"); }
  @Test void testTrue(){ ok(
    """
    [-immbase.True[]-][base.True[]]{}
    """, "base.True"); }
  @Test void testLamNoGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat"); }
  @Test void testLamEmptyGens(){ ok(
      """
      [-mut animals.Cat[]-][animals.Cat[]]{}
      """, "mut animals.Cat[]"); }
  @Test void testLamOneGens(){ ok(
      """
      [-mut base.List[imm pkg1.Person[]]-][base.List[imm pkg1.Person[]]]{}
      """, "mut base.List[imm pkg1.Person]"); }
  @Test void testLamManyGens(){ ok(
      """
      [-mut base.Either[imm pkg1.Person[],mut pkg1.Blah[]]-][base.Either[imm pkg1.Person[],mut pkg1.Blah[]]]{}
      """, "mut base.Either[imm pkg1.Person, mut pkg1.Blah]"); }
  @Test void surprisingIntberOfExprs(){ ok(
    """
      we:infer.parse/0[-]([]):infer.a/0[-]([]):infer.surprising/0[-]([]):infer.amount/0[-]([]):infer.of/0[-]([]):infer.stuff/0[-]([]):infer
      """
    ,"we .parse .a .surprising .amount .of .stuff"); }
  @Test void testFail1(){ fail(
    "[###]Dummy.fear:1:3 mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }
  @Test void singleEqSugar1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = val"); }
  @Test void singleEqSugarPOp1(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 v = (val)"); }
  @Test void singleEqSugarPOp2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m2/0[-]([]):infer}]):infer
    """, "recv .m1 v = (val) .m2"); }
  @Test void singleEqSugarPOp3(){ ok(
    """
    recv:infer.m1/2[-]([val:infer.m2/0[-]([]):infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m3/0[-]([]):infer}]):infer
    """, "recv .m1 v = (val .m2) .m3"); }
  @Test void testVarLast(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    ""","recv .m1 x=v"); }
  @Test void eqSugarSame1() { same("recv .m1 v = val", "recv .m1 v = (val)"); }
  @Test void eqSugarSame2() { same("recv .m1 v = val .m2", "recv .m1 v = (val) .m2"); }
  @Test void chainedMethCall() { ok("""
    recv:infer.m1/1[-]([a:infer]):infer .m2/1[-]([b:infer]):infer
    """, "(recv .m1 a) .m2 b"); }
  @Test void singleEqSugar2(){ ok(
    """
    recv:infer.m1/2[-]([val:infer,[-infer-][]{[-]([v,fear0$]):[-]->fear0$:infer.m2/0[-]([]):infer}]):infer
    """, "recv .m1 v = val .m2"); }
  @Test void nestedCalls1(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Test void nestedCalls2(){ ok("""
    recv:infer.m1/2[-]([v:infer,[-infer-][]{ [-]([x,fear0$]):[-]->fear0$:infer}]):infer.m2/1[-]([a:infer]):infer
    """, "(recv .m1 x=v) .m2 a"); }
  @Test void eqExpasnionNoPar() { ok("""
    recv:infer.m1/2[-]([
      v:infer,
      [-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer.m2/1[-]([a:infer]):infer}
    ]):infer
    """, "recv .m1 x=v.m2 a"); }
  @Test void eqExpasnionPar() { ok("""
    recv:infer.m1/2[-]([v:infer.m2/1[-]([a:infer]):infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer
    """, "recv .m1 x=(v.m2 a)"); }
  @Test void eqExpansionGensNoPar() { ok("""
    recv:infer.m1/2[immA]([v:infer,[-infer-][]{
      [-]([x,fear0$]):[-]->fear0$:infer.m2/1[immB,immbase.C[immD]]([a:infer]):infer
    }]):infer
    """, "recv .m1[imm A] x=v.m2[imm B,base.C[imm D]] a"); }
  @Test void nestedGenerics() { ok("""
    recv:infer .m1/0[imm pkg1.A[imm B]]([]):infer
    """, "recv .m1[pkg1.A[imm B]]"); }
  @Test void failNestedGenerics() { fail("""
    In position [###]/Dummy.fear:1:5
    [E3 concreteTypeInFormalParams]
    Trait and method declarations may only have generic type parameters. This concrete type was provided instead:
    imm pkg1.A[imm B]
    Alternatively, are you attempting to shadow an existing class name?
    """, "{ .m1[pkg1.A[imm B]]: base.Void }"); }
  @Test void sameTest1(){ same("m.a", "m.a"); }
  @Test void sameTest2(){ same("recv .m1 a .m2 b .m3 c", "((recv .m1 a) .m2 b) .m3 c"); }
  @Test void sameTest3(){ same("recv .m1 a .m2 b", "(recv .m1 a) .m2 b"); }
  @Test void sameTest4(){ same("recv .m1 a .m2", "(recv .m1 a) .m2"); }
  @Test void sameTest5(){ same("recv .m1 x=v .m2", "recv .m1 x=(v) .m2"); }
  @Test void weirdEq(){ ok("recv:infer.m1/2[-]([v:infer,[-infer-][]{[-]([x,fear0$]):[-]->fear0$:infer}]):infer.m2/0[-]([]):infer", "(recv .m1 x=v) .m2"); }
  @Test void sameTest6(){ same("recv .m1[A] x=v .m2[B,base.C[D]]", "recv .m1[A] x=(v) .m2[B,base.C[D]]"); }
  @Test void implicitLambdaImm(){ ok("[-immpkg1.L[]-][pkg1.L[]]{}", "pkg1.L{}"); }
  @Test void explicitMdfLambdaIso(){ ok("[-iso pkg1.L[]-][pkg1.L[]]{}", "iso pkg1.L{}"); }
  @Test void explicitMdfLambdaImm(){ ok("[-imm pkg1.L[]-][pkg1.L[]]{}", "imm pkg1.L{}"); }
  @Test void explicitMdfLambdaMut(){ ok("[-mut pkg1.L[]-][pkg1.L[]]{}", "mut pkg1.L{}"); }
  @Test void explicitMdfLambdaRead(){ ok("[-read pkg1.L[]-][pkg1.L[]]{}", "read pkg1.L{}"); }
  @Test void explicitMdfLambdaLent(){ ok("[-mutH pkg1.L[]-][pkg1.L[]]{}", "mutH pkg1.L{}"); }
  @Test void explicitMdfLambdaMdf(){ fail("""
    [E11 invalidMdf]
    The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type pkg1.L[]
    """, "mdf pkg1.L{}"); }

  @Test void flowPrecedence1() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}]):infer.to/1[-]([[-immbase.List[]-][base.List[]]{}]):infer
    """, """
    list.flow
      .map{}
      .filter{}
      .to base.List
    """); }
  @Test void flowPrecedence2() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}]):infer.toList/0[-]([]):infer
    """, """
    list.flow
      .map{}
      .filter{}
      .toList
    """); }
  // null is correct in the expected AST below because these tests do not visit a package, so the package is null.
  @Test void flowPrecedence2a() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer
    .filter/1[-]([[-immdummy.Fear1$[]-][dummy.Fear1$[]]{}.toList/0[-]([]):infer]):infer
    """, """
    list.flow
      .map{}
      .filter
      ({}.toList)
    """); }
  @Test void flowPrecedence2b() { ok("""
    list:infer.flow/0[-]([]):infer.map/1[-]([[-infer-][]{}]):infer.filter/1[-]([[-infer-][]{}]):infer.toList/0[-]([]):infer
    """, """
    (list.flow
      .map{}
      .filter{})
      .toList
    """); }
  @Test void flowPrecedence2c() { same("""
    (list.flow
      .map{}
      .filter{})
      .toList
    """, """
    list.flow
      .map{}
      .filter{}
      .toList
    """); }
  @Test void precedence3() { same("""
    (a + b) - c
    """, """
    a + b - c
    """); }

  @Test void bracketsWork1() { ok("""
    foo:infer#/1[-]([bar:infer]):infer.baz/0[-]([]):infer
    """, "foo#(bar).baz"); }
  @Test void bracketsWork2() { ok("""
    foo:infer#/1[-]([bar:infer]):infer.baz/0[-]([]):infer
    """, "(foo#(bar)).baz"); }
  @Test void bracketsWork3() { ok("""
    foo:infer#/1[-]([bar:infer]):infer.baz/0[-]([]):infer.bar/0[-]([]):infer
    """, "foo#(bar).baz.bar"); }
  @Test void bracketsWork4() { same("(foo#(a,b)).baz.bar", "foo#(a,b).baz.bar"); }
  @Test void bracketsWork4a() { same("(foo#(a)).baz.bar", "foo#(a).baz.bar"); }
  @Test void precedenceMCall() { same("(a - b) - c", "a - b - c"); }
  @Test void precedenceMCall2() { same("(a - b).m(c)", "a - b.m(c)"); }
  @Test void precedenceMCall2Arg() { same("(a - b).m(c,d)", "a - b.m(c,d)"); }
  @Test void precedenceMCallPlus1() { same("a + b.foo()", "(a + b).foo"); }
  @Test void precedenceMCallPlus2() { same("a + b.foo()", "a + b.foo"); }
  
  @Test void squareMeth() { same("A:{.m():A,}", "A:{.m[]():A,}"); }
  @Test void roundMeth1() { same("A:{.m:A,}", "A:{.m[]():A,}"); }
  @Test void roundMeth2() { same("A:{.m:A,}", "A:{.m():A,}"); }
  @Test void hashStringNoSpace1() { same("a#\"foo\"", "a#(\"foo\")"); }
  @Test void hashStringNoSpace2() { same("a#`foo`", "a#(`foo`)"); }
  //@Test void invalidNumberSyntax() {fail("""
  //  """, "1.2u");}//Now valid
  
  @Test void stringEscapeWork1() { ok("""
    [-imm base.uStrLit."Hello\\""[]-][base.uStrLit."Hello\\""[]]{}
    ""","""
    "Hello\\""
    """); }
  @Test void stringEscapeWork2() { ok("""
    [-mutbase.uStrLit."string\\""[]-][base.uStrLit."string\\""[]]{}+/1[-]([str:infer]):infer+/1[-]([[-immbase.uStrLit."\\""[]-][base.uStrLit."\\""[]]{}]):infer
    ""","""
    mut "string \\"" + str + "\\""
    """); }

  @Test void stringEscapeWork3() { ok("""
    [-mutbase.sStrLit.`string\\``[]-][base.sStrLit.`string\\``[]]{}+/1[-]([str:infer]):infer+/1[-]([[-immbase.sStrLit.`\\``[]-][base.sStrLit.`\\``[]]{}]):infer
    ""","""
    mut `string \\`` + str + `\\``
    """); }
  @Test void parametersVsMultipleInheritance() { ok("""
    [-imma.A[]-][a.A[]]{}#/3[-]([
      [-immbase.natLit.12[]-][base.natLit.12[]]{}==/1[-]([
        [-immbase.intLit.+84[]-][base.intLit.+84[]]{}
        ]):infer,
      [-immbase.uStrLit."findsome"[]-][base.uStrLit."findsome"[]]{},
      [-infer-][]{ [-]([]):[-]->[-infer-][]{} }
      ]):infer
    ""","""
    a.A#(12 == +84, "find some", {{}})
    """); }
  @Test void ambiguousComma1() {ok("""
    [-imma.A[]-][a.A[]]{}#/1[-]
    ([LambdaId[id=dummy.Name/0,gens=[],bounds={}]:[-immdummy.Name[]-][a.B[],b.C[]]{}]):infer
    """, """
    a.A#(Name:a.B,b.C{})
    """);}
  @Test void ambiguousComma2() {ok("""
    [-imma.A[]-][a.A[]]{}#/2[-]
    ([[-imma.B[]-][a.B[]]{},[-immb.C[]-][b.C[]]{}]):infer
    """, """
    a.A#(a.B,b.C{})
    """);}
}
  
//TODO:
//Why we had
//alias  : Alias fullCN actualGen As fullCN actualGen Comma;?
//Now is alias  : Alias fullCN actualGen As fullCN Comma;?
//removed: if(some(ctx.actualGen(1).genDecl())){ throw Bug.of("No gen on out Alias"); }
//u{numbers}//rust syntax  0..9*
//convert the hex to decimal and replace with u{..}
//-{..}
//-single
//-Name:multi{..}//optional comma after the last type name?
//for escapes, follow java escape list
//solve the todo below (may already be solved)
//.if {ch == "\\"} .return {"\\\\"} // TODO: this breaks the fearless parer