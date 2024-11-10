package codegen.java;

import id.Id;

import java.util.HashMap;

public record JavaCodegenState(String pkg, HashMap<Id.DecId, String> records, StringBuilder buffer) {
  public JavaCodegenState() {
    this(null, new HashMap<>(), new StringBuilder());
  }
}
