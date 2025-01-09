package codegen.java.vpf;

import codegen.MIR;
import codegen.java.JavaFile;
import codegen.java.JavaSingleCodegen;
import codegen.java.ToJavaFile;
import utils.DistinctBy;

import static codegen.java.StringIds.VPF_TARGET_CONTAINER_BASE_NAME;

public interface BuildVPFTargetContainer {
  static JavaFile of(MIR.Package pkg, JavaSingleCodegen codegen, ToJavaFile toFile) {
    var res = new StringBuilder();
    res.append("public interface ").append(VPF_TARGET_CONTAINER_BASE_NAME).append(" {\n");
    pkg.vpfTargets().stream()
      .gather(DistinctBy.of(target->new FnKey(target.call().parentFun(), target.call().original().callId())))
      .map(codegen::visitVPFCallTarget)
      .forEach(res::append);
    res.append("}\n");
    var code = res.toString();
    return toFile.of(pkg.name(), VPF_TARGET_CONTAINER_BASE_NAME, code);
  }
  record FnKey(MIR.FName fun, long callId) {}
}
