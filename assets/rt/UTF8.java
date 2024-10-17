package rt;

import base.List_1;
import base.Void_0;

import java.nio.ByteBuffer;

public final class UTF8 implements base.UTF8_0 {
  public static UTF8 $self = new UTF8();

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override public Fallible fromBytes$imm(List_1 utf8Bytes_m$) {
    return switch (utf8Bytes_m$) {
      case rt.ListK.ByteBufferListImpl byteList -> utf8ToStr(byteList.inner());
      case ListK.ListImpl list -> utf8ToStr(rawListToBuffer((java.util.List<Byte>) list.inner()));
      default -> utf8ToStr(listToBuffer(utf8Bytes_m$));
    };
  }

  private Fallible utf8ToStr(ByteBuffer utf8) {
    return res -> {
      try {
        return res.ok$mut(Str.fromUtf8(utf8));
      } catch (NativeRuntime.StringEncodingError e) {
        return res.info$mut(e.info);
      }
    };
  }

  private ByteBuffer rawListToBuffer(java.util.List<Byte> utf8) {
    var size = utf8.size();
    var buf = ByteBuffer.allocateDirect(size).position(0);
    utf8.forEach(buf::put);
    return buf.position(0);
  }

  private ByteBuffer listToBuffer(List_1 utf8) {
    var size = utf8.size$read().intValue();
    var buf = ByteBuffer.allocateDirect(size).position(0);
    utf8.iter$mut().for$mut(b -> {
      buf.put((byte) b);
      return Void_0.$self;
    });
    return buf.position(0);
  }
}
