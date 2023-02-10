package codegen.java;

import ast.T;
import codegen.MIR;
import codegen.MIRInjectionVisitor;
import id.Id;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor<String> {
  public String visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    var entryName = getName(entry);
    var init = "\npublic static void main(String[] args){ base.Main_0 entry = new "+entryName+"(){}; entry.$35$(); }\n";

    return "interface FProgram{" + pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .collect(Collectors.joining("\n"))+init+"}";
  }
  public String visitPackage(String pkg, List<MIR.Trait> ds) {
    return "interface "+pkg+"{" + ds.stream()
      .map(t->visitTrait(pkg, t))
      .collect(Collectors.joining("\n")) + "\n}";
  }
  public String visitTrait(String pkg, MIR.Trait trait) {
    var shortName = trait.name();
    if (trait.name().startsWith(pkg)) { shortName = trait.name().substring(pkg.length()+1); }
    var gens = trait.gens().isEmpty() ? "" : "<"+String.join(",", trait.gens())+">";
    var its = trait.its().stream().filter(tr->!tr.equals(trait.name())).collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+gens+impls+"{\n";
    var singletonGet = trait.canSingleton() ? trait.name()+" _$self = new "+ trait.name()+"(){};" : "";
    return start + singletonGet + trait.meths().stream()
      .map(m->visitMeth(m, "this", false))
      .collect(Collectors.joining("\n")) + "}";
  }
  public String visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    var selfVar = "var "+name(selfName)+" = this;\n";
    var gens = meth.gens().isEmpty() ? "" : "<"+String.join(",", meth.gens())+"> ";
    var args = meth.xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var visibility = concrete ? "public " : "default ";
    if (meth.isAbs()) { visibility = ""; }
    var start = visibility+gens+meth.rt()+" "+name(meth.name())+"("+args+")";
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
      .map(m->visitMeth(m, l.selfName(), true))
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
  private static String getName(Id.GX<T> gx) { return getBase(gx.name()); }
  private static String getName(Id.IT<T> it) { return getName(it.name()); }
  private static String getName(Id.DecId d) { return getBase(d.name())+"_"+d.gen(); }
  private static String getName(Id.MethName m) { return getBase(m.name()); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c)) { return Character.toString(c); }
      return "$"+c;
    }).collect(Collectors.joining());
  }
}
