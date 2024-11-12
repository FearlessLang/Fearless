package codegen.java;

import id.Id;

import java.util.HashMap;
import java.util.Map;

public record JavaCodegenState(String pkg, Map<Id.DecId, String> records, StringBuilder buffer) {
  public JavaCodegenState() {
    this(null, new HashMap<>(), new StringBuilder());
  }
  public static JavaCodegenState bufferOnly(StringBuilder buffer) {
    return new JavaCodegenState(null, Map.of(), buffer);
  }
}
