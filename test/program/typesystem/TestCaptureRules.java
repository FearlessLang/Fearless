package program.typesystem;

import id.Mdf;
import net.jqwik.api.Example;

import java.util.List;
import java.util.stream.Stream;

import static id.Mdf.*;
import static java.util.List.of;
import static program.typesystem.RunTypeSystem.expectFail;
import static program.typesystem.RunTypeSystem.ok;

public class TestCaptureRules {

  void newC(Mdf lambda,Mdf captured,Mdf method,List<Mdf> capturedAs, List<Mdf> capturedAsG){}
  void c(Mdf lambda,Mdf captured,Mdf method,Mdf ... capturedAs){
    //assert capturedAs.length!=0;
    var cs = List.of(capturedAs);

    cs.forEach(m->cInnerOk(lambda,captured,method,m));
    Stream.of(Mdf.values()).filter(m->!cs.contains(m))
      .forEach(m->cInnerFail(lambda,captured,method,m));
  }
  String code="""
    package test
    B:{}
    L:{ %s .absMeth: %s B }
    A:{ read .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  String codeGen1="""
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m(par: %s B) : %s L[%s B] -> %s L[%s B]{.absMeth->par} }
    """;

  String codeGen2="""
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ read .m[T](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;

  void cInnerOk(Mdf lambda,Mdf captured,Mdf method,Mdf capturedAs){
    var fCode=code.formatted(method,capturedAs,captured,lambda,lambda);
    System.out.println(fCode);
    try{ok(fCode);}
    catch(AssertionError t){ throw new AssertionError("failed on "+fCode+"\nwith:\n"+t); }
  }
  void cInnerFail(Mdf lambda,Mdf captured,Mdf method,Mdf capturedAs){
    var fCode=code.formatted(method,capturedAs,captured,lambda,lambda);
    try{expectFail(fCode);}
    catch(AssertionError t){ throw new AssertionError("expected failure but succeded on "+fCode); }
  }

  //                     lambda, captured, method, ...capturedAs
  @Example void t001(){ c(imm,   imm,   imm,   imm,read); }
  //@Example void t001(){ c(imm,   imm,   imm,   of(imm,read), of()); }
  @Example void t002(){ c(read,  imm,   imm,   imm,read); }
  @Example void t003(){ c(lent,  imm,   imm,   imm,read); }
  @Example void t004(){ c(mut,   imm,   imm,   imm,read); }
  @Example void t005(){ c(iso,   imm,   imm,   imm,read); }
  @Example void t006(){ c(mdf,   imm,   imm   /*not well formed lambda*/); }
  @Example void t007(){ c(recMdf,imm,   imm,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t011(){ c(imm,   read,  imm   /*impossible*/); }
  @Example void t012(){ c(read,  read,  imm,   imm,read); }
  @Example void t013(){ c(lent,  read,  imm,   imm,read); }
  @Example void t014(){ c(mut,   read,  imm   /*impossible*/); }//NOT NoMutHyg
  @Example void t015(){ c(iso,   read,  imm   /*impossible*/); }
  @Example void t016(){ c(mdf,   read,  imm   /*not well formed lambda*/); }
  @Example void t017(){ c(recMdf,read,  imm   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t021(){ c(imm,   lent,  imm   /*impossible*/); }
  @Example void t022(){ c(read,  lent,  imm,   imm,read); }
  @Example void t023(){ c(lent,  lent,  imm,   imm,read); }
  @Example void t024(){ c(mut,   lent,  imm   /*impossible*/); }//NOT NoMutHyg
  @Example void t025(){ c(iso,   lent,  imm   /*impossible*/); }
  @Example void t026(){ c(mdf,   lent,  imm   /*not well formed lambda*/); }
  @Example void t027(){ c(recMdf,lent,  imm   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t031(){ c(imm,   mut,   imm  /*impossible*/); }
  @Example void t032(){ c(read,  mut,   imm,   imm,read); }
  @Example void t033(){ c(lent,  mut,   imm,   imm,read); }
  @Example void t034(){ c(mut,   mut,   imm,   imm,read); }
  @Example void t035(){ c(iso,   mut,   imm,   imm,read); }
  @Example void t036(){ c(mdf,   mut,   imm   /*not well formed lambda*/); }
  @Example void t037(){ c(recMdf,mut,   imm  /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t041(){ c(imm,   iso,   imm,   imm,read); }
  @Example void t042(){ c(read,  iso,   imm,   imm,read); }
  @Example void t043(){ c(lent,  iso,   imm,   imm,read); }
  @Example void t044(){ c(mut,   iso,   imm,   imm,read); }
  @Example void t045(){ c(iso,   iso,   imm,   imm,read); }
  @Example void t046(){ c(mdf,   iso,   imm   /*not well formed lambda*/); }
  @Example void t047(){ c(recMdf,iso,   imm,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t051(){ c(imm,   mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t052(){ c(read,  mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t053(){ c(lent,  mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t054(){ c(mut,   mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t055(){ c(iso,   mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t056(){ c(mdf,   mdf,   imm /*not well formed parameter with mdf*/); }
  @Example void t057(){ c(recMdf,mdf,   imm /*not well formed parameter with mdf*/); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t061(){ c(imm,   recMdf,   imm  /*impossible*/); }
  @Example void t062(){ c(read,  recMdf,   imm,   imm,read); }
  @Example void t063(){ c(lent,  recMdf,   imm,   imm,read); }
  @Example void t064(){ c(mut,   recMdf,   imm  /*impossible*/); }
  @Example void t065(){ c(iso,   recMdf,   imm  /*impossible*/); }
  @Example void t066(){ c(mdf,   recMdf,   imm   /*not well formed lambda*/); }
  @Example void t067(){ c(recMdf,recMdf,   imm,   imm,read); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t181(){ c(imm,   imm,   read,   imm,read); }
  @Example void t182(){ c(read,  imm,   read,   imm,read); }
  @Example void t183(){ c(lent,  imm,   read,   imm,read); }
  @Example void t184(){ c(mut,   imm,   read,   imm,read); }
  @Example void t185(){ c(iso,   imm,   read,   imm,read); }
  @Example void t186(){ c(mdf,   imm,   read   /*not well formed lambda*/); }
  @Example void t187(){ c(recMdf,imm,   read,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t101(){ c(imm,   read,  read   /*impossible*/); }
  @Example void t102(){ c(read,  read,  read,   read); }
  @Example void t103(){ c(lent,  read,  read,   read); }
  @Example void t104(){ c(mut,   read,  read   /*impossible*/); }//NOT NoMutHyg
  @Example void t105(){ c(iso,   read,  read   /*impossible*/); }
  @Example void t106(){ c(mdf,   read,  read   /*not well formed lambda*/); }
  @Example void t107(){ c(recMdf,read,  read   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t111(){ c(imm,   lent,  read   /*impossible*/); }
  @Example void t112(){ c(read,  lent,  read,   read,recMdf); }//recMdf is ok, at least can not find counter example, the lent lambda can become mut only in controlled way
  @Example void t113(){ c(lent,  lent,  read,   read,recMdf); }//the lambda is created read, and can not become anything else but imm.
  @Example void t114(){ c(mut,   lent,  read   /*impossible*/); }//NOT NoMutHyg
  @Example void t115(){ c(iso,   lent,  read   /*impossible*/); }
  @Example void t116(){ c(mdf,   lent,  read   /*not well formed lambda*/); }
  @Example void t117(){ c(recMdf,lent,  read   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t121(){ c(imm,   mut,   read  /*impossible*/); }
  @Example void t122(){ c(read,  mut,   read,   read,recMdf); }
  @Example void t123(){ c(lent,  mut,   read,   read,recMdf); }
  @Example void t124(){ c(mut,   mut,   read,   read,recMdf); }
  @Example void t125(){ c(iso,   mut,   read,   read,recMdf); }
  @Example void t126(){ c(mdf,   mut,   read   /*not well formed lambda*/); }
  @Example void t127(){ c(recMdf,mut,   read  /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t131(){ c(imm,   iso,   read,   imm,read); }
  @Example void t132(){ c(read,  iso,   read,   imm,read); }
  @Example void t133(){ c(lent,  iso,   read,   imm,read); }
  @Example void t134(){ c(mut,   iso,   read,   imm,read); }
  @Example void t135(){ c(iso,   iso,   read,   imm,read); }
  @Example void t136(){ c(mdf,   iso,   read   /*not well formed lambda*/); }
  @Example void t137(){ c(recMdf,iso,   read,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t141(){ c(imm,   mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t142(){ c(read,  mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t143(){ c(lent,  mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t144(){ c(mut,   mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t145(){ c(iso,   mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t146(){ c(mdf,   mdf,   read /*not well formed parameter with mdf*/); }
  @Example void t147(){ c(recMdf,mdf,   read /*not well formed parameter with mdf*/); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t151(){ c(imm,   recMdf,   read  /*impossible*/); }
  @Example void t152(){ c(read,  recMdf,   read,   read); }
  @Example void t153(){ c(lent,  recMdf,   read,   read); }
  @Example void t154(){ c(mut,   recMdf,   read  /*impossible*/); }
  @Example void t155(){ c(iso,   recMdf,   read  /*impossible*/); }
  @Example void t156(){ c(mdf,   recMdf,   read   /*not well formed lambda*/); }
  @Example void t157(){ c(recMdf,recMdf,   read,   read,recMdf); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t161(){ c(imm,   imm,   read,   read,imm); }
  @Example void t162(){ c(read,  imm,   read,   read,imm); }
  @Example void t163(){ c(lent,  imm,   read,   read,imm); }
  @Example void t164(){ c(mut,   imm,   read,   read,imm); }
  @Example void t165(){ c(iso,   imm,   read,   read,imm); }
  @Example void t166(){ c(mdf,   imm,   read   /*not well formed lambda*/); }
  @Example void t167(){ c(recMdf,imm,   read,   read,imm); }

  //                     lambda, captured, method, ...capturedAs
  @Example void t201(){ c(imm,   imm,   lent    /*impossible*/); }
  @Example void t202(){ c(read,  imm,   lent    /*impossible*/); }
  @Example void t203(){ c(lent,  imm,   lent,   imm,read); }
  @Example void t204(){ c(mut,   imm,   lent,   imm,read); }
  @Example void t205(){ c(iso,   imm,   lent,   imm,read); }
  @Example void t206(){ c(mdf,   imm,   lent   /*not well formed lambda*/); }
  @Example void t207(){ c(recMdf,imm,   lent,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t211(){ c(imm,   read,  lent   /*impossible*/); }
  @Example void t212(){ c(read,  read,  lent   /*impossible*/); }
  @Example void t213(){ c(lent,  read,  lent,   read); }
  @Example void t214(){ c(mut,   read,  lent   /*impossible*/); }//NOT NoMutHyg
  @Example void t215(){ c(iso,   read,  lent   /*impossible*/); }
  @Example void t216(){ c(mdf,   read,  lent   /*not well formed lambda*/); }
  @Example void t217(){ c(recMdf,read,  lent   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t221(){ c(imm,   lent,  lent   /*impossible*/); }
  @Example void t222(){ c(read,  lent,  lent   /*impossible*/); } // this capture is fine because the method cannot ever be called
  @Example void t223(){ c(lent,  lent,  lent,   read,lent); }
  @Example void t224(){ c(mut,   lent,  lent   /*impossible*/); }//NOT NoMutHyg
  @Example void t225(){ c(iso,   lent,  lent   /*impossible*/); }
  @Example void t226(){ c(mdf,   lent,  lent   /*not well formed lambda*/); }
  @Example void t227(){ c(recMdf,lent,  lent   /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t231(){ c(imm,   mut,   lent  /*impossible*/); }
  @Example void t232(){ c(read,  mut,   lent  /*impossible*/); }
  @Example void t233(){ c(lent,  mut,   lent,   read,lent); }
  @Example void t234(){ c(mut,   mut,   lent,   read,lent); }
  @Example void t235(){ c(iso,   mut,   lent,   read,lent); }
  @Example void t236(){ c(mdf,   mut,   lent   /*not well formed lambda*/); }
  @Example void t237(){ c(recMdf,mut,   lent  /*impossible*/); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t241(){ c(imm,   iso,   lent    /*impossible*/); } //TODO: Marco up to here
  @Example void t242(){ c(read,  iso,   lent    /*impossible*/); }
  @Example void t243(){ c(lent,  iso,   lent,   imm,read); }
  @Example void t244(){ c(mut,   iso,   lent,   imm,read); }
  @Example void t245(){ c(iso,   iso,   lent,   imm,read); }
  @Example void t246(){ c(mdf,   iso,   lent   /*not well formed lambda*/); }
  @Example void t247(){ c(recMdf,iso,   lent,   imm,read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t251(){ c(imm,   mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t252(){ c(read,  mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t253(){ c(lent,  mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t254(){ c(mut,   mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t255(){ c(iso,   mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t256(){ c(mdf,   mdf,   lent /*not well formed parameter with mdf*/); }
  @Example void t257(){ c(recMdf,mdf,   lent /*not well formed parameter with mdf*/); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Example void t261(){ c(imm,   recMdf,   lent  /*impossible*/); }
  @Example void t262(){ c(read,  recMdf,   lent  /*impossible*/); }
  @Example void t263(){ c(lent,  recMdf,   lent,   read); }
  @Example void t264(){ c(mut,   recMdf,   lent  /*impossible*/); }
  @Example void t265(){ c(iso,   recMdf,   lent  /*impossible*/); }
  @Example void t266(){ c(mdf,   recMdf,   lent   /*not well formed lambda*/); }
  @Example void t267(){ c(recMdf,recMdf,   lent,   read); }
  //                     lambda, captured, method, ...capturedAs
  @Example void t271(){ c(imm,   imm,   lent    /*impossible*/); }
  @Example void t272(){ c(read,  imm,   lent    /*impossible*/); }
  @Example void t273(){ c(lent,  imm,   lent,   read,imm); }
  @Example void t274(){ c(mut,   imm,   lent,   read,imm); }
  @Example void t275(){ c(iso,   imm,   lent,   read,imm); }
  @Example void t276(){ c(mdf,   imm,   lent   /*not well formed lambda*/); }
  @Example void t277(){ c(recMdf,imm,   lent,   read,imm); }
}
//a mut lambda could capture a mut as iso inside an iso method?

//write counterexample

/*
//NO-a mut lambda could capture a lent as iso inside an iso method?
A:{
  read .foo(par: lent Break): mut BreakBox -> { par }
  }
BreakBox:{
  iso .release: iso Break // would this be allowed?
  }
//No since A.foo(myLent) can be promoted to iso
//NO-a mut lambda could capture a read as imm inside an iso/imm method
similar, the A.foo(myRead) can be promoted to iso
 */


/*
For the Generix X capture version:
@Example void t051(){ c(imm,   mdf,   imm); }
@Example void t052(){ c(read,  mdf,   imm,   imm,read); }
@Example void t053(){ c(lent,  mdf,   imm,   imm,read); }
@Example void t054(){ c(mut,   mdf,   imm); }//NOT NoMutHyg
@Example void t055(){ c(iso,   mdf,   imm); }//NOT NoMutHyg
@Example void t056(){ c(mdf,   mdf,   imm); }
@Example void t057(){ c(recMdf,mdf,   imm); }
 */
