package codegen.go;

import ast.Program;
import ast.T;
import codegen.MIR;
import id.Id;
import magic.Magic;
import utils.Bug;
import visitors.MIRVisitor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoCodegen implements MIRVisitor<GoCodegen.Output> {
  public record Output(String relative, String global) {
    public Output(String relative) { this(relative, ""); }
    public Output() { this("", ""); }
    Output merge(Output other) {
      String global = this.global;
      if (!this.global.isEmpty() && !other.global.isEmpty()) { global = this.global+"\n"+other.global; }
      if (this.global.isEmpty()) { global = other.global(); }

      return new Output(this.relative+"\n"+other.relative, global);
    }
  }
  private final MagicImpls magic;
  private Program p;
  public GoCodegen(Program p) {
    this.p = p;
    this.magic = new MagicImpls(this, p);
  }

  public Output visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    assert pkgs.containsKey("base");
    var entryName = getName(entry, NameKind.IMPL);
    var init = "\nfunc main(){\nvar entry baseφMain_0 = "+entryName+"{}\nentry.φ35_1φ(baseφLList_1Impl{})\n}";

    var traits = pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .reduce(Output::merge).orElseGet(Output::new);
    var imports = """
      import (
        "strconv"
        "math"
      )
      """;
    return new Output(("package main\n"+traits.global+"\n"+traits.relative+"\n"+init).replace("\n\n", "\n"));
  }
  public Output visitPackage(String pkg, List<MIR.Trait> ds) {
    return ds.stream()
      .map(t->visitTrait(pkg, t))
      .reduce(Output::merge).orElseGet(Output::new);
  }
  public Output visitTrait(String pkg, MIR.Trait trait) {
    if (pkg.equals("base") && trait.name().name().endsWith("Instance")) {
      return new Output("");
    }

    var struct = trait.canSingleton() ? generateStruct(trait) : new Output();
    return new Output(generateInterface(trait)+"\n"+struct.relative, struct.global);
  }
  private String generateInterface(MIR.Trait trait) {
    var name = getName(trait.name(), NameKind.IFACE);
    var start = "type "+name+" interface {\n";

    return start + trait.meths().stream()
      .map(m->visitMeth(m, null, null, false).relative)
      .collect(Collectors.joining("\n")) + "\n}";
  }
  private Output generateStruct(MIR.Trait trait) {
    var name = getName(trait.name(), NameKind.IMPL);
    var start = "type "+name+" struct {}\n";

    var selfName = "this "+getName(trait.name(), NameKind.IMPL);
    var res =  trait.meths().stream()
      .filter(m->!m.isAbs())
      .map(m->visitMeth(m, "this", selfName, true))
      .reduce(Output::merge).orElseGet(Output::new);
    return new Output(start+res.relative, res.global);
  }
  public Output visitMeth(MIR.Meth meth, String selfName, String selfNamePair, boolean concrete) {
    var args = meth.xs().stream()
//      .map(x->meth.gens().isEmpty() ? typePair(x) : name(x.name())+" interface{}")
      .map(this::typePair)
      .collect(Collectors.joining(","));

    if (!concrete) {
      return new Output(name(getName(meth.name()))+"("+args+") "+getRetName(meth.rt(), NameKind.IFACE));
    }
    var body = meth.body().orElseThrow().accept(new GoCodegen(p){
      @Override public Output visitX(MIR.X x, boolean checkMagic) {
        if (x.name().equals(selfName)) {
          return new Output(name(x.name()));
        }
        return super.visitX(x, checkMagic);
      }
    });
    return new Output(
      name("func ("+selfNamePair+") "+getName(meth.name()))+"("+args+") "+getRetName(meth.rt(), NameKind.IFACE)+" {\n return "+body.relative+"\n}",
      body.global
    );
  }
  public Output visitMeth(MIR.Meth meth, String selfName, boolean concrete) {
    throw Bug.unreachable();
  }

  public Output visitX(MIR.X x, boolean checkMagic) {
    return new Output(name(x.name())+".("+getName(x.t(), NameKind.IFACE)+")");
  }

  public Output visitMCall(MIR.MCall mCall, boolean checkMagic) {
//    var magicImpl = magic.get(mCall.recv());
//    if (checkMagic && magicImpl.isPresent()) {
//      var impl = magicImpl.get().call(mCall.name(), mCall.args(), Map.of());
//      if (impl.isPresent()) { return impl.get(); }
//    }

    var magicRecv = !(mCall.recv() instanceof MIR.Lambda);
    var recv = mCall.recv().accept(this, magicRecv);
    var args = mCall.args().stream()
      .map(a->a.accept(this))
      .reduce((o1,o2)->new Output(
        o1.relative+", "+o2.relative,
        o1.global+"\n"+o2.global
      )).orElseGet(Output::new);

    var start = recv.relative+"."+name(getName(mCall.name()))+"("+args.relative+").("+getRetName(mCall.t(), NameKind.IFACE)+")";
    return new Output(start, recv.global+"\n"+args.global);
  }

  public Output visitLambda(MIR.Lambda l) {
    return visitLambda(l, true);
  }
  public Output visitLambda(MIR.Lambda l, boolean checkMagic) {
    var magicImpl = magic.get(l);
    if (checkMagic && magicImpl.isPresent()) {
      return magicImpl.get().instantiate();
    }

    var relative = getName(l.freshName(), NameKind.IMPL)+"{}";
    if (l.canSingleton()) { return new Output(relative); }

    // TODO: also create the struct here so we can model capturing as a struct field. We'll need to filter out lambda structs earlier
    var inlineTypeName = name(Id.GX.fresh().name());
    relative = inlineTypeName+"{"+l.captures().stream().map(x->name(x.name())+": "+name(x.name())).collect(Collectors.joining(",\n"))+"}";
    var selfName = name(l.selfName())+" "+inlineTypeName;
    var struct = "type "+inlineTypeName+" struct {\n"+l.captures().stream()
      .filter(x->!x.name().equals(l.selfName()))
      .map(x->name(x.name())+" "+getName(x.t(), NameKind.IFACE))
      .collect(Collectors.joining("\n"))+"\n}";
    var globalMethImpls = l.allMeths().apply(l.captures())
      .map(m->visitMeth(m, l.selfName(), selfName, true))
      .reduce(Output::merge).orElseGet(Output::new);
    return new Output(relative, globalMethImpls.relative+"\n"+globalMethImpls.global+"\n"+struct);

//    var start = "new "+getName(l.freshName())+"(){\n";
//    var ms = l.meths().stream()
//      .map(m->visitMeth(m, l.selfName(), true))
//      .collect(Collectors.joining("\n"));
//    return start + ms + "}";
  }

  private String typePair(MIR.X x) {
    return name(x.name())+" "+getName(x.t(), NameKind.IFACE);
  }
  private String name(String x) {
    return x.equals("this")
      ? "this"
      : x.replace("'", "φ"+(int)'\'').replace("$", "φ"+(int)'$')+"φ";
  }

  private String getName(T t, NameKind kind) { return t.match(this::getName, it->getName(it, kind)); }
  private String getRetName(T t, NameKind kind) { return t.match(this::getName, it->getName(it, kind)); }
  private String getName(Id.GX<T> gx) { return "interface{}"; }
  private String getName(Id.IT<T> it, NameKind kind) {
    return switch (it.name().name()) {
      case "base.Int" -> "int64";
      case "base.UInt" -> "uint64";
      case "base.Float" -> "float64";
      case "base.Str" -> "string";
      default -> {
        if (magic.isMagic(Magic.Int, it.name())) { yield "int64"; }
        if (magic.isMagic(Magic.UInt, it.name())) { yield "uint64"; }
        if (magic.isMagic(Magic.Float, it.name())) { yield "float64"; }
        if (magic.isMagic(Magic.Str, it.name())) { yield "string"; }
        yield getName(it.name(), kind);
      }
    };
  }
  private static String getPkgName(String pkg) {
    return pkg.replace(".", "φ"+(int)'.');
  }
  protected static String getName(Id.DecId d, NameKind kind) {
    var pkg = getPkgName(d.pkg());
    return pkg+"φ"+getBase(d.shortName())+"_"+d.gen()+kind.suffix();
  }
  private static String getName(Id.MethName m) { return getBase(m.name())+"_"+m.num(); }
  private static String getBase(String name) {
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "φ"+c;
    }).collect(Collectors.joining());
  }
  protected enum NameKind {
    IMPL, IFACE;
    public String suffix() {
      return switch (this) {
        case IMPL -> "Impl";
        case IFACE -> "";
      };
    }
  }
}
