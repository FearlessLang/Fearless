package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
  @SafeVarargs
  public static <T> Stream<T> of(Stream<T>...ss){ return Stream.of(ss).flatMap(s->s); }

  public static <A,B> Zipper<A,B> zip(List<A> as, List<B> bs){
    assert as.size()==bs.size();
    return new ListZipper<>(as,bs);
  }
  public static <A,B> Zipper<A,B> zip(A[] as, B[] bs){
    assert as.length==bs.length;
    return new ArrayZipper<>(as,bs);
  }

  private record ListZipper<A,B>(List<A> as, List<B> bs) implements Zipper<A, B> {
    @Override public void forEach(BiConsumer<A, B> f){
      IntStream.range(0, as.size()).forEach(i->f.accept(as.get(i), bs.get(i)));
    }
    @Override public <R> Stream<R> map(BiFunction<A, B, R> f){
      return IntStream.range(0, as.size()).mapToObj(i->f.apply(as.get(i),bs.get(i)));
    }
    @Override public <R> Stream<R> parallelMap(BiFunction<A, B, R> f){
      return IntStream.range(0, as.size()).parallel().mapToObj(i->f.apply(as.get(i),bs.get(i)));
    }
    @Override public <R> Stream<R> flatMap(BiFunction<A, B, Stream<R>> f){
      return IntStream.range(0, as.size()).boxed().flatMap(i->f.apply(as.get(i),bs.get(i)));
    }
    @Override public <R> Stream<R> filterMap(BiFunction<A, B, Optional<R>> f){
      return IntStream.range(0, as.size())
        .mapToObj(i->f.apply(as.get(i),bs.get(i)))
        .filter(Optional::isPresent)
        .map(Optional::get);
    }
    @Override public Zipper<A,B> filter(BiPredicate<A, B> f){
      var asi = new ArrayList<A>();
      var bsi = new ArrayList<B>();
      IntStream.range(0, as.size())
        .filter(i->f.test(as.get(i),bs.get(i)))
        .forEachOrdered(i->{
          asi.add(as.get(i));
          bsi.add(bs.get(i));
        });
      return new ListZipper<>(asi, bsi);
    }
    @Override public <R> R fold(Acc<R, A, B> folder, R initial) {
      Box<R> acc = new Box<>(initial);
      IntStream.range(0, as.size())
        .forEach(i->acc.set(folder.apply(acc.get(), as.get(i), bs.get(i))));
      return acc.get();
    }
    @Override public boolean anyMatch(BiPredicate<A, B> test){
      return IntStream.range(0, as.size())
        .anyMatch(i->test.test(as.get(i),bs.get(i)));
    }
    @Override public boolean allMatch(BiPredicate<A, B> test){
      return IntStream.range(0, as.size())
        .allMatch(i->test.test(as.get(i),bs.get(i)));
    }
    @Override public boolean allMatchParallel(BiPredicate<A, B> test){
      return IntStream.range(0, as.size())
        .parallel()
        .allMatch(i->test.test(as.get(i),bs.get(i)));
    }
  }
  private record ArrayZipper<A,B>(A[] as, B[] bs) implements Zipper<A,B> {
    @Override public void forEach(BiConsumer<A, B> f){
      IntStream.range(0, as.length).forEach(i->f.accept(as[i], bs[i]));
    }
    @Override public <R> Stream<R> map(BiFunction<A, B, R> f){
      return IntStream.range(0, as.length).mapToObj(i->f.apply(as[i],bs[i]));
    }
    @Override public <R> Stream<R> parallelMap(BiFunction<A, B, R> f){
      return IntStream.range(0, as.length).parallel().mapToObj(i->f.apply(as[i],bs[i]));
    }
    @Override public <R> Stream<R> flatMap(BiFunction<A, B, Stream<R>> f){
      return IntStream.range(0, as.length).boxed().flatMap(i->f.apply(as[i],bs[i]));
    }
    @Override public <R> Stream<R> filterMap(BiFunction<A, B, Optional<R>> f){
      return IntStream.range(0, as.length)
        .mapToObj(i->f.apply(as[i],bs[i]))
        .filter(Optional::isPresent)
        .map(Optional::get);
    }
    @Override public Zipper<A,B> filter(BiPredicate<A, B> f){
      var asi = new ArrayList<A>();
      var bsi = new ArrayList<B>();
      IntStream.range(0, as.length)
        .filter(i->f.test(as[i],bs[i]))
        .forEachOrdered(i->{
          asi.add(as[i]);
          bsi.add(bs[i]);
        });
      return new ListZipper<>(asi, bsi);
    }
    @Override public <R> R fold(Acc<R, A, B> folder, R initial) {
      Box<R> acc = new Box<>(initial);
      IntStream.range(0, as.length)
        .forEach(i->acc.set(folder.apply(acc.get(), as[i], bs[i])));
      return acc.get();
    }
    @Override public boolean anyMatch(BiPredicate<A, B> test){
      return IntStream.range(0, as.length)
        .anyMatch(i->test.test(as[i],bs[i]));
    }
    @Override public boolean allMatch(BiPredicate<A, B> test){
      return IntStream.range(0, as.length)
        .allMatch(i->test.test(as[i],bs[i]));
    }
    @Override public boolean allMatchParallel(BiPredicate<A, B> test){
      return IntStream.range(0, as.length)
        .parallel()
        .allMatch(i->test.test(as[i],bs[i]));
    }
  }

  public static <T> Optional<Integer> firstPos(List<T> xs, Predicate<Integer> p) {
    return IntStream.range(0, xs.size()).boxed()
      .filter(p)
      .findFirst();
  }

  public static <T> Optional<Integer> firstPos(int start, List<T> xs, Predicate<Integer> p) {
    assert start <= xs.size();
    return IntStream.range(start, xs.size()).boxed()
      .filter(p)
      .findFirst();
  }

  public interface Acc<R,A,B> {
    R apply(R acc, A a, B b);
  }
}
