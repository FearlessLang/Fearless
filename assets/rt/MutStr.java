package rt;

import base.Bool_0;
import base.False_0;
import base.True_0;

import java.util.ArrayList;
import java.util.List;

public final class MutStr implements Str {
  private final List<Str> buffer = new ArrayList<>();
  private Str immStr;

  public MutStr(Str str) {
    buffer.add(str);
    immStr = str;
  }

  @Override public byte[] utf8() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr.utf8();
  }

  @Override public int[] graphemes() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr.graphemes();
  }

  @Override public Bool_0 isEmpty$read() {
    for (var str : buffer) {
      if (str.isEmpty$read() == False_0.$self) {
        return False_0.$self;
      }
    }
    return True_0.$self;
  }

  @Override public MutStr append$mut(base.Stringable_0 other$) {
    buffer.add(other$.str$read());
    immStr = null;
    return this;
  }

  @Override public base.Void_0 clear$mut() {
    buffer.clear();
    immStr = Str.EMPTY;
    return base.Void_0.$self;
  }

  @Override public rt.Str str$read() {
    if (immStr == null) {
      immStr = freeze();
    }
    return immStr;
  }

  private Str freeze() {
    byte[] utf8 = new byte[buffer.parallelStream().mapToInt(s -> s.utf8().length).sum()];
    int idx = 0;
    for (var str : buffer) {
      System.arraycopy(str.utf8(), 0, utf8, idx, str.utf8().length);
      idx = idx + str.utf8().length;
    }
    var res = Str.fromTrustedUtf8(utf8);
    buffer.clear(); // cant just set to null, because we use .freeze for just normal reading too (not just for ->imm)
    buffer.add(res);
    return res;
  }
}
