package codegen.java;

public interface ToJavaFile {
  JavaFile of(String pkgName, String name, String content);
}
