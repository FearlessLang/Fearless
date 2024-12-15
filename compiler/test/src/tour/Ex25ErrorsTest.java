package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex25ErrorsTest {
  @Test void uncaughtStackOverflow() { ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test:Main {sys -> Loop!}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtStackOverflow() { ok(new RunOutput.Res("", "Stack overflowed", 0), """
    package test
    Test:Main {sys -> UnrestrictedIO#sys.printlnErr(CapTrys#sys#[Void]{Loop!}.info!.msg)}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtDivByZero() { ok(new RunOutput.Res("", "/ by zero", 0), """
    package test
    A: {#(sys: mut System): mut IO -> UnrestrictedIO#sys}
    Test:Main {sys -> A#sys.printlnErr(Try#[Int]({+12 / +0}).info!.msg)}
    """, Base.mutBaseAliases); }
  @Test void caughtFearlessError() { ok(new RunOutput.Res("", "\"oh no\"", 0), """
    package test
    Test:Main {sys -> UnrestrictedIO#sys.printlnErr(Try#[Int]{Error.msg "oh no"}.info!.str)}
    """, Base.mutBaseAliases); }
  @Test void uncaughtFearlessError() { ok(new RunOutput.Res("", "Program crashed with: \"oh no\"[###]", 1), """
    package test
    Test:Main {sys -> Error.msg "oh no"}
    """, Base.mutBaseAliases); }

  @Test void catchNothing() { ok(new RunOutput.Res("Happy", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{"Happy"}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }

  @Test void catchExplicitError() { ok(new RunOutput.Res("\"Sad\"", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error.msg "Sad"}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }
  @Test void catchExplicitErrorMsg() { ok(new RunOutput.Res("Sad", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error.msg "Sad"}.run{
        .ok(res) -> res,
        .info(err) -> err.msg,
        })
      }
    """, Base.mutBaseAliases); }
  @Test void catchExplicitErrorList() { ok(new RunOutput.Res("""
    ["big", ["oof"]]
    """, "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error!(Infos.list(List#(
        Infos.msg "big",
        Infos.list(List#(Infos.msg "oof"))
      )))}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }
  @Test void catchExplicitErrorMap() { ok(new RunOutput.Res("""
    {"a": "big", "b": ["oof"]}
    """, "", 0), """
    package test
    Test: Main{s -> Block#
      .let[LinkedHashMap[Str,Info]] map = {
        Maps.hashMap[Str,Info]({k1,k2 -> k1 == k2}, {k -> k})
        + ("a", Infos.msg "big")
        + ("b", Infos.list(List#(Infos.msg "oof")))
        }
      .do {s.io.println(Try#[Str]{Error!(Infos.map map)}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })}
      .return {{}}
      }
    """, Base.mutBaseAliases); }

  @Test void cannotCatchStackOverflow() { ok(new RunOutput.Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Loop!}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }

  @Test void capabilityCatchStackOverflow() { ok(new RunOutput.Res("\"Stack overflowed\"", "", 0), """
    package test
    Test:Main{s -> Block#
      .let io = {UnrestrictedIO#s}
      .let try = {CapTrys#s}
      .return {io.println(try#[Str]{Loop!}.run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })}
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }

  @Test void catchWithData() { ok(new RunOutput.Res("hiya\nrip", "", 0), """
    package test
    Test:Main{s -> Block#
      .let[mut IO] io = {UnrestrictedIO#s}
      .return {io.println(Try#(io.clone, {io' -> Block#(io'.println("hiya"), Error.msg[Str] "rip")}).run{
        .ok(res) -> res,
        .info(err) -> err.msg,
        })}
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }
  @Test void capabilityCatchWithData() { ok(new RunOutput.Res("hiya\n\"Stack overflowed\"", "", 0), """
    package test
    Test:Main{s -> Block#
      .let[mut IO] io = {UnrestrictedIO#s}
      .let[mut CapTry] try = {CapTrys#s}
      .return {io.println(try#(io.clone, {io' -> Block#(io'.println("hiya"), Loop![Str])}).run{
        .ok(res) -> res,
        .info(err) -> err.str,
        })}
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }
}
