package codegen.java;

import codegen.MIR;
import codegen.MethExprKind;
import codegen.ParentWalker;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;
import magic.Magic;
import magic.MagicImpls;
import org.apache.commons.text.StringEscapeUtils;
import rt.NativeRuntime;
import utils.Box;
import utils.Bug;
import utils.Streams;
import visitors.MIRVisitor;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static magic.MagicImpls.getLiteral;

public class JavaCodegen implements MIRVisitor<String> {
  protected final MIR.Program p;
  protected final Map<MIR.FName, MIR.Fun> funMap;
  private JavaMagicImpls magic;
  private HashMap<Id.DecId, String> freshRecords;
  private MIR.Package pkg;

  public JavaCodegen(MIR.Program p) {
    this.magic = new JavaMagicImpls(this, p.p());
    this.p = new OptimisationBuilder(this.magic)
      .withBoolIfOptimisation()
      .withBlockOptimisation()
      .run(p);
    this.funMap = p.pkgs().stream().flatMap(pkg->pkg.funs().stream()).collect(Collectors.toMap(MIR.Fun::name, f->f));
  }

  protected static String argsToLList(Mdf addMdf) {
    return """
      FAux.LAUNCH_ARGS = base.LList_1._$self;
      for (String arg : args) { FAux.LAUNCH_ARGS = FAux.LAUNCH_ARGS.$43$%s$(rt.Str.fromJavaStr(arg)); }
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
          System.err.println("Program crashed with: "+t.getMessage());
          System.exit(1);
        }
      }
    """.formatted(
      argsToLList(Mdf.mut),
      entryName,
      systemImpl
    );

    final String fearlessHeader = """
      package userCode;
      class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }
      """;

    return fearlessHeader+"\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}";
  }

  public String visitPackage(MIR.Package pkg) {
    this.pkg = pkg;
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
    assert getLiteral(p.p(), def.name()).isEmpty();

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
//      .map(x->new MIR.X(x.name(), new MIR.MT.Any(x.t().mdf()))) // required for overriding meths with generic args
      .map(this::typePair)
      .collect(Collectors.joining(","));

    return getRetName(sig.rt())+" "+name(getName(sig.mdf(), sig.name()))+"("+args+");";
  }


  public String visitMeth(MIR.Meth meth, MethExprKind kind, Map<ParentWalker.FullMethId, MIR.Sig> leastSpecific) {
    var overriddenSig = this.overriddenSig(meth.sig(), leastSpecific);
    if (overriddenSig.isPresent()) {
      if (kind.kind() == MethExprKind.Kind.Unreachable) {
        return visitMeth(meth.withSig(overriddenSig.get()), MethExprKind.Kind.Unreachable, Map.of());
      }
      var delegator = visitMeth(meth.withSig(overriddenSig.get()), new MethExprKind.Delegator(meth.sig(), overriddenSig.get()), Map.of());
      var delegate = visitMeth(meth, MethExprKind.Kind.Delegate, Map.of());
      return delegator+"\n"+delegate;
    }

    var methName = switch (kind.kind()) {
      case Delegate -> name(getName(meth.sig().mdf(), meth.sig().name()))+"$Delegate";
      default -> name(getName(meth.sig().mdf(), meth.sig().name()));
    };

    var args = meth.sig().xs().stream()
      .map(this::typePair)
      .collect(Collectors.joining(","));
    var selfArg = meth.capturesSelf() ? Stream.of("this") : Stream.<String>of();
    var funArgs = Streams.of(meth.sig().xs().stream().map(MIR.X::name).map(this::name), selfArg, meth.captures().stream().map(this::name).map(x->"this."+x))
      .collect(Collectors.joining(","));

    var mustCast = meth.fName().isPresent() && this.funMap.get(meth.fName().get()).ret() instanceof MIR.MT.Any && !(meth.sig().rt() instanceof MIR.MT.Any);
    var cast = mustCast ? "(%s)".formatted(getName(meth.sig().rt()) ): "";

    var realExpr = switch (kind) {
      case MethExprKind.Kind k -> switch (k.kind()) {
        case RealExpr, Delegate -> "return %s %s.%s(%s);".formatted(cast, getName(meth.origin()), getName(meth.fName().orElseThrow()), funArgs);
        case Unreachable -> "throw new Error(\"Unreachable code\");";
        case Delegator -> throw Bug.unreachable();
      };
      case MethExprKind.Delegator k -> "return this.%s(%s);".formatted(
        methName+"$Delegate",
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
      .map(this::typePair)
      .collect(Collectors.joining(", "));
    var body = fun.body().accept(this, true);

    var ret = fun.body() instanceof MIR.Block ? "" : "return ";

    return """
      static %s %s(%s) {
        %s%s;
      }
      """.formatted(getRetName(fun.ret()), name, args, ret, body);
  }
  @Override public String visitBlockExpr(MIR.Block expr, boolean checkMagic) {
    var res = new StringBuilder();
    var stmts = new ArrayDeque<>(expr.stmts());
    var doIdx = new Box<>(0);
    while (!stmts.isEmpty()) {
      res.append(this.visitBlockStmt(expr, stmts, doIdx));
    }
    return res.toString();
  }

  private String visitBlockStmt(MIR.Block expr, ArrayDeque<MIR.Block.BlockStmt> stmts, Box<Integer> doIdx) {
    var stmt = stmts.poll();
    assert stmt != null;
    return switch (stmt) {
      case MIR.Block.BlockStmt.Return ret -> "return %s".formatted(ret.e().accept(this, true));
      case MIR.Block.BlockStmt.Do do_ -> "var doRes%s = %s;\n".formatted(doIdx.update(n->n + 1), do_.e().accept(this, true));
      case MIR.Block.BlockStmt.Loop loop -> """
          while (true) {
            var res = %s.$35$mut$();
            if (res == base.ControlFlowContinue_0._$self || res == base.ControlFlowContinue_1._$self) { continue; }
            if (res == base.ControlFlowBreak_0._$self || res == base.ControlFlowBreak_1._$self) { break; }
            if (res instanceof base.ControlFlowReturn_1 rv) { return (%s) rv.value$mut$(); }
          }
          """.formatted(loop.e().accept(this, true), getName(expr.expectedT()));
      case MIR.Block.BlockStmt.If if_ -> {
        var body = this.visitBlockStmt(expr, stmts, doIdx);
        if (body.startsWith("return")) { body += ";"; }
        yield """
          if (%s == base.True_0._$self) { %s }
          """.formatted(if_.pred().accept(this, true), body);
      }
      case MIR.Block.BlockStmt.Var var -> "var %s = %s;\n".formatted(name(var.name()), var.value().accept(this, true));
    };
  }

  @Override public String visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    if (magic.isMagic(Magic.Str, createObj.concreteT().id())) {
      return visitStringLiteral(createObj);
    }

    var magicImpl = magic.get(createObj);
    if (checkMagic && magicImpl.isPresent()) {
      var res = magicImpl.get().instantiate();
      if (res.isPresent()) { return res.get(); }
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
        .map(m->this.visitMeth(m, MethExprKind.Kind.Unreachable, leastSpecific))
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
  public String visitStringLiteral(MIR.CreateObj k) {
    var id = k.concreteT().id();
    var javaStr = getLiteral(p.p(), id).map(l->l.substring(1, l.length() - 1)).orElseThrow();
    var recordName = "str$$"+getBase(javaStr.hashCode()+"")+"$$str";
    if (!this.freshRecords.containsKey(id)) {
      // We parse literal \n, unicode escapes as if this was a Java string literal.
      var utf8 = StringEscapeUtils.unescapeJava(javaStr).getBytes(StandardCharsets.UTF_8);
      try {
        NativeRuntime.validateStringOrThrow(utf8);
      } catch (NativeRuntime.StringEncodingError err) {
        // TODO: throw a nice Fail...
        throw Bug.of(err);
      }
      var utf8Array = IntStream.range(0, utf8.length).mapToObj(i->Byte.toString(utf8[i])).collect(Collectors.joining(","));
      var graphemes = Arrays.stream(NativeRuntime.indexString(utf8)).mapToObj(Integer::toString).collect(Collectors.joining(","));

      this.freshRecords.put(id, """
        final class %s implements rt.Str {
          public static final rt.Str _self$ = new %s();
          private static final byte[] UTF8 = new byte[]{%s};
          private static final int[] GRAPHEMES = new int[]{%s};
          @Override public byte[] utf8() { return UTF8; }
          @Override public int[] graphemes() { return GRAPHEMES; }
        }
        """.formatted(recordName, recordName, utf8Array, graphemes));
    }
    return recordName+"._self$";
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
    var mustCast = !call.t().equals(call.originalRet());
    var cast = mustCast ? "(("+getName(call.t())+")" : "";

    if (checkMagic && !call.variant().contains(MIR.MCall.CallVariant.Standard)) {
      var impl = magic.variantCall(call).call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return cast+impl.get()+(mustCast ? ")" : ""); }
    }

    var magicImpl = magic.get(call.recv());
    if (checkMagic && magicImpl.isPresent()) {
      var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
      if (impl.isPresent()) { return cast+impl.get()+(mustCast ? ")" : ""); }
    }

    var start = cast+call.recv().accept(this, checkMagic)+"."+name(getName(call.mdf(), call.name()))+"(";
    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return start+args+")"+(mustCast ? ")" : "");
  }

  @Override public String visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    var recv = expr.condition().accept(this, checkMagic);
    var mustCast = !this.funMap.get(expr.then()).ret().equals(this.funMap.get(expr.else_()).ret());
    var cast = mustCast ? "(%s)".formatted(getRetName(expr.t())) : "";

    return "(%s(%s == base.True_0._$self ? %s : %s))".formatted(cast, recv, this.funMap.get(expr.then()).body().accept(this, true), this.funMap.get(expr.else_()).body().accept(this, true));
  }

  @Override public String visitStaticCall(MIR.StaticCall call, boolean checkMagic) {
    var cast = call.castTo().map(t->"(("+getName(t)+")").orElse("");
    var castEnd = cast.isEmpty() ? "" : ")";

    var args = call.args().stream()
      .map(a->a.accept(this, checkMagic))
      .collect(Collectors.joining(","));
    return "%s %s.%s(%s)%s".formatted(cast, getName(call.fun().d()), getName(call.fun()), args, castEnd);
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
      default -> {
        if (magic.isMagic(Magic.Int, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.UInt, name)) { yield isRet ? "Long" : "long"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Float, name)) { yield isRet ? "Double" : "double"; }
        if (magic.isMagic(Magic.Str, name)) { yield "rt.Str"; }
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
    if (d.pkg().equals(this.pkg.name())) {
      return getBase(d.shortName());
    }
    return getName(d);
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
