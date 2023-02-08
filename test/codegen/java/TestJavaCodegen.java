package codegen.java;

import codegen.MIR;
import id.Mdf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestJavaCodegen {
  @Test void emptyProgram() {
    var v = new JavaCodegen();
    System.out.println(v.visitPackage("test", Map.of()));
  }
  @Test void simple() {
    var v = new JavaCodegen();
    System.out.println(v.visitPackage("test", Map.of(
      "test.Foo_0_0", new MIR.Trait(
        List.of(),
        List.of("base.Main"),
        Map.of("$35_0", new MIR.Meth(
          Mdf.imm,
          List.of(), List.of(new MIR.X(Mdf.lent, "s", "base.System")), "test.Bar",
          Optional.of(new MIR.NewStaticLambda(Mdf.imm, "test.Bar_0_0"))
        ))
      ),
      "test.Bar_0_0", new MIR.Trait(
        List.of(),
        List.of(),
        Map.of()
      ),
      "base.Main_0_0", new MIR.Trait(
        List.of(),
        List.of(),
        Map.of("$35_0", new MIR.Meth(
          Mdf.imm,
          List.of("R"), List.of(new MIR.X(Mdf.lent, "s", "base.System")), "R",
          Optional.empty()
        ))
      )
    )));
  }
}
