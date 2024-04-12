package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput;

import static codegen.java.RunJavaProgramTests.ok;

public class Ex11UtilityTraitsTest {
  @Test void flowSumStr() { ok(new RunOutput.Res("", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let[mut Person] personMut = {FPerson#("Bob")}
      .let[read Person] personRead = {Foo#{personMut}}
      .assert{personMut.name == (personRead.name)}
      .do{personMut.name("Alice")}
      .assert{personRead.name == "Alice"}
      .return{{}}
      }
    Foo:{ #(f: read F[read Person]): read Person -> f# }
    FPerson:{ #(name: Str): mut Person -> Block#
      .let nameRef = {Ref#name}
      .return {mut Person:{
        read .name: Str -> nameRef.get.toImm,
        mut .name(newName: Str): Void -> nameRef.set(newName),
        }}
      }
    """, Base.mutBaseAliases);}
}
