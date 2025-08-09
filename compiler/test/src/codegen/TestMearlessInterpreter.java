package codegen;

import codegen.optimisations.interpreter.MearlessInterpreter;
import id.Id;
import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.java.LogicMainJava;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;

import java.util.Arrays;

public class TestMearlessInterpreter {
  void ok(String expected, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var vb = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var main = LogicMainJava.of(InputOutput.programmaticAutoNoCache(Arrays.asList(content)), vb);
    var fullProgram = main.parse();
    main.wellFormednessFull(fullProgram);
    var program = main.inference(fullProgram);
    main.wellFormednessCore(program);
    var resolvedCalls = main.typeSystem(program);
    var mir = main.lower(program,resolvedCalls);
    var interpreter = new MearlessInterpreter(mir);
    var res = interpreter.safely(i->i.run(new Id.DecId("test.Test", 0)));
    Err.strCmp(expected, res.get().toString());
  }

  @Test void empty() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> Void}
    """, Base.mutBaseAliases);}

  @Test void call1() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> Foo#}
    Foo: {#: Void -> {}}
    """, Base.mutBaseAliases);}

  @Test void callInline() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> {.bloop: Void -> {}}.bloop}
    """, Base.mutBaseAliases);}

  @Test void withArg() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> F[Void,Void]{::}#{}}
    """, Base.mutBaseAliases);}

  @Test void withCapture() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> (B#{})#}
    B: {#[T](x: T): B[T] -> {x}}
    B[T]: {#: T}
    """, Base.mutBaseAliases);}

  @Test void io() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> s.io.println "Hello, World!"}
    """, Base.mutBaseAliases);}

  @Test void conditionals() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> True & False ? {.then -> Abort!, .else -> Void}}
    """, Base.mutBaseAliases);}

  @Test void fib() { ok("""
    CreateObj[t=Usual[t=imm base.Void[]], selfName=this, meths=[], unreachableMs=[], captures=[]]
    """, """
    package test
    Test: Main{s -> (Fib.seq 20 == 6765) ? {.then -> Void, .else -> Abort!}}
    Fib: {
      .seq(n: Nat): Nat -> Block#
        .if {n <= 1} .return {n}
        .return {Fib.seq(n - 1) + (Fib.seq(n - 2))}
      }
    """, Base.mutBaseAliases);}
}
