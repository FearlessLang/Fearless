package main.java;

import astFull.Package;
import main.CompilerFrontEnd.Verbosity;
import rt.ResolveResource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record MutLogicMain(
  List<String> commandLineArguments,
  String entry,
  Path userApp,
  Verbosity verbosity
  )
implements LogicMainJava{
  public String baseDir() {return "/base"; }
  public Map<String,List<Package>> parseApp(){
    return load(loadFiles(userApp()));
  }
  public void onStart(Process proc) {
    proc.onExit().join();
    System.exit(proc.exitValue());
  }

}