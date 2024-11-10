package codegen.java.types;

import codegen.MIR;
import codegen.java.JavaCodegenState;
import codegen.java.JavaTarget;
import id.Id;
import utils.Bug;


public record Standard(JavaTarget target) implements JavaCodegenType {
  @Override public boolean canHandle(MIR.MT exprType) {
    return true;
  }

  @Override public void instantiate(MIR.MT t, JavaCodegenState state) {
    throw Bug.todo();
  }

  @Override public void instantiateNoSingleton(MIR.MT t, MIR.CreateObj k, JavaCodegenState state) {
    var name= k.concreteT().id();
    var recordName= target.id.getSimpleName(name)+"Impl";
    addFreshRecord(k, name, recordName, state);
//    var captures= seq(createObj.captures(),x->visitX(x, checkMagic),", ");
//    return "new "+recordName+"("+captures+")";
    throw Bug.todo();
  }

  @Override public void call(MIR.MT t, MIR.MCall call, JavaCodegenState state) {
    throw Bug.todo();
  }

  @Override public void type(MIR.MT t, JavaCodegenState state) {
    throw Bug.todo();
  }

  private void addFreshRecord(MIR.CreateObj k, Id.DecId name, String recordName, JavaCodegenState state) {
//    if(state.records().containsKey(name)){ return; }
////    assert !this.freshRecords.containsKey(name):
////      "current "+name+" in \n"+this.freshRecords.keySet();
//    var leastSpecific = ParentWalker.leastSpecificSigs(target.p(), target.p().of(name));
//    k.captures().stream()
//      .map(this::)
//    var args = seq(k.captures(), this::typePair, ", ");
//    var ms = seq(k.meths(), m->this.visitMeth(m, MethExprKind.Kind.RealExpr, leastSpecific),"\n");
//    var unreachableMs = seq(
//      k.unreachableMs(),
//      m->this.visitMeth(m, MethExprKind.Kind.Unreachable, leastSpecific),"\n"
//    );
//    this.freshRecords.put(name, """
//      public record %s(%s) implements %s {
//        %s
//        %s
//      }
//      """.formatted(
//      recordName, args, id.getFullName(name), ms, unreachableMs));
    throw Bug.todo();
  }
}
