package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput.Res;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex22DebuggingTest {
  @Test void debugPrintNumber() { ok(new Res("", "30", 0), """
    package test
    Test:Main {sys -> Block#(Debug#[Nat]30)}
    """, Base.mutBaseAliases);}
  @Test void debugPrintStringable() { ok(new Res("", "hi!", 0), """
    package test
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .str: Str -> "hi!"}
    """, Base.mutBaseAliases);}
  @Test void debugPrintNonStringable() { ok(new Res("", "test.Foo/0", 0), """
    package test
    Test:Main {sys -> Block#(Debug#(Foo))}
    Foo: {read .notStr: Str -> "hi!"}
    """, Base.mutBaseAliases);}
}
