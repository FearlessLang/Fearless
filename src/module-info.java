module FearlessCompiler {//TODO: can I use suppress warnings here?
  requires antlr4;
  requires org.antlr.antlr4.runtime;
  requires org.junit.jupiter.api;
  requires org.opentest4j;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires java.compiler;
  requires org.graalvm.truffle;
  requires commons.cli;
}