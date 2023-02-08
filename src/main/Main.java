package main;

import astFull.E;
import codegen.MIR;
import id.Id;
import program.Program;

public class Main {
  public static void resetAll(){
    E.X.reset();
    Id.GX.reset();
    Program.reset();
    MIR.L.reset();
  }
  public static void main(String[] args) {
    //   if(!exists){ Files.createDirectories(path); }//will be needed to parse a whole project

  }

}
