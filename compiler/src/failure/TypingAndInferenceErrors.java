package failure;


import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.typesystem.MultiSig;
import program.typesystem.XBs;
import utils.Bug;

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
      case undefinedMethod -> new PlainError(errorProcessor.undefinedMeth(error));
      default -> error;
    };
  }

  public static CompileError fromMethodError(Program p, CompileError error) {
    if (error.code() == 0) { return error; }
    var errorProcessor = new TypingAndInferenceErrors(p, error.posOrUnknown().fileName());
    return switch (ErrorCode.fromCode(error.code())) {
      // TODO: match on error types you want to improve
      case undefinedMethod -> new PlainError(errorProcessor.undefinedMeth(error));
      case invalidMethodArgumentTypes -> errorProcessor.invalidMethodArgumentTypes(error);
      default -> error;
    };
  }

  public String undefinedMeth(CompileError rawError) {
    // TODO: improve this error in some way
    ast.T recvT = switch (rawError.attributes.get("recvT")) {
      case astFull.T t -> t.toAstT();
      case ast.T t -> t;
      default -> throw Bug.unreachable();
    };
    var name = (Id.MethName) rawError.attributes.get("name");
    var ms = p().meths(XBs.empty(), Mdf.recMdf, recvT.itOrThrow(), 0);

    return rawError+"\nextra info for experts:\n"+ms;
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
    var msg= "Method " + e.name() + " called in position " + e.posOrUnknown() + " can not be called with current parameters of types:\n" + argTypesWithImpls;
    return of(msg+"\n"+sigs);
  }
  private List<String> addImplsToArgTypes(List<T> argTypes) {
    return argTypes.stream()
      .map(t->t.<String>match(
        _ -> t.toString(),
        it -> t + " (" + p.of(it.name()).lambda().its().stream()
          .map(iti -> iti.name().toString())
          .collect(Collectors.joining(", ")) + ")"
      ))
      .toList();
  }
}
