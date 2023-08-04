package codegen;

import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMIRTypeResolution {
  MIR.Program toMIR(String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    return new MIRInjectionVisitor(inferred).visitProgram();
  }

  @Test void shouldReduceLambdaToMostConcreteType() {
    var p = toMIR("""
      package test
      A:{}
      B:A{}
      Foo:{ .bar: A -> B }
      Baz:{ .bzzt(f: Foo): A -> f.bar }
      """);

    var res = new MIRConcreteTypeVisitor().visitMeth(p.pkgs().get("test").stream()
      .filter(t->t.name().name().equals("test.Foo"))
      .findAny()
      .get()
      .meths()
      .get(0),
      "this",
      false
    );

    assertEquals("imm test.B[]", res.toString());
  }

  @Test void shouldReduceMCallToMostConcreteType() {
    var p = toMIR("""
      package test
      A:{}
      B:A{}
      Foo:{ .bar: A -> B }
      Baz:{ .bzzt(f: Foo): A -> f.bar }
      """);

    var res = new MIRConcreteTypeVisitor().visitMeth(p.pkgs().get("test").stream()
        .filter(t->t.name().name().equals("test.Baz"))
        .findAny()
        .get()
        .meths()
        .get(0),
      "this",
      false
    );

    assertEquals("imm test.B[]", res.toString());
  }
}
