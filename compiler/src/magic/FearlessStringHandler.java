package magic;

import failure.Fail;
import failure.FailOr;

public class FearlessStringHandler {
  private final StringKind kind;
  private Mode mode = Mode.Plain;
  private final StringBuilder res = new StringBuilder();
  int expectNext = 0;

  public FearlessStringHandler(StringKind kind) {
    this.kind = kind;
  }

  public enum StringKind {
    Unicode,
    Simple
  }

  public FailOr<String> toJavaString(String fearlessString) {
    // Remove quotes
    var raw = fearlessString.substring(1, fearlessString.length() - 1);
    try {
      raw.codePoints().forEach(cp -> {
        if (expectNext != 0 && cp != expectNext) {
          throw new IllegalArgumentException("Expected "+expectNext+" but got "+cp);
        }
        expectNext = 0;
        switch (this.mode) {
          case Plain -> handlePlain(cp);
          case Escape -> handleEscape(cp);
          case UnicodeEscape -> handleUnicodeEscape(cp);
        }
      });
    } catch (IllegalArgumentException e) {
      return FailOr.err(()->Fail.invalidStr(fearlessString, kind.toString()));
    }
    if (mode != Mode.Plain) {
      return FailOr.err(()->Fail.invalidStr(fearlessString, kind.toString()));
    }
    return FailOr.res(res.toString());
  }

  private void handlePlain(int cp) {
    if (cp == '\\') {
      this.mode = Mode.Escape;
      return;
    }
    res.appendCodePoint(cp);
  }

  private void handleEscape(int cp) {
    if (cp == 'u') {
      this.mode = Mode.UnicodeEscape;
      expectNext = '{';
      return;
    }
    this.mode = Mode.Plain;
    switch (cp) {
      case 'n' -> res.append('\n');
      case 'r' -> res.append('\r');
      case 't' -> res.append('\t');
      case 'b' -> res.append('\b');
      case 'f' -> res.append('\f');
      case '\\' -> res.append('\\');
      case '`' -> res.append('`');
      case '"' -> res.append('"');
      default -> throw new IllegalArgumentException("Unknown escape sequence: \\" + cp);
    }
  }

  private StringBuilder unicodeEscape = new StringBuilder();
  private void handleUnicodeEscape(int cp) {
    if (cp == '{') {
      assert unicodeEscape.isEmpty();
      return;
    }
    if (cp == '}') {
      if (unicodeEscape.isEmpty()) {
        throw new IllegalArgumentException("Empty unicode escape sequence");
      }
      // TODO: validate simple strings do not contain invalid unicode here
      int codePoint = Integer.parseInt(unicodeEscape.toString(), 10);
      res.appendCodePoint(codePoint);
      unicodeEscape = new StringBuilder();
      mode = Mode.Plain;
      return;
    }

    switch (cp) {
      case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> unicodeEscape.appendCodePoint(cp);
      default -> throw new IllegalArgumentException("Invalid unicode escape sequence: \\u" + unicodeEscape.toString() + (char) cp);
    }
  }

  enum Mode {
    Plain,
    Escape,
    UnicodeEscape
  }
}
