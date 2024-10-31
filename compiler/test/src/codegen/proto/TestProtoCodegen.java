package codegen.proto;

import codegen.MIRInjectionVisitor;
import codegen.go.GoCodegen;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.TsT;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestProtoCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.immBaseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var gen = new ProtoCodegen();
    mir.pkgs().forEach(gen::writePackage);
//    var res = new GoCodegen(mir).visitProgram(new Id.DecId(entry, 0));
//    Err.strCmp(expected, res.toString());
  }

  @Test void capturing() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    FPerson: { #(age: Num): mut Person -> mut Person: {
      read .age: Num -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.age,
      }
    Num: {}
    FortyTwo: Num
    """);}

  @Test void stuck() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    Usage: {
      #: Num -> this#,
      }
    Num: {}
    FortyTwo: Num
    """);}

  @Test void capturingDeep() { ok("""
    """, "fake.Fake", false, Base.minimalBase, """
    package test
    Person: {
      read .age: Num,
      mut .wrap: mut Person -> {'self
       .age -> this.age.plus1,
       .wrap -> {'topLevelWrapped
         .age -> self.age.plus1,
         }
       },
      }
    FPerson: { #(age: Num): mut Person -> {'original
      .age -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.wrap.age,
      }
    Num: {
      .plus1: Num,
      }
    FortyTwo: Num{ .plus1 -> FortyThree }
    FortyThree: Num{ .plus1 -> FortyFour }
    FortyFour: Num{ .plus1 -> this.plus1 }
    """);}

  @Test void helloWorld() { ok("""
    """, "fake.Fake", true, """
    package test
    alias base.Main as Main,
    Test: Main{_ -> "Hello, World!"}
    """); }
}
