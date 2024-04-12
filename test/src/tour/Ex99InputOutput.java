package tour;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static tour.TourHelper.*;
public class Ex99InputOutput {
/*
# A tour of Fearless standard library


//Much later,Next: importance of closing



-------------------------*/@Disabled /*TODO: unimplemented lib code*/ @Test void fsReadHello() { run("""
    Test:Main {sys -> Block#
      .let[mut IO] io = {FIO#sys}
      .let[mut File] file = {io.file(Path#"test.txt")}
      .let content = {file.read!}
      .return {io.println(content)}
      }
    //prints ContentOfTextDotTxt
    """); }/*--------------------------------------------
Here we show how to read and write files.
We use `io` to open a  `mut File file`.
Note how we use `[..]` to specify the type of the local variable. The type can often be inferred,
we write it down here explicitly for clarity.
'file.read' does not directly read the content of the file,
but it creates an Action[Str] object, that it does.
The method `!` of Action gets the string out (in the positive case),
or throws an error in the case the file could not be read.
A programmer expert in Java or python would have probably expected
file.read to either return the string or throw the error.
Returning an Action object allows to provide an uniform API
to handle operations that can fail in recoverable ways,
that is, where the failure is not an observed bug.
-------------------------*/@Disabled /*TODO: unimplemented lib code*/ @Test void fsWriteHello() { run("""
    Test:Main {sys -> Block#
      .var   io = {FIO#sys}
      .let file = {io.file(Path#"test.txt")}
      .var    c = {"ContentOfTextDotTxt"}
      .return {file.write(c)!}
      }
    """); }/*--------------------------------------------
Consider this other example, writing on the file.
`file.write` does not throw an error if the operation fails, but returns an `Action[Void]`.
That is, until we call the '!' method the file is not being written.
Now, In most languages this would be bad API design, since the action could be ignored instead of
being performed, if the `Action[Void]` object is simply discarded.
This concern is less valid in Fearless, where discarding values by accident is unlikely.
If the user forgot to use the `!` and just wrote
`.return {file.write(c)}`, the code would have not compiled.
To return for that block, we need a `Void` value, but `file.write(c)` produces an `Action[Void]`.

Overall, `IO` handles file system interactions (local IO) while Network IO is handled by another capability.
*/
}