package codegen.java;

import codegen.MIR;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.Map;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor<String> {
  public String visitPackage(String pkg, Map<String, MIR.Trait> ds) {
    return "interface "+pkg+"{" + ds.entrySet().stream()
      .map(kv->visitTrait(pkg, kv.getKey(), kv.getValue()))
      .collect(Collectors.joining("\n")) + "\n}";
  }
  public String visitTrait(String pkg, String name, MIR.Trait trait) {
    var shortName = name;
    if (name.startsWith(pkg)) { shortName = name.substring(pkg.length()+1); }
    var gens = trait.gens().isEmpty() ? "" : "<"+String.join(",", trait.gens())+">";
    var its = trait.its().stream().filter(tr->!tr.equals(name)).collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+gens+impls+"{\n";
    var singletonGet = trait.canSingleton() ? name+" _$self = new "+name+"(){};" : "";
    return start + singletonGet + trait.meths().entrySet().stream()
      .map(kv->visitMeth(kv.getKey(), kv.getValue(), "this", false))
      .collect(Collectors.joining("\n")) + "}";
  }
  public String visitMeth(String name, MIR.Meth meth, String selfName, boolean concrete) {
    var selfVar = "var "+name(selfName)+" = this;\n";
    var gens = meth.gens().isEmpty() ? "" : "<"+String.join(",", meth.gens())+"> ";
    var args = meth.xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var visibility = concrete ? "public " : "default ";
    if (meth.isAbs()) { visibility = ""; }
    var start = visibility+gens+meth.rt()+" "+name(name)+"("+args+")";
    if (meth.body().isEmpty()) { return start + ";"; }
    return start + "{\n"+selfVar+"return "+meth.body().get().accept(this)+";\n}";
  }

  public String visitX(MIR.X x) {
    return name(x.name());
  }

  public String visitMCall(MIR.MCall mCall) {
    var start = mCall.recv().accept(this)+"."+name(mCall.name())+"(";
    var args = mCall.args().stream()
      .map(a->a.accept(this))
      .collect(Collectors.joining(","));
    return start+args+")";
  }

  public String visitLambda(MIR.Lambda l) {
    var start = "new "+l.freshName()+"(){\n";
//    var captures = l.captures().stream()
//      .map(c->"public _$capture_"+c.name()+"() { return "+c.name()+"; }")
//      .collect(Collectors.joining("\n"));
    var ms = l.meths().stream()
      .map(m->visitMeth(m.name(), m, l.selfName(), true))
      .collect(Collectors.joining("\n"));
    return start + ms + "}";
  }

//  public String visitNewLambda(MIR.NewLambda newL) {
//    return visitNewDynLambda(new MIR.NewDynLambda(newL.mdf(), newL.name(), newL.captures()));
//  }
//
//  public String visitNewDynLambda(MIR.NewDynLambda newL) {
//    var start = "new "+newL.name()+"(){";
//    var captures = newL.captures().stream()
//        .map(c->"public _$capture_"+c.name()+"() { return "+c.name()+"; }")
//        .collect(Collectors.joining("\n"));
//    return start + captures + "}";
//  }
//
//  public String visitNewStaticLambda(MIR.NewStaticLambda newL) {
//    return newL.name()+"._$self";
//  }

  private String typePair(MIR.X x) {
    return x.type()+" "+name(x.name());
  }
  private String name(String x) {
    return x.equals("this") ? "f$thiz" : x+"$";
  }
}
