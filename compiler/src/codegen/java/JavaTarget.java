package codegen.java;

import codegen.CodegenTarget;
import codegen.CodegenType;
import codegen.MIR;
import codegen.ParentWalker;
import codegen.java.types.Debug;
import codegen.java.types.JavaCodegenType;
import codegen.java.types.Standard;
import utils.Bug;

import java.util.List;
import java.util.stream.Collectors;

public class JavaTarget implements CodegenTarget<JavaCodegenState> {
  public final StringIds id = new StringIds();
  private final MIR.Program p;
  private final List<JavaCodegenType> typeHandlers;
  private final Standard standard;
  private JavaCodegenState state = new JavaCodegenState();
  private String pkg = "";

  public JavaTarget(MIR.Program p) {
    this.p = p;
    this.standard = new Standard(this);
    this.typeHandlers = List.of(
      new Debug(this),
      standard
    );
  }

  public JavaCodegenState consume() {
    var state = this.state;
    this.state = null;
    return state;
  }

  @Override public List<JavaCodegenType> typeHandlers() {
    return this.typeHandlers;
  }
  @Override public Standard standardType() {
    return this.standard;
  }
  @Override public MIR.Program p() {
    return this.p;
  }

  public void visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    this.pkg = pkg;
    this.state = new JavaCodegenState(pkg, this.state.records(), new StringBuilder());

    var isMagicTemplateType = pkg.equals("base") && def.name().name().endsWith("Instance");
    if (isMagicTemplateType || id.isLiteral(def.name().name())) { return; }
    var fullName = id.getFullName(def.name());
    var shortName = id.getSimpleName(def.name());

    state.buffer().append("public interface ");
    state.buffer().append(shortName);
    writeImplsStr(def,fullName);
    state.buffer().append(" {\n");
    def.singletonInstance().ifPresent(objK->{
      var handler = getHandler(objK.t());
      state.buffer().append(shortName);
      state.buffer().append(" $self = ");
      handler.instantiateNoSingleton(objK.t(), objK, state);
      state.buffer().append(";\n");
    });
    var leastSpecific = ParentWalker.leastSpecificSigs(p, def);
    // TODO: sigs
    // TODO: Static funs
    state.buffer().append("}\n");

    throw Bug.todo();
  }

  @Override public Void visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    getHandler(createObj.t()).instantiate(createObj.t(), state);
    return null;
  }

  @Override public Void visitX(MIR.X x, boolean checkMagic) {
    state.buffer().append(id.varName(x.name()));
    return null;
  }

  @Override public Void visitMCall(MIR.MCall call, boolean checkMagic) {
    getHandler(call.recv().t()).call(call.recv().t(), call, state);
    return null;
  }

  private void writeImplsStr(MIR.TypeDef def, String fullName) {
    var its = def.impls().stream()
      .map(MIR.MT.Plain::id)
      .filter(decId->!id.isLiteral(decId.name()))
      .map(id::getFullName)
      .filter(tr->!tr.equals(fullName))
      .distinct()
      .sorted()
      .collect(Collectors.joining(","));
    var res = its.isEmpty() ? "" : " extends "+its;
    state.buffer().append(res);
  }

  @Override public JavaCodegenType getHandler(MIR.MT t) {
    return (JavaCodegenType) CodegenTarget.super.getHandler(t);
  }
}
