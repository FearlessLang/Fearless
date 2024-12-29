package program.inference;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestNorm {
  @Test void shouldWorkWithSameGens() {ok("""
    package test
    A: {.m1[X](x: X): X -> x}
    B: A{.m1[X](y: X): X -> y}
    """);}
  @Test void shouldWorkWithDifferentGens() {ok("""
    package test
    A: {.m1[X](x: X): X -> x}
    B: A{.m1[Y](y: Y): Y -> y}
    """);}

  @Test void shouldWorkWithOptional() {ok("""
    package test
    alias base.Opt as Opt,
    alias base.Opts as Opts,
    A:{mut .b: mut B} B:{}
    
    Foo: {#(optA: mut Opt[mut A]): mut B -> optA.map{a->a.b}.match{
      .some(a) -> a,
      .empty -> base.Abort!,
      }}
    """, """
    package base
    Abort: {![R:readH,mutH,read,mut,imm,iso]: R -> this!}
    Opts: {#[T:*](x: T): mut Opt[T] -> {
       .match(m) -> m.some(x),
      }}
    Opt[T:*]: {
      mut  .match[R:**](m: mut OptMatch[T, R]): R -> m.empty,
      read .match[R:**](m: mut OptMatch[read T, R]): R -> m.empty,
      imm  .match[R:**](m: mut OptMatch[imm T, R]): R -> m.empty,
      mut .map[R:*](f: mut OptMap[T, R]): mut Opt[R] -> this.match[mut Opt[R]](f),
      read .map[R:*](f: mut OptMap[read T, R]): mut Opt[R] -> this.match[mut Opt[R]](f),
      imm  .map[R:*](f: mut OptMap[imm T, R]): mut Opt[R] -> this.match[mut Opt[R]](f),
      }

    OptMatch[T:**,R:**]:{ mut .some(x: T): R, mut .empty: R }
    OptOrElse[R:*]:{ mut #: R }
    OptMap[T:*,R:*]:OptMatch[T, mut Opt[R]]{
      mut #(t: T): R,
      .some(x) -> Opts#(this#x),
      .empty -> {}
      }
    """);}
  @Test void shouldWorkWithBlock() {ok("""
    package test
    alias base.Block as Block,
    A:{mut .b: mut B} B:{}
    
    Foo: {#: mut B -> Block#
      .let a = {mut A{mut B}}
      .return {a.b}
      }
    """, """
    package base
    Void: {}
    ReturnStmt[R:**]: {mut #: R}
    Continuation[T:**,C:**,R:**]: {mut #(x: T, self: C): R}
    Block: {
      #[R:*]: mut Block[R] -> {},
      }
    Block[R:*]: {
      mut .done: Void -> {},
      mut .return(a: mut ReturnStmt[R]): R -> a#,
      mut .do(r: mut ReturnStmt[Void]): mut Block[R] -> this._do(r#),
        mut ._do(v: Void): mut Block[R] -> this,
      mut .let[X:**](x: mut ReturnStmt[X], cont: mut Continuation[X, mut Block[R], R]): R -> cont#(x#, this),
      }
    """);}
  @Test void blockWithLoop() {ok("""
    package base
    Void: {}
    ReturnStmt[R:*]: {mut #: R}
    Continuation[T:*,C:*,R:*]: {mut #(x: T, self: C): R}
    Block: {
      #[R:*]: mut Block[R] -> {},
      }
    Block[R:*]: {
      mut .loop(body: mut LoopBody[R]): mut Block[R] -> body#.match mut ControlFlowMatch[R, mut Block[R]]{
        .continue -> this.loop(body),
        .break -> this,
        .return(rv) -> _DecidedBlock#rv,
        },
      }
    _DecidedBlock: {#[R:*](res: R): mut Block[R] -> {}}
    ControlFlow: {
      .continue: mut ControlFlow[Void] -> mut ControlFlowContinue: ControlFlow[Void]{m -> m.continue},
      .break: mut ControlFlow[Void] -> mut ControlFlowBreak: ControlFlow[Void]{m -> m.break},
      .continueWith[T:*]: mut ControlFlow[T] ->  mut ControlFlowContinue[T:*]: ControlFlow[T]{m -> m.continue},
      .breakWith[T:*]: mut ControlFlow[T] -> mut ControlFlowBreak[T:*]: ControlFlow[T]{m -> m.break},
      .return[T:*](returnValue: T): mut ControlFlow[T] -> mut ControlFlowReturn[T:*]: ControlFlow[T]{
        .match(m) -> m.return(returnValue),
        mut .value: T -> returnValue,
        },
      }
    ControlFlow[T:*]: {
      mut .match[R:*](m: mut ControlFlowMatch[T,R]): R,
      }
    ControlFlowMatch[T:*,R:*]: {
      mut .continue: R,
      mut .break: R,
      mut .return(returnValue: T): R,
      }
    LoopBody[R:*]: ReturnStmt[mut ControlFlow[R]]{}
    """);}
}
