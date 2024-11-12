package codegen.java;

import codegen.CodegenTarget;
import codegen.MIR;
import codegen.ParentWalker;
import codegen.Seq;
import codegen.java.types.Debug;
import codegen.java.types.JavaCodegenType;
import codegen.java.types.Standard;
import id.Id;
import utils.Bug;
import utils.Streams;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class JavaTarget implements CodegenTarget<JavaCodegenState> {
  public final StringIds id = new StringIds();
  public final Map<MIR.FName, MIR.Fun> funMap;
  private final MIR.Program p;
  private final List<JavaCodegenType> typeHandlers;
  private final Standard standard;
  private JavaCodegenState state = new JavaCodegenState();
  private String pkg = "";

  public JavaTarget(MIR.Program p) {
    this.funMap = p.pkgs().stream()
      .flatMap(pkg->pkg.funs().stream())
      .collect(Collectors.toMap(MIR.Fun::name, f->f));
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
      state.buffer().append(shortName).append(" $self = ");
      handler.instantiateNoSingleton(objK.t(), objK, state);
      state.buffer().append(";\n");
    });
    var leastSpecific = ParentWalker.leastSpecificSigs(p, def);
    Seq.of(state.buffer(), "\n", def.sigs(), sig->visitSig(sig, leastSpecific));
    Seq.of(state.buffer(), "\n", funs, this::visitFun);
    state.buffer().append("}\n");
  }

  public void visitSig(MIR.Sig sig, Map<Id.MethName, MIR.Sig> leastSpecific) {
    // If params are different in my parent, we need to objectify
    var overriddenSig= this.overriddenSig(sig, leastSpecific);
    if (overriddenSig.isPresent()) {
      visitSig(overriddenSig.get(), Map.of());
      return;
    }

    getHandler(sig.rt()).boxedType(sig.rt(), state);
    state.buffer().append(" ");
    state.buffer().append(id.getMName(sig.mdf(), sig.name()));
    state.buffer().append("(");
    Seq.of(state.buffer(), sig.xs(), x->writeTypePair(state.buffer(), x));
    state.buffer().append(");\n");
  }

  public void visitFun(MIR.Fun fun) {
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

  public void writeTypePair(StringBuilder buffer, MIR.X x) {
    getHandler(x.t()).type(x.t(), new JavaCodegenState(state.pkg(), state.records(), buffer));
    buffer.append(" ").append(id.varName(x.name()));
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

  public Optional<MIR.Sig> overriddenSig(MIR.Sig sig, Map<Id.MethName, MIR.Sig> leastSpecific) {
    var leastSpecificSig = leastSpecific.get(sig.name());
    if (leastSpecificSig != null && Streams.zip(sig.xs(),leastSpecificSig.xs()).anyMatch((a, b)->!a.t().equals(b.t()))) {
      return Optional.of(leastSpecificSig.withRT(sig.rt()));
    }
    return Optional.empty();
  }

  private final Map<MIR.MT, JavaCodegenType> handlerCache = new WeakHashMap<>();
  @Override public JavaCodegenType getHandler(MIR.MT t) {
    return handlerCache.computeIfAbsent(
      t,
      ti->(JavaCodegenType)CodegenTarget.super.getHandler(ti)
    );
  }
}
