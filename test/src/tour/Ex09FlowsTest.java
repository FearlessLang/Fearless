package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
import static codegen.java.RunJavaProgramTests.*;

/*
 * 1. Public contract for Flows
 *
 * 2. Internal deterministic specification for Flows
 *
 *
 * (this is important for early exits like .limit or .find)
 */

public class Ex09FlowsTest {
  @Test void flowSumStr() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void immFlowSumStr() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      As[imm List[Int]]#(List#[Int](5, 10, 15)).flow
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowFilterSumStr() { ok(new Res("25", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Disabled
  @Test void flowSumAssert() { ok(new Res(), "test.Test", """
    package test
    // We cannot have Assert.eq without a magic equality (HasEq)
    // and magic toString (which would help us provide a better error message)
    Test:Main {sys -> Assert.eq({n1, n2 -> n1 == n2},
      Flow#[Int](5, 10, 15)#(Flow.sum),
      30
      )}
    """, Base.mutBaseAliases);}
  @Test void flowSumAssertNoEq() { ok(new Res(), "test.Test", """
    package test
    // We cannot have Assert.eq without a magic equality and magic toString (which would help us provide a better
    // error message)
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15)#(Flow.sum)
      == 30
      )}
    """, Base.mutBaseAliases);}

  @Test void flowMap() { ok(new Res("300", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowMapWithListConstructor() { ok(new Res("300", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](5, 10, 15).flow
        .map{n -> n * 10}
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  @Test void flowFlatMap() { ok(new Res("50, 50, 100, 100, 150", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .flatMap{n -> Flow#[Int](n, n, n).limit(2u).map{n' -> n' * 10}}
        .limit(5u)
        .map{n -> n.str}
        #(Flow.str ", ")
      )}
    """, Base.mutBaseAliases); }

  @Test void flowGetFirst() { ok(new Res("100", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .find{n -> True}!
        .str
      )}
    """, Base.mutBaseAliases); }

  @Test void flowGetFirstDifferentApproaches() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .assert({Trick.find == (Trick.findAndLimit)}, "find == findAndLimit ("+(Trick.find.str)+" == "+(Trick.findAndLimit.str)+")")
      .assert({Trick.first == (Trick.firstAndLimit)}, "first == firstAndLimit ("+(Trick.first.str)+" == "+(Trick.firstAndLimit.str)+")")
      .assert({Trick.first == (Trick.find)}, "first == find ("+(Trick.first.str)+" == "+(Trick.find.str)+")")
      .return{Void}
      }
    Trick: {
      .find: Int -> Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".find was empty"},
      .findAndLimit: Int -> Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .limit(1u)
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".findAndLimit was empty"},
      .first: Int -> Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".first was empty"},
      .firstAndLimit: Int -> Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .filter{n -> n > 50}
        .limit(1u)
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".firstAndLimit was empty"},
      }
    """, Base.mutBaseAliases); }
  @Test void flowGetFirstDifferentApproachesSeq() { ok(new Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .assert({Trick.find == (Trick.findAndLimit)}, "find == findAndLimit ("+(Trick.find.str)+" == "+(Trick.findAndLimit.str)+")")
      .assert({Trick.first == (Trick.firstAndLimit)}, "first == firstAndLimit ("+(Trick.first.str)+" == "+(Trick.firstAndLimit.str)+")")
      .assert({Trick.first == (Trick.find)}, "first == find ("+(Trick.first.str)+" == "+(Trick.find.str)+")")
      .return{Void}
      }
    Trick: {
      .find: mut Person -> Flow#[mut Person](FPerson#24u, FPerson#60u, FPerson#75u)
        .map{p -> FPerson#(p.age * 10u)}
        .filter{p -> p.age > 50u}
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".find was empty"},
      .findAndLimit: mut Person -> Flow#[mut Person](FPerson#24u, FPerson#60u, FPerson#75u)
        .map{p -> FPerson#(p.age * 10u)}
        .filter{p -> p.age > 50u}
        .limit(1u)
        .find{n -> True}
        .match{.some(n) -> n, .empty -> Error.msg ".findAndLimit was empty"},
      .first: mut Person -> Flow#[mut Person](FPerson#24u, FPerson#60u, FPerson#75u)
        .map{p -> FPerson#(p.age * 10u)}
        .filter{p -> p.age > 50u}
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".first was empty"},
      .firstAndLimit: mut Person -> Flow#[mut Person](FPerson#24u, FPerson#60u, FPerson#75u)
        .map{p -> FPerson#(p.age * 10u)}
        .filter{p -> p.age > 50u}
        .limit(1u)
        .first
        .match{.some(n) -> n, .empty -> Error.msg ".firstAndLimit was empty"},
      }
    """, """
    package test
    FPerson: { #(age: UInt): mut Person -> mut Person: {'self
      read .age: UInt -> age,
      read .str: Str -> "Person that is "+(self.age.str)+" years old",
      read ==(other: read Person): Bool -> self.age == (other.age),
      }}
    """, Base.mutBaseAliases); }

  @Test void optFlow() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let f1 = {(Opt#[Int]5).flow
        .map{n -> n * 10}
        .list
        }
      .let f2 = {mut Opt[Int].flow
        .map{n -> n * 10}
        .list
        }
      .assert{f1.get(0u)! == 50}
      .assert{f2.size == 0u}
      .return {{}}
      }
    """, Base.mutBaseAliases); }

  @Test void flowLimit0() { ok(new Res("0", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(0u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit1() { ok(new Res("5", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(1u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit2() { ok(new Res("15", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(2u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit2List() { ok(new Res("15", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](5, 10, 15).flow
        .limit(2u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit3() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(3u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit3List() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      List#[Int](5, 10, 15).flow
        .limit(3u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }
  @Test void flowLimit4() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(
      Flow#[Int](5, 10, 15)
        .limit(4u)
        #(Flow.sum)
        .str
      )}
    """, Base.mutBaseAliases); }

  // Attempting to do a terminal operation on an infinite flow is always an exception.
  // The flow must be bounded by an intermediate operation before a terminal operation can be performed.
  @Test void flowFilter() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15).filter{n -> n > 5}.size
      == 2u
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterPrintSize() { ok(new Res("2", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let size = {Flow#[Int](5, 10, 15).filter{n -> n > 5}.size}
      .return {FIO#sys.println(size.str)}
      }
    """, Base.mutBaseAliases);}
  @Test void flowFilterMap() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Assert!(
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        .map{n -> n * 10}
        .max(base.CompareInts)!
      == 150
      )}
    """, Base.mutBaseAliases);}
  @Test void flowFilterMapIntEq1() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> 150.assertEq("max assert failed",
      Flow#[Int](5, 10, 15)
        .filter{n -> n > 5}
        .map{n -> n * 10}
        .max(base.CompareInts)!
      )}
    """, Base.mutBaseAliases);}
  // We prefer flowFilterMapIntEq1 because it is more clear that this test is of an assertion rather than of a flow.
  @Test void flowFilterMapIntEq2() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Flow#[Int](5, 10, 15)
      .filter{n -> n > 5}
      .map{n -> n * 10}
      .max(base.CompareInts)!
      .assertEq(150)
      }
    """, Base.mutBaseAliases);}

  @Test void flowDuplicate() { ok(new Res("35 40 45", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Flow#[Int](5, 10, 15)
      .duplicate f2 = {f1 -> f1#(Flow.sum)}
      .map{n -> n + f2}
      .map{n -> n.str}
      #(Flow.str " ")
      }
    """, Base.mutBaseAliases);}

  @Test void mutExtensionMethod() { ok(new Res("20 30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let[List[Int]] list = {List#[Int](1, 2, 3)}
      .let[Str] myFlow = {list.flow
        .filter{n -> n > 1}
        #{f -> f.map{n -> n*10}}
        .map{n -> n.str}
        #(Flow.str " ")
        }
      .return {FIO#sys.println(myFlow)}
      }
    """, Base.mutBaseAliases);}

  @Test void flowActor() { ok(new Res("31", "", 0), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}}, mut Consumer[mut Ref[Int]]{state -> FIO#sys.println(state.get.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Disabled @Test void flowActorMutRet() { ok(new Res("31", "", 0), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        // .actor requires an iso S for its initial value
        // The 3rd argument is optional
        .actorMut[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}}, mut Consumer[mut Ref[Int]]{state -> FIO#sys.println(state.get.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // TODO: right now it is possible to observe pipeline parallelism with the consumer and a limit + actor
  // This is because the limit runs in parallel with the actor, the actor won't submit any messages incorrectly
  // but it may run needlessly :(
  // This is because the STOP message is queued behind any prior messages.
  @Test void limitedFlowActorAfter() { ok(new Res("6", "", 0), "test.Test", """
    package test
    Test:Main {sys -> "42 5".assertEq(
      Flow#[Int](5, 10, 15)
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}}, {state -> FIO#sys.println(state.get.str)})
        .limit(2u)
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void limitedFlowActorBefore() { ok(new Res("16", "", 0), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10".assertEq(
      Flow#[Int](5, 10, 15)
        .limit(2u)
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}}, {state -> FIO#sys.println(state.get.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void flowActorNoConsumer() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "42 5 42 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        .actor[mut Ref[Int], Int](Ref#[Int]1, {downstream, state, n -> Block#
          .do {state := (state* + n)}
          .if {state.get > 16} .return{Block#(downstream#500, {})}
          .do {downstream#42}
          .do {downstream#n}
          .return {{}}})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // We have this as a specialisation of .actor
  @Test void flowScan() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Int](5, 10, 15)
        .scan[Str]("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}
  @Test void flowScan2() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 20 50".assertEq(
      Flow#[Int](5, 10, 15)
        .scan[Int](0, {acc, n -> acc + n})
        .scan[Int](0, {acc, n -> acc + n})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // TODO: fix top level dec issue when wanting a mut instance of a top level lambda
  @Test void flowSimpleActorMutRet() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "!5 !510 !51015".assertEq(
      Flow#[Int](5, 10, 15)
        .scan[Str]("!", {acc, n -> acc + (n.str)})
        .map{n -> n.str}
        #(Flow.str " ")
      )}
    """, """
    package test
    FPerson:{
      #(age: UInt): mut Person -> Block#
        .let[mut Ref[UInt]] age' = {Ref.ofImm(age)}
        .return mut base.ReturnStmt[mut Person]{mut Person: Person{
          read .age: UInt -> age'.getImm!,
          mut .age(n: UInt): Void -> age' := n,
          }}
      }
    """, Base.mutBaseAliases);}

//  @Test void flowActor() { ok(new Res(), "test.Test", """
//    package test
//    Test:Main {sys -> "5 10 500".assertEq(
//      Flow#[Int](5, 10, 15)
//        // .actor requires an iso S for its initial value
//        // This lambda has the type read ActorImpl[mut IsoPod[S], ... E, R]
//        .actor(Ref#1, mut Consume[mut Ref[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0u)!)}
//          .if {state.get > 10} .return {500}
//          .return {n})
//        .actor(Ref#1, mut Consume[mut Ref[Int]]{state->someRandom.set(state.get)}, {state, n -> Block#
//          .do {state.set(someMutList.get(0u)!)}
//          .if {state.get > 10} .return {500}
//          .return {n})
//        // Actors on:
//        // - mut flow of imm values with an imm lambda
//        //    + the lambda can take mut state
//              - the lambda can only capture imm
//        // - mut flow of mut values with an imm lambda
//        //    - we cannot take mut state
//        //    - the lambda can only capture imm
//        // - mut flow of imm values with an read lambda
//        //    - the lambda cannot take mut state
//        //    + the lambda can capture mut state as read
//        // - mut flow of mut values with an read lambda
//        //    - the lambda cannot take mut state
//        //    - unsound to parallelise
//        // - mut flow of imm values with an readH lambda
//        // - mut flow of mut values with an readH lambda
//
//        .actor(Ref#1, {state, n -> Block#
//          .if {state.get > 10} .return {500}
//          .do {state.put(n + state.get)}
//          .return {n})
//        .map{n -> n.str}
//        #(Flow.str " ")
//      )}
//    """, Base.mutBaseAliases);}

  // TODO: The error that this generates without the .toImm on the list item is _terrible_ (and takes like 1 and a half minutes)
  @Test void flowActorMultiParallel() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let[mut List[Int]] someMutList = {List#[Int](30)}
      .return {"500 5 500 10".assertEq(
        Flow#[Int](5, 10)
          .actor[mut Ref[Int],Int](Ref#[Int]1, {next, state, n -> Block#
            .do {state.set(someMutList.get(0u)!.toImm)}
            .if {state.get > 10} .do {next#500}
            .do {next#n}
            .return {{}}
            })
          .map{n -> n.str}
          #(Flow.str " ")
        )}
      }
    """, Base.mutBaseAliases); }

  // If we do not offer any mapMut/mut lambdas, we can have parallelised read lambdas
  @Disabled @Test void mapAndMapMut() { ok(new Res(), "test.Test", """
    package test
    Test:Main {sys -> "5 10 500".assertEq(
      Flow#[Int](5, 10, 15)
        .map{n -> n * 10}
        .mapMut{n -> Block#(counter++, n)}
        .mapMut{n -> Block#(counter++, n)}
        .map{n -> n + 10}
        #(Flow.str " ")
      )}
    """, Base.mutBaseAliases);}

  // TODO: Do we want .split?
  @Disabled @Test void flowSplit() { ok(new Res(), "test.Test", """
    package test
    Test: Main{sys -> "5 10 15".assertEq(
      Flow#[Int](5, 10, 15)
        .split(
          {f -> f.map{n -> n * 5}.filter{n -> n > 10}},
          {f -> f.map{n -> n * 10}},
          {f -> f.map{n -> n * 15}},
          {f1,f2,f3 -> f1#(Flow.str " ")+" "+f2#(Flow.str " ")+" "+f3#(Flow.str " ")},
        )
        //vs
        .split3{
          .f1(f) -> f.map{n -> n * 5},
          .f2(f) -> f.map{n -> n * 10},
          .f3(f) -> f.map{n -> n * 15},
          .merge(f1,f2,f3) -> f1#(Flow.str " ")+" "+f2#(Flow.str " ")+" "+f3#(Flow.str " "),
        }
        // split actors
        // with a capability, this is fine with imm messages. Without a capability we can call actor2 and actor3 from
        // recvActor but actor2 and actor3 cannot call any other actor. Accessing the actor graph needs a capability.
        .spawnSystemOf3(...., {
          .recvActor(state, msg) -> ...,
          .actor2(state, msg, actorRef3) -> ...,
          .actor3(state, msg, actorRef2) -> ...,
          .consume(state1, state2, state3) -> ...,
          }
          // or
        .spawnSystemOf3(...., {
          .recvActor(state, msg, downstream) -> ...,
          .actor2(state, msg, actorRef3, downstream) -> ...,
          .actor3(state, msg, actorRef2, downstream) -> ...,
          } // has to internally represent the result as three downstream lists to have a deterministic ordering
        .map{n -> n + 5}
        .to list..
      )}
    """, Base.mutBaseAliases);}
}
