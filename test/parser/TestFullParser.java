package parser;

import main.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Bug;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNull;

class TestFullParser {
  void ok(String expected, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    String res = Parser.parseAll(ps).toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
        .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
        .toList();
    try {
      var res = Parser.parseAll(ps);
      Assertions.fail("Parsing did not fail. Got: "+res);
    } catch (Bug | CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }
  @Test void testEmptyPackage(){ ok("""
    {}
    """,
    """
    package pkg1
    """); }
  @Test void testMultiFile(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg1
      """); }
  @Test void testAliasConflictsPackageLocal1(){ ok("""
    {}
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg2
      alias base.True as True,
      """); }
  @Test void failConflictingAliases1(){ fail("""
    In position null
    conflictingAlias:0
    This alias is in conflict with other aliases in the same package: True
    conflicts:
    ([###]Dummy0.fear:2:0) alias base.True[] as True
    ([###]Dummy1.fear:2:0) alias base.True[] as True
    """,
      """
      package pkg1
      alias base.True as True,
      """,
      """
      package pkg1
      alias base.True as True,
      """); }
  @Test void testMultiPackage(){ ok("""
    {}
    """,
      """
      package pkg1
      """,
      """
      package pkg2
      """,
      """
      package pkg1
      """); }
  @Test void testOneDecl(){ ok("""
    {pkg1.MyTrue/0=
      Dec[
      name=pkg1.MyTrue,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.True[]],
      selfName=null,
      meths=[],
      t=infer]]}
    """,
    """
    package pkg1
    MyTrue:base.True
    """); }
  @Test void testManyDecls(){ ok("""
    {pkg1.My12/0=Dec[
      name=pkg1.My12,xs=[],lambda=Lambda[mdf=mdf,its=[12[]],selfName=null,meths=[],t=infer]
      ],
      pkg1.MyFalse/0=Dec[
        name=pkg1.MyFalse,xs=[],lambda=Lambda[mdf=mdf,its=[base.False[]],selfName=null,meths=[],t=infer]
      ],
      pkg2.MyTrue/0=Dec[
        name=pkg2.MyTrue,xs=[],lambda=Lambda[mdf=mdf,its=[base.True[]],selfName=null,meths=[],t=infer]
      ],
      pkg1.MyTrue/0=Dec[
        name=pkg1.MyTrue,xs=[],lambda=Lambda[mdf=mdf,its=[base.True[]],selfName=null,meths=[],t=infer]
      ]}
    """,
      """
      package pkg1
      MyTrue:base.True
      MyFalse:base.False
      """,
      """
      package pkg1
      alias 12 as Twelve,
      My12:Twelve
      """,
      """
      package pkg2
      MyTrue:base.True
      """); }
  @Test void failConflictingDecls1(){ fail("""
      In position null
      conflictingDecl:1
      This trait declaration is in conflict with other trait declarations in the same package: MyTrue/0
      conflicts:
      ([###]/Dummy0.fear:2:0) MyTrue/0
      ([###]/Dummy1.fear:3:0) MyTrue/0
          """,
    """
    package pkg1
    MyTrue:base.True
    """,
    """
    package pkg1
    MyFalse:base.False
    MyTrue:base.True
    """); }

  @Test void baseVoid(){ ok("""
    {base.Void/0=Dec[
      name=base.Void,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[],t=infer]
      ]}
    """, """
    package base
    Void:{}
    """
    );}
  @Test void baseLoopSingleMeth(){ ok("""
    {pkg1.Loop/0=Dec[
      name=pkg1.Loop,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[base.AbstractLoop[]],
      selfName=null,
      meths=[[-]([]):[-]->MCall[receiver=this:infer,name=!,ts=Optional.empty,es=[],t=infer]],
      t=infer]]}
    """, """
    package pkg1
    alias base.AbstractLoop as ALoop,
    Loop:ALoop{this!}
    """
  );}
  @Test void baseLoop(){ ok("""
    {base.Loop/0=
      Dec[name=base.Loop,
      xs=[],
      lambda=Lambda[mdf=mdf,its=[],selfName=null,meths=[null],t=infer]
      ]
      }
    """, """
    package base
    Loop:{!:Void->this!}
    """
  );}
}
