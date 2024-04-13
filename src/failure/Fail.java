package failure;

import astFull.E;
import astFull.T;
import files.Pos;
import id.Id;
import id.Mdf;
import program.CM;
import program.typesystem.EMethTypeSystem;
import utils.Bug;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Fail{
  static {
    // Ensure that ErrorCode is consistent
    Method[] ms = Fail.class.getDeclaredMethods();
    Arrays.stream(ms)
      .filter(m-> Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
      .filter(m->!m.getName().equals("conflict"))
      .forEach(m->{
        try {
          ErrorCode.valueOf(m.getName());
        } catch (IllegalArgumentException e) {
          throw Bug.of("ICE: ErrorCode enum is not complete. Missing: " + m.getName());
        }
      });
  }

  public record Conflict(Pos pos,String descr){}
  public static Conflict conflict(Pos pos,String descr){ return new Conflict(pos,descr); }
  private static String conflictingMsg(String base, List<Conflict> conflicts){
    var conflictMsg = conflicts.stream()
      .map(c->String.format("(%s) %s", c.pos(), c.descr()))
      .collect(Collectors.joining("\n"));

    return String.format("%s\nconflicts:\n%s", base, conflictMsg);
    }
  private static CompileError of(String msg){
    return new CompileError(msg);
  }

  //ALL OUR ERRORS
  public static CompileError conflictingAlias(String aliased, List<Conflict> conflicts){return of(
      "This alias is in conflict with other aliases in the same package: "+conflictingMsg(aliased, conflicts));}
  public static CompileError conflictingDecl(Id.DecId decl, List<Conflict> conflicts){return of(
      "This trait declaration is in conflict with other trait declarations in the same package: "+conflictingMsg(decl.toString(), conflicts));}
  public static CompileError conflictingDecls(List<Conflict> conflicts){return of(
    conflictingMsg("Trait names must be unique.", conflicts));}

  public static CompileError uncomposableMethods(List<Conflict> conflicts) { return of(conflictingMsg("These methods could not be composed.", conflicts)); }

  public static CompileError conflictingMethParams(List<String> conflicts){return of(
    "Parameters on methods must have different names. The following parameters were conflicting: " + String.join(", ", conflicts));}

  public static CompileError conflictingMethNames(List<String> conflicts){return of(
    "Methods may not have the same name and number of parameters. The following methods were conflicting: " + String.join(", ", conflicts));}

  public static CompileError concreteTypeInFormalParams(T badType){return of(
    "Trait and method declarations may only have generic type parameters. This concrete type was provided instead:\n"+badType
      +"\nAlternatively, are you attempting to shadow an existing class name?"
  );}
  public static CompileError modifierOnInferredLambda(){return of(
    "Modifiers cannot be specified on lambdas without an explicit type."
  );}
  public static CompileError invalidMdfBound(ast.T badType, List<Mdf> bounds){
    return of(
      "The type "+badType+" is not valid because it's modifier is not in the required bounds. The allowed modifiers are: "+bounds.stream().map(Enum::toString).collect(Collectors.joining(", "))+"."
    );
  }
  public static CompileError shadowingX(String x){return of(String.format("'%s' is shadowing another variable in scope.", x));}

  public static CompileError shadowingGX(String x){return of("Type variable "+x+" is shadowing another type variable in scope.");}

  public static CompileError explicitThis(){ return of("Local variables may not be named 'this'."); }

  public static CompileError cyclicImplRelation(Id.DecId baseClass){
    return of(String.format("Implements relations must be acyclic. There is a cycle on the trait %s.", baseClass));
  }
  public static CompileError invalidMdf(T t){return of("The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type "+t);}
  public static CompileError invalidMdf(Id.IT<T> it){return of("The modifier 'mdf' can only be used on generic type variables. 'mdf' found on type "+it);}

  public static CompileError expectedConcreteType(T t){ return of("A concrete type was expected but the following generic type was given:\n" + t); }

  public static CompileError missingDecl(Id.DecId d){ return of("The following trait cannot be aliased because it does not exist:\n"+d); }

  public static CompileError invalidMethMdf(E.Sig s, Id.MethName n){ return of(String.format("%s is not a valid modifier for a method (on the method %s).", s.mdf(), n)); }
  public static CompileError invalidLambdaMdf(Mdf mdf){ return of(String.format("%s is not a valid modifier for a lambda.", mdf)); }
  public static CompileError cannotInferSig(Id.DecId d, Id.MethName m){ return of(String.format("Could not infer the signature for %s in %s.", m, d)); }
  public static CompileError cannotInferAbsSig(Id.DecId d){ return of(String.format("Could not infer the signature for the abstract lambda in %s. There must be one abstract lambda in the trait.", d)); }
  public static CompileError traitNotFound(Id.DecId d){
    return of(String.format("The trait %s could not be found.", d));
  }
  public static CompileError inferFailed(String e){ return of(String.format("Could not infer the type for the following expression:\n%s", e)); }

  public static CompileError methTypeError(ast.T expected, ast.T actual, Id.MethName m){
    var msg = "Expected the method "+m+" to return "+expected+", got "+actual+".";
    if (!expected.mdf().isRecMdf() && actual.mdf().isRecMdf()) {
      return of(msg+"\nTry writing the signature for "+m+" explicitly if it needs to return a recMdf type.");
    }
    return of(msg);
  }
  public static CompileError xTypeError(ast.T expected, ast.T actual, ast.E.X x){
    var msg = "Expected '"+x+"' to be "+expected+", got "+actual+".";
    return of(msg);
  }
  public static CompileError lambdaTypeError(ast.T expected){
    var msg = "Expected the lambda here to implement "+expected+".";
    return of(msg);
  }
  public static CompileError unimplementedInLambda(List<CM> ms){
    var unimplemented = ms.stream()
      .map(m->"("+m.pos()+") "+m.name())
      .collect(Collectors.joining("\n"));
    return of(String.format("The lambda must implement the following methods:\n%s", unimplemented));
  }

  public static CompileError cyclicSubType(ast.T t1, ast.T t2){
    return of(String.format("There is a cyclical sub-typing relationship between "+t1+" and "+t2+"."));
  }

  public static CompileError recMdfInNonRecMdf(Mdf mdf, Id.MethName name, astFull.T t){
    return of("Invalid modifier for "+t+"."+recMdfInNonRecMdfMsg(mdf, name));
  }
  public static CompileError recMdfInNonRecMdf(Mdf mdf, Id.MethName name, ast.T t){
    return of("Invalid modifier for "+t+"."+recMdfInNonRecMdfMsg(mdf, name));
  }
  private static String recMdfInNonRecMdfMsg(Mdf mdf, Id.MethName name) {
    return "\nrecMdf may only be used in recMdf methods. The method "+name+" has the "+mdf+" modifier.";
  }
  public static CompileError recMdfInImpls(ast.T t){
    return of("Invalid modifier for "+t+".\nrecMdf may not be used in the list of implemented traits.");
  }
  public static CompileError undefinedName(String name){
    return of("The identifier \""+name+"\" is undefined or cannot be captured.");
  }
  public static CompileError ignoredIdentInExpr(){
    return of("\"_\" ignores the argument in that position and thus cannot be used as an identifier in an expression.");
  }

  public static CompileError badCapture(String x, ast.T xT, Mdf lambdaMdf, Mdf methMdf) {
    var mdf = xT.mdf().isMdf() ? "" : xT.mdf()+" ";
    return of("'"+mdf+x+"' cannot be captured by "+aVsAn(methMdf)+" method in "+aVsAn(lambdaMdf)+" lambda.");
  }

  public static CompileError invalidNum(String n, String kind) {
    return of("The number "+n+" is not a valid "+kind);
  }

  public static CompileError noCandidateMeths(ast.E.MCall e, ast.T expected, List<EMethTypeSystem.TsT> candidates) {
    String tsts = candidates.stream()
      .map(EMethTypeSystem.TsT::toString)
      .collect(Collectors.joining("\n"));
    return of("When attempting to type check the method call: "+e+", no candidates for "+e.name()+" returned the expected type "+expected+". The candidates were:\n"+tsts);
  }

  public static CompileError callTypeError(ast.E.MCall e, Optional<ast.T> expected, String calls) {
    var expected_ = expected.map(ast.T::toString).orElse("?");
    return of("Type error: None of the following candidates (returning the expected type \""+expected_+"\") for this method call:\n"+e+"\nwere valid:\n"+calls);
  }

  public static CompileError bothTExpectedGens(ast.T expected, Id.DecId dec) {
    return of("Type error: the generic type "+expected+" cannot be a super-type of any concrete type, like "+dec+".");
  }

  public static CompileError sealedCreation(Id.DecId sealedDec, String pkg) {
    return of("The sealed trait "+sealedDec+" cannot be created in a different package ("+pkg+").");
  }

  public static CompileError privateMethCall(Id.MethName meth) {
    return of("The private method "+meth+" cannot be called outside of a lambda that implements it.");
  }
  public static CompileError privateTraitImplementation(Id.DecId dec) {
    return of("The private trait "+dec+" cannot be implemented outside of its package.");
  }

  public static CompileError undefinedMethod(Id.MethName name, astFull.T recv){
    return of(name+" does not exist in "+recv+".");
  }

  public static CompileError undefinedMethod(Id.MethName name, ast.T recv, Stream<CM> callableMethods){
    var callableMs = callableMethods.map(cm->cm.mdf()+" "+cm.name()).collect(Collectors.joining(", "));
    if (callableMs.isEmpty()) { callableMs = "N/A"; }
    return of(name+" does not exist in "+recv+". The following methods exist on that type: "+callableMs);
  }

  public static CompileError noSubTypingRelationship(Id.IT<T> it1, Id.IT<T> it2){
    return of("There is no sub-typing relationship between "+it1+" and "+it2+".");
  }

  public static CompileError uncallableMeths(Mdf lambdaMdf, List<ast.E.Meth> ms) {
    var meths = ms.stream().map(m->m.sig().mdf()+" "+m.name()).toList();
    return of("Methods that cannot be called must not be defined. The following methods are impossible to call on an "+lambdaMdf+" lambda:\n"+String.join(", ", meths));
  }

  public static CompileError incompatibleMdfs(T t1, T t2){
    return of("The modifiers for "+t1+" and "+t2+" are not compatible.");
  }

  public static CompileError mutCapturesHyg(ast.T t1){
    return of("The type "+t1+" is not valid because a mut lambda may not capture hygienic references.");
  }

  public static CompileError ioError(IOException err) {
    return of("There was an error handling: "+err.getLocalizedMessage()+".");
  }
  public static CompileError fsError(FileSystemException err) {
    var extra = Optional.ofNullable(err.getReason()).map(reason->" because "+reason+".").orElse(".");
    return of(err.getFile()+" does not exist or cannot be read"+extra);
  }

  public static CompileError invalidEntryPoint(Id.DecId entry, Id.IT<?> main) {
    return of(entry+" must implement "+main);
  }

  public static CompileError multipleIsoUsage(ast.E.X x) {
    return of("The isolated reference \""+x+"\" is used more than once.");
  }

  public static CompileError noMdfInFormalParams(String ty) {
    return of("Modifiers are not allowed in declarations or implementation lists: "+ty);
  }

  public static CompileError mustProvideImplsIfMdfProvided() {
    // TODO wording of this message
    return of("At least one trait must be listed for a lambda to implement if a modifier for the lambda is provided.");
  }

  public static CompileError namedTopLevelLambda() {
    return of("Trait declarations may not have a self-name other than \"this\".");
  }

  public static CompileError couldNotInferCallGenerics(Id.MethName name) {
    return of("Could not infer the generic type arguments for the method call to "+name);
  }

  public static CompileError incompatibleGenerics(Id.GX<T> gx1, Id.GX<T> gx2) {
    return of("The generic type argument "+gx1+" is not compatible with the generic type argument "+gx2+".");
  }

  public static CompileError typeError(String subErrors) {
    return of(subErrors);
  }

  public static CompileError implInlineDec(List<Id.DecId> invalidImpls) {
    var msg = invalidImpls.stream().map(Id.DecId::toString).collect(Collectors.joining(", "));
    return of("Traits declared within expressions cannot be implemented. This lambda has the following invalid implementations: "+msg);
  }

  public static CompileError freeGensInLambda(ast.E.Lambda.LambdaId name, Set<Id.GX<ast.T>> freeGens) {
    var msg = freeGens.stream().map(Id.GX::toString).collect(Collectors.joining(", "));
    return of("The declaration name for a lambda must include all type variables used in the lambda. The declaration name "+name.toIT()+" does not include the following type variables: "+msg);
  }

  public static CompileError invalidLambdaNameMdfBounds(List<String> invalidBounds) {
    var boundsMsg = String.join("\n", invalidBounds).indent(2);
    return of("This lambda is missing/has an incompatible set of bounds for its type parameters:\n"+boundsMsg);
  }

  public static CompileError mismatchedMethodGens(Id.MethName name, List<Id.GX<T>> baseGens, List<Id.GX<ast.T>> implGens) {
    var gens1Msg = baseGens.stream().map(Id.GX::toString).collect(Collectors.joining(", "));
    var gens2Msg = implGens.stream().map(Id.GX::toString).collect(Collectors.joining(", "));
    return of("The base method for "+name+" has the following type parameters: ["+gens1Msg+"]. The provided implementation has: ["+gens2Msg+"].\nThe number of type parameters must match.");
  }

  public static CompileError syntaxError(String msg) {
    return of(msg);
  }

  public static CompileError specialPackageConflict(Set<String> special, Set<String> user) {
    var conflicts = new HashSet<>(user);
    conflicts.retainAll(special);
    var taggedConflicts = conflicts.stream()
      .map(pkg->conflict(Pos.UNKNOWN, pkg))
      .toList();

    return of(conflictingMsg("The following package names are reserved for use in the Fearless standard library", taggedConflicts));
  }

  public static CompileError lambdaImplementsGeneric(astFull.T t) {
    return of("A lambda may not implement a generic type parameter '%s'".formatted(t));
  }

  private static String aVsAn(Mdf mdf) {
    if (mdf.isImm()) { return "an "+mdf; }
    return "a "+mdf;
  }
}

//only add to the bottom
enum ErrorCode {
  conflictingAlias,
  conflictingDecl,
  concreteTypeInFormalParams,
  modifierOnInferredLambda,
  invalidMdfBound,
  explicitThis,
  conflictingMethParams,
  cyclicImplRelation,
  shadowingX,
  shadowingGX,
  invalidMdf,
  typeError,
  implInlineDec,
  expectedConcreteType,
  missingDecl,
  invalidMethMdf,
  conflictingMethNames,
  uncomposableMethods,
  cannotInferSig,
  traitNotFound,
  inferFailed,
  cannotInferAbsSig,
  methTypeError,
  unimplementedInLambda,
  cyclicSubType,
  recMdfInNonRecMdf,
  recMdfInImpls,
  undefinedName,
  noDupImpls,
  badCapture,
  invalidNum,
  noCandidateMeths,
  callTypeError,
  bothTExpectedGens,
  sealedCreation,
  undefinedMethod,
  noSubTypingRelationship,
  uncallableMeths,
  incompatibleMdfs,
  mutCapturesHyg,
  ioError,
  fsError,
  invalidEntryPoint,
  ignoredIdentInExpr,
  multipleIsoUsage,
  noMdfInFormalParams,
  privateMethCall,
  privateTraitImplementation,
  mustProvideImplsIfMdfProvided,
  namedTopLevelLambda,
  couldNotInferCallGenerics,
  incompatibleGenerics,
  xTypeError,
  lambdaTypeError,
  conflictingDecls,
  freeGensInLambda,
  invalidLambdaNameMdfBounds,
  mismatchedMethodGens,
  syntaxError,
  specialPackageConflict,
  lambdaImplementsGeneric,
  invalidLambdaMdf;
  private static final ErrorCode[] values = values();
  int code() {
    return this.ordinal() + 1;
  }
  static ErrorCode fromCode(int code) {
    return values[code - 1];
  }
}
