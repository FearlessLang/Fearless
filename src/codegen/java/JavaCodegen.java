package codegen.java;

import codegen.MIR;
import codegen.ParentWalker;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

public class JavaCodegen implements MIRVisitor<String> {
  protected MIR.Program p;
  private MagicImpls magic;
  private HashMap<Id.DecId, String> freshRecords;
  private String pkg;

  public JavaCodegen(MIR.Program p) {
    this.p = p;
    this.magic = new MagicImpls(this, p.p());
  }

  protected static String argsToLList(Mdf addMdf) {
    return """
      FAux.LAUNCH_ARGS = base.LList_1._$self;
      for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$%s$(arg); }
      """.formatted(addMdf);
  }

  public String visitProgram(Id.DecId entry) {
    var entryName = getName(entry);
    var systemImpl = getName(Magic.SystemImpl);
    var init = """
      static void main(String[] args){
        %s
        base.Main_0 entry = %s._$self;
        try {
          entry.$35$imm$(%s._$self);
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getLocalizedMessage());
          System.exit(1);
        }
      }
    """.formatted(
      argsToLList(Mdf.mut),
      entryName,
      systemImpl
    );

    final String fearlessError = """
      package userCode;
      class FearlessError extends RuntimeException {
        public FProgram.base.Info_0 info;
        public FearlessError(FProgram.base.Info_0 info) {
          super();
          this.info = info;
        }
        public String getMessage() { return this.info.str$imm$(); }
      }
      class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
      """;

    return fearlessError+"\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}";
  }

  public String visitPackage(MIR.Package pkg) {
    this.pkg = pkg.name();
    this.freshRecords = new HashMap<>();
    Map<Id.DecId, List<MIR.Fun>> funs = pkg.funs().stream().collect(Collectors.groupingBy(f->f.name().d()));
    var typeDefs = pkg.defs().values().stream()
      .map(def->visitTypeDef(pkg.name(), def, funs.getOrDefault(def.name(), List.of())))
      .collect(Collectors.joining("\n"));

    var freshRecords = String.join("\n", this.freshRecords.values());
    return "interface "+getPkgName(pkg.name())+"{"+typeDefs+"\n"+freshRecords+"\n}";
  }

  public String visitTypeDef(String pkg, MIR.TypeDef def, List<MIR.Fun> funs) {
    if (pkg.equals("base") && def.name().name().endsWith("Instance")) {
      return "";
    }
    if (getLiteral(p.p(), def.name()).isPresent()) {
      return "";
    }

    var longName = getName(def.name());
    var shortName = longName;
    if (def.name().pkg().equals(pkg)) { shortName = getBase(def.name().shortName())+"_"+def.name().gen(); }
    final var selfTypeName = shortName;

    var its = def.impls().stream()
      .map(MIR.MT.Plain::id)
      .filter(dec->getLiteral(p.p(), dec).isEmpty())
      .map(JavaCodegen::getName)
      .filter(tr->!tr.equals(longName))
      .distinct()
      .collect(Collectors.joining(","));
    var impls = its.isEmpty() ? "" : " extends "+its;
    var start = "interface "+shortName+impls+"{\n";
    var singletonGet = def.singletonInstance()
      .map(objK->{
        var instance = visitCreateObjNoSingleton(objK, true);
        return """
          %s _$self = %s;
          """.formatted(selfTypeName, instance);
      })
      .orElse("");

    var leastSpecific = ParentWalker.leastSpecificSigs(p, def);

    var sigs = def.sigs().stream()
      .map(sig->visitSig(sig, leastSpecific))
      .collect(Collectors.joining("\n"));

    var staticFuns = funs.stream()
      .map(this::visitFun)
      .collect(Collectors.joining("\n"));

    return start + singletonGet + sigs + staticFuns + "}";
  }

  public String visitSig(MIR.Sig sig, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    // If params are different in my parent, we need to objectify
    var overriddenSig = this.overriddenSig(sig, leastSpecific);
    if (overriddenSig.isPresent()) {
      return visitSig(overriddenSig.get(), Map.of());
    }

    var args = sig.xs().stream()
      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    return getRetName(sig.rt())+" "+name(getName(sig.mdf(), sig.name()))+"("+args+");";
  }

  public sealed interface MethExprKind {
    Kind kind();
    enum Kind implements MethExprKind {
      RealExpr, Delegate, Unreachable, Delegator;
      @Override public Kind kind() { return this; }
    }
    record Delegator(MIR.Sig original, MIR.Sig delegate) implements MethExprKind {
      public Stream<MIR.X> xs() {
        return Streams.zip(delegate.xs(), original.xs()).map((o,d)->new MIR.X(o.name(), d.t()));
      }
      @Override public Kind kind() {
        return Kind.Delegator;
      }
    }
  }
  public String visitMeth(MIR.Meth meth, MethExprKind kind, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    var overriddenSig = this.overriddenSig(meth.sig(), leastSpecific);
    if (overriddenSig.isPresent()) {
      var delegator = visitMeth(meth.withSig(overriddenSig.get()), new MethExprKind.Delegator(meth.sig(), overriddenSig.get()), Map.of());
      var delegate = visitMeth(meth, MethExprKind.Kind.Delegate, Map.of());
      return delegator+"\n"+delegate;
    }

    var methName = switch (kind.kind()) {
      case Delegate -> name(getName(meth.sig().mdf(), meth.sig().name()))+"$Delegate";
      default -> name(getName(meth.sig().mdf(), meth.sig().name()));
    };

//    var sigArgs = meth.sig().xs().stream()
//      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
//      .toList();
    var args = meth.sig().xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var selfArg = meth.capturesSelf() ? Stream.of("this") : Stream.<String>of();
    var funArgs = Streams.of(meth.sig().xs().stream().map(MIR.X::name).map(this::name), selfArg, meth.captures().stream().map(this::name).map(x->"this."+x))
      .collect(Collectors.joining(","));

    var realExpr = switch (kind) {
      case MethExprKind.Kind k -> switch (k.kind()) {
        case RealExpr, Delegate -> "return %s.%s(%s);".formatted(getName(meth.origin()), getName(meth.fName()), funArgs);
        case Unreachable -> "throw new Error(\"Unreachable code\");";
        case Delegator -> throw Bug.unreachable();
      };
      case MethExprKind.Delegator k -> "return (%s) this.%s(%s);".formatted(
        getRetName(meth.sig().rt()),
        methName,
        k.xs()
          .map(x->"("+getName(x.t())+") "+name(x.name()))
          .collect(Collectors.joining(", "))
        );
    };

    return """
      public %s %s(%s) {
        %s
      }
      """.formatted(
        getRetName(meth.sig().rt()),
        methName,
        args,
        realExpr);
  }

  public String visitFun(MIR.Fun fun) {
    var name = getName(fun.name());
    var args = fun.args().stream()
//      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf())))
      .map(this::typePair)
      .collect(Collectors.joining(", "));
    var body = fun.body().accept(this, true);

    return """
      static %s %s(%s) {
        return (%s) %s;
      }
      """.formatted(getRetName(fun.ret()), name, args, getRetName(fun.ret()), body);
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    var id = createObj.concreteT().id();
    if (p.of(id).singletonInstance().isPresent()) {
      return getName(id)+"._$self";
    }

    return visitCreateObjNoSingleton(createObj, checkMagic);
  }
  public String visitCreateObjNoSingleton(MIR.CreateObj createObj, boolean checkMagic) {
    var name = createObj.concreteT().id();
    var recordName = getSafeName(name)+"Impl"; // todo: should this include a pkg. in front?
    if (!this.freshRecords.containsKey(name)) {
      var leastSpecific = ParentWalker.leastSpecificSigs(p, p.of(name));
      var args = createObj.captures().stream().map(this::typePair).collect(Collectors.joining(", "));
      var ms = createObj.meths().stream()
        .map(m->this.visitMeth(m, MethExprKind.Kind.RealExpr, leastSpecific))
        .collect(Collectors.joining("\n"));
      var unreachableMs = createObj.unreachableMs().stream()
        .map(m->this.visitMeth(m, MethExprKind.Kind.RealExpr, leastSpecific))
        .collect(Collectors.joining("\n"));
      this.freshRecords.put(name, """
        record %s(%s) implements %s {
          %s
          %s
        }
        """.formatted(recordName, args, getName(name), ms, unreachableMs));
    }

    var captures = createObj.captures().stream().map(x->visitX(x, checkMagic)).collect(Collectors.joining(", "));
    return "new "+recordName+"("+captures+")";
  }

  @Override public String visitX(MIR.X x, boolean checkMagic) {
//    return switch (x.t()) {
//      case MIR.MT.Any ignored -> "(("+getName(x.t())+")("+name(x.name())+"))";
//      case MIR.MT.Plain ignored -> name(x.name());
//      case MIR.MT.Usual ignored -> name(x.name());
//    };
//    return "(("+getName(x.t())+")("+name(x.name())+"))";
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

    //    var magicRecv = !(call.recv() instanceof MIR.CreateObj) || magicImpl.isPresent();
    var start = "(("+getRetName(call.t())+")"+call.recv().accept(this, checkMagic)+"."+name(getName(call.mdf(), call.name()))+"(";
    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return start+args+"))";
  }

  private Optional<MIR.Sig> overriddenSig(MIR.Sig sig, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    var leastSpecificSig = leastSpecific.get(ParentWalker.FullMethId.of(sig));
    if (leastSpecificSig != null && Streams.zip(sig.xs(),leastSpecificSig.xs()).anyMatch((a,b)->!a.t().equals(b.t()))) {
      return Optional.of(leastSpecificSig.withRT(sig.rt()));
    }
    return Optional.empty();
  }

  private String typePair(MIR.X x) {
    return getName(x.t())+" "+name(x.name());
  }
  private String name(String x) {
    return x.equals("this") ? "f$thiz" : x.replace("'", "$"+(int)'\'')+"$";
  }
  private List<String> getImplsNames(List<MIR.MT.Plain> its) {
    return its.stream()
      .map(this::getName)
      .toList();
  }
  public String getName(MIR.FName name) {
    var capturesSelf = name.capturesSelf() ? "selfCap" : "noSelfCap";
    return getSafeName(name.d())+"$"+name(getName(name.mdf(), name.m()))+"$"+capturesSelf;
  }
  public String getName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "Object";
      case MIR.MT.Plain plain -> getName(plain.id(), false);
      case MIR.MT.Usual usual -> getName(usual.it().name(), false);
    };
  }
  public String getRetName(MIR.MT t) {
    return switch (t) {
      case MIR.MT.Any ignored -> "Object";
      case MIR.MT.Plain plain -> getName(plain.id(), true);
      case MIR.MT.Usual usual -> getName(usual.it().name(), true);
    };
  }
  public String getName(Id.DecId name, boolean isRet) {
    return switch (name.name()) {
      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
      case "base.Float" -> isRet ? "Double" : "double";
      case "base.Str" -> "String";
      default -> {
        if (magic.isMagic(Magic.Int, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.UInt, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Str, name)) { yield "String"; }
        yield getName(name);
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
  protected String getRelativeName(Id.DecId d) {
    if (d.pkg().equals(this.pkg)) {
      return getBase(d.shortName());
    }
    var pkg = getPkgName(d.pkg());
    return pkg+"."+getBase(d.shortName())+"_"+d.gen();
  }
  protected static String getSafeName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"$"+getBase(d.shortName())+"_"+d.gen();
  }
  private static String getName(Mdf mdf, Id.MethName m) { return getBase(m.name())+"$"+mdf; }
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
