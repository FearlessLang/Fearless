package parser;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

class TestLiterals {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    String res = Parser.parseAll(ps, new TypeSystemFeatures()).toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps, new TypeSystemFeatures());
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  //Note: in some of those calls we start lit with a space. This is to prevent the : token of .m: to be merged with starting symbols, like .m:+5 is tokenized as .m  :+ 5
  //where :+ is a operator symbol. This would near never be a problem in practique where we do not use those funky literals as types
  void okLit(String litExpected, String lit){
    String expected= "{a.A/0=Dec[name=a.A/0,gxs=[],lambda=[-muta.A[]-][]{.m/0([]):Sig[gens=[],ts=[],ret="+litExpected+"]->[-]}]}";
    String src="package a\nA: {.m:"+lit+",}";
    ok(expected,src);
  }
  // TODO: TEST alias generic merging line 300 in FullEAntlrVisitor

  @Test void uintAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.5 is not a valid type name.
    """,
    """
    package a
    5: {}
    """); }
  @Test void intAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.+5 is not a valid type name.
    """,
    """
    package a
    +5: {}
    """); }
  @Test void negativeIntAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E59 syntaxError]
    a.-5 is not a valid type name.
    """,
    """
    package a
    -5: {}
    """); }
  @Test void floatAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E67 crossPackageDeclaration]
    You may not declare a trait in a different package than the package the declaration is in.
    """,
    """
    package a
    5.43: {}
    """); }
  @Test void negativeFloatAsTypeName(){ fail("""
    In position [###]/Dummy0.fear:2:0
    [E67 crossPackageDeclaration]
    You may not declare a trait in a different package than the package the declaration is in.
    """,
    """
    package a
    -5.43: {}
    """); }

  @Test void typeNameType1(){ okLit("imm a.A[]","A"); }
  @Test void typeNameType2(){ okLit("B","B"); }//B seen as generic
  @Test void natType(){ okLit("imm base.natLit.5[]","5"); }
  @Test void intPType(){ okLit("imm base.intLit.+5[]"," +5"); }
  @Test void intNType(){ okLit("imm base.intLit.-5[]"," -5"); }
  @Test void floatDotType(){ okLit("imm a..32[]",".32"); }
  @Test void floatPType(){ okLit("imm base.floatLit.+5.32[]"," +5.32"); }
  @Test void floatNType(){ okLit("imm base.floatLit.-5.32[]"," -5.32"); }
  @Test void floatNTypeUnder(){ okLit("imm a.__-5.32[]","__-5.32"); }
  @Test void floatNPTypeUnder(){ okLit("imm a.__-5.+32[]","__-5.+32"); }
  
  @Test void floatUnderPType(){ okLit("imm a._+3+5[]"," _+3+5"); }
  
  @Test void doublePlusUnder(){ okLit("imm a._+5+2[]","_+5+2"); }
  @Test void doublePlus(){ okLit("imm a.+5+2[]"," +5+2"); }
  @Test void floatUType(){ okLit("imm base.floatLit.5.32[]","5.32"); }
  @Test void floatDotsType(){ okLit("imm a.5....32[]","5....32"); }
  @Test void numPPType(){ okLit("imm a.+5/+2[]"," +5/+2"); }
  @Test void numNPType(){ okLit("imm a.-5/+2[]"," -5/+2"); }
  @Test void numUPType(){ okLit("imm a.5/+2[]","5/+2"); }
  @Test void numPNType(){ okLit("imm a.+5/-2[]"," +5/-2"); }
  @Test void numNNType(){ okLit("imm a.-5/-2[]"," -5/-2"); }
  @Test void numUNType(){ okLit("imm a.5/-2[]","5/-2"); }
  @Test void numPUType(){ okLit("imm a.+5/2[]"," +5/2"); }
  @Test void numNUType(){ okLit("imm a.-5/2[]"," -5/2"); }
  @Test void numUUType(){ okLit("imm a.5/2[]","5/2"); }


  @Test void uStrType(){ okLit("imm base.uStrLit.\"hello\"[]","\"hello\""); }
  @Test void sStrType(){ okLit("imm base.sStrLit.`hello`[]","`hello`"); }
  @Test void uStrTypeEsc(){ okLit("imm base.uStrLit.\"hello\\\"dd\"[]","\"hello\\\"dd\""); }
  @Test void sStrTypeEsc(){ okLit("imm base.sStrLit.`hello\"dd`[]","`hello\"dd`"); }

  @Test void composedTypeName1(){ okLit("imm a._AA`hello`++09e34[]","_AA`hello`++09e34"); }
  @Test void composedTypeName2(){ okLit("imm a._AA\"hello\"++09e34[]","_AA\"hello\"++09e34"); }

}
