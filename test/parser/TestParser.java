package parser;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import utils.Bug;
import utils.Err;

class TestParser {
  void ok(String expected, String content){
    String res = new Parser(Parser.dummy,content)
      .parseFullE(Bug::err,s->Optional.empty())
      .toString();
    Err.strCmpFormat(expected,res);
  }
  void fail(String expectedErr,String content){
    var b=new StringBuffer();
    var res=new Parser(Parser.dummy,content)
      .parseFullE(s->{b.append(s);return null;},s->Optional.empty());
    assertNull(res);
    Err.strCmp(expectedErr,b.toString());    
  }
  @Test void testMCall(){ ok("""
    MCall[
      receiver=x:infer,
      name=.m,
      ts=Optional.empty,
      es=[],
      t=infer
      ]
    """,
    "x.m"); }
  @Test void testTrue(){ ok(
    """
    Lambda[mdf=imm,
      its=[base.True[]],
      selfName=null,
      meths=[],
      t=infer]
    """, "base.True"); }
  @Test void testCat(){ ok(
      """
      Lambda[mdf=mut,
        its=[animals.Cat[]],
        selfName=null,
        meths=[],
        t=infer]
      """, "mut animals.Cat"); }
  @Test void testFail1(){ fail(
    "file:///home/nick/Programming/uni/fearless/Dummy.fear:1(col=3)mismatched input 'parse' expecting {'mut', 'lent', 'read', 'iso', 'recMdf', 'mdf', 'imm', FullCN}"
    ,"We parse a surprising amount of stuff"); }

}
