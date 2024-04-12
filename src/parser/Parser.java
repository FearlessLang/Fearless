package parser;

import astFull.E;
import astFull.Package;
import astFull.Program;
import astFull.T;
import astFull.T.Alias;
import failure.Fail;
import failure.ParserErrors;
import files.Pos;
import generated.FearlessLexer;
import generated.FearlessParser;
import generated.FearlessParser.NudeEContext;
import generated.FearlessParser.NudeProgramContext;
import id.Id;
import org.antlr.v4.runtime.*;
import program.TypeSystemFeatures;
import utils.Bug;
import visitors.FullEAntlrVisitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Parser(Path fileName,String content){
  public Parser of(String fileName){ return of(Paths.get(fileName)); }
  public static final Path dummy = Path.of("Dummy.fear");
  public Parser of(Path path){
    assert Files.exists(path);
    assert !Files.isDirectory(path);
    try{ return new Parser(path,codeFromPath(path)); }
    catch(IOException e){ throw Bug.of(e); }
  }
  public static String codeFromPath(Path path) throws IOException{
    String code = Files.readString(path,StandardCharsets.US_ASCII);
    code = code.replace("\r","");
    preCheck(code);
    return code;
  }
  public static void preCheck(String code){
    //TODO: we may want to check a bunch of stuff
    //No strange characters
    //balanced parenthesis with decent error
  }

  public static Program parseAll(List<Parser> ps, TypeSystemFeatures tsf) {
    // We currently do not have any global aliases in Fearless
    List<Alias> globals = List.of();
    var all=ps.stream()
        .map(p->p.parseFile(Bug::err))
        .collect(Collectors.groupingBy(Package::name));
    var allPs=all.values().stream()
        .map(allPi->Package.merge(globals, allPi))
        .toList();
    assert allPs.stream().map(Package::name).distinct().count()==allPs.size();//redundant?
    return new Program(tsf, Collections.unmodifiableMap(allPs.stream().map(Package::parse).reduce(new HashMap<>(),
        (acc, val) -> { acc.putAll(val); return acc; },
        (m1, m2) -> { assert m1==m2; return m1;}
    )));
  }
  public static Program parseAll(Map<String, List<Package>> ps, TypeSystemFeatures tsf) {
    var allPs=ps.values().stream()
      .map(allPi->Package.merge(List.of(), allPi))
      .toList();
    assert allPs.stream().map(Package::name).distinct().count()==allPs.size();//redundant?
    return new Program(tsf, Collections.unmodifiableMap(allPs.stream().map(Package::parse).reduce(new HashMap<>(),
      (acc, val) -> { acc.putAll(val); return acc; },
      (m1, m2) -> { assert m1==m2; return m1;}
    )));
  }

  public E parseFullE(Function<String,E> orElse,Function<String,Optional<Id.IT<T>>> resolve){
      var l = new FearlessLexer(CharStreams.fromString(content));
      var p = new FearlessParser(new CommonTokenStream(l));
      var errorst = new StringBuilder();
      var errorsp = new StringBuilder();
      FailConsole.setFail(fileName, l, p, errorst, errorsp);
      NudeEContext res = p.nudeE();
      var ok = errorst.isEmpty() && errorsp.isEmpty();
      if(ok){ return new FullEAntlrVisitor(fileName,resolve).visitNudeE(res); }
      //TODO: better errors below
      if(!errorst.isEmpty()){ return orElse.apply(errorst.toString()); }
      return orElse.apply(errorsp.toString());
  }
  public astFull.T parseFullT(){
    var l = new FearlessLexer(CharStreams.fromString(content));
    var p = new FearlessParser(new CommonTokenStream(l));
    var errorst = new StringBuilder();
    var errorsp = new StringBuilder();
    FailConsole.setFail(fileName, l, p, errorst, errorsp);
    FearlessParser.NudeTContext res = p.nudeT();
    var ok = errorst.isEmpty() && errorsp.isEmpty();
    if(ok){ return new FullEAntlrVisitor(fileName,s->Optional.empty()).visitNudeT(res); }
    throw Bug.unreachable();
  }
  
  public boolean parseX(){ return parseId(p->p.nudeX().getText());}
  public boolean parseM(){ return parseId(p->p.nudeM().getText());}
  public boolean parseFullCN(){ return parseId(p->p.nudeFullCN().getText());}
  public boolean parseGX(){
    if(!parseFullCN()){ return false; }
    //can not use AntlrApi since FullCN uses fragments for subcases
    String noStart="0123456789\"";
    var ok=!content.contains(".") && !noStart.contains(content.substring(0,1));
    return ok;
  }
  
  private boolean parseId(Function<FearlessParser,String> checker){
    var l = new FearlessLexer(CharStreams.fromString(content));
    var p = new FearlessParser(new CommonTokenStream(l));
    var errorst = new StringBuilder();
    var errorsp = new StringBuilder();
    FailConsole.setFail(fileName, l, p, errorst, errorsp);
    var res=checker.apply(p);
    var ok = errorst.isEmpty() && errorsp.isEmpty() && res.equals(content+"<EOF>");
    //need check res==content otherwise there may be spaces/comments too 
    return ok ;
  }
  public Package parseFile(Function<String,Package> orElse){
    var l = new FearlessLexer(CharStreams.fromString(content));
    var p = new FearlessParser(new CommonTokenStream(l));
    p.addErrorListener(new ParserErrors(fileName.toUri()));
    var errorst = new StringBuilder();
    var errorsp = new StringBuilder();
    FailConsole.setFail(fileName, l, p, errorst, errorsp);
    NudeProgramContext res = p.nudeProgram();
    var ok = errorst.isEmpty() && errorsp.isEmpty();
    if(ok){ return parseNudeProgram(res); }
    //TODO: better errors below
    if(!errorst.isEmpty()){ return orElse.apply(errorst.toString()); }
    return orElse.apply(errorsp.toString());
  }
  Package parseNudeProgram(NudeProgramContext res){
    return new FullEAntlrVisitor(
      fileName,
      s->{
        throw Fail.undefinedName(s).pos(FullEAntlrVisitor.pos(fileName, res));
      }
    ).visitNudeProgram(res);
  }
}
class FailConsole extends ConsoleErrorListener{
  public final StringBuilder sb;
  public final Path fileName;
  public FailConsole(Path fileName,StringBuilder sb){ this.fileName=fileName;this.sb=sb; }
  @Override public void syntaxError(Recognizer<?, ?> r,Object o,int line,int charPos,String msg,RecognitionException e){
    sb.append(Pos.of(fileName.toUri(), line, charPos)).append(" ").append(msg).append('\n');
    }
  static void setFail(Path fileName, Lexer l, org.antlr.v4.runtime.Parser p, StringBuilder errorst, StringBuilder errorsp) {
    l.removeErrorListener(ConsoleErrorListener.INSTANCE);
    l.addErrorListener(new FailConsole(fileName,errorst));
    p.removeErrorListener(ConsoleErrorListener.INSTANCE);
    p.addErrorListener(new FailConsole(fileName,errorsp));
  }
}
