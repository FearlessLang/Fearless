package errmsg;

import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import utils.Err;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestErrMsgSyntax {
  void fail(String expectedErr, String... content){
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    try {
      var res = Parser.parseAll(ps, new TypeSystemFeatures());
      Assertions.fail("Parsing did not fail. Got: "+res);
    }
    catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    }
  }

  @Test void failBasicMismatch() { fail(
    """
    [###]Dummy0.fear:2:5
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 2 pos 5
    2: Bar:{)
           ^^ mismatched close, is it meant to be '}'?
           |
           unclosed open
    """,
    """
    package pkg1
    Bar:{)
    """
  );}

  @Test void failSingleUnclosed() {fail(
    """
    [###]Dummy0.fear:3:0
    [E59 syntaxError]
    Error: unclosed opening parenthesis '{' at line 2 pos 4
    2: Bar:{
           ^ unclosed parenthesis
    """,
    """
    package pkg1
    Bar:{
    """
  );}

  @Test void failSingleUnopened() {fail(
    """
    [###]Dummy0.fear:2:4
    [E59 syntaxError]
    Error: unexpected closing parenthesis '}' at line 2 pos 4
    2: Bar:}
           ^ unexpected close
    """,
    """
    package pkg1
    Bar:}
    """
  );}

  @Test void failMultiLineCurly() { fail(
  """
    [###]Dummy0.fear:4:0
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 4 pos 0
    2  : Bar:{
             ^ unclosed open
    3  :   Hello:{}
    4  : )
         ^ mismatched close, is it meant to be '}'?
    """,
  """
    package pkg1
    Bar:{
      Hello:{}
    )
    """
  );}

  @Test void failLongMultiLineCurly() { fail(
    """
    [###]Dummy0.fear:9:0
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 9 pos 0
    2  : Bar:{
             ^ unclosed open
    3  :   // Show this line
    4-7: ... ... ...
    8  :   Hello:{}  // Show this line
    9  : )
         ^ mismatched close, is it meant to be '}'?
    """,
  """
    package pkg1
    Bar:{
      // Show this line
      // Hide these lines
      //
      //
      //
      Hello:{}  // Show this line
    )
    """
  );}

  @Test void failIgnoreComment() {fail(
    """
    [###]Dummy0.fear:3:5
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 3 pos 5
    3: Bar:{)
           ^^ mismatched close, is it meant to be '}'?
           |
           unclosed open
    """,
    """
    package pkg1
    // This should not be the error {()[}}{
    Bar:{)
    """
  );}

  @Test void failIgnoreMultiComment() {fail(
    """
    [###]Dummy0.fear:4:5
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 4 pos 5
    4: Bar:{)
           ^^ mismatched close, is it meant to be '}'?
           |
           unclosed open
    """,
    """
    package pkg1
    /* This should not be the error {()[}}{
        and this ({]}}}()) */
    Bar:{)
    """
  );}

  @Test void failIgnoreString() {fail(
    """
    [###]
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 2 pos 50
    2: Hello: Main{ s -> s.println("Hello World [)]((]") )
                  ^--------------------------------------^ mismatched close, is it meant to be '}'?
                  |
                  unclosed open
    """,
    """
    package pkg1
    Hello: Main{ s -> s.println("Hello World [)]((]") )
    """
  );}

  @Test void failIgnoreStringEscape() {fail(
    """
    [###]
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 2 pos 52
    2: Hello: Main{ s -> s.println("Hello World \\"[)]((]") )
                  ^----------------------------------------^ mismatched close, is it meant to be '}'?
                  |
                  unclosed open
    """,
    """
    package pkg1
    Hello: Main{ s -> s.println("Hello World \\"[)]((]") )
    """
  );}

  @Test void failIgnoreMultiString() {fail(
    """
    [###]
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 5 pos 6
    2  : Hello: Main{ s -> s.println( ""\"
                    ^ unclosed open
    3  :  | Hello World
    4  :  | [)]((]
    5  : ""\" ) )
               ^ mismatched close, is it meant to be '}'?
    """,
    """
    package pkg1
    Hello: Main{ s -> s.println( \"""
     | Hello World
     | [)]((]
    \""" ) )
    """
  );}

  @Test void failIgnoreMultiStringLine() {fail(
    """
    [###]
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 5 pos 6
    2  : Hello: Main{ s -> s.println( ""\"
                    ^ unclosed open
    3  :  | Hello World
    4  :  | [)]((] \"""
    5  : ""\" ) )
               ^ mismatched close, is it meant to be '}'?
    """,
    """
    package pkg1
    Hello: Main{ s -> s.println( \"""
     | Hello World
     | [)]((] \"""
    \""" ) )
    """
  );}

  @Test void failIgnoreEscapeMultiString() {fail(
    """
    [###]
    [E59 syntaxError]
    Error: mismatched closing parenthesis ')' at line 6 pos 6
    2  : Hello: Main{ s -> s.println( ""\"
                    ^ unclosed open
    3  :  | Hello World
    4-4: ... ... ...
    5  :  | asd \\""\"
    6  : ""\" ) )
               ^ mismatched close, is it meant to be '}'?
    """,
    """
    package pkg1
    Hello: Main{ s -> s.println( \"""
     | Hello World
     | [)]((]
     | asd \\"\""
    \""" ) )
    """
  );}
}
