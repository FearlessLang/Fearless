package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.Base;

import static utils.RunOutput.Res;
import static codegen.java.RunJavaProgramTests.*;
import static tour.TourHelper.*;

public class Ex02CollectionsTest {
/*
----Lists----
Lists manipulation is crucial in most software development
Fearless offers a generic List[E] type.
`List#[Int](5, 10, 15)`, or just `List#(5, 10, 15)` will create
a list with 3 elements.
To start simple, how can we print this list?
In Fearless there is no top level Object/Any type and thus there is no 
default way to turn an element into a string.
Thus while `5.str` makes perfect sense, but `List#(5, 10, 15).str`
could not possibly work: such method would need to have a way to convert
the elements in strings, as shown below.
-------------------------*/@Disabled @Test void listNums1() { run("""
    package test
    Test:Main {sys -> Block#
      .let myList= {List#[Int](5, 10, 15)}
      .var  myStr= {myList.str {n->n.str}}
      .var  myStr= {myList.str ::str}
      .return{FIO#sys.println(myStr)}
      }
    //prints [5, 10, 15]
    """); }/*--------------------------------------------
For more control, we can use flows.
Indeed lists are mostly used with flows:
an elegant way to concatenate operations and produce results.
Here we first map the elements to strings using 'number.str'
//@Nick, I now think  #(Flow.join ", ") was a terrible idea, look the new one!
//may be too gimmiky?

-------------------------*/@Disabled @Test void listNums2() { run("""
    package test
    Test:Main {sys -> Block#
      .let myList= {List#[Int](5, 10, 15)}
      .let myStr= {myList.flow
        .map{n -> n.str}
        #"; "//ok and also #Sum/#USum/#FSum, so Flow.str/Flow.sum disappear
        }
      .let myStr= {myList.flow.map::str#"; "} //options
      .let myStr= {myList.flow.map{::str}#"; "}
      .return{FIO#sys.println(myStr)}//we agreed parenthesis needed
      }
    //prints 5; 10; 15
    """); }/*--------------------------------------------
Lists can be mutated, and elements can be added after the list
is created.
We can use `.add` and `.get` as we would expect from most other languages. 
Overall, random access to list elements while possible is discouraged
in fearless and should be replaced with flow access when possible.
Of course, there are still situations where random access is crucial.
Note how .get can not possibly succeed for elements outside of the
size of the list. In this case a dynamic error is thrown. 
-------------------------*/@Test void addAndGetFromList() {run("""
    package test
    Test:Main {sys -> Block#
      .let list = {List#[Str]}//YES spaces on both sides of =?
      .do {list.add("YAY!")}
      .var  yay = {list.get(0)}
      .return {FIO#sys.println(yay)}
      }
    //prints YAY!
    """); }/*--------------------------------------------
Lists can be created with elements, and elements can be added later.
the method `.size` gives us the current size.
Fearless standard library consistently uses the word `size`
to talk about sizes of collections, flows, results etc.
Using `length`/`count` is discouraged.
-------------------------*/@Test void listSize() {run("""
    package test
    Test:Main {sys -> Block#
      .let list = {List#(5, 10, 15)}
      .do {list.add(20)}
      .assert({list.size == 4u})
      .done
      }
    """); }/*--------------------------------------------
TODO: is there an earler .return{Void} to discuss .done?
Map are a more complex kind of data structure, and while not as common as lists,
they are crucial for programming.
Fearless offers many kinds of Maps, here we show a few options.
Map keys must be immutable objects, element can be anything.
Many maps are on Str or Int types
//Int or Num?? I'm happy with Int, but we have tons of 'Num' around
//norm for non cyclic?
-------------------------*/@Test void mapOfNameToAge() {run("""
    package test
    Person:Ordered[Person]{
      .name:Str, .age:UInt
      .order-> CPerson.with this
      }
    FPerson:F[Str,UInt,Person]{n,a->{.name->n,.age->a}}
    
    PersonF:F[Str,UInt,Person]{n,a->{.name->n,.age->a}}
    Persons#(a,b) //accepted
    Persons.compare //accepted
    Ints
    Opts#bob
    NewOpt#foo
    FIO#sys
    IOs#sys //
    Opt[T]
    NewHtml.a
    //Unsign instead of UInt accepted
    //Compare instead of Comparator accepted
    //CompareUnsign   accepted
    UInt
    ComparePerson
    //CPerson:Compare[Person]{ p1,p2->p1.age>p2.age }
    CPerson:Comparator[Person]{ p1,p2->CUInt#{p->p.age}#(p1,p2) }
    CPerson:Comparator[Person]{ p1,p2->
      p1.age.compareWith(p2.age)//better
      .and{p1.name.compareWith(p2.name)}
      }
    Persons:{
      compare:Compare[Person]->//worst
        Strs.compare#{p->p.name}
          .and{Unsigns.compare#{p->p.age}}
    }
    Test:Main {sys -> Block#
      .let print= {mut _:{mut #(s:Str):Void->FIO#sys.println(s)}}
      //.let print= {mut Fresh:{mut #(s:Str):Void->FIO#sys.println(s)}}
      .let mapStr = {Map.str(
        "Alice", 24,
        "Bob", 30)}
      .do{ mapStr.put("Bob",30) }
      .let bobAgeOpt = {map.get("Bob")}
      .if {bobAgeOpt?}.do{print#(bobAgeOpt!.str)}
      .do{bobAgeOpt.flow.for{age->print#(age.str)}}
      .let mapBase1 = {Map#(CPerson,
        FPerson#("Alice",24), "50032345b",
        FPerson#("Bob",30),   "50032211c"
        )}
      .let mapBase2 = {Map#({
          .eq(k1, k2) -> k1.name == k2.name,//For more control
          .hash(k) -> k.age.hash//nope
          },
        FPerson#("Alice",24), "50032345b",
        FPerson#("Bob",30),   "50032211c"
        )}
 
     // or:
      .do {map.put("Bob", 30)}
      .let bobAgeOpt = {map.get("Bob")}
      .let bobAge = {bobAgeOpt!}
      .return {FIO#sys.println(bobAge.str)}
      }
      //30
    """);}

  // Our maps/sets are linked hashmaps/linked hashsets and thus have deterministic iteration order.
  @Test void mapFlow() { ok(new Res("30", "", 0), "test.Test", """
    package test
    Test:Main {sys -> Block#
      .let map = {Map.str("Alice",24, "Bob",30)}
      // if we have destructuring:
      // .let ageSum = {map.flow.map{[key, val] -> val}#(Flow.sum)}
      // .let ageSum = {map.flow.map{kv -> kv.val}#(Flow.sum)}
//      .return {map#(Map.str "\\n") // Map.str requires Map[Stringable, Stringable]
      .return {map.flow
        .map{kv -> kv.key + ": " + (kv.val.str)}
        #(Flow.str "\\n")
        }
      }
    """, Base.mutBaseAliases);}
}
