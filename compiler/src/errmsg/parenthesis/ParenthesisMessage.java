package errmsg.parenthesis;

import java.util.List;
import java.util.Stack;

public class ParenthesisMessage {

  public static String getErrorMessage(ParenthesisChecker checker, String input, ParenthesisCheckerState state) {
    return switch (state) {
      case DEFAULT -> getUnclosedErrorMessage(checker, input);
      case ERR_EXTRA -> getUnexpectedErrorMessage(checker, input);
      case ERR_WRONG -> getMismatchErrorMessage(checker, input);
      default -> throw new IllegalStateException("No parenthesis error was found");
    };
  }

  private static String getUnclosedErrorMessage(ParenthesisChecker checker, String input) {
    Stack<Parenthesis> stack = checker.getStack();
    assert !stack.isEmpty();
    return stack.size() == 1 ? getSingleUnclosed(stack.pop(), input) : getMultiUnclosed(stack, input);
  }

  private static String getSingleUnclosed(Parenthesis open, String input) {
    String prefix = open.line() + ": ";
    String message = String.format("Error: unclosed opening parenthesis '%s' at %d:%d\n", open.type().symbol, open.line(), open.pos());
    message += prefix + input.lines().toList().get(open.line()-1) + "\n";
    message += " ".repeat(open.pos() + prefix.length()) + "^ unclosed parenthesis\n";
    return message;
  }

  private static String getMultiUnclosed(Stack<Parenthesis> stack, String input) {
    String message = "Error: multiple unclosed opening parenthesis\n";
    // TODO: Point out multiple opening parenthesis if possible?
    // This is a placeholder
    return getSingleUnclosed(stack.pop(), input);
  }

  private static String getUnexpectedErrorMessage(ParenthesisChecker checker, String input) {
    Parenthesis close = checker.getStack().pop();
    String prefix = close.line() + ": ";
    String message = String.format("Error: unexpected closing parenthesis '%s' at %d:%d\n", close.type().symbol, close.line(), close.pos());
    message += prefix + input.lines().toList().get(close.line()-1) + "\n";
    message += " ".repeat(close.pos() + prefix.length()) + "^ unexpected close\n";
    return message;
  }

  private static String getMismatchErrorMessage(ParenthesisChecker checker, String input) {
    Stack<Parenthesis> stack = checker.getStack();
    Parenthesis close = stack.pop();
    Parenthesis open = stack.pop();
    return close.line() != open.line() ? getMultiLineMismatch(open, close, input) : getSingleLineMismatch(open, close, input);
  }

  private static String getSingleLineMismatch(Parenthesis open, Parenthesis close, String input) {
    String line = input.lines().toList().get(open.line()-1);
    String prefix = open.line() + ": ";
    String whitespace = " ".repeat(open.pos() + prefix.length());
    String suggestion = "is it meant to be '" + open.type().pair + "'?";
    String indicator = whitespace + "^" + "-".repeat(close.pos()-open.pos()-1) + "^ mismatched close, " + suggestion;
    return """
        Error: mismatched closing parenthesis '%s' at %d:%d
        %s%s
        %s
        %s|
        %sunclosed open
        
        """.formatted(close.type().symbol, close.line(), close.pos(), prefix, line, indicator, whitespace, whitespace);
  }

  private static String getMultiLineMismatch(Parenthesis open, Parenthesis close, String input) {
    List<String> lines = input.lines().toList();
    StringBuilder message = new StringBuilder(
      String.format("Error: mismatched closing parenthesis '%s' at %d:%d\n", close.type().symbol, close.line(), close.pos()));
    boolean flag = false;
    for(int i=open.line(); i<=close.line(); i++) {
      if(i > open.line()+1 && i < close.line()-1) {
        if(!flag) {
          String range = (open.line()+2) + "-" + (close.line()-2) + ": ";
          message.append(range);
          message.append("... ... ...\n");
          flag = true;}
        continue;
      }
      String prefix = i + "  : ";
      message.append(prefix).append(lines.get(i-1)).append("\n");
      if(i == open.line()) {
        message.append(" ".repeat(open.pos() + prefix.length())).append("^ unclosed open\n");
      }
      if(i == close.line()) {
        String suggestion = "is it meant to be '" + open.type().pair + "'?";
        message.append(" ".repeat(close.pos() + prefix.length())).append("^ mismatched close, ").append(suggestion).append("\n");
      }
    }
    return message.toString();
  }
}
