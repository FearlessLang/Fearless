package codegen.truffle;

import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.Test;
import com.oracle.truffle.api.Truffle;

public class TestCodegen {
  @Test void foo() {
    System.out.println("== running on " + Truffle.getRuntime().getName());
    try (Engine engine = Engine.newBuilder().build()) {
//      engine./
    }
  }
}
