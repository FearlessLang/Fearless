package magic;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MagicTrait<R> {
  Id.IT<T> name();
  R instantiate();
  Optional<R> call(Id.MethName m, List<MIR> args, Map<MIR, T> gamma);
}
