package astFull;

import generated.FearlessParser.TopDecContext;
import id.Id;
import magic.Magic;
import failure.Fail;
import utils.Range;
import utils.Streams;
import visitors.FullEAntlrVisitor;
import wellFormedness.WellFormednessFullShortCircuitVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

public record Package(
    String name,
    List<T.Alias> as,
    List<TopDecContext> ds,
    List<Path> ps
    ){
  public Map<Id.DecId,T.Dec> parse(){
    var res = new HashMap<Id.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i, false));
    return Collections.unmodifiableMap(res);
  }
  public Collection<T.Dec> shallowParse(){
    var res = new HashMap<Id.DecId,T.Dec>();
    IntStream.range(0, this.ds().size()).forEach(i->this.acc(res, i, true));
    return res.values();
  }
  private void acc(Map<Id.DecId,T.Dec> acc, int i, boolean shallow){
    Path pi = this.ps().get(i);
    TopDecContext di=this.ds().get(i);
    T.Dec dec=new FullEAntlrVisitor(pi,this::resolve).visitTopDec(di, this.name(), shallow);
    acc.put(dec.name(), dec);
  }
  Optional<Id.IT<T>> resolve(String base){
    return Magic.resolve(base)
      .or(()->this.as.stream()
        .filter(a -> base.equals(a.to()))
        .findAny()
        .map(T.Alias::from));
  }
  public static Package merge(List<T.Alias>global,List<Package>ps) {
    assert checks(global, ps);
    var wellFormednessVisitor = new WellFormednessFullShortCircuitVisitor();
    topDecDisj(ps);
    var allAliases = mergeAlias(global, ps);
    aliasDisj(allAliases);
    allAliases.stream()
      .map(wellFormednessVisitor::visitAlias)
      .dropWhile(Optional::isEmpty)
      .findFirst()
      .flatMap(o->o)
      .ifPresent(err->{ throw err; });

    var decls = ps.stream().flatMap(p -> p.ds().stream()).toList();
    var paths = ps.stream().flatMap(p -> p.ps().stream()).toList();
    return new Package(ps.get(0).name(), allAliases, decls, paths);
  }
  static List<T.Alias> mergeAlias(List<T.Alias>global, List<Package>ps){
    return Streams.of(
      global.stream(),
      ps.stream().flatMap(p->p.as().stream()),
      ps.stream().flatMap(p->p.shallowParse().stream()
        .map(d->d.name())
        .map(n->{
          assert n.name().startsWith(p.name());
          var shortName = n.name().substring(p.name().length()+1);
          assert !shortName.contains(".");
          return new T.Alias(new Id.IT<>(new Id.DecId(n.name(), 0), List.of()), shortName, Optional.empty());
        })
        .distinct()
      )
    ).toList();
  }
  static void aliasDisj(List<T.Alias> all){
    var seen = new HashSet<String>(all.size());
    for(var a : all){
      var aliased = a.to();
      if(seen.add(aliased)){ continue; }
      var conflicts = all.stream()
        .filter(al->al.to().equals(aliased))
        .map(al->Fail.conflict(al.posOrUnknown(), a.toString()))
        .toList();
      throw Fail.conflictingAlias(aliased, conflicts);
    }
  }
  static void topDecDisj(List<Package>ps){
    var ds=ps.stream()
        .flatMap(p->p.ds().stream())
        .map(d->{
          int size=0;
          if(d.mGen()!=null && d.mGen().genDecl()!=null){ size=d.mGen().genDecl().size(); }
          return new Id.DecId(d.fullCN().getText(),size);
          })
        .toList();
    var fns=ps.stream()
      .flatMap(p->Streams
        .zip(p.ds(),p.ps()).map((di,pi)->FullEAntlrVisitor.pos(pi, di))
         )
      .toList();
    assert ds.size()==fns.size();
    var uds=ds.stream().distinct().toList();
    if(uds.size()==ds.size()){ return; }
    var seen = new HashSet<Id.DecId>(ds.size());
    for(var i:Range.of(ds)){
      Id.DecId di=ds.get(i);
      if(seen.add(di)){ continue; }
      List<Fail.Conflict> conflicts = Streams.zip(ds,fns).filterMap((dj,fj)->{
        if(!dj.equals(di)){ return Optional.empty(); }
        return Optional.of(Fail.conflict(fj, di.toString()));
        }).toList();
      throw Fail.conflictingDecl(di, conflicts);
    }
  }

  static boolean checks(List<T.Alias>global,List<Package>ps){
    assert !ps.isEmpty();
    var n=ps.get(0).name();
    assert ps.stream().allMatch(p->p.name().equals(n));    
    return true;
  }
}