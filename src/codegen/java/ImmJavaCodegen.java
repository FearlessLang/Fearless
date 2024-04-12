package codegen.java;

import codegen.MIR;
import id.Id;
import id.Mdf;

import java.util.stream.Collectors;

public class ImmJavaCodegen extends JavaCodegen {
  public ImmJavaCodegen(MIR.Program p) { super(p); }

  @Override public String visitProgram(Id.DecId entry) {
    var entryName = getName(entry);
    var init = """
      static void main(String[] args){
        %s base.Main_0 entry = %s._$self;
          rt.NativeRuntime.println(entry.$35$imm$(FAux.LAUNCH_ARGS).utf8());
        try {
        } catch (StackOverflowError e) {
          System.err.println("Program crashed with: Stack overflowed");
          System.exit(1);
        } catch (Throwable t) {
          System.err.println("Program crashed with: "+t.getMessage());
          System.exit(1);
        }
      }
    """.formatted(
      argsToLList(Mdf.imm),
      entryName
    );

    return "package userCode;\nclass FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }\npublic interface FProgram{\n" +p.pkgs().stream()
      .map(this::visitPackage)
      .collect(Collectors.joining("\n"))+init+"}";
  }
}
