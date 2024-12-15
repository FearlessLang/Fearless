package errmsg;

import errmsg.parenthesis.ParenthesisChecker;
import org.antlr.v4.runtime.Token;

public class BetterErrMsgs {
  private final String input;
  Object offendingSymbol;
  private final String msg;

  public BetterErrMsgs(String input, Object offendingSymbol, String msg) {
    this.input = input;
    this.offendingSymbol = offendingSymbol;
    this.msg = msg;
  }

  public String syntaxError() {
    try {
      return new ParenthesisChecker(input).compute();
    } catch (IllegalStateException e) {
      assert e.getMessage().equals("No parenthesis error was found");
      if (isParenthesisError()) {
        return "Parenthesis checker could not find the error";
      }
    }
    return this.msg;
  }

  private boolean isParenthesisError() {
    if(offendingSymbol instanceof Token token) {
      if(token.getText().matches("[(){}\\[\\]]")) {
        return true;
      }
    }
    return this.msg.matches("missing '([(){}\\[\\]])'.*");
  }
}
