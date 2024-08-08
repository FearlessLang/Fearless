package program.typesystem;

import org.junit.jupiter.api.Test;

import static program.typesystem.RunTypeSystem.ok;

public class TestApplierSugar {
  @Test void identityCheckNoSugar() {ok("""
    package test
    alias base.F as F,
    
    Foo: {}
    A: Foo
    
    Lib: {.m1[T](f: F[T,Foo], x: T): Foo -> f#x}
    Usage: {#(a: A): Foo -> Lib.m1({x->x}, a)}
    """, """
    package base
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]: { read #(a: A): R }
    """);}
  @Test void identityCheck() {ok("""
    package test
    alias base.F as F,
    
    Foo: {}
    A: Foo
    
    Lib: {.m1[T](f: F[T,Foo], x: T): Foo -> f#x}
    Usage: {#(a: A): Foo -> Lib.m1({::}, a)}
    """, """
    package base
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]: { read #(a: A): R }
    """);}

  @Test void identityCheckCall() {ok("""
    package test
    alias base.F as F,
    
    Foo: {}
    A: {.foo: Foo -> Foo}
    
    Lib: {.m1[T](f: F[T,Foo], x: T): Foo -> f#x}
    Usage: {#(a: A): Foo -> Lib.m1({::.foo}, a)}
    """, """
    package base
    F[A:read,mut,imm,iso,R:read,mut,imm,iso]: { read #(a: A): R }
    """);}
}
