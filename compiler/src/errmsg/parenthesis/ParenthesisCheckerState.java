package errmsg.parenthesis;

public enum ParenthesisCheckerState {
  DEFAULT {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      ParenthesisType pt = ParenthesisType.getBySymbol(str);
      // Parenthesis character
      if(pt != null) {
        ParenthesisCheckerState newState = processParenthesis(checker, pt);
        checker.incrementPos();
        return newState;
      }
      // Not a parenthesis character
      switch (str) {
        case "\n" -> {
          checker.incrementLine();
          return this;
        }
        case "/" -> {
          checker.incrementPos();
          String lookAhead = checker.getStringValue(i, 2);
          if(lookAhead.equals("//")) {return COMM;}
          if(lookAhead.equals("/*")) {return MULTI_COMM;}
          return this;
        }
        case "\"" -> {
          checker.incrementPos();
          String lookahead = checker.getStringValue(i, 3);
          if(lookahead.equals("\"\"\"")) {return MULTI_STRING;}
          return STRING;
        }
        default -> {
          checker.incrementPos();
          return this;
        }
      }
    }

    private ParenthesisCheckerState processParenthesis(ParenthesisChecker checker, ParenthesisType pt) {
      if(!pt.isOpen) {
        // Check if close without open
        if(checker.getStack().isEmpty()) {
          checker.addToStack(pt);
          return ERR_EXTRA;
        }
        // Check if wrong close
        String check = checker.getStack().peek().type().symbol;
        if(!check.equals(pt.pair)) {
          checker.addToStack(pt);
          return ERR_WRONG;
        }
        // Pop because valid close
        checker.getStack().pop();
      } else {
        // Add opening parenthesis to stack
        checker.addToStack(pt);
      }
      return this;
    }
  },
  ERR_EXTRA {},
  ERR_WRONG{},
  COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      if(str.equals("\n")) {
        checker.incrementLine();
        return DEFAULT;
      }
      checker.incrementPos();
      return this;
    }
  },
  MULTI_COMM {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      if(str.equals("\n")) {checker.incrementLine();}
      else {checker.incrementPos();}
      if(str.equals("*")) {
        String lookAhead = checker.getStringValue(i-1, 3);
        if(lookAhead.equals("/*/")) {return this;}
        if(lookAhead.endsWith("*/")) {return DEFAULT;}
      }
      return this;
    }
  },
  MULTI_STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      switch(str) {
        case "|" -> {checker.incrementPos();return MULTI_STRING_LINE;}
        case "\\" -> {checker.incrementPos();return ESCAPE_MULTI;}
        case "\"" -> {
          checker.incrementPos();
          String lookAhead = checker.getStringValue(i, 3);
          if(lookAhead.equals("\"\"\"")) {return DEFAULT;}
          return this;
        }
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {checker.incrementPos();return this;}
      }
    }
  },
  MULTI_STRING_LINE {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      if (!str.equals("\n")) {return this;}
      checker.incrementLine();
      return MULTI_STRING;
    }
  },
  STRING {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      String str = checker.getStringValue(i, 1);
      switch(str) {
        case "\"" -> {checker.incrementPos();return DEFAULT;}
        case "\\" -> {checker.incrementPos();return ESCAPE;}
        case "\n" -> {checker.incrementLine(); return this;}
        default -> {checker.incrementPos();return this;}
      }
    }
  },
  ESCAPE {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      checker.incrementPos();
      return STRING;
    }
  },
  ESCAPE_MULTI {
    @Override public ParenthesisCheckerState process(ParenthesisChecker checker, int i) {
      checker.incrementPos();
      return MULTI_STRING;
    }
  };

  // Enum Methods
  public ParenthesisCheckerState process(ParenthesisChecker checker, int i){checker.incrementPos();return this;}
}
