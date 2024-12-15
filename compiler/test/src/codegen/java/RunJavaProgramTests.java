package codegen.java;

import failure.CompileError;
import main.CompilerFrontEnd;
import main.InputOutput;
import main.Main;
import main.java.LogicMainJava;
import org.junit.jupiter.api.Assertions;
import program.TypeSystemFeatures;
import utils.Err;
import utils.IoErr;
import utils.ResolveResource;
import utils.RunOutput;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static utils.RunOutput.assertResMatch;

public class RunJavaProgramTests {
  private static final TypeSystemFeatures TSF = new TypeSystemFeatures();

  public static void ok(RunOutput.Res expected, String... content) {
    okWithArgs(expected, List.of(), content);
  }
  public static void ok(RunOutput.Res expected, Path... content) {
    var contentStr = Arrays.stream(content).map(ResolveResource::read).toArray(String[]::new);
    okWithArgs(expected, List.of(), contentStr);
  }
  public static void okWithArgs(RunOutput.Res expected, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    var logicMain = LogicMainJava.of(InputOutput.programmaticAuto(Arrays.asList(content), args), verbosity);
    assertResMatch(logicMain.run(), expected);
  }

  public static void fail(String expectedErr, String... content) {
    failWithArgs(expectedErr, List.of(), content);
  }
  public static void failWithArgs(String expectedErr, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    try {
      var logicMain = LogicMainJava.of(InputOutput.programmaticAuto(Arrays.asList(content), args), verbosity);
      IoErr.of(()->logicMain.run().inheritIO().start()).onExit().join();
      Assertions.fail("Did not fail");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
}
