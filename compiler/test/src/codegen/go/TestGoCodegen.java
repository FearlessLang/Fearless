package codegen.go;

import codegen.MIRInjectionVisitor;
import id.Id;
import main.Main;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.TypeSystemFeatures;
import program.inference.InferBodies;
import program.typesystem.EMethTypeSystem;
import program.typesystem.TsT;
import utils.Base;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Disabled
public class TestGoCodegen {
  void ok(String expected, String entry, boolean loadBase, String... content) {
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    String[] baseLibs = loadBase ? Base.baseLib : new String[0];
    var ps = Stream.concat(Arrays.stream(content), Arrays.stream(baseLibs))
      .map(code->new Parser(Path.of("Dummy" + pi.getAndIncrement() + ".fear"), code))
      .toList();
    var p = Parser.parseAll(ps, new TypeSystemFeatures());
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{
      throw err;
    });
    var inferred = InferBodies.inferAll(p);
    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred);
    ConcurrentHashMap<Long, TsT> resolvedCalls = new ConcurrentHashMap<>();
    inferred.typeCheck(resolvedCalls);
    var mir = new MIRInjectionVisitor(List.of(),inferred, resolvedCalls).visitProgram();
    var res = new GoCodegen(mir).visitProgram(new Id.DecId(entry, 0));
    Err.strCmp(expected, res.toString());
  }

  @Test void emptyProgram() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }

      fmt.Println(φfakeφFake_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=base, src=package main

    type φbaseφSealed_0 interface {
     \s
    }

    type φbaseφSystem_0 interface {
     \s
    }

    type φbaseφVoid_0 interface {
     \s
    }

    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φbaseφSealed_0Impl struct {
     \s
    }



    type φbaseφSystem_0Impl struct {
     \s
    }



    type φbaseφVoid_0Impl struct {
     \s
    }




    ]]]
    """, "fake.Fake", false, """
    package test
    """, Base.minimalBase);}

  @Test void capturing() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }
    
      fmt.Println(φfakeφFake_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main
    
    type φtestφNum_0 interface {
     \s
    }
    
    type φtestφFortyTwo_0 interface {
     \s
    }
    
    type φtestφUsage_0 interface {
      φ35_0_immφ() φtestφNum_0
    }
    
    type φtestφPerson_0 interface {
      φage_0_readφ() φtestφNum_0
    }
    
    type φtestφFPerson_0 interface {
      φ35_1_immφ(ageφ φtestφNum_0) φtestφPerson_0
    }
    
    type φtestφNum_0Impl struct {
     \s
    }
    
    
    
    type φtestφFortyTwo_0Impl struct {
     \s
    }
    
    
    
    type φtestφUsage_0Impl struct {
     \s
    }
    func (FSpφself φtestφUsage_0Impl) φ35_0_immφ() φtestφNum_0 {
      return φtestφUsage_0φφ35_0_immφφnoSelfCap()
    }
    
    
    
    type φtestφPerson_0Impl struct {
      ageφ φtestφNum_0
    }
    func (FSpφself φtestφPerson_0Impl) φage_0_readφ() φtestφNum_0 {
      return φtestφPerson_0φφage_0_readφφnoSelfCap(FSpφself.ageφ)
    }
    
    
    
    type φtestφFPerson_0Impl struct {
     \s
    }
    func (FSpφself φtestφFPerson_0Impl) φ35_1_immφ(ageφ φtestφNum_0) φtestφPerson_0 {
      return φtestφFPerson_0φφ35_1_immφφnoSelfCap(ageφ)
    }
    
    
    
    func φtestφPerson_0φφage_0_readφφnoSelfCap(fear6φ36φ φtestφPerson_0, ageφ φtestφNum_0) φtestφNum_0 {
      return ageφ
    }
    
    func φtestφFPerson_0φφ35_1_immφφnoSelfCap(ageφ φtestφNum_0, this φtestφFPerson_0) φtestφPerson_0 {
      return φtestφPerson_0Impl{ageφ}
    }
    
    func φtestφUsage_0φφ35_0_immφφnoSelfCap(this φtestφUsage_0) φtestφNum_0 {
      return ((φtestφFPerson_0Impl{}.φ35_1_immφ(φtestφFortyTwo_0Impl{})).(φtestφPerson_0).φage_0_readφ())
    }
    
    ], GoPackage[pkg=base, src=package main
    
    type φbaseφSealed_0 interface {
     \s
    }
    
    type φbaseφSystem_0 interface {
     \s
    }
    
    type φbaseφVoid_0 interface {
     \s
    }
    
    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }
    
    type φbaseφSealed_0Impl struct {
     \s
    }
    
    
    
    type φbaseφSystem_0Impl struct {
     \s
    }
    
    
    
    type φbaseφVoid_0Impl struct {
     \s
    }
    
    
    
    
    ]]]
    """, "fake.Fake", false, """
    package test
    FPerson: { #(age: Num): mut Person -> mut Person: {
      read .age: Num -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.age,
      }
    Num: {}
    FortyTwo: Num
    """, Base.minimalBase);}

  @Test void capturingDeep() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }
        
      fmt.Println(φfakeφFake_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main
        
    type φtestφNum_0 interface {
      φplus1_0_immφ() φtestφNum_0
    }
        
    type φtestφFortyThree_0 interface {
      φplus1_0_immφ() φtestφNum_0
    }
        
    type φtestφFortyTwo_0 interface {
      φplus1_0_immφ() φtestφNum_0
    }
        
    type φtestφUsage_0 interface {
      φ35_0_immφ() φtestφNum_0
    }
        
    type φtestφPerson_0 interface {
      φage_0_readφ() φtestφNum_0
    φwrap_0_mutφ() φtestφPerson_0
    }
        
    type φtestφFear0φ36_0 interface {
      φage_0_readφ() φtestφNum_0
    φwrap_0_mutφ() φtestφPerson_0
    }
        
    type φtestφFear1φ36_0 interface {
      φage_0_readφ() φtestφNum_0
    φwrap_0_mutφ() φtestφPerson_0
    }
        
    type φtestφFortyFour_0 interface {
      φplus1_0_immφ() φtestφNum_0
    }
        
    type φtestφFear2φ36_0 interface {
      φage_0_readφ() φtestφNum_0
    φwrap_0_mutφ() φtestφPerson_0
    }
        
    type φtestφFPerson_0 interface {
      φ35_1_immφ(ageφ φtestφNum_0) φtestφPerson_0
    }
        
    type φtestφFortyThree_0Impl struct {
     \s
    }
    func (FSpφself φtestφFortyThree_0Impl) φplus1_0_immφ() φtestφNum_0 {
      return φtestφFortyThree_0φφplus1_0_immφφnoSelfCap()
    }
        
        
        
    type φtestφFortyTwo_0Impl struct {
     \s
    }
    func (FSpφself φtestφFortyTwo_0Impl) φplus1_0_immφ() φtestφNum_0 {
      return φtestφFortyTwo_0φφplus1_0_immφφnoSelfCap()
    }
        
        
        
    type φtestφUsage_0Impl struct {
     \s
    }
    func (FSpφself φtestφUsage_0Impl) φ35_0_immφ() φtestφNum_0 {
      return φtestφUsage_0φφ35_0_immφφnoSelfCap()
    }
        
        
        
    type φtestφFear0φ36_0Impl struct {
      selfφ φtestφFear1φ36_0
    }
    func (FSpφself φtestφFear0φ36_0Impl) φage_0_readφ() φtestφNum_0 {
      return φtestφFear0φ36_0φφage_0_readφφnoSelfCap(FSpφself.selfφ)
    }
        
    func (FSpφself φtestφFear0φ36_0Impl) φwrap_0_mutφ() φtestφPerson_0 {
      return φtestφPerson_0φφwrap_0_mutφφselfCap(FSpφself)
    }
        
        
        
    type φtestφFear1φ36_0Impl struct {
      this φtestφPerson_0
    }
    func (FSpφself φtestφFear1φ36_0Impl) φage_0_readφ() φtestφNum_0 {
      return φtestφFear1φ36_0φφage_0_readφφnoSelfCap(FSpφself.this)
    }
        
    func (FSpφself φtestφFear1φ36_0Impl) φwrap_0_mutφ() φtestφPerson_0 {
      return φtestφFear1φ36_0φφwrap_0_mutφφselfCap(FSpφself)
    }
        
        
        
    type φtestφFortyFour_0Impl struct {
     \s
    }
    func (FSpφself φtestφFortyFour_0Impl) φplus1_0_immφ() φtestφNum_0 {
      return φtestφFortyFour_0φφplus1_0_immφφselfCap(FSpφself)
    }
        
        
        
    type φtestφFear2φ36_0Impl struct {
      ageφ φtestφNum_0
    }
    func (FSpφself φtestφFear2φ36_0Impl) φage_0_readφ() φtestφNum_0 {
      return φtestφFear2φ36_0φφage_0_readφφnoSelfCap(FSpφself.ageφ)
    }
        
    func (FSpφself φtestφFear2φ36_0Impl) φwrap_0_mutφ() φtestφPerson_0 {
      return φtestφPerson_0φφwrap_0_mutφφselfCap(FSpφself)
    }
        
        
        
    type φtestφFPerson_0Impl struct {
     \s
    }
    func (FSpφself φtestφFPerson_0Impl) φ35_1_immφ(ageφ φtestφNum_0) φtestφPerson_0 {
      return φtestφFPerson_0φφ35_1_immφφnoSelfCap(ageφ)
    }
        
        
        
    func φtestφFortyThree_0φφplus1_0_immφφnoSelfCap() φtestφNum_0 {
      return φtestφFortyFour_0Impl{}
    }
        
    func φtestφFortyTwo_0φφplus1_0_immφφnoSelfCap() φtestφNum_0 {
      return φtestφFortyThree_0Impl{}
    }
        
    func φtestφUsage_0φφ35_0_immφφnoSelfCap() φtestφNum_0 {
      return (((φtestφFPerson_0Impl{}.φ35_1_immφ(φtestφFortyTwo_0Impl{})).φwrap_0_mutφ()).φage_0_readφ())
    }
        
    func φtestφFear1φ36_0φφage_0_readφφnoSelfCap(this φtestφPerson_0) φtestφNum_0 {
      return ((this.φage_0_readφ()).φplus1_0_immφ())
    }
        
    func φtestφFear0φ36_0φφage_0_readφφnoSelfCap(selfφ φtestφFear1φ36_0) φtestφNum_0 {
      return ((selfφ.φage_0_readφ()).φplus1_0_immφ())
    }
        
    func φtestφFear1φ36_0φφwrap_0_mutφφselfCap(selfφ φtestφFear1φ36_0) φtestφPerson_0 {
      return φtestφFear0φ36_0Impl{selfφ}
    }
        
    func φtestφPerson_0φφwrap_0_mutφφselfCap(this φtestφPerson_0) φtestφPerson_0 {
      return φtestφFear1φ36_0Impl{this}
    }
        
    func φtestφFortyFour_0φφplus1_0_immφφselfCap(this φtestφFortyFour_0) φtestφNum_0 {
      return (this.φplus1_0_immφ())
    }
        
    func φtestφFear2φ36_0φφage_0_readφφnoSelfCap(ageφ φtestφNum_0) φtestφNum_0 {
      return ageφ
    }
        
    func φtestφFPerson_0φφ35_1_immφφnoSelfCap(ageφ φtestφNum_0) φtestφPerson_0 {
      return φtestφFear2φ36_0Impl{ageφ}
    }
        
    ], GoPackage[pkg=base, src=package main
        
    type φbaseφSealed_0 interface {
     \s
    }
        
    type φbaseφSystem_0 interface {
     \s
    }
        
    type φbaseφVoid_0 interface {
     \s
    }
        
    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }
        
    type φbaseφSealed_0Impl struct {
     \s
    }
        
        
        
    type φbaseφSystem_0Impl struct {
     \s
    }
        
        
        
    type φbaseφVoid_0Impl struct {
     \s
    }
        
        
        
        
    ]]]
    """, "fake.Fake", false, """
    package test
    Person: {
      read .age: Num,
      mut .wrap: mut Person -> {'self
       .age -> this.age.plus1,
       .wrap -> {'topLevelWrapped
         .age -> self.age.plus1,
         }
       },
      }
    FPerson: { #(age: Num): mut Person -> {'original
      .age -> age,
      }}
    Usage: {
      #: Num -> FPerson#FortyTwo.wrap.age,
      }
    Num: {
      .plus1: Num,
      }
    FortyTwo: Num{ .plus1 -> FortyThree }
    FortyThree: Num{ .plus1 -> FortyFour }
    FortyFour: Num{ .plus1 -> this.plus1 }
    """, Base.minimalBase);}

  @Test void simpleProgram() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }

      fmt.Println(φfakeφFake_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main

    type φtestφBar_0 interface {
      φloop_0_immφ() φtestφBaz_1
    φ35_0_immφ() interface{}
    }

    type φtestφFoo_0 interface {
     \s
    }

    type φtestφFear1φ36_0 interface {
      φ35_0_immφ() φtestφOk_0
    }

    type φtestφOk_0 interface {
      φ35_0_immφ() φtestφOk_0
    }

    type φtestφBaz_1 interface {
      φ35_0_immφ() interface{}
    }

    type φtestφYo_0 interface {
      φlm_0_immφ() φtestφOk_0
    }

    type φtestφBar_0Impl struct {
     \s
    }
    func (FSpφself φtestφBar_0Impl) φloop_0_immφ() φtestφBaz_1 {
      return φtestφBar_0φφloop_0_immφφselfCap(FSpφself)
    }

    func (FSpφself φtestφBar_0Impl) φ35_0_immφ() interface{} {
      return FSpφself.φ35_0_immφφDelegate()
    }

    func (FSpφself φtestφBar_0Impl) φ35_0_immφφDelegate() φtestφFoo_0 {
      return φtestφBar_0φφ35_0_immφφnoSelfCap()
    }



    type φtestφFoo_0Impl struct {
     \s
    }



    type φtestφFear1φ36_0Impl struct {
     \s
    }
    func (FSpφself φtestφFear1φ36_0Impl) φ35_0_immφ() φtestφOk_0 {
      return φtestφFear1φ36_0φφ35_0_immφφselfCap(FSpφself)
    }



    type φtestφYo_0Impl struct {
     \s
    }
    func (FSpφself φtestφYo_0Impl) φlm_0_immφ() φtestφOk_0 {
      return φtestφYo_0φφlm_0_immφφnoSelfCap()
    }



    func φtestφBar_0φφ35_0_immφφnoSelfCap() φtestφFoo_0 {
      return φtestφFoo_0Impl{}
    }

    func φtestφBar_0φφloop_0_immφφselfCap(this φtestφBar_0) φtestφBaz_1 {
      return (this.φloop_0_immφ())
    }

    func φtestφFear1φ36_0φφ35_0_immφφselfCap(okφ φtestφFear1φ36_0) φtestφOk_0 {
      return (okφ.φ35_0_immφ())
    }

    func φtestφYo_0φφlm_0_immφφnoSelfCap() φtestφOk_0 {
      return φtestφFear1φ36_0Impl{}
    }

    ], GoPackage[pkg=base, src=package main

    type φbaseφSealed_0 interface {
     \s
    }

    type φbaseφSystem_0 interface {
     \s
    }

    type φbaseφVoid_0 interface {
     \s
    }

    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φbaseφSealed_0Impl struct {
     \s
    }



    type φbaseφSystem_0Impl struct {
     \s
    }



    type φbaseφVoid_0Impl struct {
     \s
    }




    ]]]
    """, "fake.Fake", false, """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """, Base.minimalBase);}

  @Test void bools() {ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }

      fmt.Println(φfakeφFake_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main

    type φtestφTrue_0 interface {
      φ63_1_immφ(fφ φtestφThenElse_1) interface{}
    φnot_0_immφ() φtestφBool_0
    φor_1_immφ(bφ φtestφBool_0) φtestφBool_0
    φand_1_immφ(bφ φtestφBool_0) φtestφBool_0
    }

    type φtestφFalse_0 interface {
      φ63_1_immφ(fφ φtestφThenElse_1) interface{}
    φnot_0_immφ() φtestφBool_0
    φor_1_immφ(bφ φtestφBool_0) φtestφBool_0
    φand_1_immφ(bφ φtestφBool_0) φtestφBool_0
    }

    type φtestφThenElse_1 interface {
      φthen_0_mutφ() interface{}
    φelse_0_mutφ() interface{}
    }

    type φtestφBool_0 interface {
      φ63_1_immφ(fφ φtestφThenElse_1) interface{}
    φnot_0_immφ() φtestφBool_0
    φor_1_immφ(bφ φtestφBool_0) φtestφBool_0
    φand_1_immφ(bφ φtestφBool_0) φtestφBool_0
    }

    type φtestφSealed_0 interface {
     \s
    }

    type φtestφTrue_0Impl struct {
     \s
    }
    func (FSpφself φtestφTrue_0Impl) φ63_1_immφ(fφ φtestφThenElse_1) interface{} {
      return φtestφTrue_0φφ63_1_immφφnoSelfCap(fφ)
    }

    func (FSpφself φtestφTrue_0Impl) φnot_0_immφ() φtestφBool_0 {
      return φtestφTrue_0φφnot_0_immφφnoSelfCap()
    }

    func (FSpφself φtestφTrue_0Impl) φor_1_immφ(bφ φtestφBool_0) φtestφBool_0 {
      return φtestφTrue_0φφor_1_immφφselfCap(bφ,FSpφself)
    }

    func (FSpφself φtestφTrue_0Impl) φand_1_immφ(bφ φtestφBool_0) φtestφBool_0 {
      return φtestφTrue_0φφand_1_immφφnoSelfCap(bφ)
    }



    type φtestφFalse_0Impl struct {
     \s
    }
    func (FSpφself φtestφFalse_0Impl) φ63_1_immφ(fφ φtestφThenElse_1) interface{} {
      return φtestφFalse_0φφ63_1_immφφnoSelfCap(fφ)
    }

    func (FSpφself φtestφFalse_0Impl) φnot_0_immφ() φtestφBool_0 {
      return φtestφFalse_0φφnot_0_immφφnoSelfCap()
    }

    func (FSpφself φtestφFalse_0Impl) φor_1_immφ(bφ φtestφBool_0) φtestφBool_0 {
      return φtestφFalse_0φφor_1_immφφnoSelfCap(bφ)
    }

    func (FSpφself φtestφFalse_0Impl) φand_1_immφ(bφ φtestφBool_0) φtestφBool_0 {
      return φtestφFalse_0φφand_1_immφφselfCap(bφ,FSpφself)
    }



    type φtestφSealed_0Impl struct {
     \s
    }



    func φtestφTrue_0φφand_1_immφφnoSelfCap(bφ φtestφBool_0) φtestφBool_0 {
      return bφ
    }

    func φtestφTrue_0φφor_1_immφφselfCap(bφ φtestφBool_0, this φtestφTrue_0) φtestφBool_0 {
      return this
    }

    func φtestφTrue_0φφnot_0_immφφnoSelfCap() φtestφBool_0 {
      return φtestφFalse_0Impl{}
    }

    func φtestφTrue_0φφ63_1_immφφnoSelfCap(fφ φtestφThenElse_1) interface{} {
      return (fφ.φthen_0_mutφ())
    }

    func φtestφFalse_0φφand_1_immφφselfCap(bφ φtestφBool_0, this φtestφFalse_0) φtestφBool_0 {
      return this
    }

    func φtestφFalse_0φφor_1_immφφnoSelfCap(bφ φtestφBool_0) φtestφBool_0 {
      return bφ
    }

    func φtestφFalse_0φφnot_0_immφφnoSelfCap() φtestφBool_0 {
      return φtestφTrue_0Impl{}
    }

    func φtestφFalse_0φφ63_1_immφφnoSelfCap(fφ φtestφThenElse_1) interface{} {
      return (fφ.φelse_0_mutφ())
    }

    ], GoPackage[pkg=base, src=package main

    type φbaseφSealed_0 interface {
     \s
    }

    type φbaseφSystem_0 interface {
     \s
    }

    type φbaseφVoid_0 interface {
     \s
    }

    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φbaseφSealed_0Impl struct {
     \s
    }



    type φbaseφSystem_0Impl struct {
     \s
    }



    type φbaseφVoid_0Impl struct {
     \s
    }




    ]]]
    """, "fake.Fake", false, """
    package test
    Sealed:{}
    Bool:Sealed{
      .and(b: Bool): Bool,
      .or(b: Bool): Bool,
      .not: Bool,
      ?[R](f: mut ThenElse[R]): R, // ?  because `bool ? { .then->aa, .else->bb }` is kinda like a ternary
      }
    True:Bool{ .and(b) -> b, .or(b) -> this, .not -> False, ?(f) -> f.then() }
    False:Bool{ .and(b) -> this, .or(b) -> b, .not -> True, ?(f) -> f.else() }
    ThenElse[R]:{ mut .then: R, mut .else: R, }
    """, Base.minimalBase);}
  @Test void multiPackage() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }

      fmt.Println(φtestφHelloWorld_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main

    type φtestφHelloWorld_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φtestφHelloWorld_0Impl struct {
     \s
    }
    func (FSpφself φtestφHelloWorld_0Impl) φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0 {
      return φtestφHelloWorld_0φφ35_1_immφφnoSelfCap(sφ)
    }



    func φtestφHelloWorld_0φφ35_1_immφφnoSelfCap(sφ φbaseφSystem_0) φbaseφVoid_0 {
      return φbaseφVoid_0Impl{}
    }

    ], GoPackage[pkg=base, src=package main

    type φbaseφSealed_0 interface {
     \s
    }

    type φbaseφSystem_0 interface {
     \s
    }

    type φbaseφVoid_0 interface {
     \s
    }

    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φbaseφSealed_0Impl struct {
     \s
    }



    type φbaseφSystem_0Impl struct {
     \s
    }



    type φbaseφVoid_0Impl struct {
     \s
    }




    ]]]
    """, "test.HelloWorld", false, """
    package test
    alias base.Main as Main,
    HelloWorld:Main{
      #s -> base.Void
    }
    """, Base.minimalBase); }

  @Test void nestedPkgs() { ok("""
    GoProgram[mainFile=MainFile[src=package main
    import (
      "fmt"
      "os"
    )
    func main() {
      for _, e := range os.Args[1:] {
      baseφrtφGlobalLaunchArgs = baseφrtφGlobalLaunchArgs.φ43_1_immφ(e)
    }

      fmt.Println(φtestφTest_0Impl{}.φ35_1_immφ(baseφrtφGlobalLaunchArgs))
    }
    ], pkgs=[GoPackage[pkg=test, src=package main

    type φtestφFoo_0 interface {
      φa_0_immφ() φtestφFoo_0
    }

    type φtestφFear2φ36_0 interface {
      φa_0_immφ() φtestφFoo_0
    }

    type φtestφA_0 interface {
      φ35_0_immφ() φtestφ46fooφBar_0
    }

    type φtestφTest_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φtestφFear2φ36_0Impl struct {
     \s
    }
    func (FSpφself φtestφFear2φ36_0Impl) φa_0_immφ() φtestφFoo_0 {
      return FSpφself.φa_0_immφφDelegate()
    }

    func (FSpφself φtestφFear2φ36_0Impl) φa_0_immφφDelegate() φtestφ46fooφBar_0 {
      return φtestφFear2φ36_0φφa_0_immφφnoSelfCap()
    }



    type φtestφA_0Impl struct {
     \s
    }
    func (FSpφself φtestφA_0Impl) φ35_0_immφ() φtestφ46fooφBar_0 {
      return φtestφA_0φφ35_0_immφφnoSelfCap()
    }



    type φtestφTest_0Impl struct {
     \s
    }
    func (FSpφself φtestφTest_0Impl) φ35_1_immφ(fear0φ36φ φbaseφSystem_0) φbaseφVoid_0 {
      return φtestφTest_0φφ35_1_immφφnoSelfCap(fear0φ36φ)
    }



    func φtestφFear2φ36_0φφa_0_immφφnoSelfCap() φtestφ46fooφBar_0 {
      return φtestφ46fooφBar_0Impl{}
    }

    func φtestφA_0φφ35_0_immφφnoSelfCap() φtestφ46fooφBar_0 {
      return φtestφFear2φ36_0Impl{}
    }

    func φtestφTest_0φφ35_1_immφφnoSelfCap(fear0φ36φ φbaseφSystem_0) φbaseφVoid_0 {
      return φbaseφVoid_0Impl{}
    }

    ], GoPackage[pkg=test~46foo, src=package main

    type φtestφ46fooφBar_0 interface {
      φa_0_immφ() φtestφFoo_0
    }

    type φtestφ46fooφBar_0Impl struct {
     \s
    }
    func (FSpφself φtestφ46fooφBar_0Impl) φa_0_immφ() φtestφFoo_0 {
      return φtestφ46fooφBar_0φφa_0_immφφselfCap(FSpφself)
    }



    func φtestφ46fooφBar_0φφa_0_immφφselfCap(this φtestφ46fooφBar_0) φtestφFoo_0 {
      return this
    }

    ], GoPackage[pkg=base, src=package main

    type φbaseφSealed_0 interface {
     \s
    }

    type φbaseφSystem_0 interface {
     \s
    }

    type φbaseφVoid_0 interface {
     \s
    }

    type φbaseφMain_0 interface {
      φ35_1_immφ(sφ φbaseφSystem_0) φbaseφVoid_0
    }

    type φbaseφSealed_0Impl struct {
     \s
    }



    type φbaseφSystem_0Impl struct {
     \s
    }



    type φbaseφVoid_0Impl struct {
     \s
    }




    ]]]
    """, "test.Test", false, """
    package test
    Test:base.Main[]{ _ -> {} }
    A:{ #: test.foo.Bar -> { .a -> test.foo.Bar } }
    Foo:{ .a: Foo }
    """, """
    package test.foo
    Bar:test.Foo{ .a -> this }
    """, Base.minimalBase);}
}
