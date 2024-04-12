package files;

import java.net.URI;
import java.util.Optional;

public interface HasPos {
  Optional<Pos> pos();

  default Pos posOrUnknown() {
    return pos().orElse(Pos.UNKNOWN);
  }
}
