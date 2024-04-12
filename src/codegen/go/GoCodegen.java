package codegen.go;

import codegen.MIR;
import codegen.optimisations.OptimisationBuilder;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.stream.Collectors;

public class GoCodegen {
  public record GoProgram(MainFile mainFile, List<PackageCodegen.GoPackage> pkgs) {}
  public record MainFile(String src) implements GoCompiler.Unit {
    @Override public String name() { return "entry.go"; }
  }

  private final MIR.Program p;

  public GoCodegen(MIR.Program p) {
    this.p = new OptimisationBuilder(new GoMagicImpls(null, p.p()))
      .withBoolIfOptimisation()
      .run(p);
  }

  protected static String argsToLList(Mdf addMdf) {
    return """
      for _, e := range os.Args[1:] {
        baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_%sφ(e)
      }
      """.formatted(addMdf);
  }

  public GoProgram visitProgram(Id.DecId entry) {
    var funMap = p.pkgs().stream().flatMap(pkg->pkg.funs().stream()).collect(Collectors.toMap(MIR.Fun::name, f->f));
    var pkgs = p.pkgs().stream()
      .map(pkg->new PackageCodegen(p, pkg, funMap).visitPackage())
      .toList();

    var entryImpl = getName(entry)+"Impl";

    return new GoProgram(
      new MainFile("""
        package main
        import (
          "fmt"
          "os"
        )
        func main() {
          %s
          fmt.Println(%s{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
        }
        """.formatted(argsToLList(Mdf.imm), entryImpl)),
      pkgs
    );
  }

  static String getPkgName(String pkg) {
    return pkg.replace(".", "φ"+(int)'.');
  }
  static String getPkgFileName(String pkg) {
    return pkg.replace(".", "~"+(int)'.');
  }
  public static String getName(Id.DecId d) {
    return "φ"+getPkgName(d.pkg())+"φ"+getBase(d.shortName())+"_"+d.gen();
  }
  public static String getBase(String name) {
    if (name.startsWith(".")) { name = "φ"+name.substring(1); }
    return name.chars().mapToObj(c->{
      if (c != '\'' && (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c))) {
        return Character.toString(c);
      }
      return "φ"+c;
    }).collect(Collectors.joining());
  }
}
