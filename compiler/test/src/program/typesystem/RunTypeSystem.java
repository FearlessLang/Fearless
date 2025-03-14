package program.typesystem;

import failure.CompileError;
import id.Id;
import id.Mdf;
import main.Main;
import org.junit.jupiter.api.Assertions;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public interface RunTypeSystem {
  static void ok(String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck(new ConcurrentHashMap<>());
  }
  static void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    try {
      var p = Parser.parseAll(ps, new TypeSystemFeatures());
      new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
      var inferred = InferBodies.inferAll(p);
      new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
      inferred.typeCheck(new ConcurrentHashMap<>());
      Assertions.fail("Did not fail!\n");
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  static void expectFail(String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    try {
      var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
      new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
      var inferred = InferBodies.inferAll(p);
      new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
      inferred.typeCheck(new ConcurrentHashMap<>());
      Assertions.fail("Did not fail!\n");
    } catch (CompileError ignored) { }
  }
}
