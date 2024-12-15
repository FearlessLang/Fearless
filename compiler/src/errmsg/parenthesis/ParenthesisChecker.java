package errmsg.parenthesis;

import java.util.List;
import java.util.Stack;

public class ParenthesisChecker {
  private final Stack<Parenthesis> stack = new Stack<>();
  private final String input;
  private final List<Integer> codePointList;
  private int line = 1;
  private int pos = 0;

  public ParenthesisChecker(String input) {
    this.input = input;
    this.codePointList = input.codePoints().boxed().toList();
  }

  public String compute() {
    ParenthesisCheckerState state = ParenthesisCheckerState.DEFAULT;
    for(int i=0; i<codePointList.size(); i++) {
      state = state.process(this, i);
    }
    return ParenthesisMessage.getErrorMessage(this, input, state);
  }

  public String getStringValue(int index, int length) {
    StringBuilder string = new StringBuilder();
    for (int i=index; i<index+length; i++) {
      string.append(Character.toString(codePointList.get(i)));
    }
    return string.toString();
  }

  public void incrementLine() {
    this.line++;
    this.pos = 0;
  }

  public void incrementPos() {
    this.pos++;
  }

  public void addToStack(ParenthesisType parenthesisType) {
    this.stack.push(new Parenthesis(parenthesisType, this.line, this.pos));
  }

  public Stack<Parenthesis> getStack() {
    return this.stack;
  }
}
