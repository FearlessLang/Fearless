package codegen.go;

import codegen.MIRInjectionVisitor;
import codegen.java.JavaCodegen;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TestGoCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.immBaseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    inferred.typeCheck();
    var mirVisitor = new MIRInjectionVisitor(inferred);
    var mir = mirVisitor.visitProgram();
    var go = new GoCodegen(mirVisitor.getProgram()).visitProgram(mir.pkgs(), new Id.DecId(entry, 0)).relative();
    Err.strCmp(expected, go);
  }

  @Test void emptyProgram() { ok("""
package main
type baseφSystem_1 interface {
}
type baseφSystem_1Impl struct {}
type baseφSealed_0 interface {
}
type baseφSealed_0Impl struct {}
type baseφNoMutHyg_1 interface {
}
type baseφNoMutHyg_1Impl struct {}
type baseφVoid_0 interface {
}
type baseφVoid_0Impl struct {}
type baseφMain_0 interface {
φ35φ(sφ baseφSystem_1) baseφVoid_0
}
type baseφMain_0Impl struct {}

func main(){
var entry baseφMain_0 = fakeφFake_0Impl{}
entry.φ35φ(baseφ46capsφφ95System_0{})
}
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void simpleProgram() { ok("""
    package main
    type testφBar_0 interface {
    loopφ() testφBaz_1
    φ35φ() testφFoo_0
    }
    type testφBar_0Impl struct {}
    func (this testφBar_0Impl) loopφ() testφBaz_1 {
     return this.loopφ()
    }
    func (this testφBar_0Impl) φ35φ() testφFoo_0 {
     return testφFoo_0Impl{}
    }
    type testφFoo_0 interface {
    }
    type testφFoo_0Impl struct {}
    type testφOk_0 interface {
    φ35φ() testφOk_0
    }
    type testφOk_0Impl struct {}
    type testφBaz_1 interface {
    φ35φ() interface{}
    }
    type testφBaz_1Impl struct {}
    type testφYo_0 interface {
    lmφ() testφOk_0
    }
    type testφYo_0Impl struct {}
    func (this testφYo_0Impl) lmφ() testφOk_0 {
     return testφOk_0Impl{}
    }
    type baseφSystem_1 interface {
    }
    type baseφSystem_1Impl struct {}
    type baseφSealed_0 interface {
    }
    type baseφSealed_0Impl struct {}
    type baseφNoMutHyg_1 interface {
    }
    type baseφNoMutHyg_1Impl struct {}
    type baseφVoid_0 interface {
    }
    type baseφVoid_0Impl struct {}
    type baseφMain_0 interface {
    φ35φ(sφ baseφSystem_1) baseφVoid_0
    }
    type baseφMain_0Impl struct {}
        
    func main(){
    var entry baseφMain_0 = fakeφFake_0Impl{}
    entry.φ35φ(baseφ46capsφφ95System_0{})
    }
    """, "fake.Fake", false, """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """, Base.minimalBase);}

  @Test void bools() {ok("""
package main
type testφTrue_0 interface {
notφ() testφBool_0
φ63φ(fφ interface{}) interface{}
orφ(bφ testφBool_0) testφBool_0
andφ(bφ testφBool_0) testφBool_0
}
type testφTrue_0Impl struct {}
func (this testφTrue_0Impl) notφ() testφBool_0 {
 return testφFalse_0Impl{}
}
func (this testφTrue_0Impl) φ63φ(fφ interface{}) interface{} {
 return fφ.(testφThenElse_1).thenφ()
}
func (this testφTrue_0Impl) orφ(bφ testφBool_0) testφBool_0 {
 return this
}
func (this testφTrue_0Impl) andφ(bφ testφBool_0) testφBool_0 {
 return bφ.(testφBool_0)
}
type testφFalse_0 interface {
notφ() testφBool_0
φ63φ(fφ interface{}) interface{}
orφ(bφ testφBool_0) testφBool_0
andφ(bφ testφBool_0) testφBool_0
}
type testφFalse_0Impl struct {}
func (this testφFalse_0Impl) notφ() testφBool_0 {
 return testφTrue_0Impl{}
}
func (this testφFalse_0Impl) φ63φ(fφ interface{}) interface{} {
 return fφ.(testφThenElse_1).elseφ()
}
func (this testφFalse_0Impl) orφ(bφ testφBool_0) testφBool_0 {
 return bφ.(testφBool_0)
}
func (this testφFalse_0Impl) andφ(bφ testφBool_0) testφBool_0 {
 return this
}
type testφThenElse_1 interface {
thenφ() interface{}
elseφ() interface{}
}
type testφThenElse_1Impl struct {}
type testφBool_0 interface {
notφ() testφBool_0
φ63φ(fφ interface{}) interface{}
orφ(bφ testφBool_0) testφBool_0
andφ(bφ testφBool_0) testφBool_0
}
type testφBool_0Impl struct {}
type testφSealed_0 interface {
}
type testφSealed_0Impl struct {}
type baseφSystem_1 interface {
}
type baseφSystem_1Impl struct {}
type baseφSealed_0 interface {
}
type baseφSealed_0Impl struct {}
type baseφNoMutHyg_1 interface {
}
type baseφNoMutHyg_1Impl struct {}
type baseφVoid_0 interface {
}
type baseφVoid_0Impl struct {}
type baseφMain_0 interface {
φ35φ(sφ baseφSystem_1) baseφVoid_0
}
type baseφMain_0Impl struct {}

func main(){
var entry baseφMain_0 = fakeφFake_0Impl{}
entry.φ35φ(baseφ46capsφφ95System_0{})
}
    """, "fake.Fake", false, """
    package test
    Sealed:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
package main
type testφHelloWorld_0 interface {
φ35φ(sφ baseφSystem_1) baseφVoid_0
}
type testφHelloWorld_0Impl struct {}
func (this testφHelloWorld_0Impl) φ35φ(sφ baseφSystem_1) baseφVoid_0 {
 return baseφVoid_0Impl{}
}
type baseφSystem_1 interface {
}
type baseφSystem_1Impl struct {}
type baseφSealed_0 interface {
}
type baseφSealed_0Impl struct {}
type baseφNoMutHyg_1 interface {
}
type baseφNoMutHyg_1Impl struct {}
type baseφVoid_0 interface {
}
type baseφVoid_0Impl struct {}
type baseφMain_0 interface {
φ35φ(sφ baseφSystem_1) baseφVoid_0
}
type baseφMain_0Impl struct {}

func main(){
var entry baseφMain_0 = testφHelloWorld_0Impl{}
entry.φ35φ(baseφ46capsφφ95System_0{})
}
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
package main
type testφFoo_0 interface {
aφ() testφFoo_0
}
type testφFoo_0Impl struct {}
type testφTest_0 interface {
φ35φ(fear0$φ baseφSystem_1) baseφVoid_0
}
type testφTest_0Impl struct {}
func (this testφTest_0Impl) φ35φ(fear0$φ baseφSystem_1) baseφVoid_0 {
 return baseφVoid_0Impl{}
}
type testφA_0 interface {
φ35φ() testφ46fooφBar_0
}
type testφA_0Impl struct {}
func (this testφA_0Impl) φ35φ() testφ46fooφBar_0 {
 return testφ46fooφBar_0Impl{}
}
type testφ46fooφBar_0 interface {
aφ() testφFoo_0
}
type testφ46fooφBar_0Impl struct {}
func (this testφ46fooφBar_0Impl) aφ() testφFoo_0 {
 return this
}
type baseφSystem_1 interface {
}
type baseφSystem_1Impl struct {}
type baseφSealed_0 interface {
}
type baseφSealed_0Impl struct {}
type baseφNoMutHyg_1 interface {
}
type baseφNoMutHyg_1Impl struct {}
type baseφVoid_0 interface {
}
type baseφVoid_0Impl struct {}
type baseφMain_0 interface {
φ35φ(sφ baseφSystem_1) baseφVoid_0
}
type baseφMain_0Impl struct {}

func main(){
var entry baseφMain_0 = testφTest_0Impl{}
entry.φ35φ(baseφ46capsφφ95System_0{})
}
    """, "test.Test", false, """
    package test
    Test:base.Main[]{ _ -> {} }
    A:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """, Base.minimalBase);}

  @Test void fullBase() { ok("""
    """, "test.Test", true, """
    package test
    alias base.Main as Main,
    alias base.Void as Void,
    Test:Main{ _ -> "hi" }
    """); }
}
