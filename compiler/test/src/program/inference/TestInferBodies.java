package program.inference;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import utils.Base;
import utils.Err;
import visitors.ShallowInjectionVisitor;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Disabled("03/12/24")
public class TestInferBodies {
  void ok(String expected, String... content){
    var parsed = parseProgram(content);
    var inferred =InferBodies.inferAll(parsed.full);
    var cleaned = Base.ignoreBase(inferred);
    Err.strCmpFormat(expected, cleaned.toString());
  }
  void same(String programA, String programB, String... extras){
    var a = parseProgram(programA, extras);
    var aCleaned = Base.ignoreBase(InferBodies.inferAll(a.full));
    var b = parseProgram(programB, extras);
    var bCleaned = Base.ignoreBase(InferBodies.inferAll(b.full));
    Err.strCmpFormat(aCleaned.toString(), bCleaned.toString());
  }
  void fail(String expectedErr, String... content){
    var parsed = parseProgram(content);

    try {
      var inferred = InferBodies.inferAll(parsed.full);
      Assertions.fail("Did not fail, got:\n" + Base.ignoreBase(inferred));
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  record ParsedProgram(astFull.Program full, ast.Program core){}
  private ParsedProgram parseProgram(String... content) {
    assert content.length > 0;
    return parseProgram("package test\n", content);
  }
  private ParsedProgram parseProgram(String first, String... content) {
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Stream.of(first), Arrays.stream(content))
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    return new ParsedProgram(p,ShallowInjectionVisitor.of().visitProgram(p.inferSignatures()));
  }

  @Test void baseLib() {ok("""
    {}
    """, Base.baseLib); }

  @Test void emptyProgram() { ok("""
    {}
    """, """
    package a
    """); }

  @Test void abstractProgram() { ok("""
    {a.Foo/0=Dec[name=a.Foo/0,gxs=[],lambda=[--][a.Foo[]]{'this
      .nothingToInfer/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->[-]}]}
    ""","""
    package a
    Foo:{ .nothingToInfer: Foo }
    """); }

  @Test void inferSelfFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/0([]):Sig[gens=[],ts=[],ret=imma.Id[]]->this}]}
    """, """
    package a
    Id:{ .id: Id -> this }
    """); }

  @Test void inferIdentityFn() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X],ts=[X],ret=X]->x}]}
    """, """
    package a
    Id:{ .id[X](x: X): X -> x }
    """); }
  @Test void inferIdentityFnAndSig() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X],ts=[X],ret=X]->[-]}],
    a.Id2/0=Dec[name=a.Id2/0,gxs=[],lambda=[--][a.Id2[],a.Id[]]{'this
      .id/1([x]):Sig[gens=[X0/0$],ts=[X0/0$],ret=X0/0$]->x}]}
    """,  """
    package a
    Id:{ .id[X](x: X): X }
    Id2:Id{ x -> x }
    """); }
  @Test void inferLoop() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->this.id/1[immX]([x])}]}
    """,  """
    package a
    Id:{ .id[X](x: imm X): imm X -> this.id[imm X](x) }
    """); }
  @Test void inferLoop2() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X],ts=[immX],ret=immX]->this.id/1[mut X]([x])}]}
    """,  """
    package a
    Id:{ .id[X](x: imm X): imm X -> this.id[mut X](x) }
    """); }
  @Test void inferLoopMdf() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[gens=[X],ts=[X],ret=X]->this.id/1[X]([x])}]}
    """, """
    package a
    Id:{ .id[X](x: X):  X -> this.id[X](x) }
    """); }
  @Test void inferLoopMut() { ok("""
    {a.Id/0=Dec[name=a.Id/0,gxs=[],lambda=[--][a.Id[]]{'this
      .id/1([x]):Sig[=mut,gens=[X],ts=[mutX],ret=mutX]->this.id/1[mutX]([x])}]}
    """, """
    package a
    Id:{ mut .id[X](x: mut X): mut X -> this.id[mut X](x) }
    """); }

  @Test void inferMultipleTraits1() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[--][a.A[]]{'this.foo/0([]):Sig[gens=[],ts=[],ret=imma.A[]]->[-]}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[--][a.B[]]{'this.bar/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->this}],
    a.Test/0=Dec[name=a.Test/0,gxs=[],lambda=[--][a.Test[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->
        [-imm-][a.A[],a.B[]]{'self.foo/0([]):Sig[gens=[],ts=[],ret=imma.A[]]->self}}]}
    """, """
    package a
    A:{ .foo: A } B:{ .bar: B -> this }
    Test:{ #: B -> A,B{'self .foo -> self } }
    """); }
  // TODO: an if-statement in fixType will make this pass if we want it
  @Test void inferMultipleTraits2() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[--][a.A[]]{'this.foo/0([]):Sig[gens=[],ts=[],ret=imma.A[]]->this}],
    a.B/0=Dec[name=a.B/0,gxs=[],lambda=[--][a.B[]]{'this.bar/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->this}],
    a.Test/0=Dec[name=a.Test/0,gxs=[],lambda=[--][a.Test[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=imma.B[]]->
        [-imm-][a.A[],a.B[]]{'fear0$}}]}
    """, """
    package a
    A:{ .foo: A -> this } B:{ .bar: B -> this }
    Test:{ #: B -> A }
    """); }

  @Test void immOpt() { ok("""
    {test.OptDo/1=Dec[name=test.OptDo/1,gxs=[T],lambda=[--][test.OptDo[T],test.OptMatch[immT,immtest.Void[]]]{'this
      #/1([t]):Sig[gens=[],ts=[immT],ret=immtest.Void[]]->[-],
      .some/1([x]):Sig[gens=[],ts=[immT],ret=immtest.Void[]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immtest.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),
      .none/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$},
      ._doRes/2([y,x]):Sig[gens=[],ts=[immtest.Void[],immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immT]([x])}],
    test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[--][test.NoMutHyg[X]]{'this}],
    test.OptFlatMap/2=Dec[name=test.OptFlatMap/2,gxs=[T,R],lambda=[--][test.OptFlatMap[T,R],test.OptMatch[immT,immtest.Opt[immR]]]{'this
      .none/0([]):Sig[gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],
    test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[--][test.OptMatch[T,R]]{'this
      .some/1([x]):Sig[gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[gens=[],ts=[],ret=immR]->[-]}],
    test.OptMap/2=Dec[name=test.OptMap/2,gxs=[T,R],lambda=[--][test.OptMap[T,R],test.OptMatch[immT,immtest.Opt[immR]]]{'this
      #/1([t]):Sig[gens=[],ts=[immT],ret=immR]->[-],
      .some/1([x]):Sig[gens=[],ts=[immT],ret=immtest.Opt[immR]]->
        [-imm-][test.Opt[]]{'fear[###]$}#/1[immR]([this#/1[]([x])]),
      .none/0([]):Sig[gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],
    test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[--][test.Opt[]]{'this
      #/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[imm T]]{'fear[###]$
          .match/1([m]):Sig[gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[--][test.Opt[T],test.NoMutHyg[immT]]{'this
      .match/1([m]):Sig[gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->m.none/0[]([]),
      .map/1([f]):Sig[gens=[R],ts=[immtest.OptMap[immT,immR]],ret=immtest.Opt[immR]]->
        this.match/1[immtest.Opt[immR]]([f]),
      .do/1([f]):Sig[gens=[],ts=[immtest.OptDo[immT]],ret=immtest.Opt[immT]]->
        this.match/1[immtest.Opt[immT]]([f]),
      .flatMap/1([f]):Sig[gens=[R],ts=[immtest.OptFlatMap[immT,immR]],ret=immtest.Opt[immR]]->
        this.match/1[immtest.Opt[immR]]([f])}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Opt[T]:NoMutHyg[T]{
      .match[R](m: OptMatch[T, R]): R -> m.none,
      .map[R](f: OptMap[T,R]): Opt[R]->this.match(f),
      .do(f: OptDo[T]):Opt[T]->this.match(f),
      .flatMap[R](f: OptFlatMap[T, R]): Opt[R]->this.match(f),
      }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none->{} }
    OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opts#(this#x), .none->{} }
    OptDo[T]:OptMatch[T,Void]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opts#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):Opt[T]->Opts#x
      }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    Void:{}
    NoMutHyg[X]:{}
    """); }

  @Test void immOpt2() { ok("""
    {test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[--][test.OptMatch[T,R]]{'this
      .some/1([x]):Sig[gens=[],ts=[immT],ret=immR]->[-],
      .none/0([]):Sig[gens=[],ts=[],ret=immR]->[-]}],
    test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[--][test.Opt[]]{'this
      #/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Opt[immT]]->
        [-imm-][test.Opt[imm T]]{'fear[###]$
          .match/1([m]):Sig[gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[--][test.Opt[T]]{'this
      .match/1([m]):Sig[gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->
        m.none/0[]([])}]}
    """, """
    package test
    Opt[T]:{ .match[R](m: OptMatch[T, R]): R -> m.none }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    """); }

  @Test void immOptInferR() { ok("""
    {test.OptDo/1=Dec[name=test.OptDo/1,gxs=[T],lambda=[--][test.OptDo[T],test.OptMatch[immT,immtest.Void[]]]{'this#/1([t]):Sig[gens=[],ts=[immT],ret=immtest.Void[]]->[-],.some/1([x]):Sig[gens=[],ts=[immT],ret=immtest.Void[]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immtest.Opt[immT]]([this._doRes/2[]([this#/1[]([x]),x])]),.none/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$},._doRes/2([y,x]):Sig[gens=[],ts=[immtest.Void[],immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immT]([x])}],test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[--][test.NoMutHyg[X]]{'this}],test.OptFlatMap/2=Dec[name=test.OptFlatMap/2,gxs=[T,R],lambda=[--][test.OptFlatMap[T,R],test.OptMatch[immT,immtest.Opt[immR]]]{'this.none/0([]):Sig[gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],test.OptMatch/2=Dec[name=test.OptMatch/2,gxs=[T,R],lambda=[--][test.OptMatch[T,R]]{'this.some/1([x]):Sig[gens=[],ts=[immT],ret=immR]->[-],.none/0([]):Sig[gens=[],ts=[],ret=immR]->[-]}],test.OptMap/2=Dec[name=test.OptMap/2,gxs=[T,R],lambda=[--][test.OptMap[T,R],test.OptMatch[immT,immtest.Opt[immR]]]{'this#/1([t]):Sig[gens=[],ts=[immT],ret=immR]->[-],.some/1([x]):Sig[gens=[],ts=[immT],ret=immtest.Opt[immR]]->[-imm-][test.Opt[]]{'fear[###]$}#/1[immR]([this#/1[]([x])]),.none/0([]):Sig[gens=[],ts=[],ret=immtest.Opt[immR]]->[-imm-][test.Opt[immR]]{'fear[###]$}}],test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[--][test.Opt[]]{'this#/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[immT]]{'fear[###]$.match/1([m]):Sig[gens=[X1/0$],ts=[immtest.OptMatch[immT,immX1/0$]],ret=immX1/0$]->m.some/1[]([x])}}],test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[--][test.Opt[T],test.NoMutHyg[immT]]{'this.match/1([m]):Sig[gens=[R],ts=[immtest.OptMatch[immT,immR]],ret=immR]->m.none/0[]([]),.map/1([f]):Sig[gens=[R],ts=[immtest.OptMap[immT,immR]],ret=immtest.Opt[immR]]->this.match/1[immtest.Opt[immR]]([f]),.do/1([f]):Sig[gens=[],ts=[immtest.OptDo[immT]],ret=immtest.Opt[immT]]->this.match/1[immtest.Opt[immT]]([f]),.flatMap/1([f]):Sig[gens=[R],ts=[immtest.OptFlatMap[immT,immR]],ret=immtest.Opt[immR]]->this.match/1[immtest.Opt[immR]]([f])}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Opt[T]:NoMutHyg[T]{
      .match[R](m: OptMatch[T, R]): R -> m.none,
      .map[R](f: OptMap[T,R]): Opt[R]->this.match(f),
      .do(f: OptDo[T]):Opt[T]->this.match(f),
      .flatMap[R](f: OptFlatMap[T, R]): Opt[R]->this.match(f),
      }
    OptMatch[T,R]:{ .some(x:T): R, .none: R }
    OptFlatMap[T,R]:OptMatch[T,Opt[R]]{ .none->{} }
    OptMap[T,R]:OptMatch[T,Opt[R]]{ #(t:T):R, .some(x) -> Opts#(this#x), .none->{} }
    OptDo[T]:OptMatch[T,Void]{
      #(t:T):Void,   //#[R](t:T):R,
      .some(x) -> Opts#(this._doRes(this#x, x)),
      .none->{},
      ._doRes(y:Void,x:T):Opt[T]->Opts#x
      }
    Opt:{ #[T](x: T): Opt[T] -> { .match(m)->m.some(x)} }
    Void:{}
    NoMutHyg[X]:{}
    """); }

  @Test void immDelegateExplicit() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m1/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[T]([x]),.
      m2/1([k]):Sig[gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2[ T](x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }

  @Test void immDelegateExplicitImmGen() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m1/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[imm T]([x]),.
      m2/1([k]):Sig[gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2[T](x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }

  @Test void immDelegate() { ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m1/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Void[]]->this.m2/1[immT]([x]),
      .m2/1([k]):Sig[gens=[K],ts=[immK],ret=immtest.Void[]]->[-]}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    A:{
      .m1[T](x:T):Void->this.m2(x),
      .m2[K](k:K):Void
      }
    Void:{}
    """); }
  @Test void immDelegate2() { ok("""
    {test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[--][test.B[X]]{'this}],test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this.m1/1([x]):Sig[gens=[T],ts=[immtest.B[immT]],ret=immtest.Void[]]->this.m2/1[immT]([x]),.m2/1([k]):Sig[gens=[K],ts=[immtest.B[immK]],ret=immtest.Void[]]->[-]}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    B[X]:{}
    A:{
      .m1[T](x:B[T]):Void->this.m2(x),
      .m2[K](k:B[K]):Void
      }
    Void:{}
    """); }
  @Test void immDelegate3() { ok("""
    {test.B/2=Dec[name=test.B/2,gxs=[T,R],lambda=[--][test.B[T,R]]{'this}],test.A/1=Dec[name=test.A/1,gxs=[T],lambda=[--][test.A[T]]{'this.m1/1([x]):Sig[gens=[R],ts=[immtest.B[immT,immR]],ret=immtest.Void[]]->this.m2/1[immR]([x]),.m2/1([k]):Sig[gens=[K],ts=[immtest.B[immT,immK]],ret=immtest.Void[]]->[-]}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    B[T,R]:{}
    A[T]:{
      .m1[R](x:B[T,R]):Void->this.m2(x),
      .m2[K](k:B[T,K]):Void
      }
    Void:{}
    """); }

  /*
  Void:{
    #[E](e:E):Void->this
    .and[R](v:Void,r:R):R->r
    }
  * */
  /*
  [-imm test.Let[]-][test.Let[]]{ } #/1[infer, imm test.Void[]]([[-imm test.Let[ X,  test.Void[]]-][]{
        .var/0([]): Sig[gens=[],ts=[],ret= X] ->
          this:mut test.Ref[ X] .swap/1[]([x: X]): X,
        .in/1([_]): Sig[gens=[],ts=[imm X],ret=imm test.Void[]] ->
          [-imm test.Void[]-][test.Void[]]{ }}]):imm test.Void[]
   */
  @Test void inferRefDef() { ok("""
    {test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[--][test.NoMutHyg[X]]{'this}],
    test.Let/0=Dec[name=test.Let/0,gxs=[],lambda=[--][test.Let[]]{'this
      #/1([l]):Sig[gens=[V,R],ts=[immtest.Let[V,R]],ret=R]->l.in/1[]([l.var/0[]([])])}],
    test.Let/2=Dec[name=test.Let/2,gxs=[V,R],lambda=[--][test.Let[V,R]]{'this
      .var/0([]):Sig[gens=[],ts=[],ret=V]->[-],
      .in/1([v]):Sig[gens=[],ts=[V],ret=R]->[-]}],
    test.Ref/1=Dec[name=test.Ref/1,gxs=[X],lambda=[--][test.Ref[X],test.NoMutHyg[immX],test.Sealed[]]{'this
      */0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-],
      .swap/1([x]):Sig[=mut,gens=[],ts=[X],ret=X]->[-],
      :=/1([x]):Sig[=mut,gens=[],ts=[X],ret=immtest.Void[]]->
        [-imm-][test.Let[]]{'fear[###]$}#/1[X,immtest.Void[]]([[-imm-][test.Let[X,immtest.Void[]]]{'fear[###]$
          .var/0([]):Sig[gens=[],ts=[],ret=X]->this.swap/1[]([x]),
          .in/1([fear[###]$]):Sig[gens=[],ts=[X],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}]),
      <-/1([f]):Sig[=mut,gens=[],ts=[muttest.UpdateRef[mutX]],ret=X]->this.swap/1[]([f#/1[]([this*/0[]([])])])}],
      test.UpdateRef/1=Dec[name=test.UpdateRef/1,gxs=[X],lambda=[--][test.UpdateRef[X]]{'this
        #/1([x]):Sig[=mut,gens=[],ts=[X],ret=X]->[-]}],
      test.Sealed/0=Dec[name=test.Sealed/0,gxs=[],lambda=[--][test.Sealed[]]{'this}],
      test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}],
      test.Ref/0=Dec[name=test.Ref/0,gxs=[],lambda=[--][test.Ref[]]{'this
        #/1([x]):Sig[gens=[X],ts=[X],ret=muttest.Ref[X]]->this#/1[X]([x])}]}
    """, """
    package test
    NoMutHyg[X]:{}
    Sealed:{} Void:{}
    Let:{ #[V,R](l:Let[ V, R]): R -> l.in(l.var) }
    Let[V,R]:{ .var: V, .in(v: V): R }
    Ref:{ #[X](x:  X): mut Ref[ X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      recMdf * : recMdf X,
      mut .swap(x:  X):  X,
      mut :=(x:  X): Void -> Let#{ .var -> this.swap(x), .in(_)->Void },
      mut <-(f: mut UpdateRef[mut X]):  X -> this.swap(f#(this*)),
    }
    UpdateRef[X]:{ mut #(x:  X):  X }
    """); }
  @Test void inferCallGens(){ ok("""
    {test.NoMutHyg/1=Dec[name=test.NoMutHyg/1,gxs=[X],lambda=[--][test.NoMutHyg[X]]{'this}],test.LetMut/0=Dec[name=test.LetMut/0,gxs=[],lambda=[--][test.LetMut[]]{'this#/1([l]):Sig[gens=[V,R],ts=[muttest.LetMut[V,R]],ret=R]->l.in/1[]([l.var/0[]([])])}],test.Ref/1=Dec[name=test.Ref/1,gxs=[X],lambda=[--][test.Ref[X],test.NoMutHyg[immX],test.Sealed[]]{'this*/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-],.swap/1([x]):Sig[=mut,gens=[],ts=[X],ret=X]->[-],.test1/1([x]):Sig[=mut,gens=[],ts=[X],ret=muttest.LetMut[X,immtest.Void[]]]->[-mut-][test.LetMut[X,immtest.Void[]]]{'fear[###]$.var/0([]):Sig[=mut,gens=[],ts=[],ret=X]->this.swap/1[]([x]),.in/1([fear[###]$]):Sig[=mut,gens=[],ts=[X],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}},.test2/1([x]):Sig[=mut,gens=[],ts=[X],ret=immtest.Void[]]->[-imm-][test.LetMut[]]{'fear[###]$}#/1[X,immtest.Void[]]([this.test1/1[]([x])]),:=/1([x]):Sig[=mut,gens=[],ts=[X],ret=immtest.Void[]]->[-imm-][test.LetMut[]]{'fear[###]$}#/1[X,immtest.Void[]]([[-mut-][test.LetMut[X,immtest.Void[]]]{'fear[###]$.var/0([]):Sig[=mut,gens=[],ts=[],ret=X]->this.swap/1[]([x]),.in/1([fear[###]$]):Sig[=mut,gens=[],ts=[X],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}]),<-/1([f]):Sig[=mut,gens=[],ts=[muttest.UpdateRef[X]],ret=X]->this.swap/1[]([f#/1[]([this*/0[]([])])])}],test.UpdateRef/1=Dec[name=test.UpdateRef/1,gxs=[X],lambda=[--][test.UpdateRef[X]]{'this#/1([x]):Sig[=mut,gens=[],ts=[X],ret=X]->[-]}],test.Ref/0=Dec[name=test.Ref/0,gxs=[],lambda=[--][test.Ref[]]{'this#/1([x]):Sig[gens=[X],ts=[X],ret=muttest.Ref[X]]->this#/1[X]([x])}],test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}],test.Sealed/0=Dec[name=test.Sealed/0,gxs=[],lambda=[--][test.Sealed[]]{'this}],test.LetMut/2=Dec[name=test.LetMut/2,gxs=[V,R],lambda=[--][test.LetMut[V,R]]{'this.var/0([]):Sig[=mut,gens=[],ts=[],ret=V]->[-],.in/1([v]):Sig[=mut,gens=[],ts=[V],ret=R]->[-]}]}
    """, """
    package test
    Ref:{ #[X](x:  X): mut Ref[ X] -> this#(x) }
    Ref[X]:NoMutHyg[X],Sealed{
      recMdf * : recMdf X,
      mut .swap(x:  X):  X,
      mut .test1(x:  X): mut LetMut[ X,Void]->{
        .var ->this.swap(x), .in(_) -> Void },
      mut .test2(x: X): Void -> LetMut#(this.test1(x)),
      mut :=(x:  X): Void -> LetMut#[ X,Void]mut LetMut[ X,Void]{ .var ->
       this.swap(x), .in(_) -> Void },
      mut <-(f: mut UpdateRef[ X]):  X -> this.swap(f#(this*)),
      }
    UpdateRef[X]:{ mut #(x:  X):  X }
    """, """
    package test
    NoMutHyg[X]:{} Void:{} Sealed:{}
    LetMut:{ #[V,R](l: mut LetMut[ V,  R]):  R -> l.in(l.var) }
    LetMut[V,R]:{ mut .var:  V, mut .in(v:  V):  R }
    """); }

  @Test void nestedGensClash(){ ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[],test.A[]]{'this
      .foo/0([]):Sig[gens=[X0/0$],ts=[],ret=immtest.A[]]->
        [-imm-][test.A[]]{'fear[###]$
          .foo/0([]):Sig[gens=[X1/0$],ts=[],ret=immtest.A[]]->
            [-imm-][test.A[]]{'fear[###]$.foo/0([]):Sig[gens=[X2/0$],ts=[],ret=immtest.A[]]->this}}}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this.foo/0([]):Sig[gens=[X],ts=[],ret=immtest.A[]]->[-]}]}
    """, """
    package test
    A:{
      .foo[X]:A
    }
    B:A{
      .foo -> {
        .foo -> {
          .foo -> this
        }
      }
    }
    """); }

  // TODO: why isn't this inferring gens?
  @Test void boolUsageExplicitGensBasicSameT() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[],base.Main[imm42[]]]{'this
      #/1([fear[###]$]):Sig[gens=[],ts=[lentbase.System[]],ret=imm42[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[imm42[]]([[-mut-][base.ThenElse[imm42[]]]{'fear[###]$
            .then/0([]):Sig[=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[42]{
      _->False.or(True) ?[42]{ .then -> 42, .else -> 42 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: mutH System): R }
    System:{} // Root capability
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
  @Test void boolUsageExplicitGensBasic() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then -> 42, .else -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: mutH System): R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    Int:{}
    _IntInstance:Int{}
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """); }
  @Test void boolUsageExplicitGensRTBasic1() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then: Int -> 42, .else: Int -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: mutH System): R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }
  @Test void boolUsageExplicitGensRTBasic2() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[],base.Main[imm base.Int[]]]{'this
      #/1([fear[###]$]):Sig[gens=[],ts=[lentbase.System[]],ret=imm base.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[imm base.Int[]]]{'fear[###]$
            .then/0([]):Sig[gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[=mut,gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then: Int -> 42, .else -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: mutH System):  R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }
  @Test void boolUsageExplicitGensRTBasic3() { ok("""
    {test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[],base.Main[immbase.Int[]]]{'this
      #/1([fear[###]$]):Sig[gens=[],ts=[lentbase.System[]],ret=immbase.Int[]]->
        [-imm-][base.False[]]{'fear[###]$}
          .or/1[]([[-imm-][base.True[]]{'fear[###]$}])
          ?/1[immbase.Int[]]([[-mut-][base.ThenElse[immbase.Int[]]]{'fear[###]$
            .then/0([]):Sig[=mut,gens=[],ts=[],ret=imm42[]]->[-imm-][42[]]{'fear[###]$},
            .else/0([]):Sig[gens=[],ts=[],ret=imm0[]]->[-imm-][0[]]{'fear[###]$}}])}]}
    """, """
    package test
    alias base.Main as Main, alias base.Int as Int, alias base.False as False, alias base.True as True,
    Test:Main[Int]{
      _->False.or(True) ?[Int]{ .then -> 42, .else: Int -> 0 }
    }
    """, """
    package base
    Sealed:{}
    Main[R]:{ #(s: mutH System):  R }
    System:{} // Root capability
    Bool:Sealed{
    .and(b: Bool): Bool,
    .or(b: Bool): Bool,
    .not: Bool,
    ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
    }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    Int:{}
    _IntInstance:Int{}
    IntOpts:{}
    """); }

  @Test void factoryTrait() { ok("""
    {test.MyContainer/0=Dec[name=test.MyContainer/0,gxs=[],lambda=[--][test.MyContainer[]]{'this
      #/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.MyContainer[immT]]->
        [-imm-][test.MyContainer[immT]]{'fear[###]$.get/0([]):Sig[gens=[],ts=[],ret=immT]->x}}],
    test.MyContainer/1=Dec[name=test.MyContainer/1,gxs=[T],lambda=[--][test.MyContainer[T],base.NoMutHyg[immT]]{'this
      .get/0([]):Sig[gens=[],ts=[],ret=immT]->[-]}]}
    """, """
    package test
    MyContainer[T]:base.NoMutHyg[T]{ .get: T }
    MyContainer:{
      #[T](x: T): MyContainer[T] -> { x }
    }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }
  @Test void factoryTrait2() { ok("""
    {test.Opt/0=Dec[name=test.Opt/0,gxs=[],lambda=[--][test.Opt[]]{'this
      #/1([x]):Sig[gens=[T],ts=[immT],ret=immtest.Opt[immT]]->[-imm-][test.Opt[immT]]{'fear[###]$}}],
    test.Opt/1=Dec[name=test.Opt/1,gxs=[T],lambda=[--][test.Opt[T]]{'this}]}
    """, """
    package test
    Opt[T]:{}
    Opt:{ #[T](x: T): Opt[T] -> {} }
    """); }

  @Test void recMdfInSubHygMethGens() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this
      .foo/1([x]):Sig[gens=[],ts=[immX],ret=immX]->
        [-imm-][test.B[]]{'fear[###]$
          .argh/0([]):Sig[=recMdf,gens=[X1/0$],ts=[],ret=recMdfX1/0$]->x
          }.argh/0[immX]([])}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[]]{'this
      .argh/0([]):Sig[=recMdf,gens=[X],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B{ x }.argh }
    B:{ recMdf .argh[X]: recMdf X }
    """); }
  @Test void recMdfInSubHyg() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this
      .foo/1([x]):Sig[gens=[],ts=[immX],ret=immX]->
        [-imm-][test.B[immX]]{'fear[###]$
          .argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=immX]->x}.argh/0[]([])}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[--][test.B[X]]{'this
      .argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: X): X -> B[X]{ x }.argh }
    B[X]:{ recMdf .argh: recMdf X }
    """); }
  @Test void recMdfInSubHygMut() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this
      .foo/1([x]):Sig[gens=[],ts=[mutX],ret=mutX]->
        [-mut-][test.B[mutX]]{'fear[###]$
          .argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdf X]->x}.argh/0[]([])}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[--][test.B[X]]{'this
      .argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: mut X): mut X -> mut B[mut X]{ x }.argh }
    B[X]:{ recMdf .argh: recMdf X }
    """); }
  @Test void inferRecMdf() { ok("""
    {test.FooBox/0=Dec[name=test.FooBox/0,gxs=[],lambda=[--][test.FooBox[],test.Box[immtest.Foo[]]]{'this
      .getFoo/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdftest.Foo[]]->this.get/0[]([])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this}],
    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[--][test.Box[T]]{'this
      .get/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfT]->[-]}]}
    """, """
    package test
    Foo:{}
    Box[T]:{ recMdf .get: recMdf T }
    FooBox:Box[Foo]{ recMdf .getFoo: recMdf Foo -> this.get }
    """); }
  @Test void doNotChangeExplicitLambdaMdf1() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[--][test.Bar[]]{'this
      .a/0([]):Sig[gens=[],ts=[],ret=imm test.Foo[]]->[-mut-][test.Foo[]]{'fear[###]$}}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this}]}
    """, """
    package test
    Foo:{}
    Bar:{ .a: Foo -> mut Foo }
    """); }
  @Test void doNotChangeExplicitLambdaMdf2() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[--][test.Bar[]]{'this
      .a/0([]):Sig[=read,gens=[],ts=[],ret=imm test.Foo[]]->[-recMdf-][test.Foo[]]{'fear[###]$}}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this}]}
    """, """
    package test
    Foo:{}
    Bar:{ read .a: Foo -> recMdf Foo }
    """); }

  @Test void numImpls1() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[--][test.Bar[]]{'this
      .nm/1([n]):Sig[gens=[],ts=[immbase.Int[]],ret=immbase.Int[]]->n,
      .check/0([]):Sig[gens=[],ts=[],ret=immbase.Int[]]->
        this.nm/1[]([[-imm-][test.Foo[]]{'fear[###]$}.bar/0[]([])])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this
      .bar/0([]):Sig[gens=[],ts=[],ret=imm5[]]->[-imm-][5[]]{'fear[###]$}}]}
    """, Stream.concat(Stream.of("""
    package test
    alias base.Int as Int,
    Foo:{ .bar: 5 -> 5 }
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(Foo.bar)
      }
    """), Arrays.stream(Base.baseLib)).toArray(String[]::new));}

  @Test void numImpls2() { ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[--][test.Bar[]]{'this
      .nm/1([n]):Sig[gens=[],ts=[immbase.Int[]],ret=immbase.Int[]]->n,
      .check/0([]):Sig[gens=[],ts=[],ret=immbase.Int[]]->
        this.nm/1[]([[-imm-][5[]]{'fear[###]$}])}]}
    """, Stream.concat(Stream.of("""
    package test
    alias base.Int as Int,
    Bar:{
      .nm(n: Int): Int -> n,
      .check: Int -> this.nm(5)
      }
    """), Arrays.stream(Base.baseLib)).toArray(String[]::new));}

  @Test void assertions() { ok("""
    {test.Assert/0=Dec[name=test.Assert/0,gxs=[],lambda=[--][test.Assert[]]{'this
      #/2([assertion,cont]):Sig[gens=[R],ts=[immbase.Bool[],muttest.AssertCont[R]],ret=R]->
        assertion?/1[R]([[-mut-][base.ThenElse[ R]]{'fear[###]$
          .then/0([]):Sig[=mut,gens=[],ts=[],ret= R]->cont#/0[]([]),
          .else/0([]):Sig[=mut,gens=[],ts=[],ret=R]->this._fail/0[R]([])}]),
      #/3([assertion,msg,cont]):Sig[gens=[R],ts=[immbase.Bool[],immbase.Str[],muttest.AssertCont[R]],ret=R]->
        assertion?/1[R]([[-mut-][base.ThenElse[ R]]{'fear[###]$
          .then/0([]):Sig[=mut,gens=[],ts=[],ret= R]->cont#/0[]([]),
          .else/0([]):Sig[=mut,gens=[],ts=[],ret=R]->this._fail/1[R]([msg])}]),
      ._fail/0([]):Sig[gens=[R],ts=[],ret=R]->this._fail/0[R]([]),
      ._fail/1([msg]):Sig[gens=[R],ts=[immbase.Str[]],ret=R]->this._fail/1[R]([msg])}],
    test.AssertCont/1=Dec[name=test.AssertCont/1,gxs=[R],lambda=[--][test.AssertCont[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}]}
    """, """
    package test
    alias base.Bool as Bool, alias base.Str as Str,
    Assert:{
      #[R](assertion: Bool, cont: mut AssertCont[ R]):  R -> assertion ? {
        .then -> cont#,
        .else -> this._fail[ R]()
        },
      #[R](assertion: Bool, msg: Str, cont: mut AssertCont[ R]):  R -> assertion ? {
        .then -> cont#,
        .else -> this._fail[ R](msg)
        },
      ._fail[R]:  R -> this._fail,
      ._fail[R](msg: Str):  R -> this._fail(msg),
      }
    AssertCont[R]:{ mut #:  R }
    """, """
    package base
    Sealed:{} Str:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[ R]):  R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then:  R, mut .else:  R, }
    """); }

  @Test void sugar1() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([x,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x:  X, cont: mut Cont[ X,  R]):  R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo] f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugar() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([x,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x:  X, cont: mut Cont[ X,  R]):  R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1MismatchedXs() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([y,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([z,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([z,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(y:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](z:  X, cont: mut Cont[ X,  R]):  R -> cont#(z, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo] f = Foo
        .return{ f.v }
      }
    """); }
  @Test void sugar1Brackets() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([x,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo[]]{'fear[###]$},
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo:{ .v: Void -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x:  X, cont: mut Cont[ X,  R]):  R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar[Foo](f = Foo)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([x,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[]]([
            [-imm-][test.Foo'[]]{'fear[###]$}#/0[]([]),
            [-mut-][test.Cont[immtest.Foo[],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this.v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.New/1=Dec[name=test.New/1,gxs=[R],lambda=[--][test.New[R]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=immR]->[-]}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[--][test.Foo'[],test.New[immtest.Foo[]]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=immtest.Foo[]]->[-imm-][test.Foo[]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    New[R]:{ #: R }
    Foo:{ .v: Void -> {} }
    Foo':New[Foo]{ Foo }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x:  X, cont: mut Cont[ X,  R]):  R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = Foo'#)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex2() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[--][test.Foo[X]]{'this
      .v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[X,muttest.Cont[X,R]],ret=R]->cont#/2[]([x,this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-imm-][test.Foo'[]]{'fear[###]$}#/1[imm test.Void[]]([[-imm-][test.Void[]]{'fear[###]$}]),
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[--][test.Foo'[]]{'this
      #/1([x]):Sig[gens=[X],ts=[X],ret=immtest.Foo[X]]->[-imm-][test.Foo[X]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x:  X): Foo[ X] -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x:  X, cont: mut Cont[ X,  R]):  R -> cont#(x, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = Foo'#Void)
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplex3() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[--][test.Foo[X]]{'this
      .v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[muttest.ReturnStmt[X],muttest.Cont[X,R]],ret=R]->cont#/2[]([x#/0[]([]),this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-mut-][test.ReturnStmt[immtest.Foo[immtest.Void[]]]]{'fear[###]$
              #/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Foo[immtest.Void[]]]->
                [-imm-][test.Foo'[]]{'fear[###]$}#/1[immtest.Void[]]([[-imm-][test.Void[]]{'fear[###]$}])},
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[--][test.Foo'[]]{'this
      #/1([x]):Sig[gens=[X],ts=[X],ret=immtest.Foo[X]]->[-imm-][test.Foo[X]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x:  X): Foo[ X] -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x: mut ReturnStmt[ X], cont: mut Cont[ X,  R]):  R -> cont#(x#, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = { Foo'#Void })
        .return{ f.v }
      }
    """); }
  @Test void sugar1InferSugarComplexNo() { ok("""
    {test.Cont/2=Dec[name=test.Cont/2,gxs=[X,R],lambda=[--][test.Cont[X,R]]{'this
      #/2([x,cont]):Sig[=mut,gens=[],ts=[X,muttest.Candy[R]],ret=R]->[-]}],
    test.ReturnStmt/1=Dec[name=test.ReturnStmt/1,gxs=[R],lambda=[--][test.ReturnStmt[R]]{'this
      #/0([]):Sig[=mut,gens=[],ts=[],ret=R]->[-]}],
    test.Foo/1=Dec[name=test.Foo/1,gxs=[X],lambda=[--][test.Foo[X]]{'this
      .v/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear[###]$}}],
    test.Candy/1=Dec[name=test.Candy/1,gxs=[R],lambda=[--][test.Candy[R]]{'this
      .sugar/2([x,cont]):Sig[=mut,gens=[X],ts=[muttest.ReturnStmt[X],muttest.Cont[X,R]],ret=R]->cont#/2[]([x#/0[]([]),this]),
      .return/1([a]):Sig[=mut,gens=[],ts=[muttest.ReturnStmt[R]],ret=R]->a#/0[]([])}],
    test.Usage/0=Dec[name=test.Usage/0,gxs=[],lambda=[--][test.Usage[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Candy[immtest.Void[]]]{'fear[###]$}
          .sugar/2[immtest.Foo[imm test.Void[]]]([
            [-mut-][test.ReturnStmt[immtest.Foo[immtest.Void[]]]]{'fear[###]$
              #/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Foo[immtest.Void[]]]->
                [-imm-][test.Foo'[]]{'fear[###]$}#/1[immtest.Void[]]([[-imm-][test.Void[]]{'fear[###]$}])},
            [-mut-][test.Cont[immtest.Foo[immtest.Void[]],immtest.Void[]]]{'fear[###]$
              #/2([f,fear[###]$]):Sig[=mut,gens=[],ts=[immtest.Foo[immtest.Void[]],muttest.Candy[immtest.Void[]]],ret=immtest.Void[]]->
                fear[###]$.return/1[]([[-mut-][test.ReturnStmt[immtest.Void[]]]{'fear[###]$#/0([]):Sig[=mut,gens=[],ts=[],ret=immtest.Void[]]->f.v/0[]([])}])}])}],
    test.Foo'/0=Dec[name=test.Foo'/0,gxs=[],lambda=[--][test.Foo'[]]{'this
      #/1([x]):Sig[gens=[X],ts=[X],ret=immtest.Foo[X]]->[-imm-][test.Foo[X]]{'fear[###]$}}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Void:{}
    Foo[X]:{ .v: Void -> {} }
    Foo':{ #[X](x:  X): Foo[ X] -> {} }
    Cont[X,R]:{ mut #(x:  X, cont: mut Candy[ R]):  R }
    ReturnStmt[R]:{ mut #:  R }
    Candy[R]:{
      mut .sugar[X](x: mut ReturnStmt[ X], cont: mut Cont[ X,  R]):  R -> cont#(x#, this),
      mut .return(a: mut ReturnStmt[ R]):  R -> a#,
      }
    Usage:{
      .foo: Void -> Candy[Void]
        .sugar(f = { Foo'#Void })
        .return{ f.v }
      }
    """); }

  @Disabled // TODO
  @Test void sugar1InferSugarIO() { ok("""
    """, """
    package test
    Usage:{
      .foo: Void -> System[Void]
        .use(io = UnrestrictedIO)
        .return{ io.println(Str{}) }
      }
    """, """
    package test
    Str:{} Void:{}
    LentReturnStmt[R]:{ mutH #:  R }
    System[R]:{
      mutH .use[C](c: FCap[lent _RootCap, mutH C], cont: mut UseCapCont[C,  R]):  R ->
        cont#(c#_RootCap, this),
      mutH .return(ret: mutH LentReturnStmt[ R]):  R -> ret#
      }
    _RootCap:IO{
      .print(msg) -> this.print(msg),
      .println(msg) -> this.println(msg),
      }
    UseCapCont[C, R]:{ mut #(cap: mutH C, self: mutH System[ R]):  R }
    FCap[C,R]:{
      #(auth: mutH C): mutH R,
      .close(c: mutH R): Void,
      }
    IO:{
      mutH .print(msg: Str): Void,
      mutH .println(msg: Str): Void,
      }
    UnrestrictedIO:FCap[lent _RootCap, mutH IO]{
      #(auth: mutH _RootCap): mutH IO -> this.scope(auth),
      .scope(auth: mutH IO): mutH IO -> auth,
      .close(c: mutH IO): Void -> {},
      }
    """); }

  @Test void mdfMismatch() { ok("""
    {test.Box/1=Dec[name=test.Box/1,gxs=[X],lambda=[--][test.Box[X],base.NoMutHyg[immX]]{'this
      .get/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}],
    test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[]]{'this
      #/1([t]):Sig[gens=[],ts=[readtest.Test[]],ret=lenttest.Box[readtest.Test[]]]->
        [-imm-][test.Box'[]]{'fear[###]$}.mut2lent/1[readtest.Test[]]([t])}],
    test.Box'/0=Dec[name=test.Box'/0,gxs=[],lambda=[--][test.Box'[]]{'this
      .mut2lent/1([x]):Sig[gens=[X],ts=[X],ret=muttest.Box[X]]->
        [-mut-][test.Box[X]]{'fear[###]$.get/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->x}}]}
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[X]{
      recMdf .get: recMdf X
      }
    Box':{
      .mut2lent[X](x:  X): mut Box[ X] -> { x }
      }
    Test:{
      #(t: read Test): mutH Box[read Test] -> Box'.mut2lent(t),
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void immToReadCapture() { ok("""
    {test.L/1=Dec[name=test.L/1,gxs=[X],lambda=[--][test.L[X]]{'this
      .absMeth/0([]):Sig[gens=[],ts=[],ret=readX]->[-]}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[]]{'this}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m/1([par]):Sig[=read,gens=[T],ts=[immT],ret=readtest.L[immT]]->
        [-read-][test.L[imm T]]{'fear[###]$.absMeth/0([]):Sig[gens=[],ts=[],ret=readT]->par}}]}
    """, """
    package test
    B:{}
    L[X]:{ imm .absMeth: read X }
    A:{ read .m[T](par: imm T) : read L[imm T] -> read L[imm T]{.absMeth->par} }
    """); }

  @Test void dontOverrideMdf() { ok("""
    {test.Box/1=Dec[name=test.Box/1,gxs=[X],lambda=[--][test.Box[X],base.NoMutHyg[X]]{'this
      .get/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}],
    test.Box'/0=Dec[name=test.Box'/0,gxs=[],lambda=[--][test.Box'[]]{'this
      .mut2lent/1([x]):Sig[gens=[X],ts=[X],ret=muttest.Box[X]]->
        [-mut-][test.Box[ X]]{'fear[###]$
          .get/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->x}}]}
    """, """
    package test
    alias base.NoMutHyg as NoMutHyg,
    Box[X]:NoMutHyg[ X]{
      recMdf .get: recMdf X
      }
    Box':{
      .mut2lent[X](x:  X): mut Box[ X] -> mut Box[ X]{ x }
      }
    """, """
    package base
    NoMutHyg[X]:{}
    """); }

  @Test void nestedRecMdfExplicitMdf() { ok("""
    {test.F/1=Dec[name=test.F/1,gxs=[X],lambda=[--][test.F[X]]{'this#/1([x]):Sig[gens=[],ts=[X],ret=X]->x}],
    test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this
      .m1/2([a,b]):Sig[=recMdf,gens=[],ts=[recMdfX,immtest.F[recMdfX]],ret=recMdfX]->b#/1[]([a])}],
    test.B/1=Dec[name=test.B/1,gxs=[Y],lambda=[--][test.B[Y]]{'this
      #/1([a]):Sig[=recMdf,gens=[],ts=[muttest.A[muttest.B[recMdfY]]],ret=mut test.B[recMdfY]]->
        a.m1/2[]([[-mut-][test.B[recMdfY]]{'fear0$},[-imm-][test.F[mut test.B[recMdfY]]]{'fear1$}])}],
    test.C/0=Dec[name=test.C/0,gxs=[],lambda=[--][test.C[]]{'this
      #/1([b]):Sig[gens=[],ts=[muttest.B[muttest.C[]]],ret=muttest.B[muttest.C[]]]->
        b#/1[]([[-mut-][test.A[muttest.B[mut test.C[]]]]{'fear2$}]),
      .i/1([b]):Sig[gens=[],ts=[muttest.B[immtest.C[]]],ret=muttest.B[immtest.C[]]]->
        b#/1[]([[-mut-][test.A[muttest.B[imm test.C[]]]]{'fear3$}])}]}
    """, """
    package test
    A[X]:{
      recMdf .m1(a: recMdf X, b: imm F[recMdf X]): recMdf X -> b#a,
      }
    F[X]:{ imm #(x:  X):  X -> x, }
    B[Y]:{
      recMdf #(a: mut A[mut B[recMdf Y]]): mut B[recMdf Y] -> a.m1(mut B[recMdf Y], F[mut B[recMdf Y]]),
      }
    C:{
      #(b: mut B[mut C]): mut B[mut C] -> b#({}),
      .i(b: mut B[imm C]): mut B[imm C] -> b#(mut A[mut B[imm C]]),
      }
    """); }

  @Test void inferCollapsesRecMdf() { same("""
    package test
    Foo[T]:{
      recMdf .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x:  T):  T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map(mut F[A]{ _->A }) }
    """, """
    package test
    Foo[T]:{
      recMdf .map(f: mut F[recMdf T]): recMdf Foo[recMdf T] -> this
      }
    F[T]:{ mut #(x:  T):  T }
    A:{}
    Usage:{ .break(foo: Foo[A]): Foo[A] -> foo.map{ _->A } }
    """); }

  @Test void shouldBeAbleToCaptureMutInMutRecMdfSubTypeGenericExplicit() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this
      .foo/1([x]):Sig[gens=[],ts=[mutX],ret=muttest.B[mutX]]->
        [-mut-][test.B[mutX]]{'fear0$.argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->x}}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[--][test.B[X]]{'this
      .argh/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}]}
    """, """
    package test
    A[X]:{ .foo(x: mut X): mut B[mut X] -> mut B[mut X]{ recMdf .argh: recMdf X -> x } }
    B[X]:{ recMdf .argh: recMdf X }
    """);}

  @Test void shouldNotRewriteUserWrittenTypes1() { ok("""
    {test.Break/0=Dec[name=test.Break/0,gxs=[],lambda=[--][test.Break[]]{'this
      .callMe/2([a,b]):Sig[gens=[R],ts=[R,R],ret=R]->[-],
      .break/0([]):Sig[gens=[],ts=[],ret=immtest.A[]]->
        this.callMe/2[immtest.A[]]([[-imm-][test.B[]]{'fear0$},[-imm-][test.B[],test.A[]]{'fear1$}])}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[],test.A[]]{'this}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this}]}
    """, """
    package test
    Break:{
      .callMe[R](a:  R, b:  R):  R,
      .break: A -> this.callMe[A](B, A),
      }
    A:{} B:A{}
    """); }

  @Test void shouldNotRandomlyInferRecMdfGens1() { ok("""
    {test.A/1=Dec[name=test.A/1,gxs=[X],lambda=[--][test.A[X]]{'this}],
    test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[]]{'this
      .m1/0([]):Sig[gens=[Y],ts=[],ret=muttest.B[Y]]->this.m2/0[Y]([]),
      .m2/0([]):Sig[gens=[Y],ts=[],ret=muttest.B[Y]]->[-mut-][test.B[Y]]{'fear0$}}],
    test.B/1=Dec[name=test.B/1,gxs=[X],lambda=[--][test.B[X]]{'this}]}
    """, """
    package test
    A[X]:{}
    B:{
      .m1[Y]: mut B[ Y] -> this.m2,
      .m2[Y]: mut B[ Y] -> {}
      }
    B[X]:{}
    """); }
  @Test void collapseRecMdfInInference() { ok("""
    {test.Cont/1=Dec[name=test.Cont/1,gxs=[X],lambda=[--][test.Cont[X]]{'this#/0([]):Sig[=recMdf,gens=[],ts=[],ret=recMdfX]->[-]}],
    test.Test/0=Dec[name=test.Test/0,gxs=[],lambda=[--][test.Test[]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=immtest.Void[]]->
        [-imm-][test.Supply[]]{'fear0$}#/1[immtest.Void[]]([[-imm-][test.Cont[immtest.Void[]]]{'fear1$
          #/0([]):Sig[=recMdf,gens=[],ts=[],ret=immtest.Void[]]->[-imm-][test.Void[]]{'fear2$}}])}],
    test.Supply/0=Dec[name=test.Supply/0,gxs=[],lambda=[--][test.Supply[]]{'this
      #/1([cont]):Sig[=recMdf,gens=[X],ts=[recMdftest.Cont[immX]],ret=recMdfX]->cont#/0[]([])}],
    test.Void/0=Dec[name=test.Void/0,gxs=[],lambda=[--][test.Void[]]{'this}]}
    """, """
    package test
    Test:{
      // .m1: Void -> Supply#(imm Cont[Void]{ Void }) // should infer to this
      .m1: Void -> Supply#({ Void })
      }
    Supply:{
      recMdf #[X](cont: recMdf Cont[X]): recMdf X -> cont#
      }
    Cont[X]:{ recMdf #: recMdf X }
    Void:{}
    """); }

  // TODO: this should eventually fail with an "inference failed" message when I add that error
  @Disabled
  @Test void callingEphemeralMethod() { fail("""
    """, """
    package base
    A[X]:{ .foo(x: X): X -> { .foo: recMdf X -> x}.foo }
    B:{}
    """); }

  @Test void multiTraitExplicit() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[--][a.A[],base.Sealed[]]{'this
      .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->[-]}],
     b.C/0=Dec[name=b.C/0,gxs=[],lambda=[--][b.C[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
        [-imm-][a.A[]]{'fear[###]$ .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
          [-imm-][a.Foo[]]{'fear[###]$}}.a/0[]([])}],
     a.B/0=Dec[name=a.B/0,gxs=[],lambda=[--][a.B[],a.A[]]{'this
       .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
        [-imm-][a.Foo[]]{'fear[###]$}}],
     a.Foo/0=Dec[name=a.Foo/0,gxs=[],lambda=[--][a.Foo[]]{'this}]}
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo }
    B:A{ .a -> {} }
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> a.A{ .a: Foo -> Foo }.a
      }
    """, """
    package base
    Sealed:{}
    """); }
  @Test void multiTraitInferred() { ok("""
    {a.A/0=Dec[name=a.A/0,gxs=[],lambda=[--][a.A[],base.Sealed[]]{'this
      .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->[-]}],
     b.C/0=Dec[name=b.C/0,gxs=[],lambda=[--][b.C[]]{'this
      .foo/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
        [-imm-][a.A[]]{'fear[###]$ .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
          [-imm-][a.Foo[]]{'fear[###]$}}.a/0[]([])}],
     a.B/0=Dec[name=a.B/0,gxs=[],lambda=[--][a.B[],a.A[]]{'this
       .a/0([]):Sig[gens=[],ts=[],ret=imma.Foo[]]->
        [-imm-][a.Foo[]]{'fear[###]$}}],
     a.Foo/0=Dec[name=a.Foo/0,gxs=[],lambda=[--][a.Foo[]]{'this}]}
    """, """
    package a
    alias base.Sealed as Sealed,
    A:Sealed{ .a: Foo }
    B:A{ .a -> {} }
    Foo:{}
    """, """
    package b
    alias a.A as A, alias a.Foo as Foo,
    C:{
      .foo(): Foo -> a.A{ Foo }.a
      }
    """, """
    package base
    Sealed:{}
    """); }

  @Test void foldAcc() { ok("""
    {test.Num/0=Dec[name=test.Num/0,gxs=[],lambda=[--][test.Num[]]{'this
      +/1([other]):Sig[gens=[],ts=[immtest.Num[]],ret=immtest.Num[]]->[-]}],
    test.One/0=Dec[name=test.One/0,gxs=[],lambda=[--][test.One[],test.Num[]]{'this+/1([other]):Sig[gens=[],ts=[immtest.Num[]],ret=immtest.Num[]]->[-imm-][test.Abort[]]{'fear8$}!/0[immtest.Num[]]([])}],
    test.List/1=Dec[name=test.List/1,gxs=[E],lambda=[--][test.List[E]]{'this.fold/2([acc,f]):Sig[gens=[S],ts=[S,immtest.Fold[S,E]],ret=S]->[-imm-][test.Abort[]]{'fear9$}!/0[S]([])}],
    test.Break/0=Dec[name=test.Break/0,gxs=[],lambda=[--][test.Break[]]{'this
      #/1([l]):Sig[gens=[],ts=[imm test.List[imm test.Num[]]],ret=imm test.Num[]]->
        l.fold/2[imm test.Num[]](
          [[-imm-][test.Zero[]]{'fear10$},
          [-imm-][test.Fold[imm test.Num[], imm test.Num[]]]{'fear11$
            #/2([acc,n]):Sig[gens=[],ts=[imm test.Num[],imm test.Num[]],ret=imm test.Num[]]->acc+/1[]([n])}]
          )}],
    test.Fold/2=Dec[name=test.Fold/2,gxs=[S,T],lambda=[--][test.Fold[S,T]]{'this#/2([acc,x]):Sig[gens=[],ts=[S,T],ret=S]->[-]}],test.Abort/0=Dec[name=test.Abort/0,gxs=[],lambda=[--][test.Abort[]]{'this!/0([]):Sig[gens=[R],bounds={R=[imm,iso,mutH,mut,read,readH]},ts=[],ret=R]->this!/0[R]([])}],test.Zero/0=Dec[name=test.Zero/0,gxs=[],lambda=[--][test.Zero[],test.Num[]]{'this+/1([other]):Sig[gens=[],ts=[immtest.Num[]],ret=immtest.Num[]]->other}]}
    """, """
    package test
    Num: { +(other: Num): Num }
    Zero: Num{ +(other) -> other, }
    One: Num{ +(other) -> Abort! }
    List[E]: { .fold[S](acc: S, f: Fold[S, E]): S -> Abort! }
    Fold[S,T]: { #(acc: S, x: T): S }
    
    Break:{ #(l: List[Num]): Num -> l.fold[Num](Zero, {acc, n -> acc + n}) }
    
    Abort: { ![R:readH,mutH,read,mut,imm,iso]: R -> this! }
    """); }

  @Test void literalVsIT() {ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=mut test.Box[imm base.Nat[]]]->
        [-imm-][test.Box[]]{'fear[###]$}#/1[imm base.Nat[]]([[-imm-][5[]]{'fear[###]$}])}],
    
    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[--][test.Box[T]]{'this
      .get/0([]):Sig[gens=[],ts=[],ret=T]->[-]}],
    
    test.Box/0=Dec[name=test.Box/0,gxs=[],lambda=[--][test.Box[]]{'this
      #/1([t]):Sig[gens=[T],ts=[T],ret=mut test.Box[T]]->
        [-mut-][test.Box[T]]{'fear[###]$.get/0([]):Sig[gens=[],ts=[],ret=T]->t}}]}
    """, """
    package test
    A: {#: mut Box[base.Nat] -> Box#5}
    """, """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {mut .get: T}
    """, """
    package base
    Nat: {}
    _NatInstance: Nat{}
    """);}
  @Test void explicitLiteralGen() {ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=mut test.Box[imm base.Nat[]]]->
        [-imm-][test.Box[]]{'fear[###]$}#/1[imm 5[]]([[-imm-][5[]]{'fear[###]$}])}],
    
    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[--][test.Box[T]]{'this
      .get/0([]):Sig[gens=[],ts=[],ret=T]->[-]}],
    
    test.Box/0=Dec[name=test.Box/0,gxs=[],lambda=[--][test.Box[]]{'this
      #/1([t]):Sig[gens=[T],ts=[T],ret=mut test.Box[T]]->
        [-mut-][test.Box[T]]{'fear[###]$.get/0([]):Sig[gens=[],ts=[],ret=T]->t}}]}
    """, """
    package test
    A: {#: mut Box[base.Nat] -> Box#[5]5}
    """, """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {mut .get: T}
    """, """
    package base
    Nat: {}
    _NatInstance: Nat{}
    """);}
  @Test void literalGenInference() {ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[]]{'this
      #/1([b]):Sig[gens=[],ts=[mut test.Box[imm5[]]],ret=mut test.Box[imm5[]]] -> b}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=mut test.Box[imm base.Nat[]]]->
        [-imm-][test.B[]]{'fear26$}#/1[]([[-imm-][test.Box[]]{'fear27$}#/1[imm 5[]]([[-imm-][5[]]{'fear28$}])])}],

    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[--][test.Box[T]]{'this
      .get/0([]):Sig[gens=[],ts=[],ret=T]->[-]}],
    test.Box/0=Dec[name=test.Box/0,gxs=[],lambda=[--][test.Box[]]{'this
      #/1([t]):Sig[gens=[T],ts=[T],ret=muttest.Box[T]]->
        [-mut-][test.Box[T]]{'fear30$.get/0([]):Sig[gens=[],ts=[],ret=T]->t}}]}
    """, """
    package test
    A: {#: mut Box[base.Nat] -> B#(Box#5)}
    B: {#(b: mut Box[5]): mut Box[5] -> b}
    """, """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {mut .get: T}
    """, """
    package base
    Nat: {}
    _NatInstance: Nat{}
    """);}
  @Test void nonLiteralGenInference() {ok("""
    {test.B/0=Dec[name=test.B/0,gxs=[],lambda=[--][test.B[]]{'this
      #/1([b]):Sig[gens=[],ts=[mut test.Box[imm base.Nat[]]],ret=mut test.Box[imm base.Nat[]]] -> b}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      #/0([]):Sig[gens=[],ts=[],ret=mut test.Box[imm base.Nat[]]]->
        [-imm-][test.B[]]{'fear26$}#/1[]([[-imm-][test.Box[]]{'fear27$}#/1[imm base.Nat[]]([[-imm-][5[]]{'fear28$}])])}],

    test.Box/1=Dec[name=test.Box/1,gxs=[T],lambda=[--][test.Box[T]]{'this
      .get/0([]):Sig[gens=[],ts=[],ret=T]->[-]}],
    test.Box/0=Dec[name=test.Box/0,gxs=[],lambda=[--][test.Box[]]{'this
      #/1([t]):Sig[gens=[T],ts=[T],ret=muttest.Box[T]]->
        [-mut-][test.Box[T]]{'fear30$.get/0([]):Sig[gens=[],ts=[],ret=T]->t}}]}
    """, """
    package test
    A: {#: mut Box[base.Nat] -> B#(Box#5)}
    B: {#(b: mut Box[base.Nat]): mut Box[base.Nat] -> b}
    """, """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {mut .get: T}
    """, """
    package base
    Nat: {}
    _NatInstance: Nat{}
    """);}
  @Test void doNotReplaceInlineReturnTypes() {ok("""
    {test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->
        [-imm-][test.B[]]{'self.foo/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->self,
      .bar/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->
        self.foo/0[]([]),
      .str/0([]):Sig[gens=[],ts=[],ret=imm base.Str[]]->[-imm-]["cool"[]]{'fear2$}}}]}
    """, """
    package test
    alias base.Str as Str,
    
    A: {.m1: imm B -> B: {'self
      imm .foo: B -> self,
      read .bar: B -> self.foo,
      imm .str: Str -> "cool",
      }}
    """, """
    package base
    Str: {}
    _StrInstance: Str{}
    """);}

  @Test void doNotReplaceInlineReturnTypes2() {ok("""
    {test.Bar/0=Dec[name=test.Bar/0,gxs=[],lambda=[--][test.Bar[],test.Foo[]]{'this}],
    test.Foo/0=Dec[name=test.Foo/0,gxs=[],lambda=[--][test.Foo[]]{'this}],
    test.A/0=Dec[name=test.A/0,gxs=[],lambda=[--][test.A[]]{'this
      .m1/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->
        [-imm-][test.B[]]{'self.foo/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->self,
      .bar/0([]):Sig[gens=[],ts=[],ret=immtest.B[]]->self.foo/0[]([]),
      .str/0([]):Sig[gens=[],ts=[],ret=imm test.Foo[]]->[-imm-][test.Bar[]]{'fear2$}}}]}
    """, """
    package test
    alias base.Str as Str,
    
    Foo: {}
    Bar: Foo{}
    
    A: {.m1: imm B -> B: {'self
      imm .foo: B -> self,
      read .bar: B -> self.foo,
      imm .str: Foo -> Bar,
      }}
    """);}

  @Test void shouldRejectAbstractInferenceWithMoreThanOneMeth() {fail("""
    In position [###]/Dummy1.fear:2:32
    [E22 cannotInferAbsSig]
    Could not infer the signature for the abstract lambda in test.Fear0$/0. There must be one abstract lambda in the trait.
    """, """
    package test
    Box: {#[T](t: T): mut Box[T] -> {t}}
    Box[T]: {
      mut .get: T,
      read .rget: read T,
      read .riget: read/imm T,
      }
    """);}

  @Test void namedLiteral() {ok("""
    {a.Bob/0=Dec[name=a.Bob/0,gxs=[],lambda=[--][a.Bob[]]{'this}],
    a.Bar/1=Dec[name=a.Bar/1,gxs=[X],lambda=[--][a.Bar[X]]{'thi
      .m/0([]):Sig[gens=[],ts=[],ret=imma.Foo[X]]->
        [-imm-][a.Foo[X]]{'fear3$}}],
    a.List/1=Dec[name=a.List/1,gxs=[T],lambda=[--][a.List[T]]{'this}]}
    ""","""
    package a
    List[T]:{} Bob:{}
    Bar[X]: {.m(): Foo[X] -> Foo[X]:{}}
    """);}
}
