package visitors;

import ast.E;
import ast.T;

import java.util.Map;

public interface GammaVisitor<R> {
  R visitMCall(E.MCall e, Map<String, T> gamma);
  R visitX(E.X e, Map<String, T> gamma);
  R visitLambda(E.Lambda e, Map<String, T> gamma);
}
