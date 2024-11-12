package codegen.java.types;

import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import magic.Magic;
import org.apache.commons.text.StringEscapeUtils;
import rt.NativeRuntime;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static magic.Magic.getLiteral;

public record Str(JavaTarget target) implements JavaCodegenType {
  @Override public Id.DecId magicDec() { return Magic.Str; }

  @Override public void instantiate(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    var id = k.concreteT().id();
    var javaStr = getLiteral(target.p().p(), id).map(l->l.substring(1, l.length() - 1)).orElseThrow();
    // We parse literal \n, unicode escapes as if this was a Java string literal.
    var utf8 = StringEscapeUtils.unescapeJava(javaStr).getBytes(StandardCharsets.UTF_8);
    var buf = ByteBuffer.allocateDirect(utf8.length).put(utf8).position(0).asReadOnlyBuffer();
    var recordName = ("str$"+Long.toUnsignedString(NativeRuntime.hashString(buf), 10)+"$str$");

    if (!state.records().containsKey(id)) {
      var utf8Array = IntStream.range(0, utf8.length).mapToObj(i->Byte.toString(utf8[i])).collect(Collectors.joining(","));
      // We do not need to run validateStringOrThrow because Java will never produce an invalid UTF-8 str with getBytes.
      var graphemes = Arrays.stream(NativeRuntime.indexString(buf)).mapToObj(Integer::toString).collect(Collectors.joining(","));

      state.records().put(new Id.DecId(state.pkg()+"."+recordName, 0), """
        final class %s implements rt.Str {
          public static final rt.Str $self = new %s();
          private static final java.nio.ByteBuffer UTF8 = rt.Str.wrap(new byte[]{%s});
          private static final int[] GRAPHEMES = new int[]{%s};
          @Override public java.nio.ByteBuffer utf8() { return UTF8; }
          @Override public int[] graphemes() { return GRAPHEMES; }
        }
        """.formatted(recordName, recordName, utf8Array, graphemes));
    }

    var createExpr = recordName+".$self";
    state.buffer().append(switch (k.t().mdf()) {
      case mut,iso -> "new rt.MutStr("+createExpr+")";
      default -> createExpr;
    });
  }

  @Override public void type(MIR.MT t, JavaCodegenState state) {
    state.buffer().append("rt.Str");
  }
}
