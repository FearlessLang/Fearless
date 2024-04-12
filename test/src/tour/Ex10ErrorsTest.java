package tour;

import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
import static codegen.java.RunJavaProgramTests.*;

public class Ex10ErrorsTest {
  @Test void uncaughtStackOverflow() { ok(new Res("", "Program crashed with: Stack overflowed", 1), "test.Test", """
    package test
    Test:Main {sys -> Loop!}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtStackOverflow() { ok(new Res("", "Stack overflowed", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.printlnErr(Try#[Void]{Loop!}.err!.str)}
    Loop: { ![R]: R -> this! }
    """, Base.mutBaseAliases); }
  @Test void caughtDivByZero() { ok(new Res("", "/ by zero", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.printlnErr(Try#[Int]{12 / 0}.err!.str)}
    """, Base.mutBaseAliases); }
  @Test void caughtFearlessError() { ok(new Res("", "oh no", 0), "test.Test", """
    package test
    Test:Main {sys -> FIO#sys.printlnErr(Try#[Int]{Error.msg "oh no"}.err!.str)}
    """, Base.mutBaseAliases); }
  @Test void uncaughtFearlessError() { ok(new Res("", "Program crashed with: oh no", 1), "test.Test", """
    package test
    Test:Main {sys -> Error.msg "oh no"}
    """, Base.mutBaseAliases); }
}
