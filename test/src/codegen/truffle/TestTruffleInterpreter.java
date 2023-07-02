package codegen.truffle;

import codegen.MIR;
import id.Id;
import id.Mdf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTruffleInterpreter {
  @Test void shouldReturnLambda() {
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
    var rootNode = new FearlessRootNode(initExpr);
    var callTarget = rootNode.getCallTarget();

    var res = callTarget.call();
    assertEquals(entryPoint, res);
  }
}
