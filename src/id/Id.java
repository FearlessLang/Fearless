package id;

import astFull.T;
import parser.Parser;
import utils.Bug;
import utils.OneOr;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Id {
  public sealed interface Dec permits astFull.T.Dec, ast.T.Dec {
    Map<GX<T>, Set<Mdf>> bounds();
  }
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

    static Pattern pkgRegex = Pattern.compile("(.+\\.)+([A-Za-z0-9_']+)\\$?$");
    public String pkg() {
      var pkg = OneOr.of("Malformed package: "+name, pkgRegex.matcher(name).results()).group(1);
      return pkg.substring(0, pkg.length() - 1);
    }
    public String shortName() {
      return OneOr.of("Malformed package", pkgRegex.matcher(name).results()).group(2);
    }
    @Override public String toString() {
      return String.format("%s/%d", name, gen);
    }
  }
  public record MethName(String name, int num){
    public MethName{ assert validM(name) && num>=0; }
    @Override public String toString(){ return name+"/"+num; }
  }

  public interface RT<TT extends Ty>{ <R> R match(Function<GX<TT>,R> gx, Function<IT<TT>,R> it); }
  public sealed interface Ty permits ast.T, astFull.T {}

  public record GX<TT extends Ty>(String name) implements RT<TT>{
    private static final AtomicInteger FRESH_N = new AtomicInteger(0);
    private static HashMap<Integer, List<GX<ast.T>>> freshNames = new HashMap<>();

    public static void reset() {
      // TODO: disable outside unit testing context
      if (FRESH_N.get() > 150_000) {
        throw Bug.of("FRESH_N is larger than we expected for tests.");
      }
      FRESH_N.set(0);
    }
    public static List<GX<ast.T>> standardNames(int n) {
      // this will never clash with the other FearN$ names because they are only used on declarations
      // whereas this applies to method type params after the decl gens have been applied (i.e. C[Ts]).
      return IntStream.range(0, n).mapToObj(fresh->new Id.GX<ast.T>("FearX" + fresh + "$")).toList();
    }
    public static <TT extends Ty> GX<TT> fresh() {
      var n = FRESH_N.getAndUpdate(n_ -> {
        int next = n_ + 1;
        if (next == Integer.MAX_VALUE) { throw Bug.of("Maximum fresh identifier size reached"); }
        return next;
      });
      return new GX<>("Fear" + n + "$");
    }

    public GX{ assert Id.validGX(name); }
    public <R> R match(Function<GX<TT>,R>gx, Function<IT<TT>,R>it){ return gx.apply(this); }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GX<?> gx = (GX<?>) o;
      return Objects.equals(name, gx.name);
    }

    @Override public int hashCode() {
      return Objects.hash(name);
    }

    @Override public String toString(){ return name(); }
    public GX<ast.T> toAstGX() { return (GX<ast.T>) this; }
    public GX<astFull.T> toFullAstGX() { return (GX<astFull.T>) this; }
    public GX<TT> withName(String name) { return new GX<>(name); }
  }
  public record IT<TT extends Ty>(Id.DecId name, List<TT> ts)implements RT<TT>{
    public IT{
      assert ts.size()==name.gen();
    }
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
