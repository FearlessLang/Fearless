package codegen.java;

import codegen.MIR;
import visitors.MIRVisitor;

import java.util.Map;
import java.util.stream.Collectors;

public class JavaCodegen implements MIRVisitor<String> {
  public String visitPackage(String pkg, Map<String, MIR.Trait> ds) {
    return "package "+pkg+";\n" + ds.entrySet().stream()
      .map(kv->{
        var name = kv.getKey();
        if (name.startsWith(pkg)) { name = name.substring(pkg.length()+1); }
        return visitTrait(name, kv.getValue());
      })
      .collect(Collectors.joining("\n"));
  }
  public String visitTrait(String name, MIR.Trait trait) {
    var gens = trait.gens().isEmpty() ? "" : "<"+String.join(",", trait.gens())+">";
    var impls = trait.impls().isEmpty() ? "" : " extends " + String.join(",", trait.impls());
    var start = "interface "+name+gens+impls+"{\n";
    var singletonGet = trait.canSingleton() ? name+" _$self = new "+name+"(){};" : "";
    return start + singletonGet + trait.meths().entrySet().stream()
      .map(kv->visitMeth(kv.getKey(), kv.getValue()))
      .collect(Collectors.joining("\n")) + "}";
  }
  public String visitMeth(String name, MIR.Meth meth) {
    var gens = meth.gens().isEmpty() ? "" : "<"+String.join(",", meth.gens())+"> ";
    var args = meth.xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var start = "public "+gens+meth.rt()+" "+name+"("+args+")";
    if (meth.body().isEmpty()) { return start + ";"; }
    return start + "{\nreturn "+meth.body().get().accept(this)+";\n}";
  }

  public String visitX(MIR.X x) {
    return x.name();
  }

  public String visitMCall(MIR.MCall mCall) {
    var start = mCall.recv().accept(this)+"."+mCall.name()+"(";
    var args = mCall.args().stream()
      .map(a->a.accept(this))
      .collect(Collectors.joining(","));
    return start+args+")";
  }

  public String visitNewLambda(MIR.NewLambda newL) {
    return visitNewDynLambda(new MIR.NewDynLambda(newL.mdf(), newL.name(), newL.captures()));
  }

  public String visitNewDynLambda(MIR.NewDynLambda newL) {
    var start = "new "+newL.name()+"(){";
    var captures = newL.captures().stream()
        .map(c->"public _$capture_"+c.name()+"() { return "+c.name()+"; }")
        .collect(Collectors.joining("\n"));
    return start + captures + "}";
  }

  public String visitNewStaticLambda(MIR.NewStaticLambda newL) {
    return newL.name()+"._$self";
  }

  public String visitShare(MIR.Share s) {
    return s.e().accept(this);
  }

  private String typePair(MIR.X x) {
    return x.type()+" "+x.name();
  }
}
