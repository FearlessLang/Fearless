package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex25ErrorsTest {
  @Test void catchNothing() { ok(new RunOutput.Res("Happy", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{"Happy"}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }

  @Test void catchExplicitError() { ok(new RunOutput.Res("\"Sad\"", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error.msg "Sad"}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }
  @Test void catchExplicitErrorMsg() { ok(new RunOutput.Res("Sad", "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error.msg "Sad"}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.msg,
        })
      }
    """, Base.mutBaseAliases); }
  @Test void catchExplicitErrorList() { ok(new RunOutput.Res("""
    ["big",["oof"]]
    """, "", 0), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Error!(Infos.list(List#(
        Infos.msg "big",
        Infos.list(List#(Infos.msg "oof"))
      )))}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })
      }
    """, Base.mutBaseAliases); }

  @Test void cannotCatchStackOverflow() { ok(new RunOutput.Res("", "Program crashed with: Stack overflowed", 1), """
    package test
    Test:Main{s ->
      UnrestrictedIO#s.println(Try#[Str]{Loop!}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }

  @Test void capabilityCatchStackOverflow() { ok(new RunOutput.Res("\"Stack overflowed\"", "", 0), """
    package test
    Test:Main{s -> Block#
      .let io = {UnrestrictedIO#s}
      .let try = {CapTries#s}
      .return {io.println(try#[Str]{Loop!}.resMatch{
        .ok(res) -> res,
        .err(err) -> err.str,
        })}
      }
    Loop: {![R]: R -> this!}
    """, Base.mutBaseAliases); }
}
