package codegen.rust;

import ast.T;
import codegen.MIR;
import id.Id;
import magic.Magic;
import utils.Bug;
import utils.Push;
import visitors.MIRVisitor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RustCodegen implements MIRVisitor<RustCodegen.RustProgram> {
  public record RustProgram(List<String> kinds, List<String> selectors) {
    RustProgram() { this(List.of(), List.of()); }
    RustProgram merge(RustProgram other) {
      return new RustProgram(Push.of(this.kinds, other.kinds), Push.of(this.selectors, other.selectors));
    }
  }

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
    var name = getName(trait.name());
    return new RustProgram(List.of(name), List.of());
  }

  @Override public RustProgram visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    throw Bug.todo();
  }

  @Override public RustProgram visitX(MIR.X x, boolean checkMagic) {
    throw Bug.todo();
  }

  @Override public RustProgram visitMCall(MIR.MCall mCall, boolean checkMagic) {
    throw Bug.todo();
  }

  @Override public RustProgram visitLambda(MIR.Lambda newL, boolean checkMagic) {
    throw Bug.todo();
  }

  private String name(String x) {
    return "r#" + x.replace("'", "\uD809\uDC6E"+(int)'\'')+"\uD809\uDC6E";
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
}
