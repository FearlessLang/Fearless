package codegen.truffle;

import com.oracle.truffle.api.TruffleLanguage;

// The idea is to specialise the AST, I'm gonna try this on a ast.Program instead of MIR.
// If it's too hard to specialise from ast.Program -> something MIR like we might need to
// start with MIR.

public class Lang extends TruffleLanguage<Lang.Ctx> {
  record Ctx() {}
  @Override protected Ctx createContext(Env env) {
    return null;
  }
}
