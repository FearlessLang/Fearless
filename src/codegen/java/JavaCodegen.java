package codegen.java;

import ast.Program;
import ast.T;
import codegen.MIR;
import id.Id;
import magic.MagicTrait;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor<String> {
  private final MagicImpls magic;
  public JavaCodegen(Program p) {
    this.magic = new MagicImpls(this, p);
  }

  public String visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    if (!pkgs.containsKey("base")) {
      throw Bug.todo();
    }
    var entryName = getName(entry);
    var init = "\nstatic void main(String[] args){ base.Main_1 entry = new "+entryName+"(){}; entry.$35$(new base.System_1(){}); }\n";

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
    if (pkg.equals("base") && trait.name().name().endsWith("Instance")) {
      return "";
    }

    var longName = getName(trait.name());
    var shortName = longName;
    if (trait.name().pkg().equals(pkg)) { shortName = longName.substring(pkg.length()+1); }
//    var gens = trait.gens().isEmpty() ? "" : "<"+String.join(",", trait.gens())+">";
    var its = trait.its().stream()
      .map(JavaCodegen::getName)
      .filter(tr->!tr.equals(longName))
      .collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+impls+"{\n";
    var singletonGet = trait.canSingleton() ? longName+" _$self = new "+ longName+"(){};" : "";
    return start + singletonGet + trait.meths().stream()
      .map(m->visitMeth(m, "this", false))
      .collect(Collectors.joining("\n")) + "}";
  }
  public String visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    var selfVar = "var "+name(selfName)+" = this;\n";
//    var gens = meth.gens().isEmpty() ? "" : "<"+String.join(",", meth.gens())+"> ";
    var args = meth.xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var visibility = concrete ? "public " : "default ";
    if (meth.isAbs()) { visibility = ""; }
    var start = visibility+getName(meth.rt())+" "+name(getName(meth.name()))+"("+args+")";
    if (meth.body().isEmpty()) { return start + ";"; }
    return start + "{\n"+selfVar+"return (("+getName(meth.rt())+")("+meth.body().get().accept(this)+"));\n}";
  }

  public String visitX(MIR.X x, boolean checkMagic) {
    return name(x.name());
  }

  public String visitMCall(MIR.MCall mCall, boolean checkMagic) {
    var magicImpl = magic.get(mCall.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(mCall.name(), mCall.args(), Map.of());
      if (impl.isPresent()) { return impl.get(); }
    }

    var start = mCall.recv().accept(this, false)+"."+name(getName(mCall.name()))+"(";
    var args = mCall.args().stream()
      .map(a->a.accept(this))
      .collect(Collectors.joining(","));
    return start+args+")";
  }

  public String visitLambda(MIR.Lambda l) {
    return visitLambda(l, true);
  }
  public String visitLambda(MIR.Lambda l, boolean checkMagic) {
    var magicImpl = magic.get(l);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    var start = "new "+getName(l.freshName())+"(){\n";
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
    return getName(x.t())+" "+name(x.name());
  }
  private String name(String x) {
    return x.equals("this") ? "f$thiz" : x+"$";
  }
  private static List<String> getImplsNames(List<Id.IT<T>> its) {
    return its.stream()
      .map(JavaCodegen::getName)
      .toList();
  }
  private static String getName(T t) { return t.match(JavaCodegen::getName, JavaCodegen::getName); }
  private static String getName(Id.GX<T> gx) { return "Object"; }
  private static String getName(Id.IT<T> it) {
    return switch (it.name().name()) {
      case "base.Int" -> "Long";
      case "base.UInt" -> "Long";
      case "base.Float" -> "Double";
      case "base.Str" -> "String";
      default -> getName(it.name());
    };
  }
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
