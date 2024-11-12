package codegen.java.types;

import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import magic.Magic;
import org.apache.commons.text.StringEscapeUtils;
import rt.NativeRuntime;
import utils.Bug;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static magic.Magic.getLiteral;

public record Assert(JavaTarget target) implements JavaCodegenType {
  @Override public Id.DecId magicDec() { return Magic.Assert; }
  @Override public void type(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("base.Assert_0");
  }

  private static Map<Id.MethName, MethImpl<Assert>> ms = Map.of(
    new Id.MethName("._fail", 0), (t, call, state, self)->{
      state.buffer().append("""
        (switch (1) { default -> {
          System.err.println("Assertion failed :(");
          System.exit(1);
          yield\s""");
      throw Bug.todo();
    }
  );
  @Override public void call(MIR.MT t, MIR.MCall call, JavaCodegenState state) {
    var m = ms.get(call.name());
    assert m != null : "No method found for "+call.name();
    m.of(t, call, state, this);
  }
}
