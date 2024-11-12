package codegen.java.types;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.Seq;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import utils.Bug;
import utils.Streams;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static codegen.MethExprKind.Kind.Delegate;
import static codegen.MethExprKind.Kind.Unreachable;


public record Standard(JavaTarget target) implements JavaCodegenType {
  @Override public boolean canHandle(MIR.MT exprType) {
    return true;
  }

  @Override public void instantiate(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    var objId = k.concreteT().id();
    var singleton = target.p().of(objId).singletonInstance().isPresent();
    if (!singleton) {
      instantiateNoSingleton(t, k, state);
      return;
    }
    state.buffer()
      .append(target.id.getFullName(objId))
      .append(".$self");
  }

  @Override public void instantiateNoSingleton(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    var name= k.concreteT().id();
    var recordName = target.id.getSimpleName(name)+"Impl";
    addFreshRecord(k, name, recordName, state);
    state.buffer().append("new ").append(recordName).append("(");
    Seq.of(state.buffer(), k.captures(), x->target.visitX(x, true));
    state.buffer().append(")");
  }

  @Override public void call(MIR.MT t, MIR.MCall call, JavaCodegenState state) {
    var mustCast = !call.t().equals(call.originalRet());
    if (mustCast) {
      state.buffer().append("((");
      target.getHandler(call.t()).type(call.t(), state);
      state.buffer().append(")");
    }
    target.withState(state, target_->{
      call.recv().accept(target_, true);
      state.buffer()
        .append(".")
        .append(target_.id.getMName(call.mdf(), call.name()))
        .append("(");
      Seq.of(state.buffer(), call.args(), e->e.accept(target_, true));
      state.buffer().append(")");
    });
    if (mustCast) {
      state.buffer().append(")");
    }
  }

  @Override public void type(MIR.MT t, JavaCodegenState state) {
    state.buffer().append(switch (t) {
      case MIR.MT.Any _ -> "Object";
      case MIR.MT.Plain plain -> target.id.getFullName(plain.id());
      case MIR.MT.Usual usual -> target.id.getFullName(usual.it().name());
    });
  }

  private void addFreshRecord(MIR.CreateObj k, Id.DecId name, String recordName, JavaCodegenState state) {
    if(state.records().containsKey(name)){ return; }
    var record = new StringBuilder();
//    assert !this.freshRecords.containsKey(name):
//      "current "+name+" in \n"+this.freshRecords.keySet();
    var leastSpecific = ParentWalker.leastSpecificSigs(target.p(), target.p().of(name));
    record.append("public record ").append(recordName).append("(");
    Seq.of(record, k.captures(), x->target.writeTypePair(record, x));
    record.append(") implements ").append(target.id.getFullName(name)).append(" {\n");
    Seq.of(record, "\n", k.meths(), m->visitMeth(record, m, MethExprKind.Kind.RealExpr, leastSpecific));
    Seq.of(record, "\n", k.unreachableMs(), m->visitMeth(record, m, MethExprKind.Kind.Unreachable, leastSpecific));
    record.append("}\n");
    state.records().put(name, record.toString());
  }

  public void visitMeth(StringBuilder buffer, MIR.Meth meth, MethExprKind kind, Map<Id.MethName, MIR.Sig> leastSpecific) {
    var overriddenSig = target.overriddenSig(meth.sig(), leastSpecific);
    var toSkip = overriddenSig.isPresent();
    var deleMeth = meth;
    if (toSkip){
      deleMeth = meth.withSig(overriddenSig.get());
      var canSkip = kind.kind() == Unreachable;
      if (canSkip) {
        visitMeth(buffer, deleMeth, Unreachable, Map.of());
        return;
      }
      var d = new MethExprKind.Delegator(meth.sig(), deleMeth.sig());
      // Delegator
      visitMeth(buffer, deleMeth, d, Map.of());
      buffer.append("\n");
      // Delegate
      visitMeth(buffer, meth, Delegate, Map.of());
      buffer.append("\n");
      return;
    }

    var nameSuffix = kind.kind() == Delegate ? "$Delegate" : "";
    var methName = target.id.getMName(meth.sig().mdf(), meth.sig().name())+nameSuffix;


    var ret = meth.sig().rt();
    buffer.append("public ");
    target.getHandler(ret).boxedType(ret, JavaCodegenState.bufferOnly(buffer));
    buffer.append(" ").append(methName).append("(");
    Seq.of(buffer, meth.sig().xs(), x->target.writeTypePair(buffer, x));
    buffer.append(") {\n");
    switch (kind) {
      case MethExprKind.Kind kind_ -> writeMethBody(buffer, meth, kind_);
      case MethExprKind.Delegator delegator -> {
        buffer.append("return this.").append(methName).append("$Delegate(");
        Seq.of(buffer, delegator.xs().toList(), x->writeCastedX(buffer, x));
        buffer.append(");\n");
      }
    }
    buffer.append("}\n");
  }
  private void writeMethBody(StringBuilder buffer, MIR.Meth meth, MethExprKind.Kind kind) {
    switch (kind) {
      case Unreachable -> buffer.append("throw new RuntimeException(\"Unreachable code\");");
      case Delegator -> throw Bug.unreachable();
      case RealExpr, Delegate -> {
        buffer.append("return ");
        var mustCast = meth.fName().isPresent()
          && target.funMap.get(meth.fName().get()).ret() instanceof MIR.MT.Any
          && !(meth.sig().rt() instanceof MIR.MT.Any);
        if (mustCast) {
          var ret = meth.sig().rt();
          buffer.append("(");
          target.getHandler(ret).boxedType(ret, JavaCodegenState.bufferOnly(buffer));
          buffer.append(") ");
        }
        buffer
          .append(target.id.getFullName(meth.origin()))
          .append(".")
          .append(target.id.getFName(meth.fName().orElseThrow()));
        buffer.append("(");
        var funArgs = Streams.of(
          meth.sig().xs().stream().map(MIR.X::name).map(target.id::varName),
          Stream.of("this"),
          meth.captures().stream().map(target.id::varName).map(x->"this."+x)
        ).collect(Collectors.joining(", "));
        buffer.append(funArgs);
        buffer.append(");");
      }
    }
  }

  private void writeCastedX(StringBuilder buffer, MIR.X x){
    buffer.append("(");
    target.getHandler(x.t()).type(x.t(), JavaCodegenState.bufferOnly(buffer));
    buffer.append(") ").append(target.id.varName(x.name()));
  }
}
