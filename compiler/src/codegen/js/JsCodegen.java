package codegen.js;

import codegen.MIR;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  private final JsMagicImpls magic;
  private MIR.Package pkg;
  private HashMap<Id.DecId, String> freshClasses;

  /*
   * To add as runtime code files:
   * `class Unreachable extends Error { constructor() { super("Unreachable code"); } }`
   */

  public JsCodegen(MIR.Program p) {
    this.p = p;
    this.magic = new JsMagicImpls(this, p.p());
  }

  public String visitProgram(Id.DecId entry) {
    var pkgs = p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"));

    return pkgs;
  }

  public String visitPackage(MIR.Package pkg) {
    this.pkg = pkg;
    this.freshClasses = new HashMap<>();
    var singletons = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def))
      .collect(Collectors.joining("\n"));
    var funs = pkg.funs().stream()
      .map(this::visitFun)
      .collect(Collectors.joining("\n"));

    return String.join("\n", freshClasses.values())+"\n"+singletons+funs;
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }

    if (def.singletonInstance().isEmpty()) {
      return "";
    }
    visitCreateObjNoSingleton(def.singletonInstance().get(), true);
    return "const "+getName(def.name())+"Impl = new "+getName(def.name())+"();";
  }

  public String visitMeth(MIR.Meth meth) {
    var methName = name(getName(meth.sig().mdf(), meth.sig().name()));
    var args = meth.sig().xs().stream()
      .map(x->this.visitX(x, true))
      .collect(Collectors.joining(", "));
    var selfArg = meth.capturesSelf() ? Stream.of("this") : Stream.<String>of();
    var funArgs = Streams.of(
      meth.sig().xs().stream().map(x->this.visitX(x, true)),
      selfArg,
      meth.captures().stream().map(this::name).map(x->"this."+x)
      ).collect(Collectors.joining(", "));
    return """
      %s(%s) { return %s(%s); }
      """.formatted(methName, args, getName(meth.fName().orElseThrow()), funArgs);
  }

  public String visitFun(MIR.Fun fun) {
    var name = getName(fun.name());
    var args = fun.args().stream()
      .map(x->visitX(x, true))
      .collect(Collectors.joining(", "));
    var body = fun.body().accept(this, true);

    return """
      function %s(%s) {
        return %s;
      }
      """.formatted(name, args, body);
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      var res = magicImpl.get().instantiate();
      if (res.isPresent()) { return res.get(); }
    }

    var id = createObj.concreteT().id();
    if (p.of(id).singletonInstance().isPresent()) {
      return getName(id)+"Impl";
    }
    return visitCreateObjNoSingleton(createObj, checkMagic);
  }

  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
    var id = createObj.concreteT().id();
    var className = getName(id);
    if (!this.freshClasses.containsKey(id)) {
      var capturesArgs = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
      var capturesAsFields = createObj.captures().stream()
        .map(x->visitX(x, checkMagic))
        .map(x->"this."+x+" = "+x+";")
        .collect(Collectors.joining("\n"));
      var ms = createObj.meths().stream()
        .map(this::visitMeth)
        .collect(Collectors.joining("\n"));

      var constructor = createObj.captures().isEmpty() ? "" : """
        constructor(%s) {
            %s
        }
        """.formatted(capturesArgs, capturesAsFields);

      this.freshClasses.put(id, """
        class %s {
          %s
          %s
        }
        """.formatted(className, constructor, ms));
    }
    var captures = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
    return "new "+className+"("+captures+")";
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
    return name(x.name());
  }

  @Override public String visitMCall(MIR.MCall call, boolean checkMagic) {
    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return impl.get(); }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return impl.get(); }
    }

    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return "%s.%s(%s)".formatted(call.recv().accept(this, checkMagic), name(getName(call.mdf(), call.name())), args);
  }

  private String name(String x) {
    return x.equals("this")
      ? "f$thiz"
      : x.replace("'", "$"+(int)'\'').replace("$", "$"+(int)'$')+"$";
  }

  public String getName(MIR.FName name) {
    var capturesSelf = name.capturesSelf() ? "selfCap" : "noSelfCap";
    return getName(name.d())+"$"+name(getName(name.mdf(), name.m()))+"$"+capturesSelf;
  }
  public String getName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> throw Bug.unreachable();
      case MIR.MT.Plain plain -> getName(plain.id());
      case MIR.MT.Usual usual -> getName(usual.it().name());
    };
  }
  public String getName(Id.DecId d) {
    return getPkgName(d.pkg())+"$"+getBase(d.shortName())+"_"+d.gen();
  }
  public String getPkgName(String pkg) {
    return pkg.replace(".", "$"+(int)'.');
  }
  public String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"_"+m.num()+"_"+mdf; }

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
