package utils;

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
      p.ds().entrySet().stream()
        .filter(kv->!kv.getKey().name().startsWith("base."))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
      p.compilationCaches()
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
    Main:{ #(s: lent System[Void]): Void }
    NoMutHyg[X]:{}
    Sealed:{}
    Void:{}
    
    System[R]:Sealed{}
    """;

  String mutBaseAliases = """
    package test
    alias base.As as As,
    alias base.caps.UseCapCont as UseCapCont,
    alias base.caps.System as System,
    alias base.caps.FCap as FCap,
    alias base.caps.LentReturnStmt as LentReturnStmt,
    alias base.caps.IO as IO,
    alias base.caps.FIO as FIO,
    alias base.caps.Env as Env,
    alias base.caps.FEnv as FEnv,
    alias base.caps.IsoPod as IsoPod,
    alias base.caps.IsoViewer as IsoViewer,
    alias base.caps.IsoMutator as IsoMutator,
    alias base.Error as Error,
    alias base.Info as Info,
    alias base.BlockIfTrue as BlockIfTrue,
    alias base.DecidedBlock as DecidedBlock,
    alias base.BlockIfFalse as BlockIfFalse,
    alias base.BlockIf as BlockIf,
    alias base.Continuation as Continuation,
    alias base.DoRunner as DoRunner,
    alias base.Block as Block,
    alias base.Do as Do,
    alias base.Condition as Condition,
    alias base.ReturnStmt as ReturnStmt,
    alias base.Box as Box,
    alias base.Sealed as Sealed,
    alias base.Debug as Debug,
    alias base.F as F,
    alias base.Let as Let,
    alias base.Yeet as Yeet,
    alias base.NoMutHyg as NoMutHyg,
    alias base.Loop as Loop,
    alias base.Void as Void,
    alias base.HasIdentity as HasIdentity,
    alias base.Main as Main,
    alias base.IntOps as IntOps,
    alias base.MathOps as MathOps,
    alias base.Int as Int,
    alias base.Float as Float,
    alias base.UInt as UInt,
    alias base.ThenElse as ThenElse,
    alias base.ThenElseHyg as ThenElseHyg,
    alias base.Bool as Bool,
    alias base.True as True,
    alias base.False as False,
    alias base.AssertCont as AssertCont,
    alias base.Assert as Assert,
    alias base.Ref as Ref,
    alias base.RefImm as RefImm,
    alias base.Count as Count,
    alias base.UpdateRef as UpdateRef,
    alias base.Str as Str,
    alias base.Stringable as Stringable,
    alias base.LList as LList,
    alias base.LListMatch as LListMatch,
    alias base.LListIter as LListIter,
    alias base.ListIter as ListIter,
    alias base.List as List,
    alias base.Cons as Cons,
    alias base.Opt as Opt,
    alias base.OptMap as OptMap,
    alias base.OptMatchHyg as OptMatchHyg,
    alias base.OptMatch as OptMatch,
    alias base.iter.Iter as Iter,
    alias base.iter.Sum as Sum,
    alias base.Abort as Abort,
    alias base.StrMap as StrMap,
    alias base.Map as Map,
    alias base.LensMap as LensMap,
    alias base.LinkedMap as LinkedMap,
    alias base.Try as Try,
    """;
}
