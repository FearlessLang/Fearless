package flows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class TestFlowSemantics {
  @Test void flowSum() {ok(new RunOutput.Res("60", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Nat]] x = {Flow#[Nat](1,2,3).map{x->x * 10}}
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  // TODO: currently leaving this broken because I want to change how this works
  @Disabled("redo flow re-use impl") @Test void flowReuse() {ok(new RunOutput.Res("60", "Program crashed with: \"This flow cannot be reused.\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let[mut Flow[Nat]] x = {Flow#[Nat](1,2,3).map{x->x * 10}}
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut Flow[Nat]] bigSum = {x.map{y->y * 10}}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .do {io.println(bigSum#(Flow.uSum).str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /*
   * Fearless errors in flow before a stop are propagated. Fearless errors after the "stop" are ignored.
   */
  @Test void throwInAFlowBeforeStopPar() {ok(new RunOutput.Res("", "Program crashed with: \"2\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopPar() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwMultiplePar() {ok(new RunOutput.Res("", "Program crashed with: \"2\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .if {x.nat == 3} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwMultipleActor() {ok(new RunOutput.Res("", "Program crashed with: \"2\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .actor[Void,Nat](iso Void,{next,_,x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .if {x.nat == 3} .do {Error.msg (x.str)}
          .do {next#(x.nat * 10)}
          .return {{}}
          })
        .fold[Nat](0, {a, x -> a + x})
        }
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(x.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwMultipleActorFromDP() {ok(new RunOutput.Res("", "Program crashed with: \"5\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let[List[Nat]] list = {List.withCapacity(10) + 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10}
      .let x = {list.flow
        .actor[Void,Nat](iso Void,{next,_,x->Block#
          .if {x.nat == 5} .do {Error.msg (x.str)}
          .if {x.nat == 7} .do {Error.msg (x.str)}
          .do {next#(x.nat * 10)}
          .return {{}}
          })
        .fold[Nat](0, {a, x -> a + x})
        }
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(x.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopSeq() {ok(new RunOutput.Res("", "Program crashed with: \"2\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopSeq() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopDP() {ok(new RunOutput.Res("", "Program crashed with: \"2\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow.range(+1, +50).map{n->n.nat}.list.flow
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopDP() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow.range(+1, +50).map{n->n.nat}.list.flow
        .map{x->Block#
          .if {x.nat == 2} .do {Error.msg (x.str)}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  /*
   * Non-deterministic errors bubble up, even if the flow isn't listening anymore because they're only catchable
   * in Fearless code with a capability anyway.
   */
  @Test void throwInAFlowBeforeStopDP_ND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow.range(+1, +50).map{n->n.nat}
        .map{x->Block#
          .if {x == 2} .do {StackOverflow#}
          .return {x * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowBeforeStopParND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopParND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}

  @Test void throwInAFlowBeforeStopSeqND() {ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}
  @Test void throwInAFlowAfterStopSeqND() {ok(new RunOutput.Res("10", "", 0), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .map{x->Block#
          .if {x.nat == 2} .do {StackOverflow#}
          .return {x.nat * 10}
          }
        .limit(1)
        }
      .let[Nat] sum = {x#(Flow.uSum)}
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(sum.str)}
      .return {{}}
      }
    StackOverflow: {#[R]: R -> this#}
    """, Base.mutBaseAliases);}

  @Test void mutableStrings() {ok(new RunOutput.Res("""
    abc
    yodabcyoeabcyofabc
    """, "", 0), """
    package test
    P: {#(a: Str, mutyA: Str): Str -> mut "yo" + a + mutyA}
    Test: Main{sys -> Block#
      .let[Str] mutyA = {"abc".flow.join ""}
      .let[Str] mutyB = {"def".flow.map{a->P#(a,mutyA)}.join ""}
      .do {sys.io.println(mutyA)}
      .do {sys.io.println(mutyB)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void pushErrorAndThrowSeq() {ok(new RunOutput.Res("", "Program crashed with: \"hello\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[mut Nat](mut 1, mut 2, mut 3)
        .actor[Void,Nat](iso Void,{next,_,x->Block#
          .if {x.nat == 2} .do {next.pushError(Infos.msg "hello")}
          .if {x.nat == 3} .do {Error.msg (x.str)}
          .do {next#(x.nat * 10)}
          .return {{}}
          })
        .fold[Nat](0, {a, x -> a + x})
        }
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(x.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}
  @Test void pushErrorAndThrow() {ok(new RunOutput.Res("", "Program crashed with: \"hello\"[###]", 1), """
    package test
    Test: Main{sys -> Block#
      .let x = {Flow#[Nat](1, 2, 3)
        .actor[Void,Nat](iso Void,{next,_,x->Block#
          .if {x.nat == 2} .do {next.pushError(Infos.msg "hello")}
          .if {x.nat == 3} .do {Error.msg (x.str)}
          .do {next#(x.nat * 10)}
          .return {{}}
          })
        .fold[Nat](0, {a, x -> a + x})
        }
      .let[mut IO] io = {UnrestrictedIO#sys}
      .do {io.println(x.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases);}

  @Test void dataParallelAllThrowsMustGetFirst() {ok(new RunOutput.Res("", "Program crashed with: \"0\"[###]", 1), """
    package test
    Test: Main{sys -> Block#(
      Flow.range(+0, +100_000)
        .map{i -> Error.msg[Int] (i.str)}
        .list
      )}
    """, Base.mutBaseAliases);}
}
