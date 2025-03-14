package failure;

//only add to the bottom
public enum ErrorCode {
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
  conflictingSealedImpl,
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
  noMdfInImplementedT,
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
  reservedPackageName,
  lambdaImplementsGeneric,
  invalidLambdaMdf,
  Unknown,
  noMethOnX,
  invalidMethodArgumentTypes,
  crossPackageDeclaration,
  genericMismatch,
  inferImplementsFailed,
  noUnimplementedMethods,
  invalidStr;
  private static final ErrorCode[] values = values();
  public int code() {
    return this.ordinal() + 1;
  }
  public static ErrorCode fromCode(int code) {
    return values[code - 1];
  }
}
