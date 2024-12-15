package errmsg.parenthesis;

import java.util.HashMap;
import java.util.Map;

public enum ParenthesisType {
  OR("(", ")", true),
  CR(")", "(", false),
  OC("{", "}", true),
  CC("}", "{", false),
  OS("[", "]", true),
  CS("]", "[", false);

  private static final Map<String, ParenthesisType> BY_SYMBOL = new HashMap<>();
  private static final Map<String, ParenthesisType> BY_PAIR = new HashMap<>();

  static {
    for (ParenthesisType type : values()) {
      BY_SYMBOL.put(type.symbol, type);
      BY_PAIR.put(type.pair, type);
    }
  }

  public final String symbol;
  public final String pair;
  public final boolean isOpen;

  ParenthesisType(String symbol, String pair, boolean isOpen) {
    this.symbol = symbol;
    this.pair = pair;
    this.isOpen = isOpen;
  }

  public static ParenthesisType getBySymbol(String symbol) {
    return BY_SYMBOL.get(symbol);
  }

  public static ParenthesisType getByPair(String pair) {
    return BY_PAIR.get(pair);
  }
}
