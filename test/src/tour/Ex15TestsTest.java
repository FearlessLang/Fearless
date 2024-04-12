package tour;

import org.junit.jupiter.api.Test;
import utils.Base;
import utils.RunOutput.Res;

import static tour.TourHelper.run;
import static codegen.java.RunJavaProgramTests.*;

public class Ex15TestsTest {
  @Test void runTests() { run("""
    package test
    alias base.test.Main as TestMain,
    alias base.Void as Void, alias base.caps.FIO as FIO,
    
    Test: TestMain{_, runner -> runner
      .test("Empty test", {sys -> Void})
      .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
      .run
      }
    //prints Hello, world!
    """); }
  @Test void runTestsWithSuite() { run("""
    package test
    alias base.test.Main as TestMain,
    alias base.Void as Void, alias base.caps.FIO as FIO,

    
    Test: TestMain{_, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        })
      .run
      }
    //prints Hello, world!
    """); }
  @Test void runTestsWithPrintReporter() { ok(new Res("Hello, world!", """
    # Test Results
    ## <unnamed>
    Empty test - PASSED
        
    ### Printing suite
    Printing test - PASSED
    """, 0), "test.Test", """
    package test
    alias base.test.Main as TestMain, alias base.test.ResultPrinters as ResultPrinters,
    
    Test: TestMain{system, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        })
      .withReporter(ResultPrinters#(FIO#system))
      .run
      }
    """, Base.mutBaseAliases); }

  @Test void nestedTestsWithPrintReporter() { ok(new Res("Hello, world!", """
    # Test Results
    ## <unnamed>
    Empty test - PASSED
        
    ### Printing suite
    Printing test - PASSED
        
    non-Printing test - PASSED
        
    #### nested
    test1 - PASSED
        
    test2 - ERRORED
    ```
    sad
    ```
        
    ##### more nested
    test3 - PASSED
        
    test4 - PASSED
        
    ###### more nested
    test5 - PASSED
        
    **more nested** \s
    test6 - ERRORED
    ```
    another one
    ```
        
    **more nested** \s
    test7 - PASSED
        
    **more nested** \s
    test8 - PASSED
    ### top level
    test1 - PASSED
    """, 0), "test.Test", """
    package test
    alias base.test.Main as TestMain, alias base.test.ResultPrinters as ResultPrinters,
    
    Test: TestMain{system, runner -> runner
      .test("Empty test", {sys -> Void})
      .suite("Printing suite", {suite -> suite
        .test("Printing test", {sys -> FIO#sys.println("Hello, world!")})
        .test("non-Printing test", {sys -> Void})
        .suite("nested", {suite' -> suite'
          .test("test1", {sys -> Void})
          .test("test2", {sys -> Error.msg "sad"})
          .suite("more nested", {suite'' -> suite''
            .test("test3", {sys -> Void})
            .test("test4", {sys -> Void})
            .suite("more nested", {suite3 -> suite3
              .test("test5", {sys -> Void})
              .suite("more nested", {suite4 -> suite4
                .test("test6", {sys -> Error.msg "another one"})
                .suite("more nested", {suite5 -> suite5
                  .test("test7", {sys -> Void})
                  .suite("more nested", {suite6 -> suite6
                    .test("test8", {sys -> Void})
                    })
                  })
                })
              })
            })
          })
        })
      .suite("top level", {suite -> suite
        .test("test1", {sys -> Void})
        })
      .withReporter(ResultPrinters#(FIO#system))
      .run
      }
    """, Base.mutBaseAliases); }
}
