package codegen.truffle;

import codegen.MIR;
import codegen.MIRInjectionVisitor;
import id.Id;
import id.Mdf;
import main.Main;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.OneOr;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTruffleInterpreter {
  static void ok(Id.DecId entry, String... content){
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
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var truffleAst = new MIRToTruffle().visitProgram(mir.pkgs());
//    var entryLambda = new MIR.Lambda(Mdf.mdf, entry, "", List.of(new Id.IT<>(entry, List.of())), List.of(), List.of(), true)
    var lang = new FearlessLanguage(truffleAst);
    new FearlessRootNode(lang, entry).getCallTarget().call();
  }

  private Context context;

  @BeforeEach public void setUp() {
    this.context = Context.create("fearless");
  }
  @AfterEach public void tearDown() {
    this.context.close();
  }


  @Test void shouldReturnLambda() {
    this.context.initialize("fearless");
    var entryPoint = new MIR.Lambda(
      Mdf.imm,
      new Id.DecId("test.Yolo", 0),
      "inityinit",
      List.of(new Id.IT<>("base.Main", List.of())),
      List.of(),
      List.of(),
      true
    );
    FearlessNode initExpr = new LambdaNode(entryPoint);
    var lang = new FearlessLanguage(Map.of());
    var rootNode = new FearlessRootNode(lang, new Id.DecId("test.Yolo", 0));
    var callTarget = rootNode.getCallTarget();

    var res = callTarget.call();
    assertEquals(entryPoint, res);
  }
}
