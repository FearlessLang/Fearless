package main;

import java.nio.file.Path;

public class RunAntlr {
  public static void main(String[] args) {
    String input=Path.of("antlrGrammars", "Fearless.g4").toAbsolutePath().toString();
    String dest=Path.of("").toAbsolutePath().getParent().resolve(Path.of("compiler", "src", "generated")).toString();
//    String input=Path.of("grammar","antlrGrammars", "Fearless.g4").toAbsolutePath().toString();
//    String dest=Path.of("").toAbsolutePath().resolve(Path.of("compiler", "src", "generated")).toString();
    System.out.println("Writing to: "+dest);
    org.antlr.v4.Tool.main(new String[]{"-o",dest, "-visitor",input});
  }
}
