package codegen.go;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;

public interface ObjCapImpl {
  String call(MagicImpls ctx, Id.MethName m, List<MIR> args, Map<MIR, T> gamma);
}
