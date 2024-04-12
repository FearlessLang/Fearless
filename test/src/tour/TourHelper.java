package tour;

import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static utils.RunOutput.Res;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;

import main.CompilerFrontEnd;
import main.CompilerFrontEnd.ProgressVerbosity;

public class TourHelper {
  private static String lastLine(String text) {
    text=text.trim();
    int lastIndex = text.lastIndexOf("\n");
    return lastIndex == -1 ? text : text.substring(lastIndex + 1);
  }

  public static void run(String content){
    var last= lastLine(content);
    var aliases = Base.mutBaseAliases;
    if (!content.startsWith("package")){
      content = "package test\n" + content;
      aliases = "package test\n";
    }
    String expectedPrint= "";
    if (last.startsWith("//prints ")){
      expectedPrint = last.substring("//prints ".length()); 
    } 
    if(!content.contains(":Main") && !content.contains(": TestMain")){
      content += "Test:Main{s->Void}"; 
    }
    /*ok(new Res(expectedPrint,"",0), "test.Test",
      content, Base.mutBaseAliases);*/
    runCode(List.of(content),expectedPrint);
    //TODO: add case for errs?
  }
  static void checker(Process p,String expectedIO){
    Assertions.assertEquals(
      expectedIO,
      p.inputReader().lines().collect(Collectors.joining("\n")));
    Assertions.assertEquals(0,p.exitValue());
  };
  static void runCode(List<String> files, String expectedIO){
    var v= new CompilerFrontEnd.Verbosity(false,false,
      ProgressVerbosity.None);
    var runner= new main.java.TestMutLogicMain(
      List.of(), "test.Test",v, files,p->checker(p,expectedIO));
    runner.logicMain();
  }
}
