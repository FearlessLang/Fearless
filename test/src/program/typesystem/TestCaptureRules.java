package program.typesystem;

import failure.CompileError;
import id.Mdf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static id.Mdf.*;
import static java.util.List.of;
import static program.typesystem.RunTypeSystem.expectFail;
import static program.typesystem.RunTypeSystem.ok;

public class TestCaptureRules {
  void c1(Mdf lambda, Mdf captured, Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->cInnerOk(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->cInnerFail(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
  }
  String codeGen1 = """
    package test
    B:{}
    L:{ %s .absMeth: %s B }
    A:{ recMdf .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  void c2(Mdf lambda,Mdf captured,Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->{
      cInnerOk(codeGen2a.formatted(method, mdf, captured, lambda, captured, lambda, captured));
      if (!mdf.is(mdf, recMdf)) {
        cInnerOk(codeGen2b.formatted(method, mdf, captured, captured, lambda, lambda));
      }
    });
    Stream.of(Mdf.values()).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->{
        cInnerFail(codeGen2a.formatted(method, mdf, captured, lambda, captured, lambda, captured));
        if (!mdf.is(mdf, recMdf)) {
          cInnerFail(codeGen2b.formatted(method, mdf, captured, captured, lambda, lambda));
        }
      });
  }
  String codeGen2a = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ recMdf .m(par: %s B) : %s L[%s B] -> %s L[%s B]{.absMeth->par} }
    """;
  String codeGen2b = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ recMdf .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  @SafeVarargs
  final void c3(Mdf lambda, Mdf captured, Mdf method, List<Mdf>... _capturePairs) {
    assert _capturePairs.length>0;
    assert Stream.of(_capturePairs).allMatch(l->l.size()%2==0);
    List<Mdf> capturePairs = Stream.of(_capturePairs).flatMap(Collection::stream).toList();
    assert capturePairs.size() % 2 == 0;
    record Capture(Mdf capAs, Mdf capAsG){
      @Override public String toString() {
        return capAs+","+capAsG+"  ";
      }
    }
    Set<Capture> caps = new HashSet<>(capturePairs.size() / 2);
    for (int i = 0; i < capturePairs.size(); i += 2) {
      caps.add(new Capture(capturePairs.get(i), capturePairs.get(i+1)));
    }

    var permutations = new ArrayList<Capture>(Mdf.values().length * Mdf.values().length);
    for (Mdf mdf : Mdf.values()) {
      for (Mdf mdfi : Mdf.values()) {
        permutations.add(new Capture(mdf, mdfi));
      }
    }

    var validCaps = new ConcurrentLinkedQueue<Capture>();
    var exceptions = new ConcurrentLinkedQueue<>();
    var templateA = codeGen3a;
    var templateB = codeGen3b;
    permutations.parallelStream().forEach(c->{
      var codeA = templateA.formatted(method, c.capAs, captured, lambda, c.capAsG, lambda, c.capAsG);
//      System.out.println(codeA);
      var codeB = templateB.formatted(method, c.capAs, c.capAsG, captured, lambda, lambda);
      var codeBValid = !c.capAs.is(mdf, recMdf) && !c.capAsG.is(mdf, recMdf) && !captured.is(mdf, recMdf);
//      if (codeBValid) { System.out.println(codeB); }
      var ok = caps.contains(c);
      if (ok) {
        try {
          cInnerOk(codeA);
          if (codeBValid) { cInnerOk(codeB); }
          validCaps.add(c);
        } catch (AssertionError | CompileError e) { exceptions.add(codeA+"\n"+e); }
      }
      else {
        try {
          cInnerFail(codeA);
          if (codeBValid) { cInnerFail(codeB); }
        } catch (AssertionError e) { validCaps.add(c); exceptions.add(codeA+"\n"+e); }
      }
    });
    if(!exceptions.isEmpty()){
      throw new AssertionError("valid pairs:\n"+validCaps+"\n\nbut we got the following errors:"+exceptions);
    }
  }
  String codeGen3a = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    A:{ recMdf .m[T](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;
  String codeGen3b = """
    package test
    B:{}
    L[X]:{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ recMdf .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;
  String base = """
    package base
    NoMutHyg[X]:{}
    """;

  void cInnerOk(String code){
    try{ok(code, base);}
    catch(AssertionError t){ throw new AssertionError("failed on "+code+"\nwith:\n"+t); }
  }
  void cInnerFail(String code){
    try{expectFail(code, base);}
    catch(AssertionError t){ throw new AssertionError("expected failure but succeeded on "+code); }
  }

  //                     lambda, captured, method, ...capturedAs
  @Test void t001(){ c1(imm, imm, imm, of(imm,read)); }
  @Test void t002(){ c1(read, imm, imm, of(imm,read)); }
  @Test void t003(){ c1(lent, imm, imm, of(imm,read)); }
  @Test void t004(){ c1(mut, imm, imm, of(imm,read)); }
  @Test void t005(){ c1(iso, imm, imm, of(imm,read)); }
  @Test void t006(){ c1(mdf, imm, imm, of(/*not well formed lambda*/)); }
  @Test void t007(){ c1(recMdf, imm, imm, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t011(){ c1(imm, read, imm, of(/*impossible*/)); }
  @Test void t012(){ c1(read, read, imm, of(imm,read)); }
  @Test void t013(){ c1(lent, read, imm, of(imm,read)); }
  @Test void t014(){ c1(mut, read, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t015(){ c1(iso, read, imm, of(/*impossible*/)); }
  @Test void t016(){ c1(mdf, read, imm, of(/*not well formed lambda*/)); }
  @Test void t017(){ c1(recMdf, read, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t021(){ c1(imm, lent, imm, of(/*impossible*/)); }
  @Test void t022(){ c1(read, lent, imm, of(imm,read)); }
  @Test void t023(){ c1(lent, lent, imm, of(imm,read)); }
  @Test void t024(){ c1(mut, lent, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t025(){ c1(iso, lent, imm, of(/*impossible*/)); }
  @Test void t026(){ c1(mdf, lent, imm, of(/*not well formed lambda*/)); }
  @Test void t027(){ c1(recMdf, lent, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t031(){ c1(imm, mut, imm, of(/*impossible*/)); }
  @Test void t032(){ c1(read, mut, imm, of(imm,read)); }
  @Test void t033(){ c1(lent, mut, imm, of(imm,read)); }
  @Test void t034(){ c1(mut, mut, imm, of(imm,read)); }
  @Test void t035(){ c1(iso, mut, imm, of()); }
  @Test void t036(){ c1(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t037(){ c1(recMdf, mut, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t041(){ c1(imm, iso, imm, of(imm,read)); }
  @Test void t042(){ c1(read, iso, imm, of(imm,read)); }
  @Test void t043(){ c1(lent, iso, imm, of(imm,read)); }
  @Test void t044(){ c1(mut, iso, imm, of(imm,read)); }
  @Test void t045(){ c1(iso, iso, imm, of(imm,read)); }
  @Test void t046(){ c1(mdf, iso, imm, of(/*not well formed lambda*/)); }
  @Test void t047(){ c1(recMdf, iso, imm, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t051(){ c1(imm, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t052(){ c1(read, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t053(){ c1(lent, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t054(){ c1(mut, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t055(){ c1(iso, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t056(){ c1(mdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t057(){ c1(recMdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t061(){ c1(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t062(){ c1(read, recMdf, imm, of(imm,read)); }
  @Test void t063(){ c1(lent, recMdf, imm, of(imm,read)); }
  @Test void t064(){ c1(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t065(){ c1(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t066(){ c1(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t067(){ c1(recMdf, recMdf, imm, of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t181(){ c1(imm, imm, read, of(imm,read)); }
  @Test void t182(){ c1(read, imm, read, of(imm,read)); }
  @Test void t183(){ c1(lent, imm, read, of(imm,read)); }
  @Test void t184(){ c1(mut, imm, read, of(imm,read)); }
  @Test void t185(){ c1(iso, imm, read, of(imm,read)); }
  @Test void t186(){ c1(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t187(){ c1(recMdf, imm, read, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t101(){ c1(imm, read, read, of(/*impossible*/)); }
  @Test void t102(){ c1(read, read, read, of(read)); }
  @Test void t103(){ c1(lent, read, read, of(read)); }
  @Test void t104(){ c1(mut, read, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t105(){ c1(iso, read, read, of(/*impossible*/)); }
  @Test void t106(){ c1(mdf, read, read, of(/*not well formed lambda*/)); }
  @Test void t107(){ c1(recMdf, read, read, of(/*impossible*/)); }
  //                    lambda, captured, method, ...capturedAs
  @Test void t111(){ c1(imm, lent, read, of(/*impossible*/)); }
  @Test void t112(){ c1(read, lent, read, of(read)); }
  @Test void t113(){ c1(lent, lent, read, of(read)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t114(){ c1(mut, lent, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t115(){ c1(iso, lent, read, of(/*impossible*/)); }
  @Test void t116(){ c1(mdf, lent, read, of(/*not well formed lambda*/)); }
  @Test void t117(){ c1(recMdf, lent, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t121(){ c1(imm, mut, read, of(/*impossible*/)); }
  @Test void t122(){ c1(read, mut, read, of(read)); }
  @Test void t123(){ c1(lent, mut, read, of(read)); }
  @Test void t124(){ c1(mut, mut, read, of(read)); }
  @Test void t125(){ c1(iso, mut, read, of()); }
  @Test void t126(){ c1(mdf, mut, read, of(/*not well formed lambda*/)); }
  @Test void t127(){ c1(recMdf, mut, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t131(){ c1(imm, iso, read, of(imm,read)); }
  @Test void t132(){ c1(read, iso, read, of(imm,read)); }
  @Test void t133(){ c1(lent, iso, read, of(imm,read)); }
  @Test void t134(){ c1(mut, iso, read, of(imm,read)); }
  @Test void t135(){ c1(iso, iso, read, of(imm,read)); }
  @Test void t136(){ c1(mdf, iso, read, of(/*not well formed lambda*/)); }
  @Test void t137(){ c1(recMdf, iso, read, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t141(){ c1(imm, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t142(){ c1(read, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t143(){ c1(lent, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t144(){ c1(mut, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t145(){ c1(iso, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t146(){ c1(mdf, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t147(){ c1(recMdf, mdf, read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t151(){ c1(imm, recMdf, read, of(/*impossible*/)); }
  @Test void t152(){ c1(read, recMdf, read, of(read)); }
  @Test void t153(){ c1(lent, recMdf, read, of(read)); }
  @Test void t154(){ c1(mut, recMdf, read, of(/*impossible*/)); }
  @Test void t155(){ c1(iso, recMdf, read, of(/*impossible*/)); }
  @Test void t156(){ c1(mdf, recMdf, read, of(/*not well formed lambda*/)); }
  @Test void t157(){ c1(recMdf, recMdf, read, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t161(){ c1(imm, imm, read, of(read,imm)); }
  @Test void t162(){ c1(read, imm, read, of(read,imm)); }
  @Test void t163(){ c1(lent, imm, read, of(read,imm)); }
  @Test void t164(){ c1(mut, imm, read, of(read,imm)); }
  @Test void t165(){ c1(iso, imm, read, of(read,imm)); }
  @Test void t166(){ c1(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t167(){ c1(recMdf, imm, read, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t201(){ c1(imm, imm, lent, of(/*impossible*/)); }
  @Test void t202(){ c1(read, imm, lent, of(/*impossible*/)); }
  @Test void t203(){ c1(lent, imm, lent, of(imm,read)); }
  @Test void t204(){ c1(mut, imm, lent, of(imm,read)); }
  @Test void t205(){ c1(iso, imm, lent, of(imm,read)); }
  @Test void t206(){ c1(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t207(){ c1(recMdf, imm, lent, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t211(){ c1(imm, read, lent, of(/*impossible*/)); }
  @Test void t212(){ c1(read, read, lent, of(/*impossible*/)); }
  @Test void t213(){ c1(lent, read, lent, of(read)); }
  @Test void t214(){ c1(mut, read, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t215(){ c1(iso, read, lent, of(/*impossible*/)); }
  @Test void t216(){ c1(mdf, read, lent, of(/*not well formed lambda*/)); }
  @Test void t217(){ c1(recMdf, read, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t221(){ c1(imm, lent, lent, of(/*impossible*/)); }
  @Test void t222(){ c1(read, lent, lent, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t223(){ c1(lent, lent, lent, of(read,lent)); }
  @Test void t224(){ c1(mut, lent, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t225(){ c1(iso, lent, lent, of(/*impossible*/)); }
  @Test void t226(){ c1(mdf, lent, lent, of(/*not well formed lambda*/)); }
  @Test void t227(){ c1(recMdf, lent, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t231(){ c1(imm, mut, lent, of(/*impossible*/)); }
  @Test void t232(){ c1(read, mut, lent, of(/*impossible*/)); }
  @Test void t233(){ c1(lent, mut, lent, of(read,lent)); }
  @Test void t234(){ c1(mut, mut, lent, of(read,lent)); }
  @Test void t235(){ c1(iso, mut, lent, of()); }
  @Test void t236(){ c1(mdf, mut, lent, of(/*not well formed lambda*/)); }
  @Test void t237(){ c1(recMdf, mut, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t241(){ c1(imm, iso, lent, of(/*impossible*/)); } //TODO: Marco up to here
  @Test void t242(){ c1(read, iso, lent, of(/*impossible*/)); }
  @Test void t243(){ c1(lent, iso, lent, of(imm,read)); }
  @Test void t244(){ c1(mut, iso, lent, of(imm,read)); }
  @Test void t245(){ c1(iso, iso, lent, of(imm,read)); }
  @Test void t246(){ c1(mdf, iso, lent, of(/*not well formed lambda*/)); }
  @Test void t247(){ c1(recMdf, iso, lent, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t251(){ c1(imm, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t252(){ c1(read, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t253(){ c1(lent, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t254(){ c1(mut, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t255(){ c1(iso, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t256(){ c1(mdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t257(){ c1(recMdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t261(){ c1(imm, recMdf, lent, of(/*impossible*/)); }
  @Test void t262(){ c1(read, recMdf, lent, of(/*impossible*/)); }
  @Test void t263(){ c1(lent, recMdf, lent, of(read)); }
  @Test void t264(){ c1(mut, recMdf, lent, of(/*impossible*/)); }
  @Test void t265(){ c1(iso, recMdf, lent, of(/*impossible*/)); }
  @Test void t266(){ c1(mdf, recMdf, lent, of(/*not well formed lambda*/)); }
  @Test void t267(){ c1(recMdf, recMdf, lent, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t271(){ c1(imm, imm, lent, of(/*impossible*/)); }
  @Test void t272(){ c1(read, imm, lent, of(/*impossible*/)); }
  @Test void t273(){ c1(lent, imm, lent, of(read,imm)); }
  @Test void t274(){ c1(mut, imm, lent, of(read,imm)); }
  @Test void t275(){ c1(iso, imm, lent, of(read,imm)); }
  @Test void t276(){ c1(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t277(){ c1(recMdf, imm, lent, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t301(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t302(){ c1(read, imm, mut, of(/*impossible*/)); }
  @Test void t303(){ c1(lent, imm, mut, of(imm,read)); }
  @Test void t304(){ c1(mut, imm, mut, of(imm,read)); }
  @Test void t305(){ c1(iso, imm, mut, of(imm,read)); }
  @Test void t306(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t307(){ c1(recMdf, imm, mut, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t311(){ c1(imm, read, mut, of(/*impossible*/)); }
  @Test void t312(){ c1(read, read, mut, of(/*impossible*/)); }
  @Test void t313(){ c1(lent, read, mut, of(read)); } // yes because call I can call the mut method through a lent
  @Test void t314(){ c1(mut, read, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t315(){ c1(iso, read, mut, of(/*impossible*/)); }
  @Test void t316(){ c1(mdf, read, mut, of(/*not well formed lambda*/)); }
  @Test void t317(){ c1(recMdf, read, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t321(){ c1(imm, lent, mut, of(/*impossible*/)); }
  @Test void t322(){ c1(read, lent, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t323(){ c1(lent, lent, mut, of(read,lent)); }
  @Test void t324(){ c1(mut, lent, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t325(){ c1(iso, lent, mut, of(/*impossible*/)); }
  @Test void t326(){ c1(mdf, lent, mut, of(/*not well formed lambda*/)); }
  @Test void t327(){ c1(recMdf, lent, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t331(){ c1(imm, mut, mut, of(/*impossible*/)); }
  @Test void t332(){ c1(read, mut, mut, of(/*impossible*/)); }
  @Test void t333(){ c1(lent, mut, mut, of(read,lent)); }
  @Test void t334(){ c1(mut, mut, mut, of(read,lent,mut)); }
  @Test void t335(){ c1(iso, mut, mut, of()); }
  @Test void t336(){ c1(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t337(){ c1(recMdf, mut, mut, of(mut,read,lent)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t341(){ c1(imm, iso, mut, of(/*impossible*/)); }
  @Test void t342(){ c1(read, iso, mut, of(/*impossible*/)); }
  @Test void t343(){ c1(lent, iso, mut, of(imm,read)); }
  @Test void t344(){ c1(mut, iso, mut, of(imm,read)); }
  @Test void t345(){ c1(iso, iso, mut, of(imm,read)); }
  @Test void t346(){ c1(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t347(){ c1(recMdf, iso, mut, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t351(){ c1(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t352(){ c1(read, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t353(){ c1(lent, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t354(){ c1(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t355(){ c1(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t356(){ c1(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t357(){ c1(recMdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t361(){ c1(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t362(){ c1(read, recMdf, mut, of(/*impossible*/)); }
  @Test void t363(){ c1(lent, recMdf, mut, of(read)); }
  @Test void t364(){ c1(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t365(){ c1(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t366(){ c1(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t367(){ c1(recMdf, recMdf, mut, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t371(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t372(){ c1(read, imm, mut, of(/*impossible*/)); }
  @Test void t373(){ c1(lent, imm, mut, of(read,imm)); }
  @Test void t374(){ c1(mut, imm, mut, of(read,imm)); }
  @Test void t375(){ c1(iso, imm, mut, of(read,imm)); }
  @Test void t376(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t377(){ c1(recMdf, imm, mut, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t401(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t402(){ c1(read, imm, iso, of(/*impossible*/)); }
  @Test void t403(){ c1(lent, imm, iso, of()); }
  @Test void t404(){ c1(mut, imm, iso, of(imm,read)); }
  @Test void t405(){ c1(iso, imm, iso, of(imm,read)); }
  @Test void t406(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t407(){ c1(recMdf, imm, iso, of(imm,read)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t411(){ c1(imm, read, iso, of(/*impossible*/)); }
  @Test void t412(){ c1(read, read, iso, of(/*impossible*/)); }
  @Test void t413(){ c1(lent, read, iso, of()); }
  @Test void t414(){ c1(mut, read, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t415(){ c1(iso, read, iso, of(/*impossible*/)); }
  @Test void t416(){ c1(mdf, read, iso, of(/*not well formed lambda*/)); }
  @Test void t417(){ c1(recMdf, read, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t421(){ c1(imm, lent, iso, of(/*impossible*/)); }
  @Test void t422(){ c1(read, lent, iso, of(/*impossible*/)); }
  @Test void t423(){ c1(lent, lent, iso, of()); }
  @Test void t424(){ c1(mut, lent, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t425(){ c1(iso, lent, iso, of(/*impossible*/)); }
  @Test void t426(){ c1(mdf, lent, iso, of(/*not well formed lambda*/)); }
  @Test void t427(){ c1(recMdf, lent, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t431(){ c1(imm, mut, iso, of(/*impossible*/)); }
  @Test void t432(){ c1(read, mut, iso, of(/*impossible*/)); }
  @Test void t433(){ c1(lent, mut, iso, of()); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t434(){ c1(mut, mut, iso, of(read,lent,mut)); } // TODO: maybe no could be unsound
  @Test void t435(){ c1(iso, mut, iso, of()); }
  @Test void t436(){ c1(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t437(){ c1(recMdf, mut, iso, of(mut,read,lent)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t441(){ c1(imm, iso, iso, of(/*impossible*/)); }
  @Test void t442(){ c1(read, iso, iso, of(/*impossible*/)); }
  @Test void t443(){ c1(lent, iso, iso, of()); }
  @Test void t444(){ c1(mut, iso, iso, of(imm,read)); }
  @Test void t445(){ c1(iso, iso, iso, of(imm,read)); } // all iso is captured as imm
  @Test void t446(){ c1(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t447(){ c1(recMdf, iso, iso, of(imm,read)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t451(){ c1(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t452(){ c1(read, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t453(){ c1(lent, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t454(){ c1(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t455(){ c1(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t456(){ c1(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t457(){ c1(recMdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t461(){ c1(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t462(){ c1(read, recMdf, iso, of(/*impossible*/)); }
  @Test void t463(){ c1(lent, recMdf, iso, of()); }
  @Test void t464(){ c1(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t465(){ c1(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t466(){ c1(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t467(){ c1(recMdf, recMdf, iso, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t471(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t472(){ c1(read, imm, iso, of(/*impossible*/)); }
  @Test void t473(){ c1(lent, imm, iso, of()); }
  @Test void t474(){ c1(mut, imm, iso, of(read,imm)); }
  @Test void t475(){ c1(iso, imm, iso, of(read,imm)); }
  @Test void t476(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t477(){ c1(recMdf, imm, iso, of(read,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t501(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t502(){ c1(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t503(){ c1(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t504(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t505(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t506(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t507(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t511(){ c1(imm, read, mdf, of(/*not well formed method*/)); }
  @Test void t512(){ c1(read, read, mdf, of(/*not well formed method*/)); }
  @Test void t513(){ c1(lent, read, mdf, of(/*not well formed method*/)); }
  @Test void t514(){ c1(mut, read, mdf, of(/*not well formed method*/)); }
  @Test void t515(){ c1(iso, read, mdf, of(/*not well formed method*/)); }
  @Test void t516(){ c1(mdf, read, mdf, of(/*not well formed method*/)); }
  @Test void t517(){ c1(recMdf, read, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t521(){ c1(imm, lent, mdf, of(/*not well formed method*/)); }
  @Test void t522(){ c1(read, lent, mdf, of(/*not well formed method*/)); }
  @Test void t523(){ c1(lent, lent, mdf, of(/*not well formed method*/)); }
  @Test void t524(){ c1(mut, lent, mdf, of(/*not well formed method*/)); }
  @Test void t525(){ c1(iso, lent, mdf, of(/*not well formed method*/)); }
  @Test void t526(){ c1(mdf, lent, mdf, of(/*not well formed method*/)); }
  @Test void t527(){ c1(recMdf, lent, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t531(){ c1(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t532(){ c1(read, mut, mdf, of(/*not well formed method*/)); }
  @Test void t533(){ c1(lent, mut, mdf, of(/*not well formed method*/)); }
  @Test void t534(){ c1(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t535(){ c1(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t536(){ c1(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t537(){ c1(recMdf, mut, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t541(){ c1(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t542(){ c1(read, iso, mdf, of(/*not well formed method*/)); }
  @Test void t543(){ c1(lent, iso, mdf, of(/*not well formed method*/)); }
  @Test void t544(){ c1(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t545(){ c1(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t546(){ c1(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t547(){ c1(recMdf, iso, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t551(){ c1(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t552(){ c1(read, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t553(){ c1(lent, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t554(){ c1(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t555(){ c1(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t556(){ c1(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t557(){ c1(recMdf, mdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t561(){ c1(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t562(){ c1(read, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t563(){ c1(lent, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t564(){ c1(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t565(){ c1(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t566(){ c1(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t567(){ c1(recMdf, recMdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t571(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t572(){ c1(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t573(){ c1(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t574(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t575(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t576(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t577(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t601(){ c1(imm, imm, recMdf, of(read,imm)); }
  @Test void t602(){ c1(read, imm, recMdf, of(read,imm)); }
  @Test void t603(){ c1(lent, imm, recMdf, of(read,imm)); }
  @Test void t604(){ c1(mut, imm, recMdf, of(read,imm)); }
  @Test void t605(){ c1(iso, imm, recMdf, of(read,imm)); }
  @Test void t606(){ c1(mdf, imm, recMdf, of(/* not well formed lambda */)); }
  @Test void t607(){ c1(recMdf, imm, recMdf, of(read,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t611(){ c1(imm, read, recMdf, of(/*not well formed method*/)); }
  @Test void t612(){ c1(read, read, recMdf, of(read, recMdf)); }
  @Test void t613(){ c1(lent, read, recMdf, of(read)); }
  @Test void t614(){ c1(mut, read, recMdf, of()); }
  @Test void t615(){ c1(iso, read, recMdf, of()); }
  @Test void t616(){ c1(mdf, read, recMdf, of()); }
  @Test void t617(){ c1(recMdf, read, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t621(){ c1(imm, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t622(){ c1(read, lent, recMdf, of(read, recMdf)); }
  @Test void t623(){ c1(lent, lent, recMdf, of(read, recMdf)); }
  @Test void t624(){ c1(mut, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t625(){ c1(iso, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t626(){ c1(mdf, lent, recMdf, of(/*not well formed method*/)); }
  @Test void t627(){ c1(recMdf, lent, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t631(){ c1(imm, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t632(){ c1(read, mut, recMdf, of(read, recMdf)); }
  @Test void t633(){ c1(lent, mut, recMdf, of(read, recMdf)); }
  @Test void t634(){ c1(mut, mut, recMdf, of(read, recMdf)); }
  @Test void t635(){ c1(iso, mut, recMdf, of()); }
  @Test void t636(){ c1(mdf, mut, recMdf, of(/* not well formed */)); }
  @Test void t637(){ c1(recMdf, mut, recMdf, of(/* impossible */)); } // same as with a read method
  //                     lambda, captured, method, ...capturedAs
  @Test void t641(){ c1(imm, iso, recMdf, of(read, imm)); }
  @Test void t642(){ c1(read, iso, recMdf, of(read, imm)); }
  @Test void t643(){ c1(lent, iso, recMdf, of(read, imm)); }
  @Test void t644(){ c1(mut, iso, recMdf, of(read, imm)); }
  @Test void t645(){ c1(iso, iso, recMdf, of(read, imm)); }
  @Test void t646(){ c1(mdf, iso, recMdf, of(/*not well formed lambda*/)); }
  @Test void t647(){ c1(recMdf, iso, recMdf, of(read, imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t651(){ c1(imm, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t652(){ c1(read, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t653(){ c1(lent, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t654(){ c1(mut, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t655(){ c1(iso, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t656(){ c1(mdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t657(){ c1(recMdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t661(){ c1(imm, recMdf, recMdf, of()); }
  @Test void t662(){ c1(read, recMdf, recMdf, of(read)); }
  @Test void t663(){ c1(lent, recMdf, recMdf, of(read)); }
  @Test void t664(){ c1(mut, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t665(){ c1(iso, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t666(){ c1(mdf, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t667(){ c1(recMdf, recMdf, recMdf, of(read, recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t671(){ c1(imm, imm, recMdf, of(read, imm)); }
  @Test void t672(){ c1(read, imm, recMdf, of(read, imm)); }
  @Test void t673(){ c1(lent, imm, recMdf, of(read, imm)); }
  @Test void t674(){ c1(mut, imm, recMdf, of(read, imm)); }
  @Test void t675(){ c1(iso, imm, recMdf, of(read, imm)); }
  @Test void t676(){ c1(mdf, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t677(){ c1(recMdf, imm, recMdf, of(read, imm)); }

  // ---------------------- c2 ---------------------
  //                     lambda, captured, method, ...capturedAs
  @Test void t2001(){ c2(imm, imm, imm, of(imm,read,mdf)); }
  @Test void t2002(){ c2(read, imm, imm, of(imm,read,mdf)); }
  @Test void t2003(){ c2(lent, imm, imm, of(imm,read,mdf)); }
  @Test void t2004(){ c2(mut, imm, imm, of(imm,read,mdf)); }
  @Test void t2005(){ c2(iso, imm, imm, of(imm,read,mdf)); }
  @Test void t2006(){ c2(mdf, imm, imm, of(/*not well formed lambda*/)); }
  @Test void t2007(){ c2(recMdf, imm, imm, of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2011(){ c2(imm, read, imm, of(/*impossible*/)); }
  @Test void t2012(){ c2(read, read, imm, of(imm,read,mdf)); }
  @Test void t2013(){ c2(lent, read, imm, of(imm,read,mdf)); }
  @Test void t2014(){ c2(mut, read, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2015(){ c2(iso, read, imm, of(/*impossible*/)); }
  @Test void t2016(){ c2(mdf, read, imm, of(/*not well formed lambda*/)); }
  @Test void t2017(){ c2(recMdf, read, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2021(){ c2(imm, lent, imm, of(/*impossible*/)); }
  @Test void t2022(){ c2(read, lent, imm, of(imm,read)); }
  @Test void t2023(){ c2(lent, lent, imm, of(imm,read)); }
  @Test void t2024(){ c2(mut, lent, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2025(){ c2(iso, lent, imm, of(/*impossible*/)); }
  @Test void t2026(){ c2(mdf, lent, imm, of(/*not well formed lambda*/)); }
  @Test void t2027(){ c2(recMdf, lent, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2031(){ c2(imm, mut, imm, of(/*impossible*/)); }
  @Test void t2032(){ c2(read, mut, imm, of(imm,read)); }
  @Test void t2033(){ c2(lent, mut, imm, of(imm,read)); }
  @Test void t2034(){ c2(mut, mut, imm, of(imm,read)); }
  @Test void t2035(){ c2(iso, mut, imm, of()); }
  @Test void t2036(){ c2(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t2037(){ c2(recMdf, mut, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2041(){ c2(imm, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2042(){ c2(read, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2043(){ c2(lent, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2044(){ c2(mut, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2045(){ c2(iso, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2046(){ c2(mdf, iso, imm, of(/*not well formed lambda*/)); }
  @Test void t2047(){ c2(recMdf, iso, imm, of(/*not well formed generic type*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2051(){ c2(imm, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2052(){ c2(read, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2053(){ c2(lent, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2054(){ c2(mut, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2055(){ c2(iso, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2056(){ c2(mdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2057(){ c2(recMdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2061(){ c2(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t2062(){ c2(read, recMdf, imm, of(imm,read)); }
  @Test void t2063(){ c2(lent, recMdf, imm, of(imm,read)); }
  @Test void t2064(){ c2(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t2065(){ c2(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t2066(){ c2(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t2067(){ c2(recMdf, recMdf, imm, of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2181(){ c2(imm, imm, read, of(imm,read,mdf)); }
  @Test void t2182(){ c2(read, imm, read, of(imm,read,mdf)); }
  @Test void t2183(){ c2(lent, imm, read, of(imm,read,mdf)); }//HARD!
  @Test void t2184(){ c2(mut, imm, read, of(imm,read,mdf)); }//HARD!  Note how this is different wrt c1
  @Test void t2185(){ c2(iso, imm, read, of(imm,read,mdf)); }
  @Test void t2186(){ c2(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t2187(){ c2(recMdf, imm, read, of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2101(){ c2(imm, read, read, of(/*impossible*/)); }
  @Test void t2102(){ c2(read, read, read, of(read,mdf)); }
  @Test void t2103(){ c2(lent, read, read, of(read,mdf)); }
  @Test void t2104(){ c2(mut, read, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2105(){ c2(iso, read, read, of(/*impossible*/)); }
  @Test void t2106(){ c2(mdf, read, read, of(/*not well formed lambda*/)); }
  @Test void t2107(){ c2(recMdf, read, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2111(){ c2(imm, lent, read, of(/*impossible*/)); }
  @Test void t2112(){ c2(read, lent, read, of(read)); }// captures lent as recMdf (adapt)
  @Test void t2113(){ c2(lent, lent, read, of(read)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t2114(){ c2(mut, lent, read, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2115(){ c2(iso, lent, read, of(/*impossible*/)); }
  @Test void t2116(){ c2(mdf, lent, read, of(/*not well formed lambda*/)); }
  @Test void t2117(){ c2(recMdf, lent, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2121(){ c2(imm, mut, read, of(/*impossible*/)); }
  @Test void t2122(){ c2(read, mut, read, of(read)); }
  @Test void t2123(){ c2(lent, mut, read, of(read)); }
  @Test void t2124(){ c2(mut, mut, read, of(read)); }
  @Test void t2125(){ c2(iso, mut, read, of()); }
  @Test void t2126(){ c2(mdf, mut, read, of(/*not well formed lambda*/)); }
  @Test void t2127(){ c2(recMdf, mut, read, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2131(){ c2(imm, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2132(){ c2(read, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2133(){ c2(lent, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2134(){ c2(mut, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2135(){ c2(iso, iso, read, of(/*not well formed type arg*/)); }
  @Test void t2136(){ c2(mdf, iso, read, of(/*not well formed lambda*/)); }
  @Test void t2137(){ c2(recMdf, iso, read, of(/*not well formed type arg*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2141(){ c2(imm, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2142(){ c2(read, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2143(){ c2(lent, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2144(){ c2(mut, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2145(){ c2(iso, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2146(){ c2(mdf, mdf, read, of(/*not well formed parameter with mdf*/)); }
  @Test void t2147(){ c2(recMdf, mdf, read, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2151(){ c2(imm, recMdf, read, of(/*impossible*/)); }
  @Test void t2152(){ c2(read, recMdf, read, of(read)); }
  @Test void t2153(){ c2(lent, recMdf, read, of(read)); }
  @Test void t2154(){ c2(mut, recMdf, read, of(/*impossible*/)); }
  @Test void t2155(){ c2(iso, recMdf, read, of(/*impossible*/)); }
  @Test void t2156(){ c2(mdf, recMdf, read, of(/*not well formed lambda*/)); }
  @Test void t2157(){ c2(recMdf, recMdf, read, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2161(){ c2(imm, imm, read, of(read,imm,mdf)); }
  @Test void t2162(){ c2(read, imm, read, of(read,imm,mdf)); }
  @Test void t2163(){ c2(lent, imm, read, of(read,imm,mdf)); } // this is fine because the recMdf is treated as imm
  @Test void t2164(){ c2(mut, imm, read, of(read,imm,mdf)); }
  @Test void t2165(){ c2(iso, imm, read, of(read,imm,mdf)); }
  @Test void t2166(){ c2(mdf, imm, read, of(/*not well formed lambda*/)); }
  @Test void t2167(){ c2(recMdf, imm, read, of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2201(){ c2(imm, imm, lent, of(/*impossible*/)); }
  @Test void t2202(){ c2(read, imm, lent, of(/*impossible*/)); }
  @Test void t2203(){ c2(lent, imm, lent, of(imm,read,mdf)); }
  @Test void t2204(){ c2(mut, imm, lent, of(imm,read,mdf)); }
  @Test void t2205(){ c2(iso, imm, lent, of(imm,read,mdf)); }
  @Test void t2206(){ c2(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t2207(){ c2(recMdf, imm, lent, of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2211(){ c2(imm, read, lent, of(/*impossible*/)); }
  @Test void t2212(){ c2(read, read, lent, of(/*impossible*/)); }
  @Test void t2213(){ c2(lent, read, lent, of(read,mdf)); }
  @Test void t2214(){ c2(mut, read, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2215(){ c2(iso, read, lent, of(/*impossible*/)); }
  @Test void t2216(){ c2(mdf, read, lent, of(/*not well formed lambda*/)); }
  @Test void t2217(){ c2(recMdf, read, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2221(){ c2(imm, lent, lent, of(/*impossible*/)); }
  @Test void t2222(){ c2(read, lent, lent, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2223(){ c2(lent, lent, lent, of(read,lent,mdf)); }
  @Test void t2224(){ c2(mut, lent, lent, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2225(){ c2(iso, lent, lent, of(/*impossible*/)); }
  @Test void t2226(){ c2(mdf, lent, lent, of(/*not well formed lambda*/)); }
  @Test void t2227(){ c2(recMdf, lent, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2231(){ c2(imm, mut, lent, of(/*impossible*/)); }
  @Test void t2232(){ c2(read, mut, lent, of(/*impossible*/)); }
  @Test void t2233(){ c2(lent, mut, lent, of(read,lent)); }
  @Test void t2234(){ c2(mut, mut, lent, of(read,lent)); }
  @Test void t2235(){ c2(iso, mut, lent, of()); }
  @Test void t2236(){ c2(mdf, mut, lent, of(/*not well formed lambda*/)); }
  @Test void t2237(){ c2(recMdf, mut, lent, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2241(){ c2(imm, iso, lent, of(/*impossible*/)); }
  @Test void t2242(){ c2(read, iso, lent, of(/*impossible*/)); }
  @Test void t2243(){ c2(lent, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2244(){ c2(mut, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2245(){ c2(iso, iso, lent, of(/*not well formed type argument*/)); }
  @Test void t2246(){ c2(mdf, iso, lent, of(/*not well formed lambda*/)); }
  @Test void t2247(){ c2(recMdf, iso, lent, of(/*not well formed type argument*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2251(){ c2(imm, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2252(){ c2(read, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2253(){ c2(lent, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2254(){ c2(mut, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2255(){ c2(iso, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2256(){ c2(mdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }
  @Test void t2257(){ c2(recMdf, mdf, lent, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2261(){ c2(imm, recMdf, lent, of(/*impossible*/)); }
  @Test void t2262(){ c2(read, recMdf, lent, of(/*impossible*/)); }
  @Test void t2263(){ c2(lent, recMdf, lent, of(read)); }
  @Test void t2264(){ c2(mut, recMdf, lent, of(/*impossible*/)); }
  @Test void t2265(){ c2(iso, recMdf, lent, of(/*impossible*/)); }
  @Test void t2266(){ c2(mdf, recMdf, lent, of(/*not well formed lambda*/)); }
  @Test void t2267(){ c2(recMdf, recMdf, lent, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2271(){ c2(imm, imm, lent, of(/*impossible*/)); }
  @Test void t2272(){ c2(read, imm, lent, of(/*impossible*/)); }
  @Test void t2273(){ c2(lent, imm, lent, of(read,imm,mdf)); }
  @Test void t2274(){ c2(mut, imm, lent, of(read,imm,mdf)); }
  @Test void t2275(){ c2(iso, imm, lent, of(read,imm,mdf)); }
  @Test void t2276(){ c2(mdf, imm, lent, of(/*not well formed lambda*/)); }
  @Test void t2277(){ c2(recMdf, imm, lent, of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2301(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2302(){ c2(read, imm, mut, of(/*impossible*/)); }
  @Test void t2303(){ c2(lent, imm, mut, of(imm,read,mdf)); }
  @Test void t2304(){ c2(mut, imm, mut, of(imm,read,mdf)); }
  @Test void t2305(){ c2(iso, imm, mut, of(imm,read,mdf)); }
  @Test void t2306(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2307(){ c2(recMdf, imm, mut, of(imm,read,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2311(){ c2(imm, read, mut, of(/*impossible*/)); }
  @Test void t2312(){ c2(read, read, mut, of(/*impossible*/)); }
  @Test void t2313(){ c2(lent, read, mut, of(read,mdf)); } // yes because call I can call the mut method through a lent
  @Test void t2314(){ c2(mut, read, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2315(){ c2(iso, read, mut, of(/*impossible*/)); }
  @Test void t2316(){ c2(mdf, read, mut, of(/*not well formed lambda*/)); }
  @Test void t2317(){ c2(recMdf, read, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2321(){ c2(imm, lent, mut, of(/*impossible*/)); }
  @Test void t2322(){ c2(read, lent, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2323(){ c2(lent, lent, mut, of(read,lent,mdf)); }
  @Test void t2324(){ c2(mut, lent, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2325(){ c2(iso, lent, mut, of(/*impossible*/)); }
  @Test void t2326(){ c2(mdf, lent, mut, of(/*not well formed lambda*/)); }
  @Test void t2327(){ c2(recMdf, lent, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2331(){ c2(imm, mut, mut, of(/*impossible*/)); }
  @Test void t2332(){ c2(read, mut, mut, of(/*impossible*/)); }
  @Test void t2333(){ c2(lent, mut, mut, of(read,lent)); }
  @Test void t2334(){ c2(mut, mut, mut, of(read,lent,mut,mdf)); }
  @Test void t2335(){ c2(iso, mut, mut, of()); }
  @Test void t2336(){ c2(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t2337(){ c2(recMdf, mut, mut, of(mut, lent, read, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2341(){ c2(imm, iso, mut, of(/*impossible*/)); }
  @Test void t2342(){ c2(read, iso, mut, of(/*impossible*/)); }
  @Test void t2343(){ c2(lent, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2344(){ c2(mut, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2345(){ c2(iso, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2346(){ c2(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t2347(){ c2(recMdf, iso, mut, of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2351(){ c2(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2352(){ c2(read, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2353(){ c2(lent, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2354(){ c2(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2355(){ c2(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2356(){ c2(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2357(){ c2(recMdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2361(){ c2(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t2362(){ c2(read, recMdf, mut, of(/*impossible*/)); }
  @Test void t2363(){ c2(lent, recMdf, mut, of(read)); }
  @Test void t2364(){ c2(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t2365(){ c2(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t2366(){ c2(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t2367(){ c2(recMdf, recMdf, mut, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2371(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2372(){ c2(read, imm, mut, of(/*impossible*/)); }
  @Test void t2373(){ c2(lent, imm, mut, of(read,imm,mdf)); }
  @Test void t2374(){ c2(mut, imm, mut, of(read,imm,mdf)); }
  @Test void t2375(){ c2(iso, imm, mut, of(read,imm,mdf)); }
  @Test void t2376(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2377(){ c2(recMdf, imm, mut, of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2401(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2402(){ c2(read, imm, iso, of(/*impossible*/)); }
  @Test void t2403(){ c2(lent, imm, iso, of(/*impossible*/)); }
  @Test void t2404(){ c2(mut, imm, iso, of(imm,read,mdf)); }
  @Test void t2405(){ c2(iso, imm, iso, of(imm,read,mdf)); }
  @Test void t2406(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2407(){ c2(recMdf, imm, iso, of(imm,read,mdf)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t2411(){ c2(imm, read, iso, of(/*impossible*/)); }
  @Test void t2412(){ c2(read, read, iso, of(/*impossible*/)); }
  @Test void t2413(){ c2(lent, read, iso, of(/*impossible*/)); }
  @Test void t2414(){ c2(mut, read, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2415(){ c2(iso, read, iso, of(/*impossible*/)); }
  @Test void t2416(){ c2(mdf, read, iso, of(/*not well formed lambda*/)); }
  @Test void t2417(){ c2(recMdf, read, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2421(){ c2(imm, lent, iso, of(/*impossible*/)); }
  @Test void t2422(){ c2(read, lent, iso, of(/*impossible*/)); }
  @Test void t2423(){ c2(lent, lent, iso, of(/*impossible*/)); }
  @Test void t2424(){ c2(mut, lent, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2425(){ c2(iso, lent, iso, of(/*impossible*/)); }
  @Test void t2426(){ c2(mdf, lent, iso, of(/*not well formed lambda*/)); }
  @Test void t2427(){ c2(recMdf, lent, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2431(){ c2(imm, mut, iso, of(/*impossible*/)); }
  @Test void t2432(){ c2(read, mut, iso, of(/*impossible*/)); }
  @Test void t2433(){ c2(lent, mut, iso, of(/*impossible*/)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t2434(){ c2(mut, mut, iso, of(read,lent,mut,mdf)); }
  @Test void t2435(){ c2(iso, mut, iso, of()); }
  @Test void t2436(){ c2(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t2437(){ c2(recMdf, mut, iso, of(mut, lent, read, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2441(){ c2(imm, iso, iso, of(/*impossible*/)); }
  @Test void t2442(){ c2(read, iso, iso, of(/*impossible*/)); }
  @Test void t2443(){ c2(lent, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2444(){ c2(mut, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2445(){ c2(iso, iso, iso, of(/*not well formed type params*/)); } // all iso is captured as imm
  @Test void t2446(){ c2(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t2447(){ c2(recMdf, iso, iso, of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2451(){ c2(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2452(){ c2(read, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2453(){ c2(lent, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2454(){ c2(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2455(){ c2(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2456(){ c2(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2457(){ c2(recMdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2461(){ c2(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t2462(){ c2(read, recMdf, iso, of(/*impossible*/)); }
  @Test void t2463(){ c2(lent, recMdf, iso, of()); }
  @Test void t2464(){ c2(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t2465(){ c2(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t2466(){ c2(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t2467(){ c2(recMdf, recMdf, iso, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2471(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2472(){ c2(read, imm, iso, of(/*impossible*/)); }
  @Test void t2473(){ c2(lent, imm, iso, of()); }
  @Test void t2474(){ c2(mut, imm, iso, of(read,imm,mdf)); }
  @Test void t2475(){ c2(iso, imm, iso, of(read,imm,mdf)); }
  @Test void t2476(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2477(){ c2(recMdf, imm, iso, of(read,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2501(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2502(){ c2(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2503(){ c2(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2504(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2505(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2506(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2507(){ c2(recMdf, imm, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2511(){ c2(imm, read, mdf, of(/*not well formed method*/)); }
  @Test void t2512(){ c2(read, read, mdf, of(/*not well formed method*/)); }
  @Test void t2513(){ c2(lent, read, mdf, of(/*not well formed method*/)); }
  @Test void t2514(){ c2(mut, read, mdf, of(/*not well formed method*/)); }
  @Test void t2515(){ c2(iso, read, mdf, of(/*not well formed method*/)); }
  @Test void t2516(){ c2(mdf, read, mdf, of(/*not well formed method*/)); }
  @Test void t2517(){ c2(recMdf, read, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2521(){ c2(imm, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2522(){ c2(read, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2523(){ c2(lent, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2524(){ c2(mut, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2525(){ c2(iso, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2526(){ c2(mdf, lent, mdf, of(/*not well formed method*/)); }
  @Test void t2527(){ c2(recMdf, lent, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2531(){ c2(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2532(){ c2(read, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2533(){ c2(lent, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2534(){ c2(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2535(){ c2(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2536(){ c2(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2537(){ c2(recMdf, mut, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2541(){ c2(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2542(){ c2(read, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2543(){ c2(lent, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2544(){ c2(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2545(){ c2(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2546(){ c2(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2547(){ c2(recMdf, iso, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2551(){ c2(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2552(){ c2(read, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2553(){ c2(lent, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2554(){ c2(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2555(){ c2(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2556(){ c2(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2557(){ c2(recMdf, mdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2561(){ c2(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2562(){ c2(read, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2563(){ c2(lent, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2564(){ c2(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2565(){ c2(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2566(){ c2(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2567(){ c2(recMdf, recMdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2571(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2572(){ c2(read, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2573(){ c2(lent, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2574(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2575(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2576(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2577(){ c2(recMdf, imm, mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2601(){ c2(imm, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2602(){ c2(read, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2603(){ c2(lent, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2604(){ c2(mut, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2605(){ c2(iso, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2606(){ c2(mdf, imm, recMdf, of(/*not well formed lambda*/)); }
  @Test void t2607(){ c2(recMdf, imm, recMdf, of(read, imm, recMdf, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2611(){ c2(imm, read, recMdf, of()); }
  @Test void t2612(){ c2(read, read, recMdf, of(read, recMdf, mdf)); }
  @Test void t2613(){ c2(lent, read, recMdf, of(read, recMdf, mdf)); }
  @Test void t2614(){ c2(mut, read, recMdf, of()); }
  @Test void t2615(){ c2(iso, read, recMdf, of()); }
  @Test void t2616(){ c2(mdf, read, recMdf, of()); }
  @Test void t2617(){ c2(recMdf, read, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2621(){ c2(imm, lent, recMdf, of()); }
  @Test void t2622(){ c2(read, lent, recMdf, of(recMdf, read)); } // captures as recMdf, read through subtyping
  @Test void t2623(){ c2(lent, lent, recMdf, of(recMdf, read)); } // captures as recMdf, read through subtyping
  @Test void t2624(){ c2(mut, lent, recMdf, of()); }
  @Test void t2625(){ c2(iso, lent, recMdf, of()); }
  @Test void t2626(){ c2(mdf, lent, recMdf, of()); }
  @Test void t2627(){ c2(recMdf, lent, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2631(){ c2(imm, mut, recMdf, of()); }
  @Test void t2632(){ c2(read, mut, recMdf, of(read, recMdf)); }
  @Test void t2633(){ c2(lent, mut, recMdf, of(read, recMdf)); }
  @Test void t2634(){ c2(mut, mut, recMdf, of(read, recMdf)); }
  @Test void t2635(){ c2(iso, mut, recMdf, of()); }
  @Test void t2636(){ c2(mdf, mut, recMdf, of()); }
  @Test void t2637(){ c2(recMdf, mut, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2641(){ c2(imm, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2642(){ c2(read, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2643(){ c2(lent, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2644(){ c2(mut, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2645(){ c2(iso, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2646(){ c2(mdf, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2647(){ c2(recMdf, iso, recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2651(){ c2(imm, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2652(){ c2(read, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2653(){ c2(lent, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2654(){ c2(mut, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2655(){ c2(iso, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2656(){ c2(mdf, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2657(){ c2(recMdf, mdf, recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2661(){ c2(imm, recMdf, recMdf, of()); }
  @Test void t2662(){ c2(read, recMdf, recMdf, of(read)); }
  @Test void t2663(){ c2(lent, recMdf, recMdf, of(read)); }
  @Test void t2664(){ c2(mut, recMdf, recMdf, of()); }
  @Test void t2665(){ c2(iso, recMdf, recMdf, of()); }
  @Test void t2666(){ c2(mdf, recMdf, recMdf, of()); }
  @Test void t2667(){ c2(recMdf, recMdf, recMdf, of(read, recMdf, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2671(){ c2(imm, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2672(){ c2(read, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2673(){ c2(lent, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2674(){ c2(mut, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2675(){ c2(iso, imm, recMdf, of(read, imm, recMdf, mdf)); }
  @Test void t2676(){ c2(mdf, imm, recMdf, of(/* not well formed lambda */)); }
  @Test void t2677(){ c2(recMdf, imm, recMdf, of(read, imm, recMdf, mdf)); }

  // ----------------------------- c3 --------------------------------
  static List<Mdf> readAll = of(read,mut  , read,lent  , read,read  , read,recMdf  , read,mdf  , read,imm);
  static List<Mdf> lentAll = of(lent,mut  , lent,lent  , lent,read  , lent,recMdf  , lent,mdf  , lent,imm);
  static List<Mdf> mutAll = of(mut,mut  , mut,lent  , mut,read  , mut,recMdf  , mut,mdf  , mut,imm);
  static List<Mdf> immAll = of(imm,mut  , imm,lent  , imm,read  , imm,recMdf  , imm,mdf  , imm,imm);
  static List<Mdf> mdfImm = of(mdf,imm  , mdf,read);
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3001(){ c3(imm, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3002(){ c3(read, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3003(){ c3(lent, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3004(){ c3(mut, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3005(){ c3(iso, imm, imm, mdfImm,immAll,readAll); }
  @Test void t3006(){ c3(mdf, imm, imm, of()); }
  @Test void t3007(){ c3(recMdf,imm,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3011(){ c3(imm, read, imm, of()); }
  @Test void t3012(){ c3(read, read, imm, readAll,immAll,mdfImm); }
  @Test void t3013(){ c3(lent, read, imm, readAll,immAll,mdfImm); }
  @Test void t3014(){ c3(mut, read, imm, of()); }//NOT NoMutHyg
  @Test void t3015(){ c3(iso, read, imm, of()); }
  @Test void t3016(){ c3(mdf, read, imm, of()); }
  @Test void t3017(){ c3(recMdf,read,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3021(){ c3(imm, lent, imm, of()); }
  @Test void t3022(){ c3(read, lent, imm, readAll,immAll,mdfImm); }
  @Test void t3023(){ c3(lent, lent, imm, readAll,immAll,mdfImm); }
  @Test void t3024(){ c3(mut, lent, imm, of()); }//NOT NoMutHyg
  @Test void t3025(){ c3(iso, lent, imm, of()); }
  @Test void t3026(){ c3(mdf, lent, imm, of()); }
  @Test void t3027(){ c3(recMdf,lent,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3031(){ c3(imm, mut, imm, of()); }
  @Test void t3032(){ c3(read, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3033(){ c3(lent, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3034(){ c3(mut, mut, imm, readAll,immAll,mdfImm); }
  //two rules: imm,imm implies read,imm
  //           imm,imm on imm methods should imply imm,mut using adapt
  @Test void t3035(){ c3(iso, mut, imm, of()); }
  @Test void t3036(){ c3(mdf, mut, imm, of()); }
  @Test void t3037(){ c3(recMdf,mut,   imm,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3041(){ c3(imm, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3042(){ c3(read, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3043(){ c3(lent, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3044(){ c3(mut, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3045(){ c3(iso, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3046(){ c3(mdf, iso, imm, of()); }
  @Test void t3047(){ c3(recMdf,iso,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3051(){ c3(imm, mdf, imm, of()); }
  @Test void t3052(){ c3(read, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3053(){ c3(lent, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3054(){ c3(mut, mdf, imm, of()); }
  @Test void t3055(){ c3(iso, mdf, imm, of()); }
  @Test void t3056(){ c3(mdf, mdf, imm, of()); }
  @Test void t3057(){ c3(recMdf,mdf,   imm, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3061(){ c3(imm, recMdf, imm, of()); }
  @Test void t3062(){ c3(read, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3063(){ c3(lent, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3064(){ c3(mut, recMdf, imm, of()); }
  @Test void t3065(){ c3(iso, recMdf, imm, of()); }
  @Test void t3066(){ c3(mdf, recMdf, imm, of()); }
  @Test void t3067(){ c3(recMdf,recMdf,   imm,   of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3181(){ c3(imm,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3182(){ c3(read,  imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3183(){ c3(lent,  imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3184(){ c3(mut,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3185(){ c3(iso,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3186(){ c3(mdf, imm, read, of()); }
  @Test void t3187(){ c3(recMdf,imm, read, readAll,immAll,mdfImm,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3101(){ c3(imm, read, read, of()); }
  @Test void t3102(){ c3(read,  read, read, readAll,of(mdf,read)); }
  @Test void t3103(){ c3(lent,  read, read, readAll,of(mdf,read)); }
  @Test void t3104(){ c3(mut, read, read, of()); }//NOT NoMutHyg
  @Test void t3105(){ c3(iso, read, read, of()); }
  @Test void t3106(){ c3(mdf, read, read, of()); }
  @Test void t3107(){ c3(recMdf,read,  read,   of()); }
  //                     lambda, captured, method, ...(capturedAs, capturedAsG)
  @Test void t3111(){ c3(imm, lent, read, of()); }
  @Test void t3112(){ c3(read,  lent,  read,   readAll,of(mdf,read)); }
  @Test void t3113(){ c3(lent,  lent,  read,   readAll,of(mdf,read)); }
  @Test void t3114(){ c3(mut, lent, read, of()); }//NOT NoMutHyg
  @Test void t3115(){ c3(iso, lent, read, of()); }
  @Test void t3116(){ c3(mdf, lent, read, of()); }
  @Test void t3117(){ c3(recMdf,lent,  read,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3121(){ c3(imm, mut, read, of()); }
  @Test void t3122(){ c3(read,  mut,   read,   readAll,of(mdf,read)); }
  @Test void t3123(){ c3(lent,  mut,   read,   readAll,of(mdf,read)); }
  @Test void t3124(){ c3(mut,   mut,   read,   readAll,of(mdf,read)); }
  @Test void t3125(){ c3(iso,   mut,   read,   of()); }
  @Test void t3126(){ c3(mdf, mut, read, of()); }
  @Test void t3127(){ c3(recMdf,mut,   read,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3131(){ c3(imm,   iso, read, readAll,immAll,mdfImm,of()); }
  @Test void t3132(){ c3(read,  iso, read, readAll,immAll,mdfImm,of()); }
  @Test void t3133(){ c3(lent,  iso, read, readAll,immAll,mdfImm,of()); }
  @Test void t3134(){ c3(mut,   iso, read, readAll,immAll,mdfImm,of()); }
  @Test void t3135(){ c3(iso,   iso, read, readAll,immAll,mdfImm,of()); }
  @Test void t3136(){ c3(mdf, iso, read, of()); }
  @Test void t3137(){ c3(recMdf,iso, read, readAll,immAll,mdfImm,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3141(){ c3(imm,   mdf, read, of()); }
  @Test void t3142(){ c3(read,  mdf,   read, readAll,of(mdf,read)); }
  @Test void t3143(){ c3(lent,  mdf,   read, readAll,of(mdf,read)); }
  @Test void t3144(){ c3(mut, mdf, read, of()); }
  @Test void t3145(){ c3(iso, mdf, read, of()); }
  @Test void t3146(){ c3(mdf, mdf, read, of()); }
  @Test void t3147(){ c3(recMdf,mdf,   read, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3151(){ c3(imm, recMdf, read, of()); }
  @Test void t3152(){ c3(read,  recMdf, read, readAll,of(mdf,read)); }
  @Test void t3153(){ c3(lent,  recMdf, read, readAll,of(mdf,read)); }
  @Test void t3154(){ c3(mut, recMdf, read, of()); }
  @Test void t3155(){ c3(iso, recMdf, read, of()); }
  @Test void t3156(){ c3(mdf, recMdf, read, of()); }
  @Test void t3157(){ c3(recMdf,recMdf,   read,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3161(){ c3(imm,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3162(){ c3(read,  imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3163(){ c3(lent,  imm, read, readAll,immAll,mdfImm,of()); } // this is fine because the recMdf is treated as imm
  @Test void t3164(){ c3(mut,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3165(){ c3(iso,   imm, read, readAll,immAll,mdfImm,of()); }
  @Test void t3166(){ c3(mdf, imm, read, of()); }
  @Test void t3167(){ c3(recMdf,imm, read, readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3201(){ c3(imm, imm, lent, of()); }
  @Test void t3202(){ c3(read, imm, lent, of()); }
  @Test void t3203(){ c3(lent,  imm, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3204(){ c3(mut,   imm, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3205(){ c3(iso,   imm, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3206(){ c3(mdf, imm, lent, of()); }
  @Test void t3207(){ c3(recMdf,imm, lent, readAll,mdfImm,immAll,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3211(){ c3(imm, read, lent, of()); }
  @Test void t3212(){ c3(read, read, lent, of()); }
  @Test void t3213(){ c3(lent,  read, lent, readAll,of(mdf,read)); }
  @Test void t3214(){ c3(mut, read, lent, of()); }//NOT NoMutHyg
  @Test void t3215(){ c3(iso, read, lent, of()); }
  @Test void t3216(){ c3(mdf, read, lent, of()); }
  @Test void t3217(){ c3(recMdf,read,  lent,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3221(){ c3(imm, lent, lent, of()); }
  @Test void t3222(){ c3(read, lent, lent, of()); }
  @Test void t3223(){ c3(lent,  lent,  lent,   readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3224(){ c3(mut, lent, lent, of()); }//NOT NoMutHyg
  @Test void t3225(){ c3(iso, lent, lent, of()); }
  @Test void t3226(){ c3(mdf, lent, lent, of()); }
  @Test void t3227(){ c3(recMdf,lent,  lent,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3231(){ c3(imm, mut, lent, of()); }
  @Test void t3232(){ c3(read, mut, lent, of()); }
  @Test void t3233(){ c3(lent,  mut,   lent,   readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3234(){ c3(mut,   mut,   lent,   readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3235(){ c3(iso,   mut,   lent,   of()); }
  @Test void t3236(){ c3(mdf, mut, lent, of()); }
  @Test void t3237(){ c3(recMdf,mut,   lent,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3241(){ c3(imm, iso, lent, of()); }
  @Test void t3242(){ c3(read, iso, lent, of()); }
  @Test void t3243(){ c3(lent,  iso, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3244(){ c3(mut,   iso, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3245(){ c3(iso,   iso, lent, readAll,mdfImm,immAll,of()); }
  @Test void t3246(){ c3(mdf, iso, lent, of()); }
  @Test void t3247(){ c3(recMdf,iso, lent, readAll,mdfImm,immAll,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3251(){ c3(imm, mdf, lent, of()); }
  @Test void t3252(){ c3(read, mdf, lent, of()); }
  @Test void t3253(){ c3(lent,  mdf,   lent, of()); }
  @Test void t3254(){ c3(mut, mdf, lent, of()); }
  @Test void t3255(){ c3(iso, mdf, lent, of()); }
  @Test void t3256(){ c3(mdf, mdf, lent, of()); }
  @Test void t3257(){ c3(recMdf,mdf,   lent, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3261(){ c3(imm, recMdf, lent, of()); }
  @Test void t3262(){ c3(read, recMdf, lent, of()); }
  @Test void t3263(){ c3(lent,  recMdf, lent, readAll,of(mdf,read)); }
  @Test void t3264(){ c3(mut, recMdf, lent, of()); }
  @Test void t3265(){ c3(iso, recMdf, lent, of()); }
  @Test void t3266(){ c3(mdf, recMdf, lent, of()); }
  @Test void t3267(){ c3(recMdf,recMdf,   lent,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3271(){ c3(imm, imm, lent, of()); }
  @Test void t3272(){ c3(read, imm, lent, of()); }
  @Test void t3273(){ c3(lent,  imm, lent, readAll,immAll,mdfImm,of()); }
  @Test void t3274(){ c3(mut,   imm, lent, readAll,immAll,mdfImm,of()); }
  @Test void t3275(){ c3(iso,   imm, lent, readAll,immAll,mdfImm,of()); }
  @Test void t3276(){ c3(mdf, imm, lent, of()); }
  @Test void t3277(){ c3(recMdf,imm, lent, readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3301(){ c3(imm, imm, mut, of()); }
  @Test void t3302(){ c3(read, imm, mut, of()); }
  @Test void t3303(){ c3(lent, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3304(){ c3(mut, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3305(){ c3(iso, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3306(){ c3(mdf, imm, mut, of()); }
  @Test void t3307(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3311(){ c3(imm, read, mut, of()); }
  @Test void t3312(){ c3(read, read, mut, of()); }
  @Test void t3313(){ c3(lent, read, mut, readAll,of(mdf,read)); }
  @Test void t3314(){ c3(mut, read, mut, of()); }//NOT NoMutHyg
  @Test void t3315(){ c3(iso, read, mut, of()); }
  @Test void t3316(){ c3(mdf, read, mut, of()); }
  @Test void t3317(){ c3(recMdf,read,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3321(){ c3(imm, lent, mut, of()); }
  @Test void t3322(){ c3(read, lent, mut, of()); } // this capture is fine because the method cannot ever be called
  @Test void t3323(){ c3(lent,  lent, mut, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3324(){ c3(mut, lent, mut, of()); }//NOT NoMutHyg
  @Test void t3325(){ c3(iso, lent, mut, of()); }
  @Test void t3326(){ c3(mdf, lent, mut, of()); }
  @Test void t3327(){ c3(recMdf,lent,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3331(){ c3(imm, mut, mut, of()); }
  @Test void t3332(){ c3(read, mut, mut, of()); }
  @Test void t3333(){ c3(lent,  mut, mut, readAll,lentAll,of(mdf,read, mdf,lent)); }
  @Test void t3334(){ c3(mut,   mut,   mut,   readAll,lentAll,of(mdf,read, mdf,lent, mdf,mut, mut,mut  , mut,lent  , mut,read  , mut,mdf  , mut,imm, mut,recMdf)); }
  @Test void t3335(){ c3(iso,   mut,   mut,   of()); }
  @Test void t3336(){ c3(mdf, mut, mut, of()); }
  @Test void t3337(){ c3(recMdf,mut,   mut,  readAll,lentAll,mutAll,of(mdf,lent, mdf,read, mdf,mut)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3341(){ c3(imm, iso, mut, of()); }
  @Test void t3342(){ c3(read, iso, mut, of()); }
  @Test void t3343(){ c3(lent, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3344(){ c3(mut, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3345(){ c3(iso, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3346(){ c3(mdf, iso, mut, of()); }
  @Test void t3347(){ c3(recMdf,iso,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3351(){ c3(imm, mdf, mut, of()); }
  @Test void t3352(){ c3(read, mdf, mut, of()); }
  @Test void t3353(){ c3(lent,  mdf, mut, readAll,of(mdf,read)); }
  @Test void t3354(){ c3(mut, mdf, mut, of()); }
  @Test void t3355(){ c3(iso, mdf, mut, of()); }
  @Test void t3356(){ c3(mdf, mdf, mut, of()); }
  @Test void t3357(){ c3(recMdf,mdf, mut, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3361(){ c3(imm, recMdf, mut, of()); }
  @Test void t3362(){ c3(read, recMdf, mut, of()); }
  @Test void t3363(){ c3(lent, recMdf, mut, readAll,of(mdf,read)); }
  @Test void t3364(){ c3(mut, recMdf, mut, of()); }
  @Test void t3365(){ c3(iso, recMdf, mut, of()); }
  @Test void t3366(){ c3(mdf, recMdf, mut, of()); }
  @Test void t3367(){ c3(recMdf,recMdf,   mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3371(){ c3(imm, imm, mut, of()); }
  @Test void t3372(){ c3(read, imm, mut, of()); }
  @Test void t3373(){ c3(lent, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3374(){ c3(mut, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3375(){ c3(iso, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3376(){ c3(mdf, imm, mut, of()); }
  @Test void t3377(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3401(){ c3(imm, imm, iso, of()); }
  @Test void t3402(){ c3(read, imm, iso, of()); }
  @Test void t3403(){ c3(lent, imm, iso, of()); }
  @Test void t3404(){ c3(mut, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3405(){ c3(iso, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3406(){ c3(mdf, imm, iso, of()); }
  @Test void t3407(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm,of()); } // yes, recMdf could be iso
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3411(){ c3(imm, read, iso, of()); }
  @Test void t3412(){ c3(read, read, iso, of()); }
  @Test void t3413(){ c3(lent, read, iso, of()); }
  @Test void t3414(){ c3(mut, read, iso, of()); }//NOT NoMutHyg
  @Test void t3415(){ c3(iso, read, iso, of()); }
  @Test void t3416(){ c3(mdf, read, iso, of()); }
  @Test void t3417(){ c3(recMdf,read,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3421(){ c3(imm, lent, iso, of()); }
  @Test void t3422(){ c3(read, lent, iso, of()); }
  @Test void t3423(){ c3(lent,  lent, iso, of()); }
  @Test void t3424(){ c3(mut, lent, iso, of()); }//NOT NoMutHyg
  @Test void t3425(){ c3(iso, lent, iso, of()); }
  @Test void t3426(){ c3(mdf, lent, iso, of()); }
  @Test void t3427(){ c3(recMdf,lent,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3431(){ c3(imm, mut, iso, of()); }
  @Test void t3432(){ c3(read, mut, iso, of()); }
  @Test void t3433(){ c3(lent,  mut,   iso, of()); }
  @Test void t3434(){ c3(mut,   mut,   iso, readAll,mutAll,lentAll,of(mdf,read, mdf,lent, mdf,mut)); }
  @Test void t3435(){ c3(iso,   mut,   iso, of()); }
  @Test void t3436(){ c3(mdf, mut, iso, of()); }
  @Test void t3437(){ c3(recMdf,mut,   iso,  of(mdf,mut  , lent,recMdf  , lent,mut  , read,lent  , mut,imm  , read,mdf  , mdf,read  , mut,mdf  , lent,imm  , read,imm  , lent,lent  , read,read  , read,mut  , mdf,lent  , read,recMdf  , lent,mdf  , mut,lent  , lent,read  , mut,mut  , mut,read  , mut,recMdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3441(){ c3(imm, iso, iso, of()); }
  @Test void t3442(){ c3(read, iso, iso, of()); }
  @Test void t3443(){ c3(lent,  iso, iso, of()); }
  @Test void t3444(){ c3(mut,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  @Test void t3445(){ c3(iso,   iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); } // all iso is captured as imm
  @Test void t3446(){ c3(mdf, iso, iso, of()); }
  @Test void t3447(){ c3(recMdf,iso, iso, readAll,immAll,of(mdf,read, mdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3451(){ c3(imm, mdf, iso, of()); }
  @Test void t3452(){ c3(read, mdf, iso, of()); }
  @Test void t3453(){ c3(lent,  mdf, iso, of()); }
  @Test void t3454(){ c3(mut, mdf, iso, of()); }
  @Test void t3455(){ c3(iso, mdf, iso, of()); }
  @Test void t3456(){ c3(mdf, mdf, iso, of()); }
  @Test void t3457(){ c3(recMdf, mdf, iso, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3461(){ c3(imm, recMdf, iso, of()); }
  @Test void t3462(){ c3(read, recMdf, iso, of()); }
  @Test void t3463(){ c3(lent, recMdf, iso, of()); }
  @Test void t3464(){ c3(mut, recMdf, iso, of()); }
  @Test void t3465(){ c3(iso, recMdf, iso, of()); }
  @Test void t3466(){ c3(mdf, recMdf, iso, of()); }
  @Test void t3467(){ c3(recMdf,recMdf,   iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3471(){ c3(imm, imm, iso, of()); }
  @Test void t3472(){ c3(read, imm, iso, of()); }
  @Test void t3473(){ c3(lent, imm, iso, of()); }
  @Test void t3474(){ c3(mut, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3475(){ c3(iso, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3476(){ c3(mdf, imm, iso, of()); }
  @Test void t3477(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3501(){ c3(imm, imm, mdf, of()); }
  @Test void t3502(){ c3(read, imm, mdf, of()); }
  @Test void t3503(){ c3(lent, imm, mdf, of()); }
  @Test void t3504(){ c3(mut, imm, mdf, of()); }
  @Test void t3505(){ c3(iso, imm, mdf, of()); }
  @Test void t3506(){ c3(mdf, imm, mdf, of()); }
  @Test void t3507(){ c3(recMdf,imm,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3511(){ c3(imm, read, mdf, of()); }
  @Test void t3512(){ c3(read, read, mdf, of()); }
  @Test void t3513(){ c3(lent, read, mdf, of()); }
  @Test void t3514(){ c3(mut, read, mdf, of()); }
  @Test void t3515(){ c3(iso, read, mdf, of()); }
  @Test void t3516(){ c3(mdf, read, mdf, of()); }
  @Test void t3517(){ c3(recMdf,read,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3521(){ c3(imm, lent, mdf, of()); }
  @Test void t3522(){ c3(read, lent, mdf, of()); }
  @Test void t3523(){ c3(lent, lent, mdf, of()); }
  @Test void t3524(){ c3(mut, lent, mdf, of()); }
  @Test void t3525(){ c3(iso, lent, mdf, of()); }
  @Test void t3526(){ c3(mdf, lent, mdf, of()); }
  @Test void t3527(){ c3(recMdf,lent,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3531(){ c3(imm, mut, mdf, of()); }
  @Test void t3532(){ c3(read, mut, mdf, of()); }
  @Test void t3533(){ c3(lent, mut, mdf, of()); }
  @Test void t3534(){ c3(mut, mut, mdf, of()); }
  @Test void t3535(){ c3(iso, mut, mdf, of()); }
  @Test void t3536(){ c3(mdf, mut, mdf, of()); }
  @Test void t3537(){ c3(recMdf,mut,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3541(){ c3(imm, iso, mdf, of()); }
  @Test void t3542(){ c3(read, iso, mdf, of()); }
  @Test void t3543(){ c3(lent, iso, mdf, of()); }
  @Test void t3544(){ c3(mut, iso, mdf, of()); }
  @Test void t3545(){ c3(iso, iso, mdf, of()); }
  @Test void t3546(){ c3(mdf, iso, mdf, of()); }
  @Test void t3547(){ c3(recMdf,iso,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3551(){ c3(imm, mdf, mdf, of()); }
  @Test void t3552(){ c3(read, mdf, mdf, of()); }
  @Test void t3553(){ c3(lent, mdf, mdf, of()); }
  @Test void t3554(){ c3(mut, mdf, mdf, of()); }
  @Test void t3555(){ c3(iso, mdf, mdf, of()); }
  @Test void t3556(){ c3(mdf, mdf, mdf, of()); }
  @Test void t3557(){ c3(recMdf,mdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3561(){ c3(imm, recMdf, mdf, of()); }
  @Test void t3562(){ c3(read, recMdf, mdf, of()); }
  @Test void t3563(){ c3(lent, recMdf, mdf, of()); }
  @Test void t3564(){ c3(mut, recMdf, mdf, of()); }
  @Test void t3565(){ c3(iso, recMdf, mdf, of()); }
  @Test void t3566(){ c3(mdf, recMdf, mdf, of()); }
  @Test void t3567(){ c3(recMdf,recMdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3571(){ c3(imm, imm, mdf, of()); }
  @Test void t3572(){ c3(read, imm, mdf, of()); }
  @Test void t3573(){ c3(lent, imm, mdf, of()); }
  @Test void t3574(){ c3(mut, imm, mdf, of()); }
  @Test void t3575(){ c3(iso, imm, mdf, of()); }
  @Test void t3576(){ c3(mdf, imm, mdf, of()); }
  @Test void t3577(){ c3(recMdf,imm,   mdf, of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3601(){ c3(imm, imm, recMdf, of(read,recMdf  , read,mdf  , read,read  , read,imm  , recMdf,read  , read,mut  , read,lent  , recMdf,imm  , mdf,read  , mdf,imm  , imm,lent  , imm,recMdf  , imm,mdf  , imm,mut  , imm,read  , imm,imm)); }
  @Test void t3602(){ c3(read, imm, recMdf, of(imm,lent  , read,mut  , read,lent  , read,imm  , imm,read  , recMdf,imm  , imm,mdf  , mdf,imm  , recMdf,read  , imm,mut  , imm,recMdf  , imm,imm  , read,read  , read,recMdf  , read,mdf  , mdf,read)); }
  @Test void t3603(){ c3(lent, imm, recMdf, of(imm,lent  , read,mut  , read,lent  , read,imm  , imm,read  , recMdf,imm  , imm,mdf  , mdf,imm  , recMdf,read  , imm,mut  , imm,recMdf  , imm,imm  , read,read  , read,recMdf  , read,mdf  , mdf,read)); }
  @Test void t3604(){ c3(mut, imm, recMdf, of(imm,lent  , read,mut  , read,lent  , read,imm  , imm,read  , recMdf,imm  , imm,mdf  , mdf,imm  , recMdf,read  , imm,mut  , imm,recMdf  , imm,imm  , read,read  , read,recMdf  , read,mdf  , mdf,read)); }
  @Test void t3605(){ c3(iso, imm, recMdf, of(imm,lent  , read,mut  , read,lent  , read,imm  , imm,read  , recMdf,imm  , imm,mdf  , mdf,imm  , recMdf,read  , imm,mut  , imm,recMdf  , imm,imm  , read,read  , read,recMdf  , read,mdf  , mdf,read)); }
  @Test void t3606(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3607(){ c3(recMdf,imm,   recMdf, of(imm,lent  , read,mut  , read,lent  , read,imm  , imm,read  , recMdf,imm  , imm,mdf  , mdf,imm  , recMdf,read  , imm,mut  , imm,recMdf  , imm,imm  , read,read  , read,recMdf  , read,mdf  , mdf,read)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3611(){ c3(imm, read, recMdf, of()); }
  @Test void t3612(){ c3(read, read, recMdf, of(read,mdf  , recMdf,recMdf  , read,recMdf  , read,read  , read,mut  , mdf,recMdf  , mdf,read  , recMdf,lent  , read,imm  , recMdf,mdf  , recMdf,mut  , read,lent  , recMdf,read)); }
  @Test void t3613(){ c3(lent, read, recMdf, readAll,of(recMdf,read, mdf,read)); }
  @Test void t3614(){ c3(mut, read, recMdf, of()); }
  @Test void t3615(){ c3(iso, read, recMdf, of()); }
  @Test void t3616(){ c3(mdf, read, recMdf, of()); }
  @Test void t3617(){ c3(recMdf,read,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3621(){ c3(imm, lent, recMdf, of()); }
  @Test void t3622(){ c3(read, lent, recMdf, of(recMdf,recMdf  , read,lent  , recMdf,read  , recMdf,mdf  , read,read  , recMdf,lent  , recMdf,mut  , mdf,recMdf  , mdf,read  , read,mdf  , read,recMdf  , read,mut  , read,imm)); }
  @Test void t3623(){ c3(lent, lent, recMdf, of(mdf,recMdf  , recMdf,recMdf  , read,lent  , mdf,read  , read,read  , recMdf,lent  , recMdf,read  , recMdf,mut  , read,mdf  , read,recMdf  , recMdf,mdf  , read,mut  , read,imm)); }
  @Test void t3624(){ c3(mut, lent, recMdf, of()); }
  @Test void t3625(){ c3(iso, lent, recMdf, of()); }
  @Test void t3626(){ c3(mdf, lent, recMdf, of()); }
  @Test void t3627(){ c3(recMdf,lent,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3631(){ c3(imm, mut, recMdf, of()); }
  @Test void t3632(){ c3(read, mut, recMdf, of(read,lent  , recMdf,recMdf  , recMdf,read  , read,mdf  , read,read  , mdf,recMdf  , read,recMdf  , read,mut  , mdf,read  , recMdf,mdf  , recMdf,lent  , read,imm  , recMdf,mut)); }
  @Test void t3633(){ c3(lent, mut, recMdf, of(read,lent  , recMdf,recMdf  , recMdf,read  , read,mdf  , read,read  , mdf,recMdf  , read,recMdf  , read,mut  , mdf,read  , recMdf,mdf  , recMdf,lent  , read,imm  , recMdf,mut)); }
  @Test void t3634(){ c3(mut, mut, recMdf, of(read,lent  , recMdf,recMdf  , recMdf,read  , read,mdf  , read,read  , mdf,recMdf  , read,recMdf  , read,mut  , mdf,read  , recMdf,mdf  , recMdf,lent  , read,imm  , recMdf,mut)); }
  @Test void t3635(){ c3(iso, mut, recMdf, of()); }
  @Test void t3636(){ c3(mdf, mut, recMdf, of()); }
  @Test void t3637(){ c3(recMdf,mut,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3641(){ c3(imm, iso, recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  @Test void t3642(){ c3(read, iso, recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  @Test void t3643(){ c3(lent, iso, recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  @Test void t3644(){ c3(mut, iso, recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  @Test void t3645(){ c3(iso, iso, recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  @Test void t3646(){ c3(mdf, iso, recMdf, of()); }
  @Test void t3647(){ c3(recMdf,iso,   recMdf, of(imm,mdf  , imm,lent  , imm,imm  , imm,read  , imm,recMdf  , read,mdf  , read,lent  , imm,mut  , recMdf,imm  , read,read  , recMdf,read  , mdf,imm  , read,imm  , read,recMdf  , mdf,read  , read,mut)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3651(){ c3(imm, mdf, recMdf, of()); }
  @Test void t3652(){ c3(read, mdf, recMdf, readAll,of(recMdf,recMdf, recMdf,mdf, recMdf,read, mdf,recMdf, mdf,read, recMdf,lent, recMdf,mut)); }
  @Test void t3653(){ c3(lent, mdf, recMdf, of(read,lent  , read,recMdf  , mdf,recMdf  , recMdf,recMdf  , read,mdf  , read,imm  , read,read  , read,mut  , recMdf,read  , recMdf,lent  , mdf,read  , recMdf,mdf  , recMdf,mut)); }
  @Test void t3654(){ c3(mut, mdf, recMdf, of()); }
  @Test void t3655(){ c3(iso, mdf, recMdf, of()); }
  @Test void t3656(){ c3(mdf, mdf, recMdf, of()); }
  @Test void t3657(){ c3(recMdf,mdf,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3661(){ c3(imm, recMdf, recMdf, of()); }
  @Test void t3662(){ c3(read, recMdf, recMdf, of(read,lent  , read,mdf  , read,mut  , read,imm  , read,read  , recMdf,read  , read,recMdf  , mdf,read)); }
  @Test void t3663(){ c3(lent, recMdf, recMdf, of(read,lent  , read,mdf  , read,mut  , read,imm  , read,read  , recMdf,read  , read,recMdf  , mdf,read)); }
  @Test void t3664(){ c3(mut, recMdf, recMdf, of()); }
  @Test void t3665(){ c3(iso, recMdf, recMdf, of()); }
  @Test void t3666(){ c3(mdf, recMdf, recMdf, of()); }
  @Test void t3667(){ c3(recMdf,recMdf,   recMdf, of(read,mdf  , read,read  , recMdf,recMdf  , read,lent  , read,imm  , read,mut  , recMdf,lent  , recMdf,read  , read,recMdf  , recMdf,mdf  , recMdf,mut  , mdf,recMdf  , mdf,read)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3671(){ c3(imm, imm, recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
  @Test void t3672(){ c3(read, imm, recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
  @Test void t3673(){ c3(lent, imm, recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
  @Test void t3674(){ c3(mut, imm, recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
  @Test void t3675(){ c3(iso, imm, recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
  @Test void t3676(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3677(){ c3(recMdf,imm,   recMdf, of(read,lent  , imm,lent  , read,mdf  , read,recMdf  , read,read  , imm,read  , read,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf,read  , read,mut  , recMdf,read)); }
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
@Test void t051(){ c(imm,   mdf,   imm); }
@Test void t052(){ c(read,  mdf,   imm,   of(imm,read)); }
@Test void t053(){ c(lent,  mdf,   imm,   of(imm,read)); }
@Test void t054(){ c(mut,   mdf,   imm); }//NOT NoMutHyg
@Test void t055(){ c(iso,   mdf,   imm); }//NOT NoMutHyg
@Test void t056(){ c(mdf,   mdf,   imm); }
@Test void t057(){ c(recMdf,mdf,   imm); }
 */
