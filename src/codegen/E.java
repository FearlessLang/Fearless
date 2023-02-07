package codegen;

import ast.T;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.Id;
import id.Mdf;

import java.util.List;
import java.util.Optional;

public interface E {
  @JsonSerialize
  record Lambda(Mdf mdf, List<Id.IT<T>> its, String selfName, List<Meth> meths, List<String> captures) implements E {
    boolean isSingleton() { return captures.isEmpty(); }
    boolean validJavaLambda() {
      return meths.size() == 1 && !meths.get(0).isAbs();
    }
  }
  @JsonSerialize
  record Ref(Mdf mdf, T inner) implements E {}
  @JsonSerialize
  record MCall(E recv, String name, List<E> es) implements E {}
  @JsonSerialize
  record X(String name) implements E {}
  @JsonSerialize
  record Meth(Mdf mdf, List<Id.GX<T>> gens, List<T> ts, List<String> xs, T ret, String name, Optional<E> body) {
    boolean isAbs() { return body.isPresent(); }
  }
  @JsonSerialize
  record Dec(String name, List<Id.GX<T>> gxs, Lambda lambda) {}
}
