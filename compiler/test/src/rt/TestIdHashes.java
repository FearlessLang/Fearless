package rt;

import id.Id;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestIdHashes {
  @Test void shouldHashDecId() {
    var dec = new Id.DecId("test.Foo", 0);
    var hash = dec.uniqueHash();
    Assertions.assertEquals("-2,-34,-71,29,-104,34,-40,44,-85,111,58,111,86,-44,119,-73,122,40,-17,-113,-76,110,65,-91,-1,-112,4,-83,-30,76,-111,80", byteArrayToStr(hash));
  }

  private String byteArrayToStr(byte[] input) {
    return streamBytes(input).map(Object::toString).collect(Collectors.joining(","));
  }

  private Stream<Byte> streamBytes(byte[] input) {
    var stream = Stream.<Byte>builder();
    for (var b : input) {
      stream.accept(b);
    }
    return stream.build();
  }
}
