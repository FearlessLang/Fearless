package utils;

import program.TypeSystemFeatures;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public interface Base {
  static ast.Program ignoreBase(ast.Program p) {
    return new ast.Program(
      new TypeSystemFeatures(),
      p.ds().entrySet().stream()
        .filter(kv->!kv.getKey().name().startsWith("base."))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
      Map.of()
    );
  }

  static String load(String file) {
    try {
      var root = Path.of(Thread.currentThread().getContextClassLoader().getResource("base").toURI());
      return read(root.resolve(file));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  static String loadImm(String file) {
    try {
      var root = Path.of(Thread.currentThread().getContextClassLoader().getResource("immBase").toURI());
      return read(root.resolve(file));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  static String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static String[] readAll(String prefix) {
    try {
      var root = Thread.currentThread().getContextClassLoader().getResource(prefix).toURI();
      try(var fs = Files.walk(Path.of(root))) {
        return fs
          .filter(Files::isRegularFile)
          .map(Base::read)
          .toArray(String[]::new);
      }
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  String[] baseLib = readAll("base");
  String[] immBaseLib = readAll("immBase");

  String minimalBase = """
    package base
    Main:{ #(s: mut System): Void }
    Sealed:{}
    Void:{}
    
    System:Sealed{}
    """;

  String mutBaseAliases = """
    package test
    alias base.iter.Iter as Iter,
    
    alias base.caps.IsoPod as IsoPod,
    
    alias base.caps.Write as Write,
    alias base.caps.Read as Read,
    alias base.caps.FileHandleMode as FileHandleMode,
    alias base.caps.Create as Create,
    
    alias base.caps.FIO as FIO,
    alias base.caps.IO as IO,
    
    alias base.caps.Env as Env,
    alias base.caps.FEnv as FEnv,
    
    alias base.caps.System as System,
    
    alias base.Res as Res,
    
    alias base.LinkedLens as LinkedLens,
    alias base.Map as Map,
    alias base.EmptyMap as EmptyMap,
    alias base.Lens as Lens,
    
    alias base.Extensible as Extensible,
    alias base.Extension as Extension,
    
    alias base.Either as Either,
    
    alias base.Block as Block,
    
    alias base.Str as Str,
    alias base.StrMap as StrMap,
    alias base.Stringable as Stringable,
    
    alias base.Ref as Ref,
    alias base.Count as Count,
    alias base.RefImm as RefImm,
    
    alias base.Opt as Opt,
    alias base.OptMap as OptMap,
    
    alias base.Int as Int,
    alias base.Float as Float,
    alias base.UInt as UInt,
    
    alias base.LList as LList,
    alias base.List as List,
    alias base.Collection as Collection,
    
    alias base.As as As,
    alias base.Box as Box,
    alias base.Sealed as Sealed,
    alias base.Magic as Magic,
    alias base.F as F,
    alias base.Consumer as Consumer,
    alias base.Let as Let,
    alias base.Void as Void,
    alias base.HasIdentity as HasIdentity,
    
    alias base.Ice as Ice,
    alias base.Freezer as Freezer,
    alias base.ToImm as ToImm,
    
    alias base.Abort as Abort,
    alias base.Main as Main,
    
    alias base.Error as Error,
    alias base.Try as Try,
    alias base.Info as Info,
    
    alias base.Bool as Bool,
    alias base.True as True,
    alias base.False as False,
    
    alias base.Assert as Assert,
    
    alias base.flows.Flow as Flow,
    
    alias base.Ordering as Ordering,
    alias base.FOrdering as FOrdering,
    """;
}
