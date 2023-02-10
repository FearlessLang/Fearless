package codegen.java;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestJavaCodegen {
  void ok(String expected, String entry, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor().visitProgram(inferred);
    var java = new JavaCodegen().visitProgram(mir.pkgs(), new Id.DecId(entry, 0));
    Err.strCmp(expected, java);
  }

  @Test void emptyProgram() { ok("""
    interface test{
    }
    """, "fake", """
    package test
    """);}

  @Test void simpleProgram() { ok("""
interface test{interface Foo_0{
}
interface Bar_0 extends test.Baz_1{
default test.Baz_1<test.Bar_0> loop$(){
var f$thiz = this;
return f$thiz.loop$();
}
default test.Foo_0 $35$(){
var f$thiz = this;
return new Fear7$36_0(){
};
}}
interface Yo_0{
default test.Ok_0 lm$(){
var f$thiz = this;
return new Fear8$36_0(){
public test.Ok_0 $35$(){
var ok$ = this;
return ok$.$35$();
}};
}}
interface Fear7$36_0 extends test.Foo_0{
}
interface Ok_0{
test.Ok_0 $35$();}
interface Baz_1<X>{
X $35$();}
interface Fear8$36_0 extends test.Ok_0{
}
}
    """, "fake", """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """);}

  @Test void bools() {ok("""
    interface test{interface ThenElse_1<R>{
    R else$();
    R then$();}
    interface True_0 extends test.Bool_0{
    default <X0$470$36> X0$470$36 $63$(test.ThenElse_1<X0$470$36> f$){
    var f$thiz = this;
    return f$.then$();
    }
    default test.Bool_0 not$(){
    var f$thiz = this;
    return new Fear7$36_0(){
    };
    }
    default test.Bool_0 or$(test.Bool_0 b$){
    var f$thiz = this;
    return f$thiz;
    }
    default test.Bool_0 and$(test.Bool_0 b$){
    var f$thiz = this;
    return b$;
    }}
    interface Bool_0 extends test.Sealed_0{
    <R> R $63$(test.ThenElse_1<R> f$);
    test.Bool_0 not$();
    test.Bool_0 or$(test.Bool_0 b$);
    test.Bool_0 and$(test.Bool_0 b$);}
    interface False_0 extends test.Bool_0{
    default <X0$470$36> X0$470$36 $63$(test.ThenElse_1<X0$470$36> f$){
    var f$thiz = this;
    return f$.else$();
    }
    default test.Bool_0 not$(){
    var f$thiz = this;
    return new Fear8$36_0(){
    };
    }
    default test.Bool_0 or$(test.Bool_0 b$){
    var f$thiz = this;
    return b$;
    }
    default test.Bool_0 and$(test.Bool_0 b$){
    var f$thiz = this;
    return f$thiz;
    }}
    interface Sealed_0{
    }
    interface Fear7$36_0 extends test.Bool_0,test.False_0{
    }
    interface Fear8$36_0 extends test.Bool_0,test.True_0{
    }
    }
    """, "fake.Fake", """
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
    """);}
  @Test void multiPackage() { ok("""
    """, "test.HelloWorld", """
    package test
    alias base.Main as Main, alias base.Void as Void,
    HelloWorld:Main{
      #s -> Foo
    }
    Foo:{}
    """, """
    package base
    Void:{} Sealed:{}
    Main:{ #[R](s: lent System): R }
    System:Sealed{}
    """); }
}
