package typing;

import failure.CompileError;
import id.Mdf;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.Program;
import program.typesystem.XBs;
import utils.Err;
import utils.FromContent;

import java.util.Arrays;

/** Tests for the T[[MDF; Xs=Ts]] logic done in TypeRename via cmsOf **/
public class TestAdaptRecMdf {
  void ok(String expected, String type, Mdf lambdaMdf, String ...code){
    var it = new Parser(Parser.dummy, type).parseFullT();
    Program p= FromContent.of(code);
    Err.strCmpFormat(expected, p.meths(XBs.empty(), lambdaMdf, it.toAstT().itOrThrow(), 0).toString());
  }
  void fail(String expected, String type, Mdf lambdaMdf, String ...code) {
    var it = new Parser(Parser.dummy, type).parseFullT();
    Program p = FromContent.of(code);
    try {
      var res = p.meths(XBs.empty(), lambdaMdf, it.toAstT().itOrThrow(), 0);
      Assertions.fail("Expected failure, got\n" + res);
    } catch (CompileError e) {
      Err.strCmp(expected, e.toString());
    }
  }

  @Test void adaptMutRecMdfMutImm() {
    ok("""
    [test.List[imm test.Person[]],recMdf.get/0()[][]:imm test.Person[]abs]
    """, "test.Family", Mdf.mut, """
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family:List[imm Person]{}
    """);
  }
  @Test void adaptMutRecMdfMutMut() {
    ok("""
    [test.List[muttest.Person[]],recMdf.get/0()[][]:muttest.Person[]abs]
    """, "test.Family", Mdf.mut, """
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family:List[mut Person]{}
    """);
  }
  @Test void adaptMutRecMdfMutRead() {
    ok("""
      [test.List[read test.Person[]],recMdf .get/0()[][]:read test.Person[]abs]
      """, "test.Family", Mdf.mut, """
      package test
      Person:{}
      List[X]:{ recMdf .get(): recMdf X }
      Family:List[read Person]{}
      """);
  }

  @Provide Arbitrary<Mdf> genericMdfOnConcrete() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(mdf->!mdf.is(Mdf.iso, Mdf.mdf)).toList());
  }
  @Provide Arbitrary<Mdf> lambdaMdf() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(mdf->!mdf.is(Mdf.mdf)).toList());
  }
  @Property void adaptRecMdf(@ForAll("lambdaMdf") Mdf lambdaMdf, @ForAll("genericMdfOnConcrete") Mdf genericMdf) {
    var expected = lambdaMdf.adapt(genericMdf);
    if (genericMdf.isMdf()) { expected = Mdf.recMdf; }
    ok("""
    [test.List[%s test.Person[]],recMdf.get/0()[][]:%s test.Person[]abs]
    """.formatted(genericMdf, expected), "test.Family",  lambdaMdf, """
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X }
    Family:List[%s Person]{}
    """.formatted(genericMdf));
  }
  @Provide Arbitrary<Mdf> genericMdf() {
    return Arbitraries.of(Arrays.stream(Mdf.values()).filter(mdf->!mdf.is(Mdf.iso)).toList());
  }
  @Property void adaptRecMdfGenFormalism(@ForAll("lambdaMdf") Mdf lambdaMdf, @ForAll("genericMdf") Mdf genericMdf) {
    var expected = lambdaMdf.adapt(genericMdf);
    if (genericMdf.isMdf()) { expected = Mdf.recMdf; }
    ok("""
    [test.List[%s Z],recMdf.get/0()[][]:%s Zabs,
    test.List[%s Z],read.asRead/0()[][]:read Zabs]
    """.formatted(genericMdf, expected, genericMdf), "test.Family[mdf Z]",  lambdaMdf, """
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X, read .asRead: read X }
    Family[Y]:List[%s Y]{}
    """.formatted(genericMdf));
  }

  @Property void adaptRecMdfGenCurrent(@ForAll("lambdaMdf") Mdf lambdaMdf, @ForAll("genericMdf") Mdf genericMdf) {
    var expected = lambdaMdf.adapt(genericMdf);
    if (genericMdf.isMdf() && lambdaMdf.isRecMdf()) { expected = Mdf.recMdf; }
    ok("""
    [test.List[%s Z],recMdf.get/0()[][]:%s Zabs,
    test.List[%s Z],read.asRead/0()[][]:read Zabs]
    """.formatted(genericMdf, expected, genericMdf), "test.Family[mdf Z]",  lambdaMdf, """
    package test
    Person:{}
    List[X]:{ recMdf .get(): recMdf X, read .asRead: read X }
    Family[Y]:List[%s Y]{}
    """.formatted(genericMdf));
  }
}
