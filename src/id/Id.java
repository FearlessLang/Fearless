package id;

import parser.Parser;

import java.util.List;
import java.util.function.Function;

public class Id {
  public static boolean validM(String m){
    assert m!=null && !m.isEmpty();
    return new parser.Parser(Parser.dummy,m).parseM();      
  }
  public static boolean validDecName(String name){
    assert name!=null && !name.isEmpty();
    return new parser.Parser(Parser.dummy,name).parseFullCN();      
  }
  public static boolean validGX(String name){ 
    assert name!=null && !name.isEmpty();
    return new parser.Parser(Parser.dummy,name).parseGX();      
  }
  public record DecId(String name,int gen){
    public DecId{ assert validDecName(name) && gen>=0; }
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
    public GX{assert Id.validGX(name);}
    public <R> R match(Function<GX<TT>,R>gx, Function<IT<TT>,R>it){ return gx.apply(this); }
  }
  public record IT<TT>(Id.DecId name, List<TT> ts)implements RT<TT>{
    public IT{ assert ts.size()==name.gen(); }
    public IT(String name,List<TT> ts){ this(new Id.DecId(name,ts.size()),ts); }
    public <R> R match(Function<GX<TT>,R> gx, Function<IT<TT>,R> it){ return it.apply(this); }
    public IT<TT> withTs(List<TT>ts){ return new IT<>(new DecId(name.name,ts.size()), ts); }
    @Override public String toString(){ return name.name()+ts; }
  }
}
