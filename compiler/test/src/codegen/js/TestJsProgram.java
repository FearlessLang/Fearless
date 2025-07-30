package codegen.js;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;
import utils.ResolveResource;

import java.util.List;

import static codegen.js.RunJsProgramTests.*;
import static utils.RunOutput.Res;

public class TestJsProgram {
  @Test void emptyProgram() { ok(new Res("", "", 0), """
    package test
    alias base.Main as Main,
    Test:Main{ _ -> "" }
    """);}

  @Test void intAddMulSub() {
    ok(new Res("", "", 0), """
      package test
      Test:Main{ _ -> Block#
        .do{ Assert!(1 + 2 * 3 == 7, "order of ops", {{}}) }
        .do{ Assert!(10 - 3 * 2 == 4, "subtraction", {{}}) }
        .do{ Assert!(8 / 2 + 1 == 5, "division and addition", {{}}) }
        .return{{}}
      }
    """, Base.mutBaseAliases);
  }
}
