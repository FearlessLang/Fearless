package codegen.java;

import ast.E;
import ast.Program;
import codegen.MIR;
import id.Id;
import id.Mdf;
import program.typesystem.EMethTypeSystem;
import utils.Bug;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImmJavaCodegen extends JavaCodegen {
  public ImmJavaCodegen(Program p, IdentityHashMap<E.MCall, EMethTypeSystem.TsT> resolvedCalls) { super(p, resolvedCalls); }

  @Override public String visitProgram(Map<String, List<MIR.Trait>> pkgs, Id.DecId entry) {
    if (!pkgs.containsKey("base")) {
      throw Bug.todo();
    }
    var entryName = getName(entry);
    var init = "\nstatic void main(String[] args){ "+argsToLList(Mdf.imm)+" base.Main_0 entry = new "+entryName+"(){}; System.out.println(entry.$35$imm$(FAux.LAUNCH_ARGS)); }\n";

    return "class FAux { static FProgram.base.LList_1 LAUNCH_ARGS; }\ninterface FProgram{" + pkgs.entrySet().stream()
      .map(pkg->visitPackage(pkg.getKey(), pkg.getValue()))
      .collect(Collectors.joining("\n"))+init+"}";
  }
}
