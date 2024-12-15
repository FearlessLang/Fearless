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

@Disabled("03/12/24")
public class TestCaptureRules {
  // TODO: Handle read
  private static final Mdf[] allMdfs = Arrays.stream(values()).filter(mdf->!mdf.is(read)).toArray(Mdf[]::new);
  
  void c1(Mdf lambda, Mdf captured, Mdf method, List<Mdf> capturedAs) {
    capturedAs.forEach(mdf->cInnerOk(codeGen1.formatted(method, mdf, captured, lambda, lambda)));
    Stream.of(allMdfs).filter(mdf->!mdf.isMdf()).filter(mdf->!capturedAs.contains(mdf))
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
    Stream.of(allMdfs).filter(mdf->!capturedAs.contains(mdf))
      .forEach(mdf->{
        cInnerFail(codeGen2a.formatted(method, mdf, captured, lambda, captured, lambda, captured));
        if (!mdf.is(mdf, recMdf)) {
          cInnerFail(codeGen2b.formatted(method, mdf, captured, captured, lambda, lambda));
        }
      });
  }
  // TODO: add iso to the bounds and update
  String codeGen2a = """
    package test
    B:{}
    L[X:readH,mutH,read,mut,imm]:{ %s .absMeth: %s X }
    A:{ recMdf .m(par: %s B) : %s L[%s B] -> %s L[%s B]{.absMeth->par} }
    """;
  String codeGen2b = """
    package test
    B:{}
    L[X:readH,mutH,read,mut,imm]:{ %s .absMeth: %s X }
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

    var permutations = new ArrayList<Capture>(allMdfs.length * allMdfs.length);
    for (Mdf mdf : allMdfs) {
      for (Mdf mdfi : allMdfs) {
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
  // TODO: add iso to the bounds
  String codeGen3a = """
    package test
    B:{}
    L[X:readH,mutH,read,mut,imm]:{ %s .absMeth: %s X }
    A:{ recMdf .m[T:readH,mutH,read,mut,imm](par: %s T) : %s L[%s T] -> %s L[%s T]{.absMeth->par} }
    """;
  String codeGen3b = """
    package test
    B:{}
    L[X:readH,mutH,read,mut,imm]:{ %s .absMeth: %s X }
    L:L[%s B]
    A:{ recMdf .m(par: %s B) : %s L -> %s L{.absMeth->par} }
    """;

  void cInnerOk(String code){
    try{ok(code);}
    catch(AssertionError t){ throw new AssertionError("failed on "+code+"\nwith:\n"+t); }
  }
  void cInnerFail(String code){
    try{expectFail(code);}
    catch(AssertionError t){ throw new AssertionError("expected failure but succeeded on "+code); }
  }

  //                     lambda, captured, method, ...capturedAs
  @Test void t001(){ c1(imm, imm, imm, of(imm, readH)); }
  @Test void t002(){ c1(readH, imm, imm, of(imm, readH)); }
  @Test void t003(){ c1(mutH, imm, imm, of(imm, readH)); }
  @Test void t004(){ c1(mut, imm, imm, of(imm, readH)); }
  @Test void t005(){ c1(iso, imm, imm, of(imm, readH)); }
  @Test void t007(){ c1(recMdf, imm, imm, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t011(){ c1(imm, readH, imm, of(/*impossible*/)); }
  @Test void t012(){ c1(readH, readH, imm, of(imm, readH)); }
  @Test void t013(){ c1(mutH, readH, imm, of(imm, readH)); }
  @Test void t014(){ c1(mut, readH, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t015(){ c1(iso, readH, imm, of(/*impossible*/)); }
  @Test void t016(){ c1(mdf, readH, imm, of(/*not well formed lambda*/)); }
  @Test void t017(){ c1(recMdf, readH, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t021(){ c1(imm, mutH, imm, of(/*impossible*/)); }
  @Test void t022(){ c1(readH, mutH, imm, of(imm, readH)); }
  @Test void t023(){ c1(mutH, mutH, imm, of(imm, readH)); }
  @Test void t024(){ c1(mut, mutH, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t025(){ c1(iso, mutH, imm, of(/*impossible*/)); }
  @Test void t026(){ c1(mdf, mutH, imm, of(/*not well formed lambda*/)); }
  @Test void t027(){ c1(recMdf, mutH, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t031(){ c1(imm, mut, imm, of(/*impossible*/)); }
  @Test void t032(){ c1(readH, mut, imm, of(imm, readH)); }
  @Test void t033(){ c1(mutH, mut, imm, of(imm, readH)); }
  @Test void t034(){ c1(mut, mut, imm, of(imm, readH)); }
  @Test void t035(){ c1(iso, mut, imm, of()); }
  @Test void t036(){ c1(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t037(){ c1(recMdf, mut, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t041(){ c1(imm, iso, imm, of(imm, readH)); }
  @Test void t042(){ c1(readH, iso, imm, of(imm, readH)); }
  @Test void t043(){ c1(mutH, iso, imm, of(imm, readH)); }
  @Test void t044(){ c1(mut, iso, imm, of(imm, readH)); }
  @Test void t045(){ c1(iso, iso, imm, of(imm, readH)); }
  @Test void t047(){ c1(recMdf, iso, imm, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t061(){ c1(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t062(){ c1(readH, recMdf, imm, of(imm, readH)); }
  @Test void t063(){ c1(mutH, recMdf, imm, of(imm, readH)); }
  @Test void t064(){ c1(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t065(){ c1(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t066(){ c1(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t067(){ c1(recMdf, recMdf, imm, of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t181(){ c1(imm, imm, readH, of(imm, readH)); }
  @Test void t182(){ c1(readH, imm, readH, of(imm, readH)); }
  @Test void t183(){ c1(mutH, imm, readH, of(imm, readH)); }
  @Test void t184(){ c1(mut, imm, readH, of(imm, readH)); }
  @Test void t185(){ c1(iso, imm, readH, of(imm, readH)); }
  @Test void t187(){ c1(recMdf, imm, readH, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t101(){ c1(imm, readH, readH, of(/*impossible*/)); }
  @Test void t102(){ c1(readH, readH, readH, of(readH)); }
  @Test void t103(){ c1(mutH, readH, readH, of(readH)); }
  @Test void t104(){ c1(mut, readH, readH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t105(){ c1(iso, readH, readH, of(/*impossible*/)); }
  @Test void t106(){ c1(mdf, readH, readH, of(/*not well formed lambda*/)); }
  @Test void t107(){ c1(recMdf, readH, readH, of(/*impossible*/)); }
  //                    lambda, captured, method, ...capturedAs
  @Test void t111(){ c1(imm, mutH, readH, of(/*impossible*/)); }
  @Test void t112(){ c1(readH, mutH, readH, of(readH)); }
  @Test void t113(){ c1(mutH, mutH, readH, of(readH)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t114(){ c1(mut, mutH, readH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t115(){ c1(iso, mutH, readH, of(/*impossible*/)); }
  @Test void t116(){ c1(mdf, mutH, readH, of(/*not well formed lambda*/)); }
  @Test void t117(){ c1(recMdf, mutH, readH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t121(){ c1(imm, mut, readH, of(/*impossible*/)); }
  @Test void t122(){ c1(readH, mut, readH, of(readH)); }
  @Test void t123(){ c1(mutH, mut, readH, of(readH)); }
  @Test void t124(){ c1(mut, mut, readH, of(readH)); }
  @Test void t125(){ c1(iso, mut, readH, of()); }
  @Test void t126(){ c1(mdf, mut, readH, of(/*not well formed lambda*/)); }
  @Test void t127(){ c1(recMdf, mut, readH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t131(){ c1(imm, iso, readH, of(imm, readH)); }
  @Test void t132(){ c1(readH, iso, readH, of(imm, readH)); }
  @Test void t133(){ c1(mutH, iso, readH, of(imm, readH)); }
  @Test void t134(){ c1(mut, iso, readH, of(imm, readH)); }
  @Test void t135(){ c1(iso, iso, readH, of(imm, readH)); }
  @Test void t137(){ c1(recMdf, iso, readH, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t151(){ c1(imm, recMdf, readH, of(/*impossible*/)); }
  @Test void t152(){ c1(readH, recMdf, readH, of(readH)); }
  @Test void t153(){ c1(mutH, recMdf, readH, of(readH)); }
  @Test void t154(){ c1(mut, recMdf, readH, of(/*impossible*/)); }
  @Test void t155(){ c1(iso, recMdf, readH, of(/*impossible*/)); }
  @Test void t156(){ c1(mdf, recMdf, readH, of(/*not well formed lambda*/)); }
  @Test void t157(){ c1(recMdf, recMdf, readH, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t161(){ c1(imm, imm, readH, of(readH,imm)); }
  @Test void t162(){ c1(readH, imm, readH, of(readH,imm)); }
  @Test void t163(){ c1(mutH, imm, readH, of(readH,imm)); }
  @Test void t164(){ c1(mut, imm, readH, of(readH,imm)); }
  @Test void t165(){ c1(iso, imm, readH, of(readH,imm)); }
  @Test void t167(){ c1(recMdf, imm, readH, of(readH,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t201(){ c1(imm, imm, mutH, of(/*impossible*/)); }
  @Test void t202(){ c1(readH, imm, mutH, of(/*impossible*/)); }
  @Test void t203(){ c1(mutH, imm, mutH, of(imm, readH)); }
  @Test void t204(){ c1(mut, imm, mutH, of(imm, readH)); }
  @Test void t205(){ c1(iso, imm, mutH, of(imm, readH)); }
  @Test void t206(){ c1(mdf, imm, mutH, of(/*not well formed lambda*/)); }
  @Test void t207(){ c1(recMdf, imm, mutH, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t211(){ c1(imm, readH, mutH, of(/*impossible*/)); }
  @Test void t212(){ c1(readH, readH, mutH, of(/*impossible*/)); }
  @Test void t213(){ c1(mutH, readH, mutH, of(readH)); }
  @Test void t214(){ c1(mut, readH, mutH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t215(){ c1(iso, readH, mutH, of(/*impossible*/)); }
  @Test void t216(){ c1(mdf, readH, mutH, of(/*not well formed lambda*/)); }
  @Test void t217(){ c1(recMdf, readH, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t221(){ c1(imm, mutH, mutH, of(/*impossible*/)); }
  @Test void t222(){ c1(readH, mutH, mutH, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t223(){ c1(mutH, mutH, mutH, of(readH, mutH)); }
  @Test void t224(){ c1(mut, mutH, mutH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t225(){ c1(iso, mutH, mutH, of(/*impossible*/)); }
  @Test void t226(){ c1(mdf, mutH, mutH, of(/*not well formed lambda*/)); }
  @Test void t227(){ c1(recMdf, mutH, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t231(){ c1(imm, mut, mutH, of(/*impossible*/)); }
  @Test void t232(){ c1(readH, mut, mutH, of(/*impossible*/)); }
  @Test void t233(){ c1(mutH, mut, mutH, of(readH, mutH)); }
  @Test void t234(){ c1(mut, mut, mutH, of(readH, mutH)); }
  @Test void t235(){ c1(iso, mut, mutH, of()); }
  @Test void t236(){ c1(mdf, mut, mutH, of(/*not well formed lambda*/)); }
  @Test void t237(){ c1(recMdf, mut, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t241(){ c1(imm, iso, mutH, of(/*impossible*/)); }
  @Test void t242(){ c1(readH, iso, mutH, of(/*impossible*/)); }
  @Test void t243(){ c1(mutH, iso, mutH, of(imm, readH)); }
  @Test void t244(){ c1(mut, iso, mutH, of(imm, readH)); }
  @Test void t245(){ c1(iso, iso, mutH, of(imm, readH)); }
  @Test void t246(){ c1(mdf, iso, mutH, of(/*not well formed lambda*/)); }
  @Test void t247(){ c1(recMdf, iso, mutH, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t251(){ c1(imm, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t252(){ c1(readH, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t253(){ c1(mutH, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t254(){ c1(mut, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t255(){ c1(iso, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t256(){ c1(mdf, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t257(){ c1(recMdf, mdf, mutH, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t261(){ c1(imm, recMdf, mutH, of(/*impossible*/)); }
  @Test void t262(){ c1(readH, recMdf, mutH, of(/*impossible*/)); }
  @Test void t263(){ c1(mutH, recMdf, mutH, of(readH)); }
  @Test void t264(){ c1(mut, recMdf, mutH, of(/*impossible*/)); }
  @Test void t265(){ c1(iso, recMdf, mutH, of(/*impossible*/)); }
  @Test void t266(){ c1(mdf, recMdf, mutH, of(/*not well formed lambda*/)); }
  @Test void t267(){ c1(recMdf, recMdf, mutH, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t271(){ c1(imm, imm, mutH, of(/*impossible*/)); }
  @Test void t272(){ c1(readH, imm, mutH, of(/*impossible*/)); }
  @Test void t273(){ c1(mutH, imm, mutH, of(readH,imm)); }
  @Test void t274(){ c1(mut, imm, mutH, of(readH,imm)); }
  @Test void t275(){ c1(iso, imm, mutH, of(readH,imm)); }
  @Test void t276(){ c1(mdf, imm, mutH, of(/*not well formed lambda*/)); }
  @Test void t277(){ c1(recMdf, imm, mutH, of(readH,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t301(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t302(){ c1(readH, imm, mut, of(/*impossible*/)); }
  @Test void t303(){ c1(mutH, imm, mut, of(imm, readH)); }
  @Test void t304(){ c1(mut, imm, mut, of(imm, readH)); }
  @Test void t305(){ c1(iso, imm, mut, of(imm, readH)); }
  @Test void t306(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t307(){ c1(recMdf, imm, mut, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t311(){ c1(imm, readH, mut, of(/*impossible*/)); }
  @Test void t312(){ c1(readH, readH, mut, of(/*impossible*/)); }
  @Test void t313(){ c1(mutH, readH, mut, of(readH)); } // yes because call I can call the mut method through a lent
  @Test void t314(){ c1(mut, readH, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t315(){ c1(iso, readH, mut, of(/*impossible*/)); }
  @Test void t316(){ c1(mdf, readH, mut, of(/*not well formed lambda*/)); }
  @Test void t317(){ c1(recMdf, readH, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t321(){ c1(imm, mutH, mut, of(/*impossible*/)); }
  @Test void t322(){ c1(readH, mutH, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t323(){ c1(mutH, mutH, mut, of(readH, mutH)); }
  @Test void t324(){ c1(mut, mutH, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t325(){ c1(iso, mutH, mut, of(/*impossible*/)); }
  @Test void t326(){ c1(mdf, mutH, mut, of(/*not well formed lambda*/)); }
  @Test void t327(){ c1(recMdf, mutH, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t331(){ c1(imm, mut, mut, of(/*impossible*/)); }
  @Test void t332(){ c1(readH, mut, mut, of(/*impossible*/)); }
  @Test void t333(){ c1(mutH, mut, mut, of(readH, mutH)); }
  @Test void t334(){ c1(mut, mut, mut, of(readH, mutH,mut)); }
  @Test void t335(){ c1(iso, mut, mut, of()); }
  @Test void t336(){ c1(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t337(){ c1(recMdf, mut, mut, of(mut, readH, mutH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t341(){ c1(imm, iso, mut, of(/*impossible*/)); }
  @Test void t342(){ c1(readH, iso, mut, of(/*impossible*/)); }
  @Test void t343(){ c1(mutH, iso, mut, of(imm, readH)); }
  @Test void t344(){ c1(mut, iso, mut, of(imm, readH)); }
  @Test void t345(){ c1(iso, iso, mut, of(imm, readH)); }
  @Test void t346(){ c1(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t347(){ c1(recMdf, iso, mut, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t351(){ c1(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t352(){ c1(readH, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t353(){ c1(mutH, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t354(){ c1(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t355(){ c1(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t356(){ c1(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t357(){ c1(recMdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t361(){ c1(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t362(){ c1(readH, recMdf, mut, of(/*impossible*/)); }
  @Test void t363(){ c1(mutH, recMdf, mut, of(readH)); }
  @Test void t364(){ c1(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t365(){ c1(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t366(){ c1(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t367(){ c1(recMdf, recMdf, mut, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t371(){ c1(imm, imm, mut, of(/*impossible*/)); }
  @Test void t372(){ c1(readH, imm, mut, of(/*impossible*/)); }
  @Test void t373(){ c1(mutH, imm, mut, of(readH,imm)); }
  @Test void t374(){ c1(mut, imm, mut, of(readH,imm)); }
  @Test void t375(){ c1(iso, imm, mut, of(readH,imm)); }
  @Test void t376(){ c1(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t377(){ c1(recMdf, imm, mut, of(readH,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t401(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t402(){ c1(readH, imm, iso, of(/*impossible*/)); }
  @Test void t403(){ c1(mutH, imm, iso, of()); }
  @Test void t404(){ c1(mut, imm, iso, of(imm, readH)); }
  @Test void t405(){ c1(iso, imm, iso, of(imm, readH)); }
  @Test void t406(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t407(){ c1(recMdf, imm, iso, of(imm, readH)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t411(){ c1(imm, readH, iso, of(/*impossible*/)); }
  @Test void t412(){ c1(readH, readH, iso, of(/*impossible*/)); }
  @Test void t413(){ c1(mutH, readH, iso, of()); }
  @Test void t414(){ c1(mut, readH, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t415(){ c1(iso, readH, iso, of(/*impossible*/)); }
  @Test void t416(){ c1(mdf, readH, iso, of(/*not well formed lambda*/)); }
  @Test void t417(){ c1(recMdf, readH, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t421(){ c1(imm, mutH, iso, of(/*impossible*/)); }
  @Test void t422(){ c1(readH, mutH, iso, of(/*impossible*/)); }
  @Test void t423(){ c1(mutH, mutH, iso, of()); }
  @Test void t424(){ c1(mut, mutH, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t425(){ c1(iso, mutH, iso, of(/*impossible*/)); }
  @Test void t426(){ c1(mdf, mutH, iso, of(/*not well formed lambda*/)); }
  @Test void t427(){ c1(recMdf, mutH, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t431(){ c1(imm, mut, iso, of(/*impossible*/)); }
  @Test void t432(){ c1(readH, mut, iso, of(/*impossible*/)); }
  @Test void t433(){ c1(mutH, mut, iso, of()); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t434(){ c1(mut, mut, iso, of(readH, mutH,mut)); } // TODO: maybe no could be unsound
  @Test void t435(){ c1(iso, mut, iso, of()); }
  @Test void t436(){ c1(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t437(){ c1(recMdf, mut, iso, of(mut, readH, mutH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t441(){ c1(imm, iso, iso, of(/*impossible*/)); }
  @Test void t442(){ c1(readH, iso, iso, of(/*impossible*/)); }
  @Test void t443(){ c1(mutH, iso, iso, of()); }
  @Test void t444(){ c1(mut, iso, iso, of(imm, readH)); }
  @Test void t445(){ c1(iso, iso, iso, of(imm, readH)); } // all iso is captured as imm
  @Test void t446(){ c1(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t447(){ c1(recMdf, iso, iso, of(imm, readH)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t451(){ c1(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t452(){ c1(readH, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t453(){ c1(mutH, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t454(){ c1(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t455(){ c1(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t456(){ c1(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t457(){ c1(recMdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t461(){ c1(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t462(){ c1(readH, recMdf, iso, of(/*impossible*/)); }
  @Test void t463(){ c1(mutH, recMdf, iso, of()); }
  @Test void t464(){ c1(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t465(){ c1(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t466(){ c1(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t467(){ c1(recMdf, recMdf, iso, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t471(){ c1(imm, imm, iso, of(/*impossible*/)); }
  @Test void t472(){ c1(readH, imm, iso, of(/*impossible*/)); }
  @Test void t473(){ c1(mutH, imm, iso, of()); }
  @Test void t474(){ c1(mut, imm, iso, of(readH,imm)); }
  @Test void t475(){ c1(iso, imm, iso, of(readH,imm)); }
  @Test void t476(){ c1(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t477(){ c1(recMdf, imm, iso, of(readH,imm)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t501(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t502(){ c1(readH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t503(){ c1(mutH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t504(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t505(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t506(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t507(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t511(){ c1(imm, readH, mdf, of(/*not well formed method*/)); }
  @Test void t512(){ c1(readH, readH, mdf, of(/*not well formed method*/)); }
  @Test void t513(){ c1(mutH, readH, mdf, of(/*not well formed method*/)); }
  @Test void t514(){ c1(mut, readH, mdf, of(/*not well formed method*/)); }
  @Test void t515(){ c1(iso, readH, mdf, of(/*not well formed method*/)); }
  @Test void t516(){ c1(mdf, readH, mdf, of(/*not well formed method*/)); }
  @Test void t517(){ c1(recMdf, readH, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t521(){ c1(imm, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t522(){ c1(readH, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t523(){ c1(mutH, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t524(){ c1(mut, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t525(){ c1(iso, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t526(){ c1(mdf, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t527(){ c1(recMdf, mutH, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t531(){ c1(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t532(){ c1(readH, mut, mdf, of(/*not well formed method*/)); }
  @Test void t533(){ c1(mutH, mut, mdf, of(/*not well formed method*/)); }
  @Test void t534(){ c1(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t535(){ c1(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t536(){ c1(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t537(){ c1(recMdf, mut, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t541(){ c1(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t542(){ c1(readH, iso, mdf, of(/*not well formed method*/)); }
  @Test void t543(){ c1(mutH, iso, mdf, of(/*not well formed method*/)); }
  @Test void t544(){ c1(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t545(){ c1(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t546(){ c1(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t547(){ c1(recMdf, iso, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t551(){ c1(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t552(){ c1(readH, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t553(){ c1(mutH, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t554(){ c1(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t555(){ c1(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t556(){ c1(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t557(){ c1(recMdf, mdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t561(){ c1(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t562(){ c1(readH, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t563(){ c1(mutH, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t564(){ c1(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t565(){ c1(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t566(){ c1(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t567(){ c1(recMdf, recMdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t571(){ c1(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t572(){ c1(readH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t573(){ c1(mutH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t574(){ c1(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t575(){ c1(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t576(){ c1(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t577(){ c1(recMdf, imm, mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t601(){ c1(imm, imm, recMdf, of(readH,imm)); }
  @Test void t602(){ c1(readH, imm, recMdf, of(readH,imm)); }
  @Test void t603(){ c1(mutH, imm, recMdf, of(readH,imm)); }
  @Test void t604(){ c1(mut, imm, recMdf, of(readH,imm)); }
  @Test void t605(){ c1(iso, imm, recMdf, of(readH,imm)); }
  @Test void t606(){ c1(mdf, imm, recMdf, of(/* not well formed lambda */)); }
  @Test void t607(){ c1(recMdf, imm, recMdf, of(readH,imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t611(){ c1(imm, readH, recMdf, of(/*not well formed method*/)); }
  @Test void t612(){ c1(readH, readH, recMdf, of(readH, recMdf)); }
  @Test void t613(){ c1(mutH, readH, recMdf, of(readH)); }
  @Test void t614(){ c1(mut, readH, recMdf, of()); }
  @Test void t615(){ c1(iso, readH, recMdf, of()); }
  @Test void t616(){ c1(mdf, readH, recMdf, of()); }
  @Test void t617(){ c1(recMdf, readH, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t621(){ c1(imm, mutH, recMdf, of(/*not well formed method*/)); }
  @Test void t622(){ c1(readH, mutH, recMdf, of(readH, recMdf)); }
  @Test void t623(){ c1(mutH, mutH, recMdf, of(readH, recMdf)); }
  @Test void t624(){ c1(mut, mutH, recMdf, of(/*not well formed method*/)); }
  @Test void t625(){ c1(iso, mutH, recMdf, of(/*not well formed method*/)); }
  @Test void t626(){ c1(mdf, mutH, recMdf, of(/*not well formed method*/)); }
  @Test void t627(){ c1(recMdf, mutH, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t631(){ c1(imm, mut, recMdf, of(/*not well formed method*/)); }
  @Test void t632(){ c1(readH, mut, recMdf, of(readH, recMdf)); }
  @Test void t633(){ c1(mutH, mut, recMdf, of(readH, recMdf)); }
  @Test void t634(){ c1(mut, mut, recMdf, of(readH, recMdf)); }
  @Test void t635(){ c1(iso, mut, recMdf, of()); }
  @Test void t636(){ c1(mdf, mut, recMdf, of(/* not well formed */)); }
  @Test void t637(){ c1(recMdf, mut, recMdf, of(/* impossible */)); } // same as with a read method
  //                     lambda, captured, method, ...capturedAs
  @Test void t641(){ c1(imm, iso, recMdf, of(readH, imm)); }
  @Test void t642(){ c1(readH, iso, recMdf, of(readH, imm)); }
  @Test void t643(){ c1(mutH, iso, recMdf, of(readH, imm)); }
  @Test void t644(){ c1(mut, iso, recMdf, of(readH, imm)); }
  @Test void t645(){ c1(iso, iso, recMdf, of(readH, imm)); }
  @Test void t646(){ c1(mdf, iso, recMdf, of(/*not well formed lambda*/)); }
  @Test void t647(){ c1(recMdf, iso, recMdf, of(readH, imm)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t651(){ c1(imm, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t652(){ c1(readH, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t653(){ c1(mutH, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t654(){ c1(mut, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t655(){ c1(iso, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t656(){ c1(mdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  @Test void t657(){ c1(recMdf, mdf, recMdf, of(/*not well formed value to capture*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t661(){ c1(imm, recMdf, recMdf, of()); }
  @Test void t662(){ c1(readH, recMdf, recMdf, of(readH,recMdf)); }
  @Test void t663(){ c1(mutH, recMdf, recMdf, of(readH)); }
  @Test void t664(){ c1(mut, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t665(){ c1(iso, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t666(){ c1(mdf, recMdf, recMdf, of(/*not well formed method*/)); }
  @Test void t667(){ c1(recMdf, recMdf, recMdf, of(readH, recMdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t671(){ c1(imm, imm, recMdf, of(readH, imm)); }
  @Test void t672(){ c1(readH, imm, recMdf, of(readH, imm)); }
  @Test void t673(){ c1(mutH, imm, recMdf, of(readH, imm)); }
  @Test void t674(){ c1(mut, imm, recMdf, of(readH, imm)); }
  @Test void t675(){ c1(iso, imm, recMdf, of(readH, imm)); }
  @Test void t676(){ c1(mdf, imm, recMdf, of(/*not well formed method*/)); }
  @Test void t677(){ c1(recMdf, imm, recMdf, of(readH, imm)); }

  // ---------------------- c2 ---------------------
  //                     lambda, captured, method, ...capturedAs
  @Test void t2001(){ c2(imm, imm, imm, of(imm, readH,mdf)); }
  @Test void t2002(){ c2(readH, imm, imm, of(imm, readH,mdf)); }
  @Test void t2003(){ c2(mutH, imm, imm, of(imm, readH,mdf)); }
  @Test void t2004(){ c2(mut, imm, imm, of(imm, readH,mdf)); }
  @Test void t2005(){ c2(iso, imm, imm, of(imm, readH,mdf)); }
  @Test void t2006(){ c2(mdf, imm, imm, of(/*not well formed lambda*/)); }
  @Test void t2007(){ c2(recMdf, imm, imm, of(imm, readH,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2011(){ c2(imm, readH, imm, of(/*impossible*/)); }
  @Test void t2012(){ c2(readH, readH, imm, of(imm, readH,mdf)); }
  @Test void t2013(){ c2(mutH, readH, imm, of(imm, readH,mdf)); }
  @Test void t2014(){ c2(mut, readH, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2015(){ c2(iso, readH, imm, of(/*impossible*/)); }
  @Test void t2016(){ c2(mdf, readH, imm, of(/*not well formed lambda*/)); }
  @Test void t2017(){ c2(recMdf, readH, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2021(){ c2(imm, mutH, imm, of(/*impossible*/)); }
  @Test void t2022(){ c2(readH, mutH, imm, of(imm, readH)); }
  @Test void t2023(){ c2(mutH, mutH, imm, of(imm, readH)); }
  @Test void t2024(){ c2(mut, mutH, imm, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2025(){ c2(iso, mutH, imm, of(/*impossible*/)); }
  @Test void t2026(){ c2(mdf, mutH, imm, of(/*not well formed lambda*/)); }
  @Test void t2027(){ c2(recMdf, mutH, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2031(){ c2(imm, mut, imm, of(/*impossible*/)); }
  @Test void t2032(){ c2(readH, mut, imm, of(imm, readH)); }
  @Test void t2033(){ c2(mutH, mut, imm, of(imm, readH)); }
  @Test void t2034(){ c2(mut, mut, imm, of(imm, readH)); }
  @Test void t2035(){ c2(iso, mut, imm, of()); }
  @Test void t2036(){ c2(mdf, mut, imm, of(/*not well formed lambda*/)); }
  @Test void t2037(){ c2(recMdf, mut, imm, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2041(){ c2(imm, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2042(){ c2(readH, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2043(){ c2(mutH, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2044(){ c2(mut, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2045(){ c2(iso, iso, imm, of(/*not well formed generic type*/)); }
  @Test void t2046(){ c2(mdf, iso, imm, of(/*not well formed lambda*/)); }
  @Test void t2047(){ c2(recMdf, iso, imm, of(/*not well formed generic type*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2051(){ c2(imm, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2052(){ c2(readH, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2053(){ c2(mutH, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2054(){ c2(mut, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2055(){ c2(iso, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2056(){ c2(mdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }
  @Test void t2057(){ c2(recMdf, mdf, imm, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2061(){ c2(imm, recMdf, imm, of(/*impossible*/)); }
  @Test void t2062(){ c2(readH, recMdf, imm, of(imm, readH)); }
  @Test void t2063(){ c2(mutH, recMdf, imm, of(imm, readH)); }
  @Test void t2064(){ c2(mut, recMdf, imm, of(/*impossible*/)); }
  @Test void t2065(){ c2(iso, recMdf, imm, of(/*impossible*/)); }
  @Test void t2066(){ c2(mdf, recMdf, imm, of(/*not well formed lambda*/)); }
  @Test void t2067(){ c2(recMdf, recMdf, imm, of()); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2181(){ c2(imm, imm, readH, of(imm, readH,mdf)); }
  @Test void t2182(){ c2(readH, imm, readH, of(imm, readH,mdf)); }
  @Test void t2183(){ c2(mutH, imm, readH, of(imm, readH,mdf)); }//HARD!
  @Test void t2184(){ c2(mut, imm, readH, of(imm, readH,mdf)); }//HARD!  Note how this is different wrt c1
  @Test void t2185(){ c2(iso, imm, readH, of(imm, readH,mdf)); }
  @Test void t2186(){ c2(mdf, imm, readH, of(/*not well formed lambda*/)); }
  @Test void t2187(){ c2(recMdf, imm, readH, of(imm, readH,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2101(){ c2(imm, readH, readH, of(/*impossible*/)); }
  @Test void t2102(){ c2(readH, readH, readH, of(readH,mdf)); }
  @Test void t2103(){ c2(mutH, readH, readH, of(readH,mdf)); }
  @Test void t2104(){ c2(mut, readH, readH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2105(){ c2(iso, readH, readH, of(/*impossible*/)); }
  @Test void t2106(){ c2(mdf, readH, readH, of(/*not well formed lambda*/)); }
  @Test void t2107(){ c2(recMdf, readH, readH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2111(){ c2(imm, mutH, readH, of(/*impossible*/)); }
  @Test void t2112(){ c2(readH, mutH, readH, of(readH)); }// captures mutH as recMdf (adapt)
  @Test void t2113(){ c2(mutH, mutH, readH, of(readH)); }//the lambda is created read, and can not become anything else but imm.
  @Test void t2114(){ c2(mut, mutH, readH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2115(){ c2(iso, mutH, readH, of(/*impossible*/)); }
  @Test void t2116(){ c2(mdf, mutH, readH, of(/*not well formed lambda*/)); }
  @Test void t2117(){ c2(recMdf, mutH, readH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2121(){ c2(imm, mut, readH, of(/*impossible*/)); }
  @Test void t2122(){ c2(readH, mut, readH, of(readH)); }
  @Test void t2123(){ c2(mutH, mut, readH, of(readH)); }
  @Test void t2124(){ c2(mut, mut, readH, of(readH)); }
  @Test void t2125(){ c2(iso, mut, readH, of()); }
  @Test void t2126(){ c2(mdf, mut, readH, of(/*not well formed lambda*/)); }
  @Test void t2127(){ c2(recMdf, mut, readH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2131(){ c2(imm, iso, readH, of(/*not well formed type arg*/)); }
  @Test void t2132(){ c2(readH, iso, readH, of(/*not well formed type arg*/)); }
  @Test void t2133(){ c2(mutH, iso, readH, of(/*not well formed type arg*/)); }
  @Test void t2134(){ c2(mut, iso, readH, of(/*not well formed type arg*/)); }
  @Test void t2135(){ c2(iso, iso, readH, of(/*not well formed type arg*/)); }
  @Test void t2136(){ c2(mdf, iso, readH, of(/*not well formed lambda*/)); }
  @Test void t2137(){ c2(recMdf, iso, readH, of(/*not well formed type arg*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2141(){ c2(imm, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2142(){ c2(readH, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2143(){ c2(mutH, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2144(){ c2(mut, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2145(){ c2(iso, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2146(){ c2(mdf, mdf, readH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2147(){ c2(recMdf, mdf, readH, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2151(){ c2(imm, recMdf, readH, of(/*impossible*/)); }
  @Test void t2152(){ c2(readH, recMdf, readH, of(readH)); }
  @Test void t2153(){ c2(mutH, recMdf, readH, of(readH)); }
  @Test void t2154(){ c2(mut, recMdf, readH, of(/*impossible*/)); }
  @Test void t2155(){ c2(iso, recMdf, readH, of(/*impossible*/)); }
  @Test void t2156(){ c2(mdf, recMdf, readH, of(/*not well formed lambda*/)); }
  @Test void t2157(){ c2(recMdf, recMdf, readH, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2161(){ c2(imm, imm, readH, of(readH,imm,mdf)); }
  @Test void t2162(){ c2(readH, imm, readH, of(readH,imm,mdf)); }
  @Test void t2163(){ c2(mutH, imm, readH, of(readH,imm,mdf)); } // this is fine because the recMdf is treated as imm
  @Test void t2164(){ c2(mut, imm, readH, of(readH,imm,mdf)); }
  @Test void t2165(){ c2(iso, imm, readH, of(readH,imm,mdf)); }
  @Test void t2166(){ c2(mdf, imm, readH, of(/*not well formed lambda*/)); }
  @Test void t2167(){ c2(recMdf, imm, readH, of(readH,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2201(){ c2(imm, imm, mutH, of(/*impossible*/)); }
  @Test void t2202(){ c2(readH, imm, mutH, of(/*impossible*/)); }
  @Test void t2203(){ c2(mutH, imm, mutH, of(imm, readH,mdf)); }
  @Test void t2204(){ c2(mut, imm, mutH, of(imm, readH,mdf)); }
  @Test void t2205(){ c2(iso, imm, mutH, of(imm, readH,mdf)); }
  @Test void t2206(){ c2(mdf, imm, mutH, of(/*not well formed lambda*/)); }
  @Test void t2207(){ c2(recMdf, imm, mutH, of(imm, readH,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2211(){ c2(imm, readH, mutH, of(/*impossible*/)); }
  @Test void t2212(){ c2(readH, readH, mutH, of(/*impossible*/)); }
  @Test void t2213(){ c2(mutH, readH, mutH, of(readH,mdf)); }
  @Test void t2214(){ c2(mut, readH, mutH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2215(){ c2(iso, readH, mutH, of(/*impossible*/)); }
  @Test void t2216(){ c2(mdf, readH, mutH, of(/*not well formed lambda*/)); }
  @Test void t2217(){ c2(recMdf, readH, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2221(){ c2(imm, mutH, mutH, of(/*impossible*/)); }
  @Test void t2222(){ c2(readH, mutH, mutH, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2223(){ c2(mutH, mutH, mutH, of(readH, mutH,mdf)); }
  @Test void t2224(){ c2(mut, mutH, mutH, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2225(){ c2(iso, mutH, mutH, of(/*impossible*/)); }
  @Test void t2226(){ c2(mdf, mutH, mutH, of(/*not well formed lambda*/)); }
  @Test void t2227(){ c2(recMdf, mutH, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2231(){ c2(imm, mut, mutH, of(/*impossible*/)); }
  @Test void t2232(){ c2(readH, mut, mutH, of(/*impossible*/)); }
  @Test void t2233(){ c2(mutH, mut, mutH, of(readH, mutH)); }
  @Test void t2234(){ c2(mut, mut, mutH, of(readH, mutH)); }
  @Test void t2235(){ c2(iso, mut, mutH, of()); }
  @Test void t2236(){ c2(mdf, mut, mutH, of(/*not well formed lambda*/)); }
  @Test void t2237(){ c2(recMdf, mut, mutH, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2241(){ c2(imm, iso, mutH, of(/*impossible*/)); }
  @Test void t2242(){ c2(readH, iso, mutH, of(/*impossible*/)); }
  @Test void t2243(){ c2(mutH, iso, mutH, of(/*not well formed type argument*/)); }
  @Test void t2244(){ c2(mut, iso, mutH, of(/*not well formed type argument*/)); }
  @Test void t2245(){ c2(iso, iso, mutH, of(/*not well formed type argument*/)); }
  @Test void t2246(){ c2(mdf, iso, mutH, of(/*not well formed lambda*/)); }
  @Test void t2247(){ c2(recMdf, iso, mutH, of(/*not well formed type argument*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2251(){ c2(imm, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2252(){ c2(readH, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2253(){ c2(mutH, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2254(){ c2(mut, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2255(){ c2(iso, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2256(){ c2(mdf, mdf, mutH, of(/*not well formed parameter with mdf*/)); }
  @Test void t2257(){ c2(recMdf, mdf, mutH, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2261(){ c2(imm, recMdf, mutH, of(/*impossible*/)); }
  @Test void t2262(){ c2(readH, recMdf, mutH, of(/*impossible*/)); }
  @Test void t2263(){ c2(mutH, recMdf, mutH, of(readH)); }
  @Test void t2264(){ c2(mut, recMdf, mutH, of(/*impossible*/)); }
  @Test void t2265(){ c2(iso, recMdf, mutH, of(/*impossible*/)); }
  @Test void t2266(){ c2(mdf, recMdf, mutH, of(/*not well formed lambda*/)); }
  @Test void t2267(){ c2(recMdf, recMdf, mutH, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2271(){ c2(imm, imm, mutH, of(/*impossible*/)); }
  @Test void t2272(){ c2(readH, imm, mutH, of(/*impossible*/)); }
  @Test void t2273(){ c2(mutH, imm, mutH, of(readH,imm,mdf)); }
  @Test void t2274(){ c2(mut, imm, mutH, of(readH,imm,mdf)); }
  @Test void t2275(){ c2(iso, imm, mutH, of(readH,imm,mdf)); }
  @Test void t2276(){ c2(mdf, imm, mutH, of(/*not well formed lambda*/)); }
  @Test void t2277(){ c2(recMdf, imm, mutH, of(readH,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2301(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2302(){ c2(readH, imm, mut, of(/*impossible*/)); }
  @Test void t2303(){ c2(mutH, imm, mut, of(imm, readH,mdf)); }
  @Test void t2304(){ c2(mut, imm, mut, of(imm, readH,mdf)); }
  @Test void t2305(){ c2(iso, imm, mut, of(imm, readH,mdf)); }
  @Test void t2306(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2307(){ c2(recMdf, imm, mut, of(imm, readH,mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2311(){ c2(imm, readH, mut, of(/*impossible*/)); }
  @Test void t2312(){ c2(readH, readH, mut, of(/*impossible*/)); }
  @Test void t2313(){ c2(mutH, readH, mut, of(readH,mdf)); } // yes because call I can call the mut method through a lent
  @Test void t2314(){ c2(mut, readH, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2315(){ c2(iso, readH, mut, of(/*impossible*/)); }
  @Test void t2316(){ c2(mdf, readH, mut, of(/*not well formed lambda*/)); }
  @Test void t2317(){ c2(recMdf, readH, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2321(){ c2(imm, mutH, mut, of(/*impossible*/)); }
  @Test void t2322(){ c2(readH, mutH, mut, of(/*impossible*/)); } // this capture is fine because the method cannot ever be called
  @Test void t2323(){ c2(mutH, mutH, mut, of(readH, mutH,mdf)); }
  @Test void t2324(){ c2(mut, mutH, mut, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2325(){ c2(iso, mutH, mut, of(/*impossible*/)); }
  @Test void t2326(){ c2(mdf, mutH, mut, of(/*not well formed lambda*/)); }
  @Test void t2327(){ c2(recMdf, mutH, mut, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2331(){ c2(imm, mut, mut, of(/*impossible*/)); }
  @Test void t2332(){ c2(readH, mut, mut, of(/*impossible*/)); }
  @Test void t2333(){ c2(mutH, mut, mut, of(readH, mutH)); }
  @Test void t2334(){ c2(mut, mut, mut, of(readH, mutH,mut,mdf)); }
  @Test void t2335(){ c2(iso, mut, mut, of()); }
  @Test void t2336(){ c2(mdf, mut, mut, of(/*not well formed lambda*/)); }
  @Test void t2337(){ c2(recMdf, mut, mut, of(mut, mutH, readH, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2341(){ c2(imm, iso, mut, of(/*impossible*/)); }
  @Test void t2342(){ c2(readH, iso, mut, of(/*impossible*/)); }
  @Test void t2343(){ c2(mutH, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2344(){ c2(mut, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2345(){ c2(iso, iso, mut, of(/*not well formed type params*/)); }
  @Test void t2346(){ c2(mdf, iso, mut, of(/*not well formed lambda*/)); }
  @Test void t2347(){ c2(recMdf, iso, mut, of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2351(){ c2(imm, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2352(){ c2(readH, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2353(){ c2(mutH, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2354(){ c2(mut, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2355(){ c2(iso, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2356(){ c2(mdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }
  @Test void t2357(){ c2(recMdf, mdf, mut, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2361(){ c2(imm, recMdf, mut, of(/*impossible*/)); }
  @Test void t2362(){ c2(readH, recMdf, mut, of(/*impossible*/)); }
  @Test void t2363(){ c2(mutH, recMdf, mut, of(readH)); }
  @Test void t2364(){ c2(mut, recMdf, mut, of(/*impossible*/)); }
  @Test void t2365(){ c2(iso, recMdf, mut, of(/*impossible*/)); }
  @Test void t2366(){ c2(mdf, recMdf, mut, of(/*not well formed lambda*/)); }
  @Test void t2367(){ c2(recMdf, recMdf, mut, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2371(){ c2(imm, imm, mut, of(/*impossible*/)); }
  @Test void t2372(){ c2(readH, imm, mut, of(/*impossible*/)); }
  @Test void t2373(){ c2(mutH, imm, mut, of(readH,imm,mdf)); }
  @Test void t2374(){ c2(mut, imm, mut, of(readH,imm,mdf)); }
  @Test void t2375(){ c2(iso, imm, mut, of(readH,imm,mdf)); }
  @Test void t2376(){ c2(mdf, imm, mut, of(/*not well formed lambda*/)); }
  @Test void t2377(){ c2(recMdf, imm, mut, of(readH,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2401(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2402(){ c2(readH, imm, iso, of(/*impossible*/)); }
  @Test void t2403(){ c2(mutH, imm, iso, of(/*impossible*/)); }
  @Test void t2404(){ c2(mut, imm, iso, of(imm, readH,mdf)); }
  @Test void t2405(){ c2(iso, imm, iso, of(imm, readH,mdf)); }
  @Test void t2406(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2407(){ c2(recMdf, imm, iso, of(imm, readH,mdf)); } // yes, recMdf could be iso
  //                     lambda, captured, method, ...capturedAs
  @Test void t2411(){ c2(imm, readH, iso, of(/*impossible*/)); }
  @Test void t2412(){ c2(readH, readH, iso, of(/*impossible*/)); }
  @Test void t2413(){ c2(mutH, readH, iso, of(/*impossible*/)); }
  @Test void t2414(){ c2(mut, readH, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2415(){ c2(iso, readH, iso, of(/*impossible*/)); }
  @Test void t2416(){ c2(mdf, readH, iso, of(/*not well formed lambda*/)); }
  @Test void t2417(){ c2(recMdf, readH, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2421(){ c2(imm, mutH, iso, of(/*impossible*/)); }
  @Test void t2422(){ c2(readH, mutH, iso, of(/*impossible*/)); }
  @Test void t2423(){ c2(mutH, mutH, iso, of(/*impossible*/)); }
  @Test void t2424(){ c2(mut, mutH, iso, of(/*impossible*/)); }//NOT NoMutHyg
  @Test void t2425(){ c2(iso, mutH, iso, of(/*impossible*/)); }
  @Test void t2426(){ c2(mdf, mutH, iso, of(/*not well formed lambda*/)); }
  @Test void t2427(){ c2(recMdf, mutH, iso, of(/*impossible*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2431(){ c2(imm, mut, iso, of(/*impossible*/)); }
  @Test void t2432(){ c2(readH, mut, iso, of(/*impossible*/)); }
  @Test void t2433(){ c2(mutH, mut, iso, of(/*impossible*/)); } // These 3 look odd, but it's correct because iso lambdas are treated like mut
  @Test void t2434(){ c2(mut, mut, iso, of(readH, mutH,mut,mdf)); }
  @Test void t2435(){ c2(iso, mut, iso, of()); }
  @Test void t2436(){ c2(mdf, mut, iso, of(/*not well formed lambda*/)); }
  @Test void t2437(){ c2(recMdf, mut, iso, of(mut, mutH, readH, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2441(){ c2(imm, iso, iso, of(/*impossible*/)); }
  @Test void t2442(){ c2(readH, iso, iso, of(/*impossible*/)); }
  @Test void t2443(){ c2(mutH, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2444(){ c2(mut, iso, iso, of(/*not well formed type params*/)); }
  @Test void t2445(){ c2(iso, iso, iso, of(/*not well formed type params*/)); } // all iso is captured as imm
  @Test void t2446(){ c2(mdf, iso, iso, of(/*not well formed lambda*/)); }
  @Test void t2447(){ c2(recMdf, iso, iso, of(/*not well formed type params*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2451(){ c2(imm, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2452(){ c2(readH, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2453(){ c2(mutH, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2454(){ c2(mut, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2455(){ c2(iso, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2456(){ c2(mdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }
  @Test void t2457(){ c2(recMdf, mdf, iso, of(/*not well formed parameter with mdf*/)); }/*not well formed parameter with mdf*/
  //                     lambda, captured, method, ...capturedAs
  @Test void t2461(){ c2(imm, recMdf, iso, of(/*impossible*/)); }
  @Test void t2462(){ c2(readH, recMdf, iso, of(/*impossible*/)); }
  @Test void t2463(){ c2(mutH, recMdf, iso, of()); }
  @Test void t2464(){ c2(mut, recMdf, iso, of(/*impossible*/)); }
  @Test void t2465(){ c2(iso, recMdf, iso, of(/*impossible*/)); }
  @Test void t2466(){ c2(mdf, recMdf, iso, of(/*not well formed lambda*/)); }
  @Test void t2467(){ c2(recMdf, recMdf, iso, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2471(){ c2(imm, imm, iso, of(/*impossible*/)); }
  @Test void t2472(){ c2(readH, imm, iso, of(/*impossible*/)); }
  @Test void t2473(){ c2(mutH, imm, iso, of()); }
  @Test void t2474(){ c2(mut, imm, iso, of(readH,imm,mdf)); }
  @Test void t2475(){ c2(iso, imm, iso, of(readH,imm,mdf)); }
  @Test void t2476(){ c2(mdf, imm, iso, of(/*not well formed lambda*/)); }
  @Test void t2477(){ c2(recMdf, imm, iso, of(readH,imm,mdf)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2501(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2502(){ c2(readH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2503(){ c2(mutH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2504(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2505(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2506(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2507(){ c2(recMdf, imm, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2511(){ c2(imm, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2512(){ c2(readH, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2513(){ c2(mutH, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2514(){ c2(mut, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2515(){ c2(iso, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2516(){ c2(mdf, readH, mdf, of(/*not well formed method*/)); }
  @Test void t2517(){ c2(recMdf, readH, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2521(){ c2(imm, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2522(){ c2(readH, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2523(){ c2(mutH, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2524(){ c2(mut, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2525(){ c2(iso, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2526(){ c2(mdf, mutH, mdf, of(/*not well formed method*/)); }
  @Test void t2527(){ c2(recMdf, mutH, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2531(){ c2(imm, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2532(){ c2(readH, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2533(){ c2(mutH, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2534(){ c2(mut, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2535(){ c2(iso, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2536(){ c2(mdf, mut, mdf, of(/*not well formed method*/)); }
  @Test void t2537(){ c2(recMdf, mut, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2541(){ c2(imm, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2542(){ c2(readH, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2543(){ c2(mutH, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2544(){ c2(mut, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2545(){ c2(iso, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2546(){ c2(mdf, iso, mdf, of(/*not well formed method*/)); }
  @Test void t2547(){ c2(recMdf, iso, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2551(){ c2(imm, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2552(){ c2(readH, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2553(){ c2(mutH, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2554(){ c2(mut, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2555(){ c2(iso, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2556(){ c2(mdf, mdf, mdf, of(/*not well formed method*/)); }
  @Test void t2557(){ c2(recMdf, mdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2561(){ c2(imm, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2562(){ c2(readH, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2563(){ c2(mutH, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2564(){ c2(mut, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2565(){ c2(iso, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2566(){ c2(mdf, recMdf, mdf, of(/*not well formed method*/)); }
  @Test void t2567(){ c2(recMdf, recMdf, mdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2571(){ c2(imm, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2572(){ c2(readH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2573(){ c2(mutH, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2574(){ c2(mut, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2575(){ c2(iso, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2576(){ c2(mdf, imm, mdf, of(/*not well formed method*/)); }
  @Test void t2577(){ c2(recMdf, imm, mdf, of(/*not well formed method*/)); }

  //                     lambda, captured, method, ...capturedAs
  @Test void t2601(){ c2(imm, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2602(){ c2(readH, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2603(){ c2(mutH, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2604(){ c2(mut, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2605(){ c2(iso, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2606(){ c2(mdf, imm, recMdf, of(/*not well formed lambda*/)); }
  @Test void t2607(){ c2(recMdf, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2611(){ c2(imm, readH, recMdf, of()); }
  @Test void t2612(){ c2(readH, readH, recMdf, of(readH, recMdf, mdf)); }
  @Test void t2613(){ c2(mutH, readH, recMdf, of(readH, recMdf, mdf)); }
  @Test void t2614(){ c2(mut, readH, recMdf, of()); }
  @Test void t2615(){ c2(iso, readH, recMdf, of()); }
  @Test void t2616(){ c2(mdf, readH, recMdf, of()); }
  @Test void t2617(){ c2(recMdf, readH, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2621(){ c2(imm, mutH, recMdf, of()); }
  @Test void t2622(){ c2(readH, mutH, recMdf, of(recMdf, readH)); } // captures as recMdf, read through subtyping
  @Test void t2623(){ c2(mutH, mutH, recMdf, of(recMdf, readH)); } // captures as recMdf, read through subtyping
  @Test void t2624(){ c2(mut, mutH, recMdf, of()); }
  @Test void t2625(){ c2(iso, mutH, recMdf, of()); }
  @Test void t2626(){ c2(mdf, mutH, recMdf, of()); }
  @Test void t2627(){ c2(recMdf, mutH, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2631(){ c2(imm, mut, recMdf, of()); }
  @Test void t2632(){ c2(readH, mut, recMdf, of(readH, recMdf)); }
  @Test void t2633(){ c2(mutH, mut, recMdf, of(readH, recMdf)); }
  @Test void t2634(){ c2(mut, mut, recMdf, of(readH, recMdf)); }
  @Test void t2635(){ c2(iso, mut, recMdf, of()); }
  @Test void t2636(){ c2(mdf, mut, recMdf, of()); }
  @Test void t2637(){ c2(recMdf, mut, recMdf, of()); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2641(){ c2(imm, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2642(){ c2(readH, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2643(){ c2(mutH, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2644(){ c2(mut, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2645(){ c2(iso, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2646(){ c2(mdf, iso, recMdf, of(/*not well formed method*/)); }
  @Test void t2647(){ c2(recMdf, iso, recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2651(){ c2(imm, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2652(){ c2(readH, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2653(){ c2(mutH, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2654(){ c2(mut, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2655(){ c2(iso, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2656(){ c2(mdf, mdf, recMdf, of(/*not well formed method*/)); }
  @Test void t2657(){ c2(recMdf, mdf, recMdf, of(/*not well formed method*/)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2661(){ c2(imm, recMdf, recMdf, of()); }
  @Test void t2662(){ c2(readH, recMdf, recMdf, of(readH,recMdf,mdf)); }
  @Test void t2663(){ c2(mutH, recMdf, recMdf, of(readH)); }
  @Test void t2664(){ c2(mut, recMdf, recMdf, of()); }
  @Test void t2665(){ c2(iso, recMdf, recMdf, of()); }
  @Test void t2666(){ c2(mdf, recMdf, recMdf, of()); }
  @Test void t2667(){ c2(recMdf, recMdf, recMdf, of(readH, recMdf, mdf)); }
  //                     lambda, captured, method, ...capturedAs
  @Test void t2671(){ c2(imm, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2672(){ c2(readH, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2673(){ c2(mutH, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2674(){ c2(mut, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2675(){ c2(iso, imm, recMdf, of(readH, imm, recMdf, mdf)); }
  @Test void t2676(){ c2(mdf, imm, recMdf, of(/* not well formed lambda */)); }
  @Test void t2677(){ c2(recMdf, imm, recMdf, of(readH, imm, recMdf, mdf)); }

  // ----------------------------- c3 --------------------------------
  static List<Mdf> readAll = of(readH,mut  , readH, mutH, readH, readH, readH,recMdf  , readH,mdf  , readH,imm);
  static List<Mdf> lentAll = of(mutH,mut  , mutH, mutH, mutH, readH, mutH,recMdf  , mutH,mdf  , mutH,imm);
  static List<Mdf> mutAll = of(mut,mut  , mut, mutH, mut, readH, mut,recMdf  , mut,mdf  , mut,imm);
  static List<Mdf> immAll = of(imm,mut  , imm, mutH, imm, readH, imm,recMdf  , imm,mdf  , imm,imm);
  static List<Mdf> mdfImm = of(mdf,imm  , mdf, readH);
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3001(){ c3(imm, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3002(){ c3(readH, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3003(){ c3(mutH, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3004(){ c3(mut, imm, imm, readAll,immAll,mdfImm); }
  @Test void t3005(){ c3(iso, imm, imm, mdfImm,immAll,readAll); }
  @Test void t3006(){ c3(mdf, imm, imm, of()); }
  @Test void t3007(){ c3(recMdf,imm,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3011(){ c3(imm, readH, imm, of()); }
  @Test void t3012(){ c3(readH, readH, imm, readAll,immAll,mdfImm); }
  @Test void t3013(){ c3(mutH, readH, imm, readAll,immAll,mdfImm); }
  @Test void t3014(){ c3(mut, readH, imm, of()); }//NOT NoMutHyg
  @Test void t3015(){ c3(iso, readH, imm, of()); }
  @Test void t3016(){ c3(mdf, readH, imm, of()); }
  @Test void t3017(){ c3(recMdf, readH,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3021(){ c3(imm, mutH, imm, of()); }
  @Test void t3022(){ c3(readH, mutH, imm, readAll,immAll,mdfImm); }
  @Test void t3023(){ c3(mutH, mutH, imm, readAll,immAll,mdfImm); }
  @Test void t3024(){ c3(mut, mutH, imm, of()); }//NOT NoMutHyg
  @Test void t3025(){ c3(iso, mutH, imm, of()); }
  @Test void t3026(){ c3(mdf, mutH, imm, of()); }
  @Test void t3027(){ c3(recMdf, mutH,  imm,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3031(){ c3(imm, mut, imm, of()); }
  @Test void t3032(){ c3(readH, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3033(){ c3(mutH, mut, imm, readAll,immAll,mdfImm); }
  @Test void t3034(){ c3(mut, mut, imm, readAll,immAll,mdfImm); }
  //two rules: imm,imm implies read,imm
  //           imm,imm on imm methods should imply imm,mut using adapt
  @Test void t3035(){ c3(iso, mut, imm, of()); }
  @Test void t3036(){ c3(mdf, mut, imm, of()); }
  @Test void t3037(){ c3(recMdf,mut,   imm,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3041(){ c3(imm, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3042(){ c3(readH, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3043(){ c3(mutH, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3044(){ c3(mut, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3045(){ c3(iso, iso, imm, readAll,immAll,mdfImm); }
  @Test void t3046(){ c3(mdf, iso, imm, of()); }
  @Test void t3047(){ c3(recMdf,iso,   imm,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3051(){ c3(imm, mdf, imm, of()); }
  @Test void t3052(){ c3(readH, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3053(){ c3(mutH, mdf, imm, readAll,immAll,mdfImm); }
  @Test void t3054(){ c3(mut, mdf, imm, of()); }
  @Test void t3055(){ c3(iso, mdf, imm, of()); }
  @Test void t3056(){ c3(mdf, mdf, imm, of()); }
  @Test void t3057(){ c3(recMdf,mdf,   imm, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3061(){ c3(imm, recMdf, imm, of()); }
  @Test void t3062(){ c3(readH, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3063(){ c3(mutH, recMdf, imm, readAll,immAll,mdfImm); }
  @Test void t3064(){ c3(mut, recMdf, imm, of()); }
  @Test void t3065(){ c3(iso, recMdf, imm, of()); }
  @Test void t3066(){ c3(mdf, recMdf, imm, of()); }
  @Test void t3067(){ c3(recMdf,recMdf,   imm,   of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3181(){ c3(imm,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3182(){ c3(readH,  imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3183(){ c3(mutH,  imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3184(){ c3(mut,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3185(){ c3(iso,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3186(){ c3(mdf, imm, readH, of()); }
  @Test void t3187(){ c3(recMdf,imm, readH, readAll,immAll,mdfImm,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3101(){ c3(imm, readH, readH, of()); }
  @Test void t3102(){ c3(readH, readH, readH, readAll,of(mdf, readH)); }
  @Test void t3103(){ c3(mutH, readH, readH, readAll,of(mdf, readH)); }
  @Test void t3104(){ c3(mut, readH, readH, of()); }//NOT NoMutHyg
  @Test void t3105(){ c3(iso, readH, readH, of()); }
  @Test void t3106(){ c3(mdf, readH, readH, of()); }
  @Test void t3107(){ c3(recMdf, readH, readH,   of()); }
  //                     lambda, captured, method, ...(capturedAs, capturedAsG)
  @Test void t3111(){ c3(imm, mutH, readH, of()); }
  @Test void t3112(){ c3(readH, mutH, readH,   readAll,of(mdf, readH)); }
  @Test void t3113(){ c3(mutH, mutH, readH,   readAll,of(mdf, readH)); }
  @Test void t3114(){ c3(mut, mutH, readH, of()); }//NOT NoMutHyg
  @Test void t3115(){ c3(iso, mutH, readH, of()); }
  @Test void t3116(){ c3(mdf, mutH, readH, of()); }
  @Test void t3117(){ c3(recMdf, mutH, readH,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3121(){ c3(imm, mut, readH, of()); }
  @Test void t3122(){ c3(readH,  mut, readH,   readAll,of(mdf, readH)); }
  @Test void t3123(){ c3(mutH,  mut, readH,   readAll,of(mdf, readH)); }
  @Test void t3124(){ c3(mut,   mut, readH,   readAll,of(mdf, readH)); }
  @Test void t3125(){ c3(iso,   mut, readH,   of()); }
  @Test void t3126(){ c3(mdf, mut, readH, of()); }
  @Test void t3127(){ c3(recMdf,mut, readH,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3131(){ c3(imm,   iso, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3132(){ c3(readH,  iso, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3133(){ c3(mutH,  iso, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3134(){ c3(mut,   iso, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3135(){ c3(iso,   iso, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3136(){ c3(mdf, iso, readH, of()); }
  @Test void t3137(){ c3(recMdf,iso, readH, readAll,immAll,mdfImm,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3141(){ c3(imm,   mdf, readH, of()); }
  @Test void t3142(){ c3(readH,  mdf, readH, readAll,of(mdf, readH)); }
  @Test void t3143(){ c3(mutH,  mdf, readH, readAll,of(mdf, readH)); }
  @Test void t3144(){ c3(mut, mdf, readH, of()); }
  @Test void t3145(){ c3(iso, mdf, readH, of()); }
  @Test void t3146(){ c3(mdf, mdf, readH, of()); }
  @Test void t3147(){ c3(recMdf,mdf, readH, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3151(){ c3(imm, recMdf, readH, of()); }
  @Test void t3152(){ c3(readH,  recMdf, readH, readAll,of(mdf, readH)); }
  @Test void t3153(){ c3(mutH,  recMdf, readH, readAll,of(mdf, readH)); }
  @Test void t3154(){ c3(mut, recMdf, readH, of()); }
  @Test void t3155(){ c3(iso, recMdf, readH, of()); }
  @Test void t3156(){ c3(mdf, recMdf, readH, of()); }
  @Test void t3157(){ c3(recMdf,recMdf, readH,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3161(){ c3(imm,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3162(){ c3(readH,  imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3163(){ c3(mutH,  imm, readH, readAll,immAll,mdfImm,of()); } // this is fine because the recMdf is treated as imm
  @Test void t3164(){ c3(mut,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3165(){ c3(iso,   imm, readH, readAll,immAll,mdfImm,of()); }
  @Test void t3166(){ c3(mdf, imm, readH, of()); }
  @Test void t3167(){ c3(recMdf,imm, readH, readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3201(){ c3(imm, imm, mutH, of()); }
  @Test void t3202(){ c3(readH, imm, mutH, of()); }
  @Test void t3203(){ c3(mutH,  imm, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3204(){ c3(mut,   imm, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3205(){ c3(iso,   imm, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3206(){ c3(mdf, imm, mutH, of()); }
  @Test void t3207(){ c3(recMdf,imm, mutH, readAll,mdfImm,immAll,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3211(){ c3(imm, readH, mutH, of()); }
  @Test void t3212(){ c3(readH, readH, mutH, of()); }
  @Test void t3213(){ c3(mutH, readH, mutH, readAll,of(mdf, readH)); }
  @Test void t3214(){ c3(mut, readH, mutH, of()); }//NOT NoMutHyg
  @Test void t3215(){ c3(iso, readH, mutH, of()); }
  @Test void t3216(){ c3(mdf, readH, mutH, of()); }
  @Test void t3217(){ c3(recMdf, readH, mutH,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3221(){ c3(imm, mutH, mutH, of()); }
  @Test void t3222(){ c3(readH, mutH, mutH, of()); }
  @Test void t3223(){ c3(mutH, mutH, mutH,   readAll,lentAll,of(mdf, readH, mdf, mutH)); }
  @Test void t3224(){ c3(mut, mutH, mutH, of()); }//NOT NoMutHyg
  @Test void t3225(){ c3(iso, mutH, mutH, of()); }
  @Test void t3226(){ c3(mdf, mutH, mutH, of()); }
  @Test void t3227(){ c3(recMdf, mutH, mutH,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3231(){ c3(imm, mut, mutH, of()); }
  @Test void t3232(){ c3(readH, mut, mutH, of()); }
  @Test void t3233(){ c3(mutH,  mut, mutH,   readAll,lentAll,of(mdf, readH, mdf, mutH)); }
  @Test void t3234(){ c3(mut,   mut, mutH,   readAll,lentAll,of(mdf, readH, mdf, mutH)); }
  @Test void t3235(){ c3(iso,   mut, mutH,   of()); }
  @Test void t3236(){ c3(mdf, mut, mutH, of()); }
  @Test void t3237(){ c3(recMdf,mut, mutH,  of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3241(){ c3(imm, iso, mutH, of()); }
  @Test void t3242(){ c3(readH, iso, mutH, of()); }
  @Test void t3243(){ c3(mutH,  iso, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3244(){ c3(mut,   iso, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3245(){ c3(iso,   iso, mutH, readAll,mdfImm,immAll,of()); }
  @Test void t3246(){ c3(mdf, iso, mutH, of()); }
  @Test void t3247(){ c3(recMdf,iso, mutH, readAll,mdfImm,immAll,of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3251(){ c3(imm, mdf, mutH, of()); }
  @Test void t3252(){ c3(readH, mdf, mutH, of()); }
  @Test void t3253(){ c3(mutH,  mdf, mutH, of(readH, mutH, mdf, readH, readH,mdf  , readH, readH, readH,recMdf  , readH,imm  , readH,mut)); }
  @Test void t3254(){ c3(mut, mdf, mutH, of()); }
  @Test void t3255(){ c3(iso, mdf, mutH, of()); }
  @Test void t3256(){ c3(mdf, mdf, mutH, of()); }
  @Test void t3257(){ c3(recMdf,mdf, mutH, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3261(){ c3(imm, recMdf, mutH, of()); }
  @Test void t3262(){ c3(readH, recMdf, mutH, of()); }
  @Test void t3263(){ c3(mutH,  recMdf, mutH, readAll,of(mdf, readH)); }
  @Test void t3264(){ c3(mut, recMdf, mutH, of()); }
  @Test void t3265(){ c3(iso, recMdf, mutH, of()); }
  @Test void t3266(){ c3(mdf, recMdf, mutH, of()); }
  @Test void t3267(){ c3(recMdf,recMdf, mutH,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3271(){ c3(imm, imm, mutH, of()); }
  @Test void t3272(){ c3(readH, imm, mutH, of()); }
  @Test void t3273(){ c3(mutH,  imm, mutH, readAll,immAll,mdfImm,of()); }
  @Test void t3274(){ c3(mut,   imm, mutH, readAll,immAll,mdfImm,of()); }
  @Test void t3275(){ c3(iso,   imm, mutH, readAll,immAll,mdfImm,of()); }
  @Test void t3276(){ c3(mdf, imm, mutH, of()); }
  @Test void t3277(){ c3(recMdf,imm, mutH, readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3301(){ c3(imm, imm, mut, of()); }
  @Test void t3302(){ c3(readH, imm, mut, of()); }
  @Test void t3303(){ c3(mutH, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3304(){ c3(mut, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3305(){ c3(iso, imm, mut, readAll,immAll,mdfImm); }
  @Test void t3306(){ c3(mdf, imm, mut, of()); }
  @Test void t3307(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3311(){ c3(imm, readH, mut, of()); }
  @Test void t3312(){ c3(readH, readH, mut, of()); }
  @Test void t3313(){ c3(mutH, readH, mut, readAll,of(mdf, readH)); }
  @Test void t3314(){ c3(mut, readH, mut, of()); }//NOT NoMutHyg
  @Test void t3315(){ c3(iso, readH, mut, of()); }
  @Test void t3316(){ c3(mdf, readH, mut, of()); }
  @Test void t3317(){ c3(recMdf, readH,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3321(){ c3(imm, mutH, mut, of()); }
  @Test void t3322(){ c3(readH, mutH, mut, of()); } // this capture is fine because the method cannot ever be called
  @Test void t3323(){ c3(mutH, mutH, mut, readAll,lentAll,of(mdf, readH, mdf, mutH)); }
  @Test void t3324(){ c3(mut, mutH, mut, of()); }//NOT NoMutHyg
  @Test void t3325(){ c3(iso, mutH, mut, of()); }
  @Test void t3326(){ c3(mdf, mutH, mut, of()); }
  @Test void t3327(){ c3(recMdf, mutH,  mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3331(){ c3(imm, mut, mut, of()); }
  @Test void t3332(){ c3(readH, mut, mut, of()); }
  @Test void t3333(){ c3(mutH,  mut, mut, readAll,lentAll,of(mdf, readH, mdf, mutH)); }
  @Test void t3334(){ c3(mut,   mut,   mut,   readAll,lentAll,of(mdf, readH, mdf, mutH, mdf,mut, mut,mut  , mut, mutH, mut, readH, mut,mdf  , mut,imm, mut,recMdf)); }
  @Test void t3335(){ c3(iso,   mut,   mut,   of()); }
  @Test void t3336(){ c3(mdf, mut, mut, of()); }
  @Test void t3337(){ c3(recMdf,mut,   mut,  readAll,lentAll,mutAll,of(mdf, mutH, mdf, readH, mdf,mut)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3341(){ c3(imm, iso, mut, of()); }
  @Test void t3342(){ c3(readH, iso, mut, of()); }
  @Test void t3343(){ c3(mutH, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3344(){ c3(mut, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3345(){ c3(iso, iso, mut, readAll,immAll,mdfImm); }
  @Test void t3346(){ c3(mdf, iso, mut, of()); }
  @Test void t3347(){ c3(recMdf,iso,   mut,   readAll,immAll,mdfImm); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3351(){ c3(imm, mdf, mut, of()); }
  @Test void t3352(){ c3(readH, mdf, mut, of()); }
  @Test void t3353(){ c3(mutH,  mdf, mut, readAll,of(mdf, readH)); }
  @Test void t3354(){ c3(mut, mdf, mut, of()); }
  @Test void t3355(){ c3(iso, mdf, mut, of()); }
  @Test void t3356(){ c3(mdf, mdf, mut, of()); }
  @Test void t3357(){ c3(recMdf,mdf, mut, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3361(){ c3(imm, recMdf, mut, of()); }
  @Test void t3362(){ c3(readH, recMdf, mut, of()); }
  @Test void t3363(){ c3(mutH, recMdf, mut, readAll,of(mdf, readH)); }
  @Test void t3364(){ c3(mut, recMdf, mut, of()); }
  @Test void t3365(){ c3(iso, recMdf, mut, of()); }
  @Test void t3366(){ c3(mdf, recMdf, mut, of()); }
  @Test void t3367(){ c3(recMdf,recMdf,   mut,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3371(){ c3(imm, imm, mut, of()); }
  @Test void t3372(){ c3(readH, imm, mut, of()); }
  @Test void t3373(){ c3(mutH, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3374(){ c3(mut, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3375(){ c3(iso, imm, mut, readAll,immAll,mdfImm,of()); }
  @Test void t3376(){ c3(mdf, imm, mut, of()); }
  @Test void t3377(){ c3(recMdf,imm,   mut,   readAll,immAll,mdfImm,of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3401(){ c3(imm, imm, iso, of()); }
  @Test void t3402(){ c3(readH, imm, iso, of()); }
  @Test void t3403(){ c3(mutH, imm, iso, of()); }
  @Test void t3404(){ c3(mut, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3405(){ c3(iso, imm, iso, readAll,immAll,mdfImm,of()); }
  @Test void t3406(){ c3(mdf, imm, iso, of()); }
  @Test void t3407(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm,of()); } // yes, recMdf could be iso
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3411(){ c3(imm, readH, iso, of()); }
  @Test void t3412(){ c3(readH, readH, iso, of()); }
  @Test void t3413(){ c3(mutH, readH, iso, of()); }
  @Test void t3414(){ c3(mut, readH, iso, of()); }//NOT NoMutHyg
  @Test void t3415(){ c3(iso, readH, iso, of()); }
  @Test void t3416(){ c3(mdf, readH, iso, of()); }
  @Test void t3417(){ c3(recMdf, readH,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3421(){ c3(imm, mutH, iso, of()); }
  @Test void t3422(){ c3(readH, mutH, iso, of()); }
  @Test void t3423(){ c3(mutH, mutH, iso, of()); }
  @Test void t3424(){ c3(mut, mutH, iso, of()); }//NOT NoMutHyg
  @Test void t3425(){ c3(iso, mutH, iso, of()); }
  @Test void t3426(){ c3(mdf, mutH, iso, of()); }
  @Test void t3427(){ c3(recMdf, mutH,  iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3431(){ c3(imm, mut, iso, of()); }
  @Test void t3432(){ c3(readH, mut, iso, of()); }
  @Test void t3433(){ c3(mutH,  mut,   iso, of()); }
  @Test void t3434(){ c3(mut,   mut,   iso, readAll,mutAll,lentAll,of(mdf, readH, mdf, mutH, mdf,mut)); }
  @Test void t3435(){ c3(iso,   mut,   iso, of()); }
  @Test void t3436(){ c3(mdf, mut, iso, of()); }
  @Test void t3437(){ c3(recMdf,mut,   iso,  of(mdf,mut  , mutH,recMdf  , mutH,mut  , readH, mutH, mut,imm  , readH,mdf  , mdf, readH, mut,mdf  , mutH,imm  , readH,imm  , mutH, mutH, readH, readH, readH,mut  , mdf, mutH, readH,recMdf  , mutH,mdf  , mut, mutH, mutH, readH, mut,mut  , mut, readH, mut,recMdf)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3441(){ c3(imm, iso, iso, of()); }
  @Test void t3442(){ c3(readH, iso, iso, of()); }
  @Test void t3443(){ c3(mutH,  iso, iso, of()); }
  @Test void t3444(){ c3(mut,   iso, iso, readAll,immAll,of(mdf, readH, mdf,imm)); }
  @Test void t3445(){ c3(iso,   iso, iso, readAll,immAll,of(mdf, readH, mdf,imm)); } // all iso is captured as imm
  @Test void t3446(){ c3(mdf, iso, iso, of()); }
  @Test void t3447(){ c3(recMdf,iso, iso, readAll,immAll,of(mdf, readH, mdf,imm)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3451(){ c3(imm, mdf, iso, of()); }
  @Test void t3452(){ c3(readH, mdf, iso, of()); }
  @Test void t3453(){ c3(mutH,  mdf, iso, of()); }
  @Test void t3454(){ c3(mut, mdf, iso, of()); }
  @Test void t3455(){ c3(iso, mdf, iso, of()); }
  @Test void t3456(){ c3(mdf, mdf, iso, of()); }
  @Test void t3457(){ c3(recMdf, mdf, iso, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3461(){ c3(imm, recMdf, iso, of()); }
  @Test void t3462(){ c3(readH, recMdf, iso, of()); }
  @Test void t3463(){ c3(mutH, recMdf, iso, of()); }
  @Test void t3464(){ c3(mut, recMdf, iso, of()); }
  @Test void t3465(){ c3(iso, recMdf, iso, of()); }
  @Test void t3466(){ c3(mdf, recMdf, iso, of()); }
  @Test void t3467(){ c3(recMdf,recMdf,   iso,   of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3471(){ c3(imm, imm, iso, of()); }
  @Test void t3472(){ c3(readH, imm, iso, of()); }
  @Test void t3473(){ c3(mutH, imm, iso, of()); }
  @Test void t3474(){ c3(mut, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3475(){ c3(iso, imm, iso, readAll,immAll,mdfImm); }
  @Test void t3476(){ c3(mdf, imm, iso, of()); }
  @Test void t3477(){ c3(recMdf,imm,   iso,   readAll,immAll,mdfImm); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3501(){ c3(imm, imm, mdf, of()); }
  @Test void t3502(){ c3(readH, imm, mdf, of()); }
  @Test void t3503(){ c3(mutH, imm, mdf, of()); }
  @Test void t3504(){ c3(mut, imm, mdf, of()); }
  @Test void t3505(){ c3(iso, imm, mdf, of()); }
  @Test void t3506(){ c3(mdf, imm, mdf, of()); }
  @Test void t3507(){ c3(recMdf,imm,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3511(){ c3(imm, readH, mdf, of()); }
  @Test void t3512(){ c3(readH, readH, mdf, of()); }
  @Test void t3513(){ c3(mutH, readH, mdf, of()); }
  @Test void t3514(){ c3(mut, readH, mdf, of()); }
  @Test void t3515(){ c3(iso, readH, mdf, of()); }
  @Test void t3516(){ c3(mdf, readH, mdf, of()); }
  @Test void t3517(){ c3(recMdf, readH,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3521(){ c3(imm, mutH, mdf, of()); }
  @Test void t3522(){ c3(readH, mutH, mdf, of()); }
  @Test void t3523(){ c3(mutH, mutH, mdf, of()); }
  @Test void t3524(){ c3(mut, mutH, mdf, of()); }
  @Test void t3525(){ c3(iso, mutH, mdf, of()); }
  @Test void t3526(){ c3(mdf, mutH, mdf, of()); }
  @Test void t3527(){ c3(recMdf, mutH,  mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3531(){ c3(imm, mut, mdf, of()); }
  @Test void t3532(){ c3(readH, mut, mdf, of()); }
  @Test void t3533(){ c3(mutH, mut, mdf, of()); }
  @Test void t3534(){ c3(mut, mut, mdf, of()); }
  @Test void t3535(){ c3(iso, mut, mdf, of()); }
  @Test void t3536(){ c3(mdf, mut, mdf, of()); }
  @Test void t3537(){ c3(recMdf,mut,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3541(){ c3(imm, iso, mdf, of()); }
  @Test void t3542(){ c3(readH, iso, mdf, of()); }
  @Test void t3543(){ c3(mutH, iso, mdf, of()); }
  @Test void t3544(){ c3(mut, iso, mdf, of()); }
  @Test void t3545(){ c3(iso, iso, mdf, of()); }
  @Test void t3546(){ c3(mdf, iso, mdf, of()); }
  @Test void t3547(){ c3(recMdf,iso,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3551(){ c3(imm, mdf, mdf, of()); }
  @Test void t3552(){ c3(readH, mdf, mdf, of()); }
  @Test void t3553(){ c3(mutH, mdf, mdf, of()); }
  @Test void t3554(){ c3(mut, mdf, mdf, of()); }
  @Test void t3555(){ c3(iso, mdf, mdf, of()); }
  @Test void t3556(){ c3(mdf, mdf, mdf, of()); }
  @Test void t3557(){ c3(recMdf,mdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3561(){ c3(imm, recMdf, mdf, of()); }
  @Test void t3562(){ c3(readH, recMdf, mdf, of()); }
  @Test void t3563(){ c3(mutH, recMdf, mdf, of()); }
  @Test void t3564(){ c3(mut, recMdf, mdf, of()); }
  @Test void t3565(){ c3(iso, recMdf, mdf, of()); }
  @Test void t3566(){ c3(mdf, recMdf, mdf, of()); }
  @Test void t3567(){ c3(recMdf,recMdf,   mdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3571(){ c3(imm, imm, mdf, of()); }
  @Test void t3572(){ c3(readH, imm, mdf, of()); }
  @Test void t3573(){ c3(mutH, imm, mdf, of()); }
  @Test void t3574(){ c3(mut, imm, mdf, of()); }
  @Test void t3575(){ c3(iso, imm, mdf, of()); }
  @Test void t3576(){ c3(mdf, imm, mdf, of()); }
  @Test void t3577(){ c3(recMdf,imm,   mdf, of()); }

  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3601(){ c3(imm, imm, recMdf, of(readH,recMdf  , readH,mdf  , readH, readH, readH,imm  , recMdf, readH, readH,mut  , readH, mutH, recMdf,imm  , mdf, readH, mdf,imm  , imm, mutH, imm,recMdf  , imm,mdf  , imm,mut  , imm, readH, imm,imm)); }
  @Test void t3602(){ c3(readH, imm, recMdf, of(imm, mutH, readH,mut  , readH, mutH, readH,imm  , imm, readH, recMdf,imm  , imm,mdf  , mdf,imm  , recMdf, readH, imm,mut  , imm,recMdf  , imm,imm  , readH, readH, readH,recMdf  , readH,mdf  , mdf, readH)); }
  @Test void t3603(){ c3(mutH, imm, recMdf, of(imm, mutH, readH,mut  , readH, mutH, readH,imm  , imm, readH, recMdf,imm  , imm,mdf  , mdf,imm  , recMdf, readH, imm,mut  , imm,recMdf  , imm,imm  , readH, readH, readH,recMdf  , readH,mdf  , mdf, readH)); }
  @Test void t3604(){ c3(mut, imm, recMdf, of(imm, mutH, readH,mut  , readH, mutH, readH,imm  , imm, readH, recMdf,imm  , imm,mdf  , mdf,imm  , recMdf, readH, imm,mut  , imm,recMdf  , imm,imm  , readH, readH, readH,recMdf  , readH,mdf  , mdf, readH)); }
  @Test void t3605(){ c3(iso, imm, recMdf, of(imm, mutH, readH,mut  , readH, mutH, readH,imm  , imm, readH, recMdf,imm  , imm,mdf  , mdf,imm  , recMdf, readH, imm,mut  , imm,recMdf  , imm,imm  , readH, readH, readH,recMdf  , readH,mdf  , mdf, readH)); }
  @Test void t3606(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3607(){ c3(recMdf,imm,   recMdf, of(imm, mutH, readH,mut  , readH, mutH, readH,imm  , imm, readH, recMdf,imm  , imm,mdf  , mdf,imm  , recMdf, readH, imm,mut  , imm,recMdf  , imm,imm  , readH, readH, readH,recMdf  , readH,mdf  , mdf, readH)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3611(){ c3(imm, readH, recMdf, of()); }
  @Test void t3612(){ c3(readH, readH, recMdf, of(readH,mdf  , recMdf,recMdf  , readH,recMdf  , readH, readH, readH,mut  , mdf,recMdf  , mdf, readH, recMdf, mutH, readH,imm  , recMdf,mdf  , recMdf,mut  , readH, mutH, recMdf, readH)); }
  @Test void t3613(){ c3(mutH, readH, recMdf, readAll,of(mdf, readH, recMdf, readH)); }
  @Test void t3614(){ c3(mut, readH, recMdf, of()); }
  @Test void t3615(){ c3(iso, readH, recMdf, of()); }
  @Test void t3616(){ c3(mdf, readH, recMdf, of()); }
  @Test void t3617(){ c3(recMdf, readH,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3621(){ c3(imm, mutH, recMdf, of()); }
  @Test void t3622(){ c3(readH, mutH, recMdf, of(recMdf,recMdf  , readH, mutH, recMdf, readH, recMdf,mdf  , readH, readH, recMdf, mutH, recMdf,mut  , mdf,recMdf  , mdf, readH, readH,mdf  , readH,recMdf  , readH,mut  , readH,imm)); }
  @Test void t3623(){ c3(mutH, mutH, recMdf, of(mdf,recMdf  , recMdf,recMdf  , readH, mutH, mdf, readH, readH, readH, recMdf, mutH, recMdf, readH, recMdf,mut  , readH,mdf  , readH,recMdf  , recMdf,mdf  , readH,mut  , readH,imm)); }
  @Test void t3624(){ c3(mut, mutH, recMdf, of()); }
  @Test void t3625(){ c3(iso, mutH, recMdf, of()); }
  @Test void t3626(){ c3(mdf, mutH, recMdf, of()); }
  @Test void t3627(){ c3(recMdf, mutH,  recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3631(){ c3(imm, mut, recMdf, of()); }
  @Test void t3632(){ c3(readH, mut, recMdf, of(readH, mutH, recMdf,recMdf  , recMdf, readH, readH,mdf  , readH, readH, mdf,recMdf  , readH,recMdf  , readH,mut  , mdf, readH, recMdf,mdf  , recMdf, mutH, readH,imm  , recMdf,mut)); }
  @Test void t3633(){ c3(mutH, mut, recMdf, of(readH, mutH, recMdf,recMdf  , recMdf, readH, readH,mdf  , readH, readH, mdf,recMdf  , readH,recMdf  , readH,mut  , mdf, readH, recMdf,mdf  , recMdf, mutH, readH,imm  , recMdf,mut)); }
  @Test void t3634(){ c3(mut, mut, recMdf, of(readH, mutH, recMdf,recMdf  , recMdf, readH, readH,mdf  , readH, readH, mdf,recMdf  , readH,recMdf  , readH,mut  , mdf, readH, recMdf,mdf  , recMdf, mutH, readH,imm  , recMdf,mut)); }
  @Test void t3635(){ c3(iso, mut, recMdf, of()); }
  @Test void t3636(){ c3(mdf, mut, recMdf, of()); }
  @Test void t3637(){ c3(recMdf,mut,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3641(){ c3(imm, iso, recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  @Test void t3642(){ c3(readH, iso, recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  @Test void t3643(){ c3(mutH, iso, recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  @Test void t3644(){ c3(mut, iso, recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  @Test void t3645(){ c3(iso, iso, recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  @Test void t3646(){ c3(mdf, iso, recMdf, of()); }
  @Test void t3647(){ c3(recMdf,iso,   recMdf, of(imm,mdf  , imm, mutH, imm,imm  , imm, readH, imm,recMdf  , readH,mdf  , readH, mutH, imm,mut  , recMdf,imm  , readH, readH, recMdf, readH, mdf,imm  , readH,imm  , readH,recMdf  , mdf, readH, readH,mut)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3651(){ c3(imm, mdf, recMdf, of()); }
  @Test void t3652(){ c3(readH, mdf, recMdf, readAll,of(recMdf,recMdf, recMdf,mdf, recMdf, readH, mdf,recMdf, mdf, readH, recMdf, mutH, recMdf,mut)); }
  @Test void t3653(){ c3(mutH, mdf, recMdf, of()); }
  @Test void t3654(){ c3(mut, mdf, recMdf, of()); }
  @Test void t3655(){ c3(iso, mdf, recMdf, of()); }
  @Test void t3656(){ c3(mdf, mdf, recMdf, of()); }
  @Test void t3657(){ c3(recMdf,mdf,   recMdf, of()); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3661(){ c3(imm, recMdf, recMdf, of()); }
  @Test void t3662(){ c3(readH, recMdf, recMdf, of(readH, mutH, readH,mdf  , readH,mut  , readH,imm  , readH, readH, recMdf,recMdf  , mdf,recMdf  , readH,recMdf  , recMdf, mutH, recMdf,mut  , recMdf, readH, mdf, readH, recMdf,mdf)); }
  @Test void t3663(){ c3(mutH, recMdf, recMdf, of(readH, mutH, readH,mdf  , readH,mut  , readH,imm  , readH, readH, recMdf, readH, readH,recMdf  , mdf, readH)); }
  @Test void t3664(){ c3(mut, recMdf, recMdf, of()); }
  @Test void t3665(){ c3(iso, recMdf, recMdf, of()); }
  @Test void t3666(){ c3(mdf, recMdf, recMdf, of()); }
  @Test void t3667(){ c3(recMdf,recMdf,   recMdf, of(readH,mdf  , readH, readH, recMdf,recMdf  , readH, mutH, readH,imm  , readH,mut  , recMdf, mutH, recMdf, readH, readH,recMdf  , recMdf,mdf  , recMdf,mut  , mdf,recMdf  , mdf, readH)); }
  //                     lambda, captured, method, ..(returnedAs, capturedAsGen)
  @Test void t3671(){ c3(imm, imm, recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
  @Test void t3672(){ c3(readH, imm, recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
  @Test void t3673(){ c3(mutH, imm, recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
  @Test void t3674(){ c3(mut, imm, recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
  @Test void t3675(){ c3(iso, imm, recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
  @Test void t3676(){ c3(mdf, imm, recMdf, of()); }
  @Test void t3677(){ c3(recMdf,imm,   recMdf, of(readH, mutH, imm, mutH, readH,mdf  , readH,recMdf  , readH, readH, imm, readH, readH,imm  , imm,mut  , mdf,imm  , imm,mdf  , recMdf,imm  , imm,recMdf  , imm,imm  , mdf, readH, readH,mut  , recMdf, readH)); }
}
//a mut lambda could capture a mut as iso inside an iso method?

//write counterexample

/*
//NO-a mut lambda could capture a mutH as iso inside an iso method?
A:{
  read .foo(par: mutH Break): mut BreakBox -> { par }
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
@Test void t053(){ c(mutH,  mdf,   imm,   of(imm,read)); }
@Test void t054(){ c(mut,   mdf,   imm); }//NOT NoMutHyg
@Test void t055(){ c(iso,   mdf,   imm); }//NOT NoMutHyg
@Test void t056(){ c(mdf,   mdf,   imm); }
@Test void t057(){ c(recMdf,mdf,   imm); }
 */
