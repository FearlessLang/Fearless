package codegen.java;

import codegen.MIRInjectionVisitor;
import failure.CompileError;
import id.Id;
import main.CompilerFrontEnd;
import main.Main;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import utils.Base;
import utils.Err;
import utils.RunOutput;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class RunJavaProgramTests {
  private static final TypeSystemFeatures TSF = new TypeSystemFeatures.TypeSystemFeaturesBuilder()
    .build();

  public static void ok(RunOutput.Res expected, String entry, String... content) {
    okWithArgs(expected, entry, List.of(), content);
  }
  public static void okWithArgs(RunOutput.Res expected, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TSF);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{
      throw err;
    });
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    var java = new codegen.java.JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    System.out.println("Running...");
    var res = RunOutput.java(new JavaCompiler().compile(verbosity, List.of(new JavaFile(JavaCompiler.MAIN_CLASS_NAME,java))), args).join();
    Assertions.assertEquals(expected, res);
  }

  public static void fail(String expectedErr, String entry, String... content) {
    failWithArgs(expectedErr, entry, List.of(), content);
  }
  public static void failWithArgs(String expectedErr, String entry, List<String> args, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(Base.baseLib))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, TSF);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, EMethTypeSystem.TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mirInjectionVisitor = new MIRInjectionVisitor(inferred, resolvedCalls);
    var mir = mirInjectionVisitor.visitProgram();
    var verbosity = new CompilerFrontEnd.Verbosity(false, false, CompilerFrontEnd.ProgressVerbosity.None);
    try {
      var java = new codegen.java.JavaCodegen(mir).visitProgram(new Id.DecId(entry, 0));
      System.out.println("Running...");
      var res = RunOutput.java(new JavaCompiler().compile(verbosity, List.of(new JavaFile(JavaCompiler.MAIN_CLASS_NAME,java))), args).join();
      Assertions.fail("Did not fail. Got: "+res);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
}
