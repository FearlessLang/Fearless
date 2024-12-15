package failure;


import errmsg.BetterErrMsgs;
import files.Pos;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.net.URI;
import java.util.BitSet;

public record ParserErrors(URI fileName) implements ANTLRErrorListener {
  public static CompileError fromCompileError(CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new ParserErrors(error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case mustProvideImplsIfMdfProvided -> new PlainError(errorProcessor.mustProvideImplsIfMdfProvided(error));
      default -> error;
    };
  }

  public String mustProvideImplsIfMdfProvided(CompileError rawError) {
    // TODO: improve this error in some way
    return rawError.toString();
  }

  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    TokenStream input = ((Parser) recognizer).getInputStream();
    try {
      String betterMsg = new BetterErrMsgs(input.getText(), offendingSymbol, msg).syntaxError();
      throw Fail.syntaxError(betterMsg).pos(Pos.of(this.fileName, line, charPositionInLine));
    } catch (AssertionError err) {
      System.err.println("Error in BetterErrMsgs, falling back: " + err + " with message: " + err.getMessage());
    }
    throw Fail.syntaxError(msg).pos(Pos.of(this.fileName, line, charPositionInLine));
  }

  public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {}
  public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {}
  public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {}
}
