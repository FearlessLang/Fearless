package id;

import files.Pos;
import parser.Parser;
import utils.Bug;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Id {
  public static boolean validM(String m){
    assert m!=null && !m.isEmpty();
    return new parser.Parser(Parser.dummy,m).parseM();      
  }
  public static boolean validDecName(String name){
    assert name!=null && !name.isEmpty();
    if (name.endsWith("$")) { return true; }
    return new parser.Parser(Parser.dummy,name).parseFullCN();      
  }
  public static boolean validGX(String name){ 
    assert name!=null && !name.isEmpty();
    // Compiler-inserted names are valid
    if (name.endsWith("$")) { return true; }
    return new parser.Parser(Parser.dummy,name).parseGX();      
  }
  public record DecId(String name,int gen){
    public DecId{ assert validDecName(name) && gen>=0; }
    public String pkg() {
      return name.split("\\..+$")[0];
    }
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record MethName(String name, int num){
    public MethName{ assert validM(name) && num>=0; }
    @Override public String toString(){ return name+"/"+num; }
  }

  public interface RT<TT>{ <R> R match(Function<GX<TT>,R> gx, Function<IT<TT>,R> it); }

  public record GX<TT>(String name)implements RT<TT>{
    private static int FRESH_N = 0;
    private static HashMap<Integer, List<GX<ast.T>>> freshNames = new HashMap<>();
    public static void reset() {
      freshNames.clear();
      if (FRESH_N > 100) { throw Bug.of("FRESH_N is larger than we expected for tests."); }
      FRESH_N = 0;
    }
    public static List<GX<ast.T>> standardNames(int n) {
      // this will never clash with the other FearN$ names because they are only used on declarations
      // whereas this applies to method type params after the decl gens have been applied (i.e. C[Ts]).
      return IntStream.range(0, n).mapToObj(fresh->new Id.GX<ast.T>("FearX" + fresh + "$")).toList();
    }
    public static <TT> GX<TT> fresh() {
      if (FRESH_N + 1 == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
      return new GX<>("Fear" + FRESH_N++ + "$");
    }

    public GX{ assert Id.validGX(name); }
    public <R> R match(Function<GX<TT>,R>gx, Function<IT<TT>,R>it){ return gx.apply(this); }
    @Override public String toString(){ return name(); }
    public GX<ast.T> toAstGX() { return (GX<ast.T>) this; }
    public GX<astFull.T> toFullAstGX() { return (GX<astFull.T>) this; }
    public GX<TT> withName(String name) { return new GX<>(name); }
  }
  public record IT<TT>(Id.DecId name, List<TT> ts)implements RT<TT>{
    public IT{ assert ts.size()==name.gen(); }
    public IT(String name,List<TT> ts){ this(new Id.DecId(name,ts.size()),ts); }
    public <R> R match(Function<GX<TT>,R> gx, Function<IT<TT>,R> it){ return it.apply(this); }
    public IT<TT> withTs(List<TT>ts){ return new IT<>(new DecId(name.name,ts.size()), ts); }
    @Override public String toString(){ return name.name()+ts; }
    public IT<ast.T> toAstIT(Function<TT, ast.T> transform) {
      return new IT<ast.T>(name, ts.stream().map(transform).toList());
    }
    public IT<astFull.T> toFullAstIT(Function<TT, astFull.T> transform) {
      return new IT<astFull.T>(name, ts.stream().map(transform).toList());
    }
  }
}
