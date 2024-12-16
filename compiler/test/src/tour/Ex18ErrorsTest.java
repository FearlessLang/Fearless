package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
import static codegen.java.RunJavaProgramTests.*;

public class Ex18ErrorsTest {
  @Disabled("loops on marco") @Test void uncaughtStackOverflow() { ok(new Res("", "Program crashed with: Stack overflowed[###]", 1), """
    package test
    Test:Main {sys -> Loop!}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtStackOverflow() { ok(new Res("", "Stack overflowed", 0), """
    package test
    Test:Main {sys -> UnrestrictedIO#sys.printlnErr(CapTries#sys#[Void]{Loop!}.info!.msg)}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtDivByZero() { ok(new Res("", "/ by zero", 0), """
    package test
    Test:Main {sys -> UnrestrictedIO#sys.printlnErr(Try#[Int]{+12 / +0}.info!.msg)}
    """, Base.mutBaseAliases); }
  @Test void caughtFearlessError() { ok(new Res("", "\"oh no\"", 0), """
    package test
    Test:Main {sys -> UnrestrictedIO#sys.printlnErr(Try#[Int]{Error.msg "oh no"}.info!.str)}
    """, Base.mutBaseAliases); }
  @Test void uncaughtFearlessError() { ok(new Res("", "Program crashed with: \"oh no\"[###]", 1), """
    package test
    Test:Main {sys -> Error.msg "oh no"}
    """, Base.mutBaseAliases); }
}
