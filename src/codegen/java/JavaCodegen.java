package codegen.java;

import ast.Program;
import ast.T;
import codegen.MIR;
import id.Id;
import magic.Magic;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor<String> {
  private final MagicImpls magic;
  private Program p;
  public JavaCodegen(Program p) {
    this.magic = new MagicImpls(this, p);
  }

  static String argsToLList() {
    return """
      FAux.LAUNCH_ARGS = new base.LList_1(){};
      for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$(arg); }
      """;
  }

  public String visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    assert pkgs.containsKey("base");
    var entryName = getName(entry);
    var init = "\nstatic void main(String[] args){ "+argsToLList()+" base.Main_0 entry = new "+entryName+"(){}; entry.$35$(new base$46caps.$95System_0(){}); }\n";

    return "class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }\ninterface FProgram{" + pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .collect(Collectors.joining("\n"))+init+"}";
  }
  public String visitPackage(String pkg, List<MIR.Trait> ds) {
    return "interface "+getPkgName(pkg)+"{" + ds.stream()
      .map(t->visitTrait(pkg, t))
      .collect(Collectors.joining("\n")) + "\n}";
  }
  public String visitTrait(String pkg, MIR.Trait trait) {
    if (pkg.equals("base") && trait.name().name().endsWith("Instance")) {
      return "";
    }

    var longName = getName(trait.name());
    var shortName = longName;
    if (trait.name().pkg().equals(pkg)) { shortName = getBase(trait.name().shortName())+"_"+trait.name().gen(); }
//    var gens = trait.gens().isEmpty() ? "" : "<"+String.join(",", trait.gens())+">";
    var its = trait.its().stream()
      .map(this::getName)
      .filter(tr->!tr.equals(longName))
      .distinct()
      .collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+impls+"{\n";
    var singletonGet = "";
    if (trait.canSingleton()) {
      singletonGet = """
        %s _$self = new %s(){};
        """.formatted(longName, longName);
    }

    return start + singletonGet + trait.meths().stream()
      .map(m->visitMeth(m, "this", false))
      .collect(Collectors.joining("\n")) + "}";
  }
  public String visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    var selfVar = "var "+name(selfName)+" = this;\n";
//    var gens = meth.gens().isEmpty() ? "" : "<"+String.join(",", meth.gens())+"> ";
    var args = meth.xs().stream()
      .map(x->new MIR.X(x.name(), new T(x.t().mdf(), new Id.GX<>("Object")))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var visibility = concrete ? "public " : "default ";
    if (meth.isAbs()) { visibility = ""; }
    var start = visibility+getRetName(meth.rt())+" "+name(getName(meth.name()))+"("+args+")";
    if (meth.isAbs()) { return start + ";"; }
    return start + "{\n"+selfVar+"return (("+getName(meth.rt())+")("+meth.body().get().accept(this)+"));\n}";
  }

  public String visitX(MIR.X x, boolean checkMagic) {
    return "(("+getName(x.t())+")("+name(x.name())+"))";
  }

  public String visitMCall(MIR.MCall mCall, boolean checkMagic) {
    var magicImpl = magic.get(mCall.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(mCall.name(), mCall.args(), Map.of());
      if (impl.isPresent()) { return impl.get(); }
    }

    var magicRecv = !(mCall.recv() instanceof MIR.Lambda);
    var start = "(("+getRetName(mCall.t())+")"+mCall.recv().accept(this, magicRecv)+"."+name(getName(mCall.name()))+"(";
    var args = mCall.args().stream()
      .map(a->a.accept(this))
      .collect(Collectors.joining(","));
    return start+args+"))";
  }

  public String visitLambda(MIR.Lambda l) {
    return visitLambda(l, true);
  }
  public String visitLambda(MIR.Lambda l, boolean checkMagic) {
    var magicImpl = magic.get(l);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    if (l.canSingleton()) {
      return getName(l.freshName())+"._$self";
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
    return x.equals("this") ? "f$thiz" : x.replace("'", "$"+(int)'\'')+"$";
  }
  private List<String> getImplsNames(List<Id.IT<T>> its) {
    return its.stream()
      .map(this::getName)
      .toList();
  }
  private String getName(T t) { return t.match(this::getName, this::getName); }
  private String getRetName(T t) { return t.match(this::getName, it->getName(it, true)); }
  private String getName(Id.GX<T> gx) { return "Object"; }
  private String getName(Id.IT<T> it) { return getName(it, false); }
  private String getName(Id.IT<T> it, boolean isRet) {
    return switch (it.name().name()) {
      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
      case "base.Float" -> isRet ? "Double" : "double";
      case "base.Str" -> "String";
      default -> {
        if (magic.isMagic(Magic.Int, it.name())) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.UInt, it.name())) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Str, it.name())) { yield "String"; }
        yield getName(it.name());
      }
    };
  }
  private static String getPkgName(String pkg) {
    return pkg.replace(".", "$"+(int)'.');
  }
  protected static String getName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"."+getBase(d.shortName())+"_"+d.gen();
  }
  private static String getName(Id.MethName m) { return getBase(m.name()); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "$"+c;
    }).collect(Collectors.joining());
  }
}
