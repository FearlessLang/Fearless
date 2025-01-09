package program.typesystem;

import java.util.List;
import java.util.stream.Collectors;

import ast.T;
import id.Mdf;
import program.CM;
import vpf.VPFCallMode;

public record TsT(Mdf recv, List<T> ts, T t, CM original, VPFCallMode vpfMode){
  public String toString(){
    return recv+" ("+ts.stream().map(T::toString).collect(Collectors.joining(", "))+"): "+t;
  }

  public TsT withCallMode(VPFCallMode callMode) {
    return new TsT(recv, ts, t, original, callMode);
  }
}