package codegen.java;

import ast.E;
import failure.Res;
import visitors.Visitor;

public class JavaCodegen implements Visitor<Res> {
  @Override
  public Res visitMCall(E.MCall e) {
    return null;
  }

  @Override
  public Res visitX(E.X e) {
    return null;
  }

  @Override
  public Res visitLambda(E.Lambda e) {
    return null;
  }
}
