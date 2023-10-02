package codegen.md;

import ast.E;
import ast.Program;
import ast.T;
import id.Id;
import org.apache.commons.text.StringEscapeUtils;
import program.CM;
import utils.Streams;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownDocgen {
  private final Program p;

  public MarkdownDocgen(Program p) { this.p = p; }

//  public record TraitDoc(String fileName, String markdown) {
//    static TraitDoc fromPkg(String pkgName, String markdown) {
//      return new TraitDoc(pkgName+".md", markdown);
//    }
//  }
//  public List<TraitDoc> visitProgram() {
//    return p.ds().values().stream()
//      .collect(Collectors.groupingBy(d->d.name().pkg()))
//      .entrySet().stream()
//      .map(pkg->TraitDoc.fromPkg(pkg.getKey(), visitPackage(pkg.getKey(), pkg.getValue())))
//      .toList();
//  }
//  public String visitPackage(String name, List<T.Dec> traits) {
//    var ts = traits.stream()
//      .filter(d->d.name().shortName().charAt(0) != '_')
//      .map(this::visitTrait)
//      .collect(Collectors.joining("\n\n"));
//    return """
//      <h1><code>%s</code></h1>
//
//      %s
//      """.formatted(name, ts).stripIndent().strip();
//  }
//  public String visitTrait(T.Dec dec) {
//    var typeParams = dec.gxs().stream().map(x->"- `"+x+"`").collect(Collectors.joining("\n"));
//    if (!typeParams.isEmpty()) { typeParams = "**Type parameters**:\n" + typeParams+"\n"; }
//
//    var impls = p.itsOf(dec.toIT()).stream()
//      .map(it->"[`%s`](%s)".formatted(it.toString(), toURLPath(it.name().pkg(), fragment(it.name()))))
//      .collect(Collectors.joining(", "));
//    if (!impls.isEmpty()) { impls = "**Implements**: " + impls; }
//
//    var fragment = fragment(dec.name());
//    return """
//      <h2 id="%s"><a href="#%s"><code>%s</code></a></h2>
//
//      %s
//      %s
//      %s
//      """.formatted(
//        fragment,
//        fragment,
//        dec.name().toString(),
//        impls,
//        typeParams,
//        visitLambda(dec.name().pkg(), dec.name(), dec.lambda())
//        ).stripIndent().strip();
//  }
//
//  public String visitMeth(String pkg, Id.DecId d, E.Meth m) {
//    var typeParams = m.sig().gens().stream().map(x->"- `"+x+"`").collect(Collectors.joining("\n"));
//    if (!typeParams.isEmpty()) { typeParams = "**Type parameters**:\n" + typeParams; }
//    var args = Streams.zip(m.xs(), m.sig().ts())
//      .map((x, t)->t.match(
//        gx->"- `%s: %s`".formatted(x, gx),
//        it->"- [`%s: %s`](%s)".formatted(x, it, toURLPath(it.name().pkg(), fragment(it.name())))
//        ))
//      .collect(Collectors.joining("\n"));
//    if (!args.isEmpty()) { args = "**Parameters**:\n" + args; }
//
//    var name = StringEscapeUtils.escapeHtml4(m.name().toString());
//    var fragment = fragment(pkg, d, m);
//    var url = toURLPath(pkg, fragment);
//    var header = (m.isAbs() ? "<h3 id=\"%s\"><a href=\"#%s\"><em><code>%s</code></em></a></h3>\n\n*Abstract*\n" : "<h3 id=\"#%s\"><a href=\"#%s\"><code>%s</code></a></h3>")
//      .formatted(url, fragment, name);
//    var ret = m.sig().ret().match(gx->"`"+gx+"`", it->"[`"+it+"`]("+toURLPath(it.name().pkg(), fragment(it.name()))+")");
//    return """
//      %s
//
//      **Returns**: %s
//      %s
//      %s
//      """.formatted(header, ret, typeParams, args).stripIndent().strip();
//  }
//
//  public String visitLambda(String pkg, Id.DecId d, E.Lambda e) {
//    return e.meths().stream()
//      .collect(Collectors.groupingBy(m->m.name().name()))
//      .values().stream()
//      .flatMap(Collection::stream)
//      .filter(m->!m.name().name().startsWith("._"))
//      .map(m->this.visitMeth(pkg, d, m))
//      .collect(Collectors.joining("\n\n"));
//  }
//
//  private static String fragment(Id.DecId d) {
//    return URLEncoder.encode(d.name(), StandardCharsets.UTF_8).replace("+", "%20");
//  }
//  private static String fragment(String pkg, Id.DecId d, E.Meth m) {
//    return URLEncoder.encode(d.name()+"_"+m.name(), StandardCharsets.UTF_8).replace("+", "%20");
//  }
//  private static String toURLPath(String pkg, String fragment) {
//    return "../"+pkg+"/#"+fragment;
//  }
}
