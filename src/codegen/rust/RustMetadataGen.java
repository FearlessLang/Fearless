package codegen.rust;

import ast.T;
import codegen.MIR;
import id.Id;
import utils.Bug;
import utils.Mapper;
import utils.Push;
import visitors.MIRVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RustMetadataGen implements MIRVisitor<RustMetadataGen.RustProgram> {
  private static int KIND_N = 0;

  @Override public RustProgram visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    return pkgs.entrySet().stream()
      .flatMap(kv->kv.getValue().stream().map(t->visitTrait(kv.getKey(), t)))
      .reduce(RustProgram::merge)
      .orElseGet(RustProgram::new);
  }

  @Override public RustProgram visitTrait(String pkg, MIR.Trait trait) {
    if (pkg.equals("base") && trait.name().name().endsWith("Instance")) {
      return new RustProgram();
    }
    var kind = getName(trait.name())+"_"+(KIND_N++);
    var selfV = new Value.Singleton(kind);
    var ms = trait.meths().stream()
      .map(m->visitMeth(kind, selfV, m, "this", false))
      .reduce(RustProgram::merge).orElseGet(RustProgram::new);
    return ms.merge(RustProgram.kinds(List.of(kind)));
  }

  public RustProgram visitMeth(String kind, Value self, MIR.Meth meth, String selfName, boolean concrete) {
    if (meth.body().isEmpty()) { return new RustProgram(); }
    var body = meth.body().get();
    var children = body.accept(this);

    var selector = methName(getName(meth.name()));
    var key = new RustMethKey(kind, selector);

    if (!meth.gens().isEmpty()) { throw Bug.todo(); }

    Map<String, Value> args = Mapper.of(c -> {
      c.put(name(new MIR.X(selfName, null)), self);
      meth.xs().forEach(x->c.put(name(x), Value.fromT(x.t())));
    });

    // we need the real type of the expression, not the signatures declared rt
    // we can't just get the type from body rn because mCall.t() gives us the cm's return type.
    Value realRT = null;

    var m = new RustMeth(key, List.of(), args, null, body);

    return new RustProgram(
      List.of(),
      List.of(selector),
      Map.of(),
      Map.of(kind, List.of(m))
    ).merge(children);
  }

  @Override public RustProgram visitX(MIR.X x, boolean checkMagic) {
    return new RustProgram();
  }

  @Override public RustProgram visitMCall(MIR.MCall mCall, boolean checkMagic) {
    throw Bug.todo();
  }

  @Override public RustProgram visitLambda(MIR.Lambda newL, boolean checkMagic) {
    throw Bug.todo();
  }

  private String methName(String x) {
    return x.replace("'", "\uD809\uDC6E"+(int)'\'')+"\uD809\uDC6E";
  }
  private String name(MIR.X x) {
    return "r#"+x.name().replace("'", "\uD809\uDC6E"+(int)'\'')+"\uD809\uDC6E";
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
//      case "base.Int", "base.UInt" -> isRet ? "Long" : "long";
//      case "base.Float" -> isRet ? "Double" : "double";
//      case "base.Str" -> "String";
      default -> {
//        if (magic.isMagic(Magic.Int, it.name())) { yield isRet ? "Long" : "long"; }
//        if (magic.isMagic(Magic.UInt, it.name())) { yield isRet ? "Long" : "long"; }
//        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
//        if (magic.isMagic(Magic.Float, it.name())) { yield isRet ? "Double" : "double"; }
//        if (magic.isMagic(Magic.Str, it.name())) { yield "String"; }
        yield getName(it.name());
      }
    };
  }
  private static String getPkgName(String pkg) {
    return pkg.replace(".", "\uD809\uDC6E"+(int)'.');
  }
  protected static String getName(Id.DecId d) {
    var pkg = getPkgName(d.pkg());
    return pkg+"\uD809\uDC6E"+getBase(d.shortName())+"\uD809\uDC6E"+d.gen();
  }
  private static String getName(Id.MethName m) { return getBase(m.name()); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "\uD809\uDC6E"+c;
    }).collect(Collectors.joining());
  }

  public record RustProgram(
    List<String> kinds,
    List<String> selectors,
    // kind -> (x -> v)
    Map<String, Map<String, Value>> ctxs,
    Map<String, List<RustMeth>> ms) {
    static RustProgram kinds(List<String> kind) { return new RustProgram(kind, List.of(), Map.of(), Map.of()); }
    static RustProgram selectors(List<String> selectors) { return new RustProgram(List.of(), selectors, Map.of(), Map.of()); }
    static RustProgram ctxs(Map<String, Map<String, Value>> ctxs) { return new RustProgram(List.of(), List.of(), ctxs, Map.of()); }
    RustProgram() { this(List.of(), List.of(), Map.of(), Map.of()); }
    RustProgram merge(RustProgram other) {
      var ctxs_ = new HashMap<>(ctxs);
      ctxs_.putAll(other.ctxs);
      var ms_ = new HashMap<>(ms);
      other.ms.forEach((k, mss)->{
        if (ms_.containsKey(k)) {
          ms_.put(k, Push.of(ms_.get(k), mss));
          return;
        }
        ms_.put(k, mss);
      });
      return new RustProgram(
        Push.of(this.kinds, other.kinds),
        Push.of(this.selectors, other.selectors),
        ctxs_,
        ms_);
    }
  }
  public record RustMethKey(String kind, String selector) {
    String toFnName() {
      return "fm_"+kind+"_"+selector+"_";
    }
  }
  public record RustMeth(RustMethKey key, List<String> gens, Map<String, Value> explicitGamma, Value rt, MIR body) {}
  public sealed interface Value {
    record Singleton(String kind) implements Value {}
    record B(String kind, String ctx) implements Value {}
    record L(String ctx) implements Value {}
    record Generic(String id) implements Value {}
    // The following instances are not officially values of the fearless language.
    // However, for performance reasons we use them as values at runtime.
//    record Str() implements Value {}
//    record UInt() implements Value {}
    static Value fromT(T t) {
      throw Bug.todo();
    }
  }

  @Override public RustProgram visitMeth(MIR.Meth meth, String selfName, boolean concrete) { throw Bug.unreachable(); }
}
