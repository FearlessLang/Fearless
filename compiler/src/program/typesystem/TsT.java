package program.typesystem;

import java.util.List;
import java.util.stream.Collectors;

import ast.T;
import id.Mdf;
import program.CM;

public record TsT(XBs xbs, Mdf recv, List<T> ts, T t, CM original){
  public String toString(){
    return recv+" ("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
  }
}