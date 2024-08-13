package program.typesystem;

import ast.T;
import failure.Fail;
import failure.FailOr;
import id.Id;
import id.Id.GX;
import id.Mdf;
import program.Program;
import program.TypeTable;
import utils.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static program.typesystem.SubTyping.isSubType;

record MultiSigBuilder(
    SubTyping subTyping,
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
  static FailOr<MultiSig> multiMethod(SubTyping subTyping, Id.MethName name, XBs bounds, Mdf formalMdf, List<T> formalTs, T formalRet, Mdf mdf0, List<T> expectedRes){
    var res= new MultiSigBuilder(
      subTyping,
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

    if (res.kinds.isEmpty()) {
      return FailOr.err(()->Fail.callTypeError(name, mdf0, formalMdf, expectedRes, formalRet));
    }

    return FailOr.res(new MultiSig(
      res.tss.stream().map(Collections::unmodifiableList).toList(),
      Collections.unmodifiableList(res.rets),
      Collections.unmodifiableList(res.kinds)
    ));
  }
  boolean filterMdf(Function<Mdf,Mdf> f) {    
    Mdf limit= f.apply(formalRecMdf());
    return isSubType(mdf0, limit);
  }
  boolean filterExpectedRes(T resToAdd){
    if(expectedRes.isEmpty()){ return true; }
    return expectedRes.stream()
      .map(T::mdf)
      .anyMatch(mi->mdfSubtypeWithBounds(mi,resToAdd));
  }
  boolean mdfSubtypeWithBounds(Mdf expectedMdf, T resToAdd){
    if(!resToAdd.isGX()) { 
      return isSubType(resToAdd.mdf(),expectedMdf);
    }
    T other= resToAdd.withMdf(expectedMdf);
    return subTyping.isSubType(bounds,resToAdd,other);
  }

  void fillBase(){ fillProm("Base",m->m); }
  void fillIsoProm(){ fillProm("IsoProm",this::mutIsoReadImm); }
  void fillIsoHProm(){ fillProm("IsoHProm",this::mutIsoReadImmReadHImm); }
  void fillReadHProm(){
    fillProm("ReadHProm",this::mutIsoReadReadH,this::mutIsoReadReadH,this::toHyg);
    }
  void fillMutHPromRec(){//two version: one for receiver mut <->mutH
    if(!formalRecMdf().isMut()) { return; }
    fillProm("MutHPromRec",this::toHyg,this::mutIsoReadImm,this::toHyg);
  }
  void fillMutHPromPar(){//one for parameter mut <->mutH
    if(!filterMdf(this::mutIsoReadImm)){ return; }
    var addRet= fix(true,this::toHyg,formalRet);
    if(!filterExpectedRes(addRet)){ return; }

    var muts = IntStream.range(0, formalTs.size()).filter(i -> formalTs.get(i).mdf().isMut()).toArray();
    for (int mutIdx : muts) {
      rets.add(addRet);
      transformMuts(mutIdx);
      kinds.add(STR."MutHPromPar(\{mutIdx})");
    }
  }
  void transformMuts(int toTransform) {
    for (int i = 0; i < formalTs.size(); ++i) {
      if (i == toTransform) {
        tss.get(i).add(fix(false, this::toHyg, formalTs.get(i)));
      } else {
        tss.get(i).add(fix(false, this::mutIsoReadImm, formalTs.get(i)));
      }
    }
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
      case read, readH ->Mdf.imm;
      default ->m;
    };
  }
  Mdf mutIsoReadReadH(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.iso;
      case read->Mdf.readH;
      default ->m;
    };
  }
  Mdf toHyg(Mdf m){
    assert m.isSyntaxMdf();
    return switch(m){
      case mut->Mdf.mutH;
      case read->Mdf.readH;
      default ->m;
    };
  }
}