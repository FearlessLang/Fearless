package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex14RandomTest {
  static final String RNG_ALIASES = """
    package test
    alias base.rng.FRandom as FRandom,
    alias base.rng.Random as Random,
    """;
  @Test void generatesNumber() { ok(new RunOutput.Res("570564682", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println((FRandom#1337u).uint.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesFloat() { ok(new RunOutput.Res("0.26568988443617236", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println((FRandom#1337u).float.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesNumberWithRandomSeed() { ok(new RunOutput.Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let[mut RandomSeed] seeder = {FRandomSeed#sys}
      .let rng1 = {FRandom#(seeder#)}
      .let rng2 = {FRandom#(seeder#)}
      // I mean, this technically has a _tiny_ chance of failing but it's so small that
      // a bug in the random number generator is more likely than this test failing due to a genuine collision
      .assert {(rng1.uint == (rng2.uint)).not}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesMultipleNumbers() { ok(new RunOutput.Res("570564682\n1499355484\n1372376065\n1209872585\n241596240", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Rng#(FRandom#1337u, FIO#sys, Count.uint(5u))}
    Rng: {#(rng: mut Random, io: mut IO, n: mut Count[UInt]): Void -> Block#
      .loop {n.get == 0u ? {.then -> ControlFlow.break, .else -> Block#(io.println(rng.uint.str), n--, ControlFlow.continue)}}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesMultipleNumbersInRange() { ok(new RunOutput.Res("10 18 17 16 7 14 15 18 17 6", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Rng#(FRandom#1337u, FIO#sys, Count.uint(10u))}
    Rng: {#(rng: mut Random, io: mut IO, n: mut Count[UInt]): Void -> Block#
      .loop {n.get == 0u ? {.then -> ControlFlow.break, .else -> Block#(io.print(rng.uint(5u, 25u).str+" "), n--, ControlFlow.continue)}}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generatesNumberDifferentSeed() { ok(new RunOutput.Res("570564682\n1717387703", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .do {FIO#sys.println((FRandom#1337u).uint.str)}
      .do {FIO#sys.println((FRandom#50000000u).uint.str)}
      .return {{}}
      }
    """, Base.mutBaseAliases, RNG_ALIASES); }

  @Test void generateRandomSeed() { ok(new RunOutput.Res("[###]", "", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.println(FRandomSeed#sys#.str)}
    """, Base.mutBaseAliases, RNG_ALIASES); }
}
