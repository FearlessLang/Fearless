package program.inference;

import astFull.T;
import main.Main;
import net.jqwik.api.Example;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.Bug;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TestRefineTypes {
  T addInfers(T t){
    return t.match(
      gx->gx.name().equals("Infer") ? T.infer:t,
      it->new T(t.mdf(),it.withTs(it.ts().stream().map(this::addInfers).toList()))
    );
  }
  void ok(String expected, String expr, String t1, String t2, String program){
    Main.resetAll();
    var e = new Parser(Parser.dummy,expr).parseFullE(Bug::err, s->Optional.empty());
    var pT1 = addInfers(new Parser(Parser.dummy, t1).parseFullT());
    var pT2 = addInfers(new Parser(Parser.dummy, t2).parseFullT());
    var p = Parser.parseAll(List.of(new Parser(Path.of("Dummy.fear"), program)));
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();

    var e1 = new RefineTypes(inferredSigs).fixType(e, pT1);
    var e2 = new RefineTypes(inferredSigs).fixType(e1, pT2);
    Err.strCmpFormat(expected, e2.toString());
  }

  @Example void ab() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "a.B[]", """
    package a
    A:{}
    B:A{}
    """);}

  @Example void firstInfer() {ok("""
    varName:imm a.B[]
    """, "varName", "Infer", "a.B[]", """
    package a
    A:{}
    B:A{}
    """);}

  @Example void secondInfer() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "Infer", """
    package a
    A:{}
    B:A{}
    """);}

  @Example void inferInfer() {ok("""
    varName:infer
    """, "varName", "Infer", "Infer", """
    package a
    A:{}
    B:A{}
    """);}
  @Example void same() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "a.A[]", """
    package a
    A:{}
    """);}
  @Example void aGen() {ok("""
    varName:imm a.A[imm a.B[],imm a.B[]]
    """, "varName", "a.A[X,a.B[]]", "a.A[a.B[],Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}

  @Example//varName:imm a.A[mut a.B[],read a.B[]]
  void aGenMdf1() {ok("""
    varName:imma.A[lent a.C[],read a.B[]]
    """, "varName", "a.A[mut X,imm a.B[]]", "a.A[lent a.C[],read Y]", """
    package a
    A[X,Y]:{}
    B:{}
    C:{}
    """);}
  //Yes, it should be a.A[lent a.C[],_] instead of a.A[mut a.C[],_]:
  //We get to lent a.C[] = mut a.C[] and we take the 'best type: as specified by t2 (the best type)
  @Example void aGenMdf2() {ok("""
    varName:imm a.A[lent a.B[],read a.B[]]
    """, "varName", "a.A[mut X,read a.B[]]", "a.A[lent a.B[],mdf Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}
  @Example void aGenDeep() {ok("""
    varName:imma.A[imma.B[],imma.A[imma.B[],imma.B[]]]
    """, "varName", "a.A[X,a.A[X,X]]", "a.A[a.B[],Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}
  // TODO: Test gen A and gen B, which one is kept
  @Example//Note: unstable, below many possible options
  //varName:imma.A[imma.B[],imma.A[imm a.B[],imm a.B[]]]
  //varName:imma.A[imma.A[imm a.B[],imm a.B[]],imma.A[imm a.B[],imm a.B[]]]
  //varName:imma.A[imma.B[],imma.B[]]
  // X = a.B
  // X= a.A[X,X]
  void aGenInfinite1() {ok("""
    varName:imma.A[imma.B[],imma.B[]]
    """, "varName", "a.A[X,a.A[X,X]]", "a.A[a.B[],X]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}
  @Example
  // X=a.A[X,X]
  void aGenInfinite2() {ok("""
    varName:imma.A[imma.B[],imma.B[]]
    """, "varName", "a.A[a.A[X,X],X]", "a.A[X,a.B[]]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}

  @Example void aGenInfinite3() {ok("""
    varName:imma.A[imma.B[],imma.A[imma.B[],imma.B[]]]
    """, "varName", "a.A[X,a.A[X,X]]", "a.A[a.B[],Y]", """
    package a
    A[X,Y]:{}
    B:{}
    """);}

  @Example void lhsGxRhsIT() {ok("""
    varName:imm a.A[]
    """, "varName", "X", "a.A[]", """
    package a
    A:{}
    """);}
  @Example void lhsITRhsGx() {ok("""
    varName:imm a.A[]
    """, "varName", "a.A[]", "X", """
    package a
    A:{}
    """);}

  @Example void refineGens() {ok("""
    varName:imm a.A[imm a.B[]]
    """, "varName", "a.A[Infer]", "a.A[a.B[]]", """
    package a
    A[X]:{}
    B:{}
    """);}

  @Example void refineGensNested1() {ok("""
    varName:imm a.A[imm a.A[imm a.B[]]]
    """, "varName", "a.A[Infer]", "a.A[a.A[a.B[]]]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensNested2() {ok("""
    varName:imm a.A[imm a.A[imm a.B[]]]
    """, "varName", "a.A[imm a.A[Infer]]", "a.A[a.A[a.B[]]]", """
    package a
    A[X]:{}
    B:{}
    """);}

  @Example void refineGensNoInfo() {ok("""
    varName:imm a.A[imm X]
    """, "varName", "a.A[X]", "a.A[X]", """
    package a
    A[X]:{}
    B:{}
    """);}

  @Example void refineGensMdf1() {ok("""
    varName:imm a.A[imm X]
    """, "varName", "a.A[mdf X]", "a.A[imm X]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdf2() {ok("""
    varName:imm a.A[mdf X]
    """, "varName", "a.A[imm X]", "a.A[mdf X]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdf3() {ok("""
    varName:imm a.A[mut a.B[]]
    """, "varName", "a.A[mut a.B[]]", "a.A[mdf X]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdf4() {ok("""
    varName:imm a.A[mut a.B[]]
    """, "varName", "a.A[mdf X]", "a.A[mut a.B[]]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdf5() {ok("""
    varName:imm a.A[imm a.B[]]
    """, "varName", "a.A[mut X]", "a.A[imm a.B[]]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdfNested1() {ok("""
    varName:imma.A[imma.B[]]
    """, "varName", "a.A[recMdf a.A[mdf X]]", "a.A[imm a.B[]]", """
    package a
    A[X]:{}
    B:{}
    """);}
  @Example void refineGensMdfNested2() {ok("""
    varName:imma.A[recMdfa.A[imma.B[]]]
    """, "varName", "a.A[recMdf a.A[mdf X]]", "a.A[recMdf a.A[imm a.B[]]]", """
    package a
    A[X]:{}
    B:{}
    """);}
}
