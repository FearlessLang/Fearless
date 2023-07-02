module FearlessCompiler {//TODO: can I use suppress warnings here?
  requires antlr4;
  requires org.antlr.antlr4.runtime;
  requires org.junit.jupiter.api;
  requires org.opentest4j;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.databind;
  requires java.compiler;
//  requires org.graalvm.truffle;
  requires commons.cli;
  requires cmdline.app;
  requires faux.pas;
  requires org.eclipse.lsp4j;
  requires org.eclipse.lsp4j.jsonrpc;
  requires java.logging;
}