package failure;


import ast.E;
import ast.Program;
import ast.T;
import errmsg.typeSystem.TypeSystemMsg;
import program.typesystem.MultiSig;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static failure.CompileError.of;

public record TypingAndInferenceErrors(Program p, URI fileName) {
  public static CompileError fromInference(Program p, CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new TypingAndInferenceErrors(p, error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case undefinedMethod -> new PlainError(TypeSystemMsg.undefinedMeth(error, p));
      default -> error;
    };
  }

  public static CompileError fromMethodError(Program p, CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new TypingAndInferenceErrors(p, error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case undefinedMethod -> new PlainError(TypeSystemMsg.undefinedMeth(error, p));
      case invalidMethodArgumentTypes -> errorProcessor.invalidMethodArgumentTypes(error);
      default -> error;
    };
  }

  public CompileError invalidMethodArgumentTypes(CompileError rawError) {
    @SuppressWarnings("unchecked")
    var argTypes = (List<T>) rawError.attributes.get("argTypes");
    @SuppressWarnings("unchecked")
    var _expectedRets = (List<T>) rawError.attributes.get("expected");
    var expectedRets = _expectedRets.stream().map(T::toString).distinct().collect(Collectors.joining(", "));
    var sigs = (MultiSig) rawError.attributes.get("sigs");
    var e = (E.MCall) rawError.attributes.get("mCall");
    List<String> argTypesWithImpls = addImplsToArgTypes(argTypes);
    var msg= STR."Method \{e.name()} called in position \{e.posOrUnknown()} can not be called with current parameters of types:\n\{argTypesWithImpls}";
    return of(msg+"\n"+sigs);
  }
  private List<String> addImplsToArgTypes(List<T> argTypes) {
    return argTypes.stream()
      .map(t->t.<String>match(
        _ -> t.toString(),
        it -> STR."\{t} (\{p.of(it.name()).lambda().its().stream()
          .map(iti->iti.name().toString())
          .collect(Collectors.joining(", "))})"
      ))
      .toList();
  }
}
