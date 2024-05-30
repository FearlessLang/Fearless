package codegen.java;

import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.java.LogicMainJava;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.Err;

import java.util.Arrays;

public class TestJavaOptimisations {
  void ok(String expected, String fileName, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var vb = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var main = LogicMainJava.of(InputOutput.programmaticAuto(Arrays.asList(content)), vb);
    var fullProgram = main.parse();
    main.wellFormednessFull(fullProgram);
    var program = main.inference(fullProgram);
    main.wellFormednessCore(program);
    var resolvedCalls = main.typeSystem(program);
    var mir = main.lower(program,resolvedCalls);
    var code = main.codeGeneration(mir);
    var fileCode = code.files().stream()
      .filter(f->f.toUri().toString().endsWith(fileName))
      .map(JavaFile::code)
      .findFirst().orElseThrow();
    Err.strCmp(expected, fileCode);
  }

  @Test void blockVarDoRet() { ok("""
    package test;
    public interface Test_0 extends base.Main_0{
    Test_0 $self = new Test_0Impl();
    base.Void_0 $hash$imm(base.caps.System_0 fear[###]$_m$);
    static base.Void_0 $hash$imm$fun(base.caps.System_0 fear[###]$_m$, test.Test_0 $this) {
      var n_m$ = 5L;
    var doRes1 = test.ForceGen_0.$self.$hash$imm();
    return base.Void_0.$self;
    }
    }
    """, "/test/Test_0.java", """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test:base.Main {_ -> Block#
     .let[Int] n = {+5}
     .do {ForceGen#}
     .return {Void}
     }
    ForceGen: {#: Void -> {}}
    """);}

  @Test void blockRet() { ok("""
    package test;
    public interface Test_0 extends base.Main_0{
    Test_0 $self = new Test_0Impl();
    base.Void_0 $hash$imm(base.caps.System_0 fear[###]$_m$);
    static base.Void_0 $hash$imm$fun(base.caps.System_0 fear[###]$_m$, test.Test_0 $this) {
      return base.Void_0.$self;
    }
    }
    """, "test/Test_0.java", """
    package test
    alias base.Int as Int, alias base.Str as Str, alias base.Block as Block, alias base.Void as Void,
    Test: base.Main{_ -> Block#
     .return {Void}
     }
    """);}

  @Test void incrementLoop() { ok("""
    package test;
    public interface Test_0 extends base.Main_0{
    Test_0 $self = new Test_0Impl();
    base.Void_0 $hash$imm(base.caps.System_0 sys_m$);
    static base.Void_0 $hash$imm$fun(base.caps.System_0 sys_m$, test.Test_0 $this) {
      var n_m$ = base.Count_0.$self.int$imm(0L);
    while (true) {
      var res = new Fear[###]$_0Impl(n_m$).$hash$mut();
      if (res == base.ControlFlowContinue_0.$self || res == base.ControlFlowContinue_1.$self) { continue; }
        if (res == base.ControlFlowBreak_0.$self || res == base.ControlFlowBreak_1.$self) { break; }
        if (res instanceof base.ControlFlowReturn_1 rv) { return (base.Void_0) rv.value$mut(); }
      }
    return base.Void_0.$self;
    }
    }
    """, "test/Test_0.java", """
    package test
    Test:Main {sys -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.break}
        .do {Block#(n++)}
        .return {ControlFlow.continue}
        }
//      .assert {n.get == +10}
      .return {Void}
      }
    """, Base.mutBaseAliases);}

  @Test void earlyReturnLoop() { ok("""
    package test;
    public interface Foo_0{
    Foo_0 $self = new Foo_0Impl();
    rt.Str $hash$imm();
    static rt.Str $hash$imm$fun(test.Foo_0 $this) {
      var n_m$ = base.Count_0.$self.int$imm(0L);
    while (true) {
      var res = new Fear[###]$_0Impl(n_m$).$hash$mut();
      if (res == base.ControlFlowContinue_0.$self || res == base.ControlFlowContinue_1.$self) { continue; }
        if (res == base.ControlFlowBreak_0.$self || res == base.ControlFlowBreak_1.$self) { break; }
        if (res instanceof base.ControlFlowReturn_1 rv) { return (rt.Str) rv.value$mut(); }
      }
    return str$1004811944375518034$str$.$self;
    }
    }
    """, "test/Foo_0.java", """
    package test
    Test:Main {sys -> (FIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.continueWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases);}

  @Test void earlyReturnLoopEarlyExit() { ok("""
    package test;
    public interface Foo_0{
    Foo_0 $self = new Foo_0Impl();
    rt.Str $hash$imm();
    static rt.Str $hash$imm$fun(test.Foo_0 $this) {
      var n_m$ = base.Count_0.$self.int$imm(0L);
    while (true) {
      var res = new Fear[###]$_0Impl(n_m$).$hash$mut();
      if (res == base.ControlFlowContinue_0.$self || res == base.ControlFlowContinue_1.$self) { continue; }
        if (res == base.ControlFlowBreak_0.$self || res == base.ControlFlowBreak_1.$self) { break; }
        if (res instanceof base.ControlFlowReturn_1 rv) { return (rt.Str) rv.value$mut(); }
      }
    return str$1004811944375518034$str$.$self;
    }
    }
    """, "test/Foo_0.java", """
    package test
    Test:Main{sys -> (FIO#sys).println(Foo#)}
    Foo: {#: Str -> Block#
      .let n = {Count.int(+0)}
      .loop {Block#
        .if {n.get == +10} .return {ControlFlow.return[Str](n.get.str)}
        .do {Block#(n++)}
        .return {ControlFlow.breakWith[Str]}
        }
      .return {"Boo :("}
      }
    """, Base.mutBaseAliases);}

  @Disabled
  @Test void methodChainDevirtualisation() { ok("""
    """, "", """
    package test
    A: {.m1(a: A): A}
    B: {.m1: A -> A{a0 -> a0}.m1(A{.m1(a1) -> A{.m1(a2) -> a1}})}
    Test: Main{sys -> Void}
    """, Base.mutBaseAliases); }

  @Test void dataParallelFlow() { ok("""
    package test;
    public interface Test_0 extends base.Main_0{
    Test_0 $self = new Test_0Impl();
    base.Void_0 $hash$imm(base.caps.System_0 sys_m$);
    static base.Void_0 $hash$imm$fun(base.caps.System_0 sys_m$, test.Test_0 $this) {
      return rt.IO.$self.println$mut(((rt.Str)rt.dataParallel.DataParallelFlowK.$self.fromOp$imm(str$3297469917561599766$str$.$self._flow$imm(), base.Opt_1.$self).map$mut(test.Fear[###]_0.$self).$hash$mut(base.flows.Flow_0.$self.str$imm(str$14492805990617963705$str$.$self))));
    }
    }
    """, "/test/Test_0.java", """
    package test
    Test: Main{sys -> FIO#sys.println("Hello".flow
      .map{ch -> ch == "H" ? {.then -> "J", .else -> ch}}
      .join ""
      )}
    """, Base.mutBaseAliases);}
  @Test void dataParallelFlowInvalidation() { ok("""
    package test;
    public interface Test_0 extends base.Main_0{
    Test_0 $self = new Test_0Impl();
    base.Void_0 $hash$imm(base.caps.System_0 sys_m$);
    static base.Void_0 $hash$imm$fun(base.caps.System_0 sys_m$, test.Test_0 $this) {
      return rt.IO.$self.println$mut(((rt.Str)str$3297469917561599766$str$.$self.flow$imm().map$mut(test.Fear[###]_0.$self).scan$mut(test.Fear[###]_0.$self,test.Fear[###]_0.$self).map$mut(test.Fear[###]_0.$self).$hash$mut(base.flows.Flow_0.$self.str$imm(str$6448469143720294743$str$.$self))));
    }
    }
    """, "/test/Test_0.java", """
    package test
    StrInfo: Stringable{
      .size: Nat,
      .facts: Str -> this.size.str+" "+this.str,
      }
    Test: Main{sys -> FIO#sys.println("Hello".flow
      .map{ch -> ch == "H" ? {.then -> "J", .else -> ch}}
      .scan[StrInfo](
        {.size -> 0, .str -> ""},
        {acc, ch -> {.size -> acc.size + 1, .str -> acc.str + ch}}
        )
      .map{i -> i.facts}
      .join "\\n"
      )}
    """, Base.mutBaseAliases);}
}
