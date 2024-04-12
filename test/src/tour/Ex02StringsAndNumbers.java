package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static tour.TourHelper.*;
public class Ex02StringsAndNumbers {
/*
# Strings and numeric types

# Numeric types

The Fearless standard library offers the following numeric types:

- `Nat` : unsigned 64 bit integers
- `Int` : signed 64 bit integers
- `Float` : double precision 64 bit floating points according to specification IEEE 754
- `Num` : Unlimited precision fractional numbers
//Is this all?
//We need to discuss how to fix this: not Stringable due to limitations of the Java codegen target
//I had the same exact problem in 42 and I fixed it.

With `T` in `Nat`,`Int`,`Float`,`Num`, they support the following conventional operations:
``` 
  + (n: T): T,
  - (n: T): T,
  * (n: T): T,
  / (n: T): T,
  **(n: T): T, // there is a good reason for pow to have different signatures
  % (n: T): T, //really, also for floats? Java supports it, do we want to?
  .abs: T, //really? also for Nat?
  > (n: T): Bool,
  < (n: T): Bool,
  >=(n: T): Bool,
  <=(n: T): Bool,
  ==(n: T): Bool,
  // TODO: add .assertEq
```
This means that we can only operate on homogeneous numeric types.
We can sum two `Int` but we can not directly sum `Int` and `Float`.
In other languages, this is allowed through a process called coercion.
We think that coercion is the source of plenty of bugs.
Instead, in Fearless you convert numeric types to each other manually, by using those methods:
```
  readH .int: Int,
  readH .nat: Nat,
  readH .float: Float,
  readH .num: Num,
```
All numeric types support all of those methods, so `myInt.int` is just going to just return itself.
They take a `readH` receiver, so that if somehow the type system have lost the knowledge that a numeric type is immutable, we can recover it. This is safe since all instances of numeric types are always immutable.
This can happen for example when generic code is used, and integers are passed to satisfy a `read` parameter.

`Int` and `Nat` support also binary operations:
```
  >>(n: T): T, //Nick version
  <<(n: T): T,
  ^ (n: T): T,
  & (n: T): T,
  | (n: T): T,
```  

```
  .rightShift(n: Nat): T, //Marco version (should take an Int or a Nat, but the same for both types!)
  .leftShift(n: Nat): T,
  .bitwiseXor(n: T): T,
  .bitwiseAnd(n: T): T,
  .bitwiseOr(n: T): T,
```  
It is however quite rare to need those operations in Fearless code.
  
Finally, all the numeric types have a method
```
  readH .str: Str,
```
returning a string.
// We should use `read` everywhere we used `readH` due to our read promotion rules. A readH can call a read method like this
// (i.e. takes no args & returns an imm)

The code below shows those numeric types in action.
-------------------------*/@Test void numericTypes() { run("""
    package test
    Test:Main {sys ->Block#
      .let ten = {10}// the Nat 10, 32 bit unsigned
      .let pTen = {+10} //Int +10, 64 bit signed
      .let mTen = {-10} //Int -10, 64 bit signed
      .let zero = pTen + mTen
      .let fTen = {+10.0} //should we force the sign?
      //if we do not, `.1` can not be a method name!!
      .let frac = {34554329/14456354}//unlimited precision fractional
      .let justNat =  {34554329 / 14456354}
      //I hate this. Any better idea?
      .let ok = {+10 + -10}
    //.let err = {+10 + - 10}//error: method Int.+() does not exists
      //this is less drammatic, since that space is really innatural
      .let mixTypes1 = {fTen + (ten.float)}
      .let mixTypes2 = {fTen.nat + ten} //very different results!
      .return {UnrestrictedIO#sys.println(ten.str)}
      }
    //prints 0
    """); }/*--------------------------------------------

To defend our choice to have no automatic conversions, consider
the two local bindings `mixTypes1` and `mixTypes2`: they produce different results, especially for very large numbers.
If you want to avoid worrying about representation details, consider just using `Num` all of the time.

# Strings and String builders (accumulator? from list/flow?)

Fearless `Str` encode sequences of characters, under encoding XXXX (check Java). (TODO Talk to Michael Homer about string encodings)
For simplicity Fearless do not have a dedicate character type, and elements of the string are visible just as `Nat`.
Here the methods supported by Str:
```
  read .str: Str,
  read .int: mut Action[Int],
  read .nat: mut Action[Nat],
  read .float: mut Action[Float],
  read .num: mut Action[Num],
  .size: Nat,
  .isEmpty: Bool,
  +(other: Stringable): Str,
  ==(other: Str): Bool,
  .subString(begin: Nat, end: Nat): Str,
  .subStringLast(begin: Nat,end: Nat): Str,
  .startsWith(s: Str): Bool,
  .endsWith(s: Str): Bool,
  .indexOf(s: Str): Opt[Nat],
  .indexOfChar(char: Nat): Opt[Nat],
  .addChar(char: Nat): Str,
  .replaceAll(old:Str,new:Str): Str,
  .assertEq(other: Str): Void,
  //looks out of place, can it be private?
  .flow  
```
As you can see, strings support '+' to concatenate with other stringables, and the numeric types are all Stringable, so we can 
write code like the following:
-------------------------*/@Test void stringable() { run("""
    package test
    //as defined in the standard library
    //Stringable: {read .str: Str, }

    Persons:F[Str,Nat,Person],Stringable{name,age->Person:{
      .name:Str->name,
      .age:Nat->age,
      .str->"Person["+name+", "+age+"]"
      }}
    Test:Main {sys -> 
      UnrestrictedIO#sys.println(Persons#("Bob",43))
      }
    //prints Person[Bob, 43]
    """); }/*--------------------------------------------

Method `Str.subString(begin,end)` takes substrings of a string.
Method `Str.subStringLast(begin,end)` is the same but starts from the end, so `"qwertyuiop".subStringLast(5,1)` returns the 4 characters string `"yuio"`: the last 5 characters except the last one.
Using those methods with indexes outside of the string is an observed bug and will produce an error.

Method `Str.int` can be used to convert the string into an integer. 
It returns a `mut Action[Int]`.
Actions are used to enforce that we check for an error condition.
Action is declared as follows in the standard library:
-------------------------*/@Test void action() { run("""
  //sholuld a mut Action cache the result?
  MF[A,R]:{ mut #(a:A):R }
  Actions:{
    .some[E](e: E): mut Action[E]   -> {m-> m.some(e)},
    .info[E](i: Info): mut Action[E]-> {m-> m.info(i)},
    .info[E](kind: Str, msg: Str): mut Action[E]-> {m-> m.info(Info#(kind,msg))},//How to make info {kind:"..", msg:".."}
    }
  ActionMatch[E,R]:{ .some(e: E): R, .info(i: Info): R, }
  Action[E]:{
    mut .run[R] (m: mut ActionMatch[E,R]): R,
    mut .map[R'](f: mut MF[R,R']): Action[R']->this.run{//WRONG, eager!
      .some(e)->Actions.some(f#e),
      .info(i)->this,
      }
    mut .map[R'](f: mut MF[R,R']): Action[R']->{m->this.run{//RIGHT, lazy!
      .some(e)->m.some(f#e),
      .info(i)->m.info(i),
      }}
    mut !: E-> this.match{
      .some(e)-> e,
      .info(i)-> Error#(i),
      }
    }
    //showing how Str.int can be implemented
    _StrPrivate:{
      .intInfoStart(s: Str): Action[Int]->
        Action.info("Invalid Int",
          "Nees to start with +/-"
          + " but it starts with "
          + this.substring(0,1),
      .intInfoNoDigit(char: Num): Action[Int]->
        Action.info("Invalid Int",
          "Non digit character ".addChar(char)
          + " found.",
      .intOverflow(s: Str): Action[Int]->
        Action.info("Invalid Int", 
          (s.size>20?{
            .then-> "String of lenght " + s.size + " would encode"
            .else-> "String "+s+" encodes"
            })
           +" a number overflowing the Int representation",
      .int(s:Str): Action[Int] ->Block#//this works like an Either
        .if {s.size>20} .return {this.intOverflow(s)}
        .let pos = {s.startsWith("+")}
        .let neg = {s.startsWith("-")}
        .if {pos .or neg !} .return {this.intInfoStart(s)}
        .let start = neg ? {.then-> +0, .else-> -0} // Nick: this does not make sense
        .let s0 = {s.substring(1,s.size)}
        .let digits[List[Nat]] = {"0123456789".flow.list}
        .let err = {s0.flow.findFirst{c->digits.indexOf({e->e == c).isSome}}
        .if {err.isPresent} .return {this.intInfoNoDigit(err!)}
        .let res= {s0.flow
          .flatMap{c->digits.indexOf({e->e == c}).flow}//{e->e==c} converts ashii to int
          .fold(start,{acc,curr-> acc * +10 + curr.int})
          }
         .if {(res >= 0 .and neg) .or (res < 0 . and pos)}
           .return {this.intOverflow(s)}
         .return{res}
      }
    Str:{//..other code of string
      .int: Action[Int] ->{m->
        _StrPrivate.int(this).run{
          .some(e)-> m.some(e),
          .info(i)-> m.info(i), 
          }
        }
      }
    """); }/*--------------------------------------------
As you can see, Actions can be used both as a conventional Either
or as a lazy operation that can produce an informative message instead of failing.
Actions are our answer to Java checked exceptions: when something is not necessarily an observed bug, we can turn the operation into an Action.
Actions can be combined with `Action.map`.
Actions are intrinsically imperative, in the sense that they can capture mutable state and cause side effects when they `.run`.

This code also shows that our matcher methods are much more then 
ADT matchers.

Now we can show optionals: `Opt[E]`.
`Opt[E]` is in some sense a much simpler type then `Action[E]`,
and it is simply modeling that a value can be present or not.
An optional is the simplest form of collection, and it is basically
a collection of zero or one element.
An empty optional it is not representing a mistake/error/problem.

-------------------------*/@Test void optional() { run("""
  Opts:{
    #[T](x: T): mut Opt[T] -> {.match(m) -> m.some(x)}
    }
  _Opt[T]:{
    mut  .match[R](m: mut OptMatch[T, R]): R,
    read .match[R](m: mut OptMatch[read/imm T, R]): R,
    imm  .match[R](m: mut OptMatch[imm T, R]): R,

    mut  .map[R](f: mut OptMap[T, R]):          mut Opt[R],
    read .map[R](f: mut OptMap[read/imm T, R]): mut Opt[R],
    imm  .map[R](f: mut OptMap[imm T, R]):      mut Opt[R],
    
    mut  ||(default: mut MF[T]): T,
    read ||(default: mut MF[read/imm T]): read/imm T,
    imm  ||(default: mut MF[imm T]): imm T,

    mut  !: T,
    read !: read/imm T,
    imm  !: imm T,
        
    mut  .flow: mut Flow[T],
    read .flow: mut Flow[read/imm T],
    imm  .flow: mut Flow[imm T],
    }
  Opt[T]:,_Opt[T]{
    .match(m) -> m.empty,
      
    .map(f) -> this.match(f),
  
    ||(f) -> this.match{.some(x)-> x, .empty->f# },
    
    ! -> this || {Error.msg "Opt was empty"},
      
    read .isEmpty: Bool ->
      this .match {.empty -> True, .some(_) -> False},

    read .isSome: Bool ->
      this .match { .empty -> False, .some(_) -> True },
    
    .flow -> 
      this.match{.empty -> Flow#, .some(x) -> Flow#(x) },
  }

  OptMatch[T,R]:{ mut .some(x: T): R, mut .empty: R }
  OptMap[T,R]:OptMatch[T, mut Opt[R]]{
    mut #(t: T): R,
    .some(x) -> Opt#(this#x),
    .empty -> {}
    }
  """); }/*--------------------------------------------

As you can see, optional implementation is very small,
but there are a lot of type signatures. In order to understand
a library you need to understand the type signatures and the
behavior of the methods. 

- If a library is mostly behavior,
then going by examples and by the documentation of the individual methods is best.
- If a library is mostly type signatures, as in this case,
then just showing the code may be the best approach.
 
@Nick: is this or working better than the old one?
it still allows for chains like this
  o1 || {o2 || {o3 || {v}}}


//Int,Nat,Str,Optional,HasStr,Ordered/Comparator,List,
//Much later,Next: importance of closing

*/
}