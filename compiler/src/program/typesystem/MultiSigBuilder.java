package program.typesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import failure.CompileError;
import failure.FailOr;
import program.Program;
import ast.T;
import id.Mdf;
import id.Id.GX;
import utils.Range;

record MultiSigBuilder(
    Mdf mdf0,//actual receiver modifier
    List<T> expectedRes,
    Mdf formalRecMdf,
    List<T> formalTs,
    T formalRet,
    int size, //parameter count (no this)
    XBs bounds,
    List<ArrayList<T>> tss, //parameter types (no this)
    ArrayList<T> rets, //return types
    ArrayList<String> kinds//debug info about applied promotion
    ){
  static FailOr<MultiSig> multiMethod(XBs bounds, Mdf formalMdf, List<T> formalTs, T formalRet, Mdf mdf0, List<T> expectedRes){
    var res= new MultiSigBuilder(
      mdf0,expectedRes,formalMdf,formalTs,formalRet,
      formalTs.size(),
      bounds,
      formalTs.stream().map(_->new ArrayList<T>()).toList(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    res.fillIsoHProm();
    res.fillIsoProm();
    res.fillBase();
    res.fillReadHProm();
    res.fillMutHPromRec();
    res.fillMutHPromPar();
    try {
      return FailOr.res(new MultiSig(
        res.tss.stream().map(Collections::unmodifiableList).toList(),
        Collections.unmodifiableList(res.rets),
        Collections.unmodifiableList(res.kinds)
      ));
    } catch (CompileError err) {
      return FailOr.err(()->err);
    }
  }
  boolean filterMdf(Function<Mdf,Mdf> f) {    
    Mdf limit= f.apply(formalRecMdf());
    return program.Program.isSubType(mdf0, limit);
  }
  boolean filterExpectedRes(T resToAdd){
    if(expectedRes.isEmpty()){ return true; }
    return expectedRes.stream()
      .map(T::mdf)
      .anyMatch(mi->mdfSubtypeWithBounds(mi,resToAdd));
  }
  boolean mdfSubtypeWithBounds(Mdf expectedMdf, T resToAdd){
    if(!resToAdd.isGX()) { 
      return Program.isSubType(resToAdd.mdf(),expectedMdf);
    }
    T other= resToAdd.withMdf(expectedMdf);
    return Program.isSubTypeGX(bounds,resToAdd,other);
  }

  void fillBase(){ fillProm("Base",m->m); }
  void fillIsoProm(){ fillProm("IsoProm",this::mutIsoReadImm); }
  void fillIsoHProm(){ fillProm("IsoHProm",this::mutIsoReadImmReadHImm); }
  void fillReadHProm(){
    fillProm("ReadHProm",this::mutIsoReadReadH,this::mutIsoReadReadH,this::toHyg);
    }
  void fillMutHPromRec(){//two version: one for receiver mut <->mutH
    if(!formalRecMdf().isMut()){ return; }
    fillProm("MutHPromRec",this::mutMutH,this::mutIsoReadImm,this::toHyg);
    }
  void fillMutHPromPar(){//one for parameter mut <->mutH
    int countMut= (int)formalTs.stream()
      .skip(1).filter(t->t.mdf().isMut()).count();
    for(int i:Range.of(0,countMut)){ fillMutHPromPar(i); }
    }
  void fillMutHPromPar(int specialMut){//one for parameter mut <->mutH
    if(!filterMdf(this::mutIsoReadImm)){ return; }
    var addRet= fix(true,this::toHyg,formalRet);
    if(!filterExpectedRes(addRet)){ return; }
    rets.add(addRet);
    int count= 0;    
    for(var i : Range.of(0,size)){
      T currT= formalTs.get(i);
      var special= currT.mdf().isMut() && i==count;
      if(special){ count++; }
      var addT= fix(false,special?this::mutMutH:this::mutIsoReadImm,currT);
      tss.get(i).add(addT);
    }
    kinds.add("MutHPromRec");
  }
  void fillProm(String kind,Function<Mdf,Mdf> f){ fillProm(kind,f,f,f); }  
  
  void fillProm(String kind,Function<Mdf,Mdf> rec,Function<Mdf,Mdf> p,Function<Mdf,Mdf> r){
    if(!filterMdf(rec)){ return; }
    var addRet= fix(true,r,formalRet);
    if(!filterExpectedRes(addRet)){ return; }
    rets.add(addRet);
    for(var i : Range.of(0,size)){
      tss.get(i).add(fix(false,p,formalTs.get(i)));
      //ok receiver is not in s.ts
    }
    kinds.add(kind);
  }
  T fix(boolean isRet, Function<Mdf,Mdf> f,T t) {
    var mdf=t.mdf();
    if(mdf.isSyntaxMdf()){ return t.withMdf(f.apply(mdf)); }
    GX<T> gx= t.gxOrThrow();
    List<Mdf> xb= new ArrayList<>(bounds.get(gx));
    //converted to lists to ensure one to one correspondence
    List<Mdf> options= xb.stream()
        .map(f)
        .toList();
    if(xb.equals(options)){ return t; }
    //we return t if the transformation does not change any
    //information about mdf X. 
    //It also works for read/imm X
    Mdf goodMdf= isRet?MdfLubGlb.glb(options):MdfLubGlb.lub(options);
    if(mdf.isMdf()){ return t.withMdf(goodMdf); }
    assert mdf.isReadImm();
    if(goodMdf.is(Mdf.imm, Mdf.iso)){ return t.withMdf(Mdf.imm); }
    return t.withMdf(Mdf.read);
  }

  Mdf mutMutH(Mdf m){
    assert m.isMut();
    return Mdf.lent;
  }

  Mdf mutIsoReadImm(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read->Mdf.imm;
      default ->m;
    };
  }
  Mdf mutIsoReadImmReadHImm(Mdf m){
    assert m.isSyntaxMdf():
      "["+m+"]";
    return switch(m){
      case mut->Mdf.iso;
      case read,readOnly->Mdf.imm;
      default ->m;
    };
  }
  Mdf mutIsoReadReadH(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read->Mdf.readOnly;
      default ->m;
    };
  }
  Mdf toHyg(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.lent;
      case read->Mdf.readOnly;
      default ->m;
    };
  }
}