package codegen.java;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Disabled
public class TestJavaOptimisations {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(inferred, resolvedCalls).visitProgram();
    var java = new JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    Err.strCmpFormat(expected, java);
  }

  @Test void blockVarDoRet() { ok("""
    [###]static base.Void_0 test$Test_0$$35$imm$$noSelfCap(base$46caps.System_0 fear5$$) {
      var n$ = 5L;
    var doRes1 = test.ForceGen_0._$self.$35$imm$();
    return base.Void_0._$self;
    }
    [###]
    """, "test.Test", true, """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main {_ -> Block#
     .let[Int] n = {5}
     .do {ForceGen#}
     .return {Void}
     }
    ForceGen: {#: Void -> {}}
    """);}

  @Test void blockRet() { ok("""
    [###]static base.Void_0 test$Test_0$$35$imm$$noSelfCap(base$46caps.System_0 fear5$$) {
      return base.Void_0._$self;
    }
    [###]
    """, "test.Test", true, """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main{ _ -> Block#
     .return {Void}
     }
    """);}

  @Test void incrementLoop() { ok("""
    [###]static base.Void_0 test$Test_0$$35$imm$$noSelfCap(base$46caps.System_0 sys$) {
      var n$ = base.Count_0._$self.int$imm$(0L);
    while (true) {
      var res = new test$Fear61$36_0Impl(n$).$35$mut$();
      if (res == base.ControlFlowContinue_0._$self || res == base.ControlFlowContinue_1._$self) { continue; }
      if (res == base.ControlFlowBreak_0._$self || res == base.ControlFlowBreak_1._$self) { break; }
      if (res instanceof base.ControlFlowReturn_1 rv) { return (base.Void_0) rv.value$mut$(); }
    }
    return base.Void_0._$self;
    }
    [###]
    """, "test.Test", true, """
    package test
    Test:Main {sys -> Block#
      .let n = {Count.int(0)}
      .loop {Block#
        .if {n.get == 10} .return {ControlFlow.break}
        .do {Block#(n++)}
        .return {ControlFlow.continue}
        }
//      .assert {n.get == 10}
      .return {Void}
      }
    """, Base.mutBaseAliases);}

  @Test void earlyReturnLoop() { ok("""
    [###]static String test$Foo_0$$35$imm$$noSelfCap() {
      var n$ = base.Count_0._$self.int$imm$(0L);
    while (true) {
     var res = new test$Fear63$36_0Impl(n$).$35$mut$();
     if (res == base.ControlFlowContinue_0._$self || res == base.ControlFlowContinue_1._$self) { continue; }
     if (res == base.ControlFlowBreak_0._$self || res == base.ControlFlowBreak_1._$self) { break; }
     if (res instanceof base.ControlFlowReturn_1 rv) { return (String) rv.value$mut$(); }
   }
   return "Boo :(";
   }
    [###]
    """, "test.Test", true, """
    package test
    Test:Main {sys -> (FIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(0)}
      .loop {Block#
        .if {n.get == 10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.continueWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases);}

  @Test void earlyReturnLoopEarlyExit() { ok("""
    [###]
    static String test$Foo_0$$35$imm$$noSelfCap() {
      var n$ = base.Count_0._$self.int$imm$(0L);
      while (true) {
        var res = new test$Fear63$36_0Impl(n$).$35$mut$();
        if (res == base.ControlFlowContinue_0._$self || res == base.ControlFlowContinue_1._$self) { continue; }
        if (res == base.ControlFlowBreak_0._$self || res == base.ControlFlowBreak_1._$self) { break; }
        if (res instanceof base.ControlFlowReturn_1 rv) { return (String) rv.value$mut$(); }
      }
      return "Boo :(";
    }
    [###]
    """, "test.Test", true, """
    package test
    Test:Main{sys -> (FIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(0)}
      .loop {Block#
        .if {n.get == 10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.breakWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases);}

  @Disabled @Test void methodChainDevirtualisation() { ok("""
    """, "test.Test", true, """
    package test
    A: {.m1(a: A): A}
    B: {.m1: A -> A{a0 -> a0}.m1(A{.m1(a1) -> A{.m1(a2) -> a1}})}
    Test: Main{sys -> Void}
    """, Base.mutBaseAliases); }
}
