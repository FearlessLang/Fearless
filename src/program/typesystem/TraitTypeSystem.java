package program.typesystem;

import ast.Program;
import ast.T;
import ast.T.Dec;
import failure.CompileError;
import id.Id;
import id.Id.IT;
import id.Mdf;
import utils.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface TraitTypeSystem {
  Program p();
  static List<CompileError> dsOk(Collection<Dec> ds){
    Map<Id.DecId, Dec> pDs = Mapper.of(c->ds.forEach(e->c.put(e.name(),e)));
    TraitTypeSystem ttt = ()->new Program(pDs);
    return ds.parallelStream().flatMap(di->ttt.dOk(di).stream()).toList();
  }
  default Optional<CompileError> dOk(Dec d){
    var c=d.name();
    var xs=d.gxs();
    var b=d.lambda();
    assert b.selfName().equals("this");
    var cT=new IT<>(c,xs.stream().map(x->new T(Mdf.mdf,x)).toList());
    try{ p().meths(XBs.empty().addBounds(d.gxs(), d.bounds()), Mdf.recMdf, cT,0); }
    catch(CompileError ce){ return Optional.of(ce); }
    assert d.lambda().mdf()==Mdf.mdf;
    // TODO: get XBs
    return ETypeSystem.of(p(),Gamma.empty(), XBs.empty(), Optional.empty(),0).bothT(d).err();
  }
}