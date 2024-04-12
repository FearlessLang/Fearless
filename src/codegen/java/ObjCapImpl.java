package codegen.java;

import codegen.MIR;
import id.Id;

import java.util.List;

public interface ObjCapImpl {
  String call(JavaMagicImpls ctx, Id.MethName m, List<? extends MIR.E> args);
}
