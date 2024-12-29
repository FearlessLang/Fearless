package main.java;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ast.T;
import ast.E.Lambda;
import ast.E.MCall;
import ast.E.X;
import ast.E.Lambda.LambdaId;
import ast.T.Dec;
import id.Id;
import id.Mdf;
import id.Id.DecId;
import id.Id.GX;
import magic.LiteralKind;

//TODO: refactoring
//-remove ciclyic impl and the 3 extra ifs about it
//  one in this file, one in subtyping, one in Java generation

class DecTypeInfo implements visitors.Visitor<Void>{
  boolean outerMost= true;
  Map<String,String> mapGX=new HashMap<>();
  Map<String,String> mapGXType=new HashMap<>();
  StringBuilder res= new StringBuilder();
  List<Lambda> nested= new ArrayList<>();

  String get() { 
    var tmp= res.toString();
    res.setLength(0);
    return tmp;
    }
  void c(String s){ res.append(s); }
  void stringMdf(Mdf m){ res.append(m); }
  
  <E> void seq(List<E> es,Consumer<E> f,String sep){
    boolean first= true;
    for(var e:es) {
      if(first){ first = false; }
      else {c(sep);}
      f.accept(e);
    }
  }
  String bounds(GX<T> gx, Map<Id.GX<T>, Set<Mdf>> map){
    var bounds= map.get(gx);
    if(bounds==null){ return ""; }
    return ":"+bounds.stream()
      .map(Mdf::toString)
      .collect(Collectors.joining(", "));
  }
  void registerGX(GX<T> gx){
    String n= gx.name();
    assert !mapGX.containsKey(n):mapGX+"\n\n"+n;
    if(!n.contains("$")){
      mapGX.put(n,n); 
      return;
    }//OriginalName$n
    int i= n.indexOf("$");
    assert i>0;
    String nn=n.substring(0,i);
    if(!mapGX.containsValue(nn)){
      mapGX.put(n,nn); 
      return;        
    }
    int j=1;
    String nj=nn+j;
    while(mapGX.containsValue(nj)){ j++; nj = nn + j; }
    mapGX.put(n,nj); 
  }
  void stringGXDec(GX<T> gx,Map<Id.GX<T>, Set<Mdf>> bounds){
    registerGX(gx);
    stringGX(gx);
    c(bounds(gx,bounds));
  }
  void stringGX(GX<T> gx){
    String res= this.mapGX.get(gx.name());
    assert res!=null;
    c(res);
  }
  public void stringLambda(Lambda l) {
    l.meths().stream()
      .flatMap(m->m.body().stream())
      .forEach(e->e.accept(this));
    if(l.id().id().name().contains("$")){ return; }
    if(l.id().id().name().startsWith("_")){ return; }
    mapGXType.clear();
    mapGX.clear();
    LambdaId id= l.id();
    c(id.id().shortName());
    c("[");
    seq(id.gens(),gx->stringGXDec(gx,id.bounds()),", ");
    c("]:");
    mapGXType.putAll(mapGX);
    stringImplements(l);
    c("{\n");
    l.meths().forEach(this::stringMeth);
    c("}\n");
  }
  private void stringImplements(Lambda l) {
    var its= new ArrayList<>(l.its());
    var sealedId= new Id.IT<ast.T>(new DecId("base.Sealed",0),List.of());
    its.removeIf(it->it.name().equals(l.id().id()));
    if(!outerMost && !its.contains(sealedId)){
      its.add(sealedId);
    }
    seq(its,it->{it.match(
      gx->{stringGX(gx); return null;},
      t->{
        stringName(t.name());
        c("[");
        seq(t.ts(),this::stringType,", ");
        c("]");
        return null;
        });},",");
  }
  public void stringName(DecId decId) {
    if (LiteralKind.isLiteral(decId.name())) {
      c(decId.name());
      return;
    }
    c(decId.pkg());
    c(".");
    c(decId.shortName());
  }
  public void stringType(ast.T t) {
    stringMdf(t.mdf());
    c(" ");
    t.match(
      gx->{stringGX(gx);return null;},
      it->{
        stringName(it.name());
        c("[");
        seq(it.ts(),this::stringType,", ");
        c("]");
        return null;
      });
  }
  public void stringParName(String x){
    var fresh= x.contains("$");
    c(fresh?"_":x);
  }
  public void stringMeth(ast.E.Meth m) {
    mapGX.clear();
    mapGX.putAll(mapGXType);
    var s=m.sig();
    c("  ");
    stringMdf(m.mdf());
    c(" ");
    c(m.name().name());
    c("[");
    seq(s.gens(),gx->stringGXDec(gx,s.bounds()),", ");
    c("]");
    c("(");
    assert m.xs().size()==s.ts().size();
    seq(IntStream.range(0,m.xs().size()).boxed().toList(),i->{
      stringParName(m.xs().get(i));
      c(":");
      stringType(s.ts().get(i));
      },", ");
    c("):");
    stringType(s.ret());
    if(m.isAbs()) {c(",\n");return;}
    c("->base.Abort![");
    stringType(m.sig().ret());
    c("],\n");
  }
  public String stringLambdaGet(Lambda l) {
    stringLambda(l);
    return get();
  }
  public String visitDec(Dec dec) {
    assert outerMost;
    LambdaId id= dec.lambda().id();
    assert id.bounds().equals(dec.bounds());
    assert id.id().equals(dec.name());
    assert id.gens().equals(dec.gxs());
    String main= stringLambdaGet(dec.lambda());
    outerMost=false;
    StringBuilder res=new StringBuilder(main);
    for (int i=0;i<nested.size();i++) {//Yes, this is needed here,
      //since we add to nested while we read from it.
      String resi = stringLambdaGet(nested.get(i));
      if (!resi.isBlank()) { res.append(resi).append('\n'); }
    }
    return res.toString();
    //return main+nested.stream()//concurrent modification exception
    //  .map(this::stringLambdaGet)
    //  .filter(s->!s.isBlank())
    //  .collect(Collectors.joining("\n"));
  }

  @Override public Void visitMCall(MCall e) {
    e.receiver().accept(this);
    e.es().forEach(ei->ei.accept(this));
    return null;
  }

  @Override public Void visitX(X e) {
    return null;
  }

  @Override public Void visitLambda(Lambda e) {
    //if(e.name().id().name().contains("TestResults")) {
    //  System.out.println(e);
    //}
    nested.add(e);
    return null;
  }
}