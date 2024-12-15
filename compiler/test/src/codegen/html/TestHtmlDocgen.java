package codegen.html;

import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestHtmlDocgen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck(new ConcurrentHashMap<>());
    var md = new HtmlDocgen(inferred).visitProgram();
    Err.strCmpFormat(expected, md.toString());
  }

  @Test void emptyProgram() { ok("""
    ProgramDocs[docs=[PackageDoc[pkgName=base,traits=[TraitDoc[traitName=base.Main/0,traitT=base.Main[],content=<pre><codeclass="language-fearlesscode-block">imm#(s:mutbase.System):immbase.Void,</code></pre>],TraitDoc[traitName=base.Sealed/0,traitT=base.Sealed[],content=<pre><codeclass="language-fearlesscode-block"></code></pre>],TraitDoc[traitName=base.System/0,traitT=base.System[],content=<pre><codeclass="language-fearlesscode-block"></code></pre>],TraitDoc[traitName=base.Void/0,traitT=base.Void[],content=<pre><codeclass="language-fearlesscode-block"></code></pre>]]]]]
    """, "fake.Fake", false, Base.minimalBase);
  }
}
