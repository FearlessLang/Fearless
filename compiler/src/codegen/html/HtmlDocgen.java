package codegen.html;

import ast.Program;
import ast.T;
import id.Id;
import id.Mdf;
import program.CM;
import program.typesystem.XBs;
import utils.Streams;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlDocgen {
  private final Program p;
  public HtmlDocgen(Program p) { this.p = p; }

  public record ProgramDocs(List<PackageDoc> docs) {
    public String fileName() { return "index.html"; }
    public String index() {
      var links = docs.stream()
        .map(d->"<li><a href=\"%s\"><code>%s</code></a></li>".formatted(d.fileName(), d.pkgName))
        .collect(Collectors.joining("\n"));
      return generatePage("Package index", Optional.empty(), "<ul>"+links+"</ul>", "");
    }
  }

  public record PackageDoc(String pkgName, List<TraitDoc> traits) {
    public String fileName() { return pkgFileName(pkgName); }
    public String links() {
      return traits.stream()
        .map(d->"<li><a href=\"%s\"><code>%s</code></a></li>".formatted(d.fileName(), d.traitName()))
        .collect(Collectors.joining("\n"));
    }
    public String index(String links) {
      return generatePage(pkgName, Optional.of("."), "<ul>"+links+"</ul>", "");
    }
  }
  public record TraitDoc(Id.DecId traitName, T traitT, String content) {
    public String fileName() { return traitName.name().replace('.', '_')+"_"+traitName.gen()+".html"; }
    public String html(String links) {
      var pkgLink = pkgFileName(traitName.pkg());
      return generatePage(formatT(traitT), Optional.of(pkgLink), "<ul>"+links+"</ul>", content);
    }
  }

  public ProgramDocs visitProgram() {
    Map<String, List<TraitDoc>> docs = Stream.concat(this.p.ds().values().stream(), this.p.inlineDs().values().stream().filter(d->!d.name().isFresh()))
      .filter(t->!t.name().shortName().startsWith("_"))
      .map(this::visitTrait)
      .sorted(Comparator.comparing(doc->doc.traitName.toString()))
      .collect(Collectors.groupingBy(doc->doc.traitName().pkg()));

    return new ProgramDocs(docs.entrySet().stream()
      .map(ds->new PackageDoc(ds.getKey(), ds.getValue()))
      .toList());
  }

  public TraitDoc visitTrait(T.Dec trait) {
    var sigs = this.p.meths(
        XBs.empty().addBounds(trait.gxs(), trait.bounds()),
        trait.lambda().mdf().isMdf() ? Mdf.recMdf : trait.lambda().mdf(),
        trait.lambda(),
        0
      ).stream()
      .filter(cm->!cm.name().name().startsWith("._"))
      .map(cm->visitMeth(trait, cm))
      .collect(Collectors.joining("\n"));

    String preCode= "<pre><code class=\"language-fearless code-block\">" + sigs + "</code></pre>";
    return new TraitDoc(trait.name(), new T(trait.lambda().mdf(), trait.toIT()), preCode);
  }

  public String visitMeth(T.Dec parent, CM cm) {
    var gens = cm.sig().gens().stream().map(Id.GX::name).collect(Collectors.joining(","));
    if (!gens.isEmpty()) { gens = "["+gens+"]"; }
    var args = Streams.zip(cm.xs(), cm.sig().ts())
      .map((x,t)->"%s: %s".formatted(x, formatT(t)))
      .collect(Collectors.joining(", "));
    if (!args.isEmpty()) { args = "("+args+")"; }
    var body = cm.isAbs() ? "," : " -> …,";
    var name = cm.name().name();
    name = astFull.E.X.isFresh(name) ? "_" : name;
    var sig = "%s%s%s%s: %s%s".formatted(
      formatMdf(cm.mdf()),
      name,
      gens,
      args,
      formatT(cm.ret()),
      body
    );

    var attribution = parent.name().equals(cm.c().name()) ? "" : " // from "+cm.c().name();

    return "%s%s".formatted(
      sig,
      attribution
    ).stripIndent();
  }

  private static String formatMdf(Mdf mdf) {
    return switch (mdf) {
      case mdf -> "";
      default -> mdf+" ";
    };
  }

  private static String formatT(T t) {
    var body = t.rt().match(
      Id.GX::name,
      it->{
        var gens = it.ts().stream().map(HtmlDocgen::formatT).collect(Collectors.joining(","));
        if (!gens.isEmpty()) { gens = "["+gens+"]"; }
        return it.name().name()+gens;
      }
    );
    return formatMdf(t.mdf())+body;
  }

  private static String fragment(Id.DecId d) {
    return URLEncoder.encode(d.name(), StandardCharsets.UTF_8).replace("+", "%20");
  }
  private static String fragment(Id.DecId d, Id.MethName m) {
    var methName = m.name();
    if (m.mdf().isPresent()) { methName = methName+"_"+m.mdf().get(); }
    return URLEncoder.encode(d.name()+"_"+methName, StandardCharsets.UTF_8).replace("+", "%20");
  }
  private static String toURLPath(String pkg, String fragment) {
    return "../"+pkg+"/#"+fragment;
  }

  private static String pkgFileName(String pkgName) {
    return pkgName.replace('.', '_')+".html";
  }

  private static String generatePage(String title, Optional<String> parent, String index, String content) {
    var singleContent = content.isEmpty() ? index : content;
    var parentHtml = parent.map(p-> ("<a href=\"" + p + "\" alt=\"Go up one level\" title=\"Go up one level\">&#8624;</a>\n").stripIndent()).orElse("");
    return String.format("""
      <!DOCTYPE html>
      <html>
      <head>
      	<meta charset="utf-8">
      	<meta name="viewport" content="width=device-width, initial-scale=1.0">
      	<title>%s</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
       	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
       	<link href="https://fonts.googleapis.com/css2?family=Hanken+Grotesk:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
       	<link rel="stylesheet" type="text/css" href="https://unpkg.com/highlightjs@9.16.2/styles/tomorrow-night-eighties.css">
      	<link rel="stylesheet" type="text/css" href="style.css">
      </head>
      <body>
        <header><h1><code>%s%s</code></h1></header>
      	<div id="split-layout">
      		<div id="split-layout__index">
      		%s
      		</div>
      		<div id="split-layout__content">
      			%s
      		</div>
      	</div>
      	<div id="single-layout">
          %s
        </div>
      	<footer>
      		This documentation page includes work from <code>glitch-hello-eleventy</code> under the MIT licence (&copy; Glitch, Inc.).
      	</footer>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
        <script src="highlighting.js"></script>
      </body>
      </html>
      """, title, parentHtml, title, index, content, singleContent).stripIndent();
  }
}
