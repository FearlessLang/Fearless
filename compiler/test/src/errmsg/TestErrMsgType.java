package errmsg;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;
import static program.typesystem.RunTypeSystem.fail;

public class TestErrMsgType {
  @Test void methodThatExists() {ok("""
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callAMethod: A -> A.meth1,
      }
    """);}

  @Test void nonExistentMethod() {
    fail("""
    In position [###]/Dummy0.fear:6:26
    [E36 undefinedMethod]
    Method <.meh1> with 0 args does not exist in <imm test.A[]>
    Did you mean <test.A.meth1()>
    
    Other candidates:
    test.B[].callNonExistent(): imm test.A[]
    """, """
    package test
    A: {
      .meth1: A -> this,
      }
    B: {
      .callNonExistent: A -> A.meh1,
      }
    """);
  }

  @Test void multipleAlternatives() {fail("""
   In position [###]/Dummy0.fear:9:26
   [E36 undefinedMethod]
   Method <.meh1> with 0 args does not exist in <imm test.A[]>
   Did you mean <test.A.meth1()>
   
   Other candidates:
   test.A[].meth2(): imm test.A[]
   test.A[].k(): imm test.A[]
   test.A[].randommeth(): imm test.A[]
   test.B[].callNonExistent(): imm test.A[]
   """, """
   package test
   A: {
     .meth1: A -> this,
     .randommeth: A -> this,
     .k: A -> this,
     .meth2: A -> this,
     }
   B: {
     .callNonExistent: A -> A.meh1,
     }
   """);}

  @Test void multipleAlternatives2() {fail("""
   In position [###]/Dummy0.fear:9:26
   [E36 undefinedMethod]
   Method <.meh1> with 0 args does not exist in <imm test.A[]>
   Did you mean <test.A.meth1()>
   
   Other candidates:
   test.C[].meh1(): imm test.C[]
   test.A[].meth2(imm test.B[], imm test.B[]): imm test.A[]
   test.A[].k(): imm test.A[]
   test.A[].randommeth(): imm test.A[]
   test.B[].callNonExistent(): imm test.A[]
   """, """
   package test
   A: {
     .meth1: A -> this,
     .randommeth: A -> this,
     .k: A -> this,
     .meth2(a:B, b:B): A -> this,
     }
   B: {
     .callNonExistent: A -> A.meh1,
     }
   C: {
     .meh1: C -> this,
   }
   """);}

  @Test void multipleAlternatives3() {fail("""
   In position [###]/Dummy0.fear:9:26
   [E36 undefinedMethod]
   Method <.meh1> with 0 args does not exist in <imm test.A[]>
   Did you mean <test.C.meh1()>
   
   Other candidates:
   test.A[].method1(): imm test.A[]
   test.A[].method2(imm test.B[], imm test.B[]): imm test.A[]
   test.A[].k(): imm test.A[]
   test.A[].randommeth(): imm test.A[]
   test.B[].callNonExistent(): imm test.A[]
   """, """
   package test
   A: {
     .method1: A -> this,
     .randommeth: A -> this,
     .k: A -> this,
     .method2(a:B, b:B): A -> this,
     }
   B: {
     .callNonExistent: A -> A.meh1,
     }
   C: {
     .meh1: C -> this,
   }
   """);}

  @Test void differentArguments() {fail("""
  In position [###]/Dummy0.fear:9:24
  [E36 undefinedMethod]
  Method <.meth1> with 2 args does not exist in <imm test.A[]>
  Did you mean <test.A.meth1(imm test.B[])>
  
  Other candidates:
  test.A[].meth1(imm test.B[], imm test.B[], imm test.B[], imm test.B[]): imm test.A[]
  test.A[].meth2(): imm test.A[]
  test.A[].anothermeth(): imm test.A[]
  test.B[].callNonExistent(): imm test.A[]
  """, """
  package test
  A: {
  .meth1(a: B, b: B, c: B, d: B): A -> this,
  .meth1(a: B): A -> this,
  .meth2: A -> this,
  .anothermeth: A -> this,
  }
  B: {
  .callNonExistent: A -> A.meth1(this, this),
  }
  """);}
}
