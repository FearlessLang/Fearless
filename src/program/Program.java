package program;

import astFull.E;
import astFull.T;
import id.Id;
import magic.Magic;
import utils.Bug;
import utils.Range;
import visitors.FullCloneVisitor;
import visitors.FullCollectorVisitor;

import java.util.*;
import java.util.stream.Collectors;

public class Program {
  private final Map<Id.DecId, T.Dec> ds;
  public Program(Map<Id.DecId, T.Dec> ds) { this.ds = ds; }

  T.Dec of(Id.DecId d) {
    var res = ds.get(d);
    if (res == null) { res = Magic.getDec(d); }
    assert res != null;
    return res;
  }

  T.Dec of(Id.IT<T> t) {
    throw Bug.todo();
  }

  public Map<Id.DecId, T.Dec> ds() { return this.ds; }

  private final HashMap<Id.DecId, Set<Id.DecId>> superDecIdsCache = new HashMap<>();
  public Set<Id.DecId> superDecIds(Id.DecId start) {
    if (superDecIdsCache.containsKey(start)) { return superDecIdsCache.get(start); }

    HashSet<Id.DecId> visited = new HashSet<>();
    superDecIds(visited, start);
    var res = Collections.unmodifiableSet(visited);
    superDecIdsCache.put(start, res);
    return res;
  }

  public void superDecIds(HashSet<Id.DecId> visited, Id.DecId current) {
    if (superDecIdsCache.containsKey(current)) {
      visited.addAll(superDecIdsCache.get(current));
      return;
    }

    var currentDec = of(current);
    for(var it : currentDec.lambda().its()) {
      var novel=visited.add(it.name());
      if(novel){ superDecIds(visited, it.name()); }
    }
  }

  /**
   * Applies inference 5a
   */
  public Program inferSignatures() {
    var is=new InferSignatures(this);
    for(int i: Range.of(is.decs)){
      var di = is.inferSignatures(is.decs.get(i));
      is.updateDec(di,i);
    }
    return is.p;
  }
  List<E.Meth> dom(Id.DecId id){ return this.of(id).lambda().meths(); }

  public static class InferSignatures {
    List<T.Dec> decs;
    Program p;
    InferSignatures(Program p){ this.p=p; this.decs = orderDecs(p.ds().values()); }
    private List<T.Dec> orderDecs(Collection<T.Dec>ds){
      return ds.stream().sorted().collect(Collectors.toList());
    }
    private int sortDec(T.Dec d1,T.Dec d2){
      if(p.superDecIds(d1.name()).contains(d2.name())) { return -1; }
      if(p.superDecIds(d2.name()).contains(d1.name())) { return 1; }
      return 0;
    }
    private void updateDec(T.Dec d, int i) {
      decs.set(i,d);
      p=new Program(decs.stream().collect(Collectors.toMap(T.Dec::name, di->di)));
      }

    private T.Dec inferSignatures(T.Dec d) {
      return d.withLambda(inferSignatures(d, d.lambda()));
    }
    private E.Lambda inferSignatures(T.Dec dec, E.Lambda l) {
      if (l.selfName() == null) {
        l = l.withSelfName(new E.X(T.infer).name());
      }
      var ms = l.meths().stream().map(m->inferSignature(dec,m)).toList();
      return l.withMeths(ms);
    }
    Id.MethName onlyAbs(T.Dec dec){
      var absMeths = dec.lambda().meths().stream().filter(m->m.body().isEmpty()).toList();
      assert absMeths.size() == 1;
      return absMeths.get(0).name().orElseThrow();
    }
    E.Meth inferSignature(T.Dec dec, E.Meth m) {
      if (m.sig().isPresent()) { return m; }
      var name=m.name().orElse(onlyAbs(dec));
      assert name.num()==m.xs().size();
      var decIds=p.superDecIds(dec.name());
      for(var decId:decIds){
        var candidates = p.dom(decId).stream()
          .filter(mi->mi.name().equals(Optional.of(name)))
          .map(mi->mi.sig().get())
          .distinct()
          .toList();
        if(candidates.size()!=1){throw Bug.todo(/*better err*/);}
        return m.withSig(candidates.get(0));
      }
/* m(xs) is just using the length of xs and the name (m) to extract the method
  Ds,C[Xs]|-m(xs)->e => sig->e, // task 1
  where
    exists sig such that
      forall IT in supertypes(C[Xs]) such that m(xs) in dom(Ds,IT)
        sig.xs = xs
        Ds|-meths(IT)(m(xs)) compatible with sig //that is, equal except for sig.xs

A:{ m:A }
B:A{ m:B }
C:B{m->e}
exists sig s
    s ==m:A, s==m:B
*/
      throw Bug.todo();
    }
  }
  @Override public String toString() { return this.ds().toString(); }
}
