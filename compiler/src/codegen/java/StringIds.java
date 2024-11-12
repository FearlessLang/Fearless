package codegen.java;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ast.Program;
import codegen.MIR;
import id.Id;
import id.Mdf;

public final class StringIds{
  public static final StringIds $self = new StringIds();
  public Optional<String> getLiteral(Program p, Id.DecId d) {
    return p.superDecIds(d).stream()
      .map(Id.DecId::name)
      .filter(this::isLiteral)
      .findFirst();
  }
  public boolean isLiteral(String name) {
    return isDigit(name.codePointAt(0)) 
      || name.startsWith("\"")
      || name.startsWith("-")
      || name.startsWith("+");
  }
  public boolean isDigit(int codepoint){
    //Character.isDigit is way too relaxed
    return codepoint >= '0' && codepoint <= '9';
  }
  public boolean isAlphabetic(int codepoint){
    return (codepoint >= 'A' && codepoint <= 'Z') 
      || (codepoint >= 'a' && codepoint <= 'z');
    //return Character.isAlphabetic(codepoint);
    //Here instead I'm actually not sure what the best way is.
    //What do we want our identifiers to be?
  }
  public String getFullName(Id.DecId d) {
    return d.pkg()+"."+getSimpleName(d);
  }
  public String getSimpleName(Id.DecId d) {
    return getBase(d.shortName())+"_"+d.gen();//just to translate the '
  }

  public String getRelativeName(String currentPkg,Id.DecId d) {
    if(!currentPkg.equals(d.pkg())){ return getFullName(d); }
    return getSimpleName(d);
  }
  public String getMName(Mdf mdf, Id.MethName m) {
    return getBase(m.name())+"$"+mdf;
  }
  public String getFName(MIR.FName name) {
    return getMName(name.mdf(), name.m())+"$fun";
  }

  public String getBase(String name) {
    String _name=name;
    if (name.startsWith(".")) { name = name.substring(1); }
    return name.codePoints().mapToObj(c->{
      var base= isAlphabetic(c) || isDigit(c);
      if (base){ return Character.toString(c); }
      assert c != '.';
      var res= escape.get(c);
      assert res!=null
        :"not considered character ["+Character.toString(c)+"] in "+_name;
      return res;
    }).collect(Collectors.joining());
  }
  private static final Map<Integer, String> escape = Map.ofEntries(
    Map.entry((int)'_', "_"),
    Map.entry((int)'\'', "$apostrophe"),
    Map.entry((int)'+', "$plus"),
    Map.entry((int)'-', "$minus"),
    Map.entry((int)'*', "$asterisk"),
    Map.entry((int)'/', "$slash"),
    Map.entry((int)'\\', "$backslash"),
    Map.entry((int)'|', "$pipe"),
    Map.entry((int)'!', "$exclamation"),
    Map.entry((int)'@', "$at"),
    Map.entry((int)'#', "$hash"),
    Map.entry((int)'$', "$"),
    Map.entry((int)'%', "$percent"),
    Map.entry((int)'^', "$caret"),
    Map.entry((int)'&', "$ampersand"),
    Map.entry((int)'?', "$question"),
    Map.entry((int)'~', "$tilde"),
    Map.entry((int)'<', "$lt"),
    Map.entry((int)'>', "$gt"),
    Map.entry((int)'=', "$equals"),
    Map.entry((int)':', "$colon")
  );
  private static final String[] keywords = {
    "abstract", "assert", "boolean", "break", "byte",
    "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "enum", "extends",
    "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native",
    "new", "package", "private", "protected", "public", "return",
    "short", "static", "strictfp", "super", "switch", "synchronized",
    "this", "throw", "throws", "transient", "try", "void", "volatile",
    "while", "true", "false", "null"
    };
  private static final Map<String,String> keywordsMap = Stream.of(keywords)
    .collect(Collectors.toMap(e->e,e->"$"+e));
  public String varName(String name) {
    return Optional.ofNullable(keywordsMap.get(name))
      .orElse(name.replace("'","$")+"_m$");
  }
}