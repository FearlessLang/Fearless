package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static codegen.java.RunJavaProgramTests.ok;
import static tour.TourHelper.run;
import static utils.RunOutput.Res;

;
public class Ex01HelloWorldTest {
/*
# A tour of Fearless standard library

# Preface
This is a guide to learn the standard library of fearless.
This guide assumes that you know all the language features already, but you have no idea how the standard library works, or
how the files are organized into a project.

# Hello world

A Fearless project is a folder containing files with extension *.fear.
Those files can be at top level or inside folders. The organization of files in folders is for the benefit of the programmers and
has no impact on the semantics of fearless.
Assume in folder 'myFolder' we have a file with the following content:  
    -------------------------*/@Test void helloWorld() { run("""
    package test
    alias base.Main as Main,
    alias base.caps.UnrestrictedIO as UnrestrictedIO,
    //alias base.caps.UnrestrictedIO as UnrestrictedIO,//should it be like this?
    
    Test:Main {sys -> UnrestrictedIO#sys.println("Hello, World!")}
    //prints Hello, World!
    """); }/*--------------------------------------------

The code above is a minimal Hello World program.
- In the first line we declare that our file belongs to the package 'test'.
Note how there is no need for the files inside the package 'test' to be all contained inside a 'test' folder. In this example it is just contained directly inside `myFolder`
We will omit the `package test` line in all other examples.
- We then declare the type Test. Test implements Main. All runnable types implement Main.
- `Main` is declared like this: `Main: {.main(sys: mut System): Void}`
- In the body of Test we implement Main by `sys -> UnrestrictedIO#sys.println("Hello, World!")`,
- `sys` is a parameter name (we can freely choose those) that will refer to a mutable System object.
- `UnrestrictedIO` is a factory creating an IO capability that can do any kind of IO.
The System object is our starting point for any kind of interaction with the real world.
We call those objects that allows us to interact with the real world 'Object Capabilities'.
In this example, we call `UnrestrictedIO#sys` to produce an `IO` object capability, and then we call the method `.println` on it.
The syntax `UnrestrictedIO#sys` calls the method `#` on the object `IOs` passing `sys` as a parameter.

As you can see, `UnrestrictedIO#sys.println(..)`
is quite verbose.
We do not expect this code to be very common in Fearless.
If someone is printing just because they want a debugging printout, the can use
-------------------------*/@Test void helloWorldDebug() { ok(new Res("", "Hello, World!", 0), """
    package test
    Test:Main {sys -> base.Debug.println("Hello, World!")}//OK
    """, Base.mutBaseAliases); }/*--------------------------------------------

On the other hand, if they are writing a console program, or any kind of program that needs to do input output, there will be a few
well-designed types that have this responsibility, and the main will assign capabilities to those types. We will see an example of this (much) later.

 # Block
One of the easier ways to start writing fearless code when coming from other languages is to use `Block`.
-------------------------*/@Test void helloWorldBlockVar() { run("""
    Test:Main {sys -> Block#
      .let io = {UnrestrictedIO#sys}
      .return {io.println("Hello, World!")}
      }
    //prints Hello, World!
    """); }/*--------------------------------------------
In this example, by using `Block` we declare a local binding `io` and then we use it to call the `.println` method.
We use `Block` to open a statements block.
The `.let` method is used to declare the local binding `io`, that is initialized with the result of executing `UnrestrictedIO#sys`.
The `.return` method concludes the block of statements.
Since the block is implementing `Main.main`, we need to return `Void`.
We could alternatively write
-------------------------*/@Test void helloWorldBlockVar2() { run("""
    Test:Main {sys -> Block#
      .let io = {UnrestrictedIO#sys}
      .do {io.println("Hello, World!")}
      .return {Void}
      }
    //prints Hello, World!
    """); }/*--------------------------------------------
The method `.do` just do some action in the middle of a block.
It is like declaring a local variable of type `Void` whose name is never used.
Since writing `.return{Void}` can get verbose and repetitive, we can just write `.done` instead.
-------------------------*/@Test void helloWorldBlockDone() { run("""
    Test:Main {sys -> Block#
      .let io = {UnrestrictedIO#sys}
      .do {io.println("Hello, World!")}
      .done
      }
    //prints Hello, World!
    """); }/*--------------------------------------------
Block supports early exits, using the `.if` method, and many other useful features. To explore them, we write a `StrToMessage` function
that can be useful to map strings into good error messages
-------------------------*/@Disabled("03/12/24") @Test void blockIf() { run("""
    StrToMessage:F[Str,Str]{s->Block#
      .if {s.isEmpty} .return {"<EmptyString>"}
      .var res = {s}
      .if {res#.contains("\\n")} .do//showing alternative below, what is better? 
        {res := (res#.replaceAll("\\n","\\\\n"))}//but need parenthesis
        {res.set(res#.replaceAll("\\n","\\\\n"))}        
        {res#{si->si.replaceAll("\\n","\\\\n")}}
        {res#{::replaceAll("\\n","\\\\n")}}
      .if {res#.size > 100} .do {Block#
        .let start = {res#.substring(0,48)}
        .let end   = {res#.substringLast(48,0)}
        .return {res := start + "[..]" + end}
        }
      .return {res#}
      }
    """); }/*--------------------------------------------

The code starts by marking empty strings in an easily recognizable way,
then replaces new lines with their escaped form, and finally cut off strings that are too long.
Here we can see that `.if .. .return` can return a value early.
`.if .. .error` is a variant that throws an error.
The Fearless standard library throws errors only to signal observed bugs, and encourages you to do the same. 
It is common to start methods with a Block with a bunch of
`.if .. .return` or `.if .. .error` at the start.

Finally, `.if .. .do` just does something if the condition is true.
Note the difference between `.var` and `.let`:

- `.let` declares a local binding.
  Note how after we declare `start`, we can directly access its value of  in the following code.
- `.var` declares a local variable. We use `.var` to access and update the value of something.
  In the example we use it like this: `res := res#.replaceAll(..)`//Bad precedence
  As you can see, we update it with `:=` and we access it with `res#`.
  That is, `res` on its own is of type `Var[T]` and can be passed around
  directly. 
  However doing so may prevent the Fearless compiler to apply some optimizations.

The nested block ends with '.return'. This is different from what you may expect, the '.return' here is making the inner block return `Void`, the result of `:=`.
To clarify the behavior of nested blocks, we show some alternative to that code below:
-------------------------*/@Disabled("03/12/24") @Test void blockNested1() { run("""
    StrToMessage:F[Str,Str]{s->Block#
      .if {s.isEmpty} .return {"<EmptyString>"}
      .let res = {s}
      .if {res.contains("\\n")} .do {res := res#.replaceAll("\\n","\\\\n")}
      .if {res.size() > 100} .return {Block#
        .let start = {res#.substring(0,48)}
        .let end   = {res#.substringLast(48,0)}
        .return {start + "[..]" + end}
        }
      .return {res#}
      }
    """); }/*--------------------------------------------
In this alternative, we use `.if .. .return` to propagate out the result. Note how we now return the final result directly instead of updating `res`.
-------------------------*/@Disabled("03/12/24") @Test void blockNested2() { run("""
    StrToMessage:F[Str,Str]{s->Block#
      .if {s.isEmpty} .return {"<EmptyString>"}
      .let res = {s}
      .if {res#.contains("\\n")} .do {res := res#.replaceAll("\\n","\\\\n")}
      .if {res#.size() > 100} .do {Block#
        .let start = {res#.substring(0,48)}
        .let end   = {res#.substringLast(48,0)}
        .do {res := start + "[..]" + end}
        .done
        }
      .return {res#}
      }
    """); }/*--------------------------------------------
In this alternative, we mimic more closely what happens in conventional statements. We think this is just more verbose that using `.return` directly.
Note that without `.done` the code would not compile, since `.do` returns a `Block[Void]` and not `Void`.

As always, the best alternative is to avoid nesting and to write

-------------------------*/@Disabled("03/12/24") @Test void blockNested3() { run("""
    StrToMessage:F[Str,Str]{s->Block#      
      .if {s.isEmpty} .return {"<EmptyString>"}
      .let res = {s}
      .if {res#.contains("\\n")} .do {res := res#.replaceAll("\\n","\\\\n")} 
      .if {res#.size() <= 100} .return {res#}
      .let start = {res#.substring(0,48)}
      .let end   = {res#.substringLast(48,0)}
      .return {start + "[..]" + end}
      }
    """); }/*--------------------------------------------

As you can see, by inverting the `.if` condition and inserting a one liner early return, we can make the code look much better.

Block is much more powerful that usual statements, because each individual bit is an expression. Thus we can use it to modularize method bodies that requires many early returns, and we can scope local variables:

-------------------------*/@Disabled("03/12/24") @Test void blockScoped() { run("""
  Example: F[Str,Str]{s->Block# //bad all mixed
    //part1 //comments used to denote section of code: bad smell
    .let res = {s}
    .if {res#.isEmpty} .return {"<EmptyString>"}
    .if {res#.contains("\\n")} .do {res := res#.replaceAll("\\n","\\\\n")}
    .if {res#.size() <= 100} .return {res#}
    //part2
    .let start = {res#.substring(0,48)}
    .let end   = {res#.substringLast(48,0)}
    .return {start + "[..]" + end}
    }
    
  Example:F[Str,Str]{//Better version, with division in parts
    #(s) -> Block#
      .let res = {s}
      .let scope = {All:ExampleParts1,ExamplePart2{.res->res}}
      .part{b->scope.part1(b)}
      .part{b->scope.part2(b)}
    }
   ExampleParts1:{
    .res:Var[Str],
    
    .part1(c: Block[Str]): Block[Str] -> c
      .if {this.res#.isEmpty} .return {"<EmptyString>"}
      .if {this.res#.contains("\\n")}
        .do {this.res# := this.res#.replaceAll("\\n","\\\\n")}
      .if {this.res#.size() <= 100} .return {this.res#},
    }
   ExampleParts2:{
    .res:Var[Str],
       
    .part2(c: Block[Str]):Str -> c
      .let start = {this.res#.substring(0,48)}
      .let end   = {this.res#.substringLast(48,0)}
      .return start + "[..]" + end
    }
  """); }/*--------------------------------------------
As we can see, using `Block.part` we can divide a large block in many parts.
- The early return feature would still work.
- Shared local variables will have to be lifted as captured data.
- Local bindings and variables will be scoped to the part.
- In this way we could divide a large method in many types, that could even be spread between files.

*/
}