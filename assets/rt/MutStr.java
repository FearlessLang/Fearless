package rt;

import base.Bool_0;
import base.False_0;
import base.True_0;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class MutStr implements Str {
  private ByteBuffer buffer = ByteBuffer.allocateDirect(16);
  private volatile int[] graphemes = null;

  public MutStr(Str str) {
    assert str != null;
    put(str);
  }

  @Override public ByteBuffer utf8() {
    return buffer.slice(0, buffer.position());
  }

  @Override public int[] graphemes() {
    var graphemes = this.graphemes;
    if (graphemes != null) {
      return graphemes;
    }
    graphemes = NativeRuntime.indexString(utf8());
    this.graphemes = graphemes;
    return graphemes;
  }

  @Override public Bool_0 isEmpty$read() {
    return buffer.position() == 0 ? True_0.$self : False_0.$self;
  }

  @Override public MutStr $plus$mut(base.Stringable_0 other$) {
    var other = other$.str$read();
    assert other != null;
    put(other);
    return this;
  }

  @Override public base.Void_0 append$mut(base.Stringable_0 other$) {
    this.$plus$mut(other$);
    return base.Void_0.$self;
  }

  @Override public base.Void_0 clear$mut() {
    buffer = ByteBuffer.allocateDirect(16);
    return base.Void_0.$self;
  }

  @Override public rt.Str str$read() {
    return rt.Str.fromTrustedUtf8(utf8());
  }

  private void put(Str str) {
    graphemes = null;
    var toAdd = str.utf8();
    assert toAdd.position() == 0;
    if (buffer.remaining() < toAdd.capacity()) {
      var minSizeIncrease = (buffer.capacity() * 3) / 2 + 1;
      var newBuffer = ByteBuffer.allocateDirect(Math.max(minSizeIncrease, buffer.capacity() + toAdd.capacity()));
      newBuffer.put(this.utf8());
      buffer = newBuffer;
    }
    buffer.put(toAdd.duplicate());
  }
}
