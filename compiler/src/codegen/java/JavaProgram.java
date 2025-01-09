package codegen.java;

import codegen.MIR;
import codegen.java.vpf.BuildVPFTargetContainer;
import main.java.LogicMainJava;
import utils.IoErr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JavaProgram(List<JavaFile> files) {
  public JavaProgram(LogicMainJava main, MIR.Program program){
    this(new ToJavaProgram(main,program).of());
  }
  /*JavaProgram(Path output, List<JavaFile> files,JavaCompiler c){
   this(output,files,c.compile(output, files));
  }*/

  //Just to help testing
  public void writeJavaFiles(Path output){IoErr.of(()->_writeJavaFiles(output));}
  private void _writeJavaFiles(Path output) throws IOException{
    for(var fi:files) {
      var line1 = fi.code().lines().limit(1).findFirst().orElseThrow();
      var pkgName = line1
              .substring("package ".length(), line1.length() - 1)
              .trim()
              .replace(".", "/");
      var dirName = output.resolve(pkgName);
      Files.createDirectories(dirName);
      Files.write(dirName.resolve(fi.getName()),fi.code().getBytes());
    }
  }
}

record ToJavaProgram(LogicMainJava main, MIR.Program program) {
  public List<JavaFile> of(){
    ArrayList<JavaFile> javaFiles= generateFiles();
    List<JavaFile> magicFiles=main.io().magicFiles();
    assert !magicFiles.isEmpty() : "Failed to read magic files";
    javaFiles.addAll(magicFiles);
    return Collections.unmodifiableList(javaFiles);
  }
  private ArrayList<JavaFile> generateFiles(){
    var res= new ArrayList<JavaFile>();
    var gen= new JavaSingleCodegen(program);
    for (MIR.Package pkg : program.pkgs()) {
      if (main.cachedPkg().contains(pkg.name())){ continue; }
      for (MIR.TypeDef def : pkg.defs().values()) {
        var funs= pkg.funs().stream()
          .filter(f->f.name().d().equals(def.name()))
          .toList();
        String typeDefContent= gen.visitTypeDef(pkg.name(), def, funs);
        if(typeDefContent.isEmpty()){ continue; }
        String name= gen.id.getSimpleName(def.name());
        res.add(toFile(pkg.name(), name, typeDefContent));
      }
      res.add(BuildVPFTargetContainer.of(pkg, gen, this::toFile));
    }
    for(var e : gen.freshRecords.entrySet()){
      String pkg    = e.getKey().pkg();
      String name   = gen.id.getSimpleName(e.getKey())+"Impl";
      String content= e.getValue();
      res.add(toFile(pkg, name, content));
    }
    return res;
  }

  private JavaFile toFile(String pkgName, String name, String content) {
    String pkg= "package "+pkgName+";\n";
    String fileName= pkgName.replace(".","/")+"/"+name+".java";
    Path p=main.io().output().resolve(fileName);
    return new JavaFile(p,pkg+content);
  }
}