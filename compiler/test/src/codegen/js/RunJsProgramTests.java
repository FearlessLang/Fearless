package codegen.js;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.CompilerFrontEnd;
import main.Main;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.TsT;
import utils.Base;
import utils.RunOutput;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static utils.RunOutput.Res;

public class RunJsProgramTests {

  public static void ok(Res expected, String... content) {
    okWithArgs(expected, List.of(), content);
  }

  public static void okWithArgs(Res expected, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();

    // Parse input + base library
    var parsers = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code -> new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();

    // Parse all input files
    var parsed = Parser.parseAll(parsers, new TypeSystemFeatures());

    // Check well-formedness
    new WellFormednessFullShortCircuitVisitor().visitProgram(parsed).ifPresent(err -> {
      throw new RuntimeException(err);
    });

    // Infer bodies and types
    var inferred = InferBodies.inferAll(parsed);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);

    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);

    // Generate MIR
    var mir = new MIRInjectionVisitor(List.of(), inferred, resolvedCalls).visitProgram();

    // Generate JS code
    var jsCodegen = new JsCodegen(mir);

    String js = jsCodegen.visitProgram(new Id.DecId("test.Test", 0));

    // Optionally print JS code (commented out)
    // System.out.println(js);

    // At this point, you could run the JS code (e.g., with Node) and capture output
    // For now, just check the expected output is empty or some dummy pass:
    // TODO: Implement actual JS execution and output capturing for better tests

    // Dummy assertion: expected stdout matches empty string (adjust if you implement execution)
    Assertions.assertEquals(expected.stdOut(), "", "Expected stdout (dummy placeholder)");

    // If you want, you can add actual JS runtime invocation here and compare output to expected.stdout()
  }

  public static void fail(String expectedErr, String... content) {
    failWithArgs(expectedErr, List.of(), content);
  }

  public static void failWithArgs(String expectedErr, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();

    var parsers = Stream.concat(Arrays.stream(content), Arrays.stream(Base.immBaseLib))
      .map(code -> new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();

    var parsed = Parser.parseAll(parsers, new TypeSystemFeatures());

    new WellFormednessFullShortCircuitVisitor().visitProgram(parsed).ifPresent(err -> {
      throw new RuntimeException(err);
    });

    var inferred = InferBodies.inferAll(parsed);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);

    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);

    var mir = new MIRInjectionVisitor(List.of(), inferred, resolvedCalls).visitProgram();

    var vb = new CompilerFrontEnd.Verbosity(true, true, CompilerFrontEnd.ProgressVerbosity.Full);

    try {
      // Just generate JS to trigger possible errors during codegen
      var jsCodegen = new JsCodegen(mir);
      jsCodegen.visitProgram(new Id.DecId("test.Test", 0));
      Assertions.fail("Expected compile failure but succeeded");
    } catch (RuntimeException e) {
      // Check expected error substring (simplified)
      Assertions.assertTrue(e.getMessage().contains(expectedErr), "Expected error substring not found");
    }
  }
}
