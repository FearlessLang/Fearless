package main.java;

import astFull.Package;
import main.CompilerFrontEnd.Verbosity;
import parser.Parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public record TestMutLogicMain(
  List<String> commandLineArguments,
  String entry,
  Verbosity verbosity,
  List<String> files,
  Consumer<Process> res
  )
implements LogicMainJava{
  public String baseDir() {return "/base"; }
  public Map<String,List<Package>> parseApp(){
    var ps= IntStream.range(0,files.size())
      .mapToObj(i->new Parser(Path.of("Dummy"+i+".fear"),files.get(i)))
      .toList();
    return load(ps);
  }
  public void onStart(Process proc) { 
    proc.onExit().thenAccept(res).join();
  }

  @Override public void preStart(ProcessBuilder pb) {}
}