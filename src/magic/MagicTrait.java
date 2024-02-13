package magic;

import ast.T;
import codegen.MIR;
import id.Id;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface MagicTrait<E,R> extends MagicCallable<E,R> {
  Id.IT<T> name();
  R instantiate();
}
