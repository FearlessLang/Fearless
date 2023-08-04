package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Mapper {
  public static <K,V> Map<K,V> of(Consumer<HashMap<K,V>> c){
    return Collections.unmodifiableMap(ofMut(c));
  }
  public static <K,V> Map<K,V> of(Map<K,V> base, Consumer<HashMap<K,V>> c){
    return Collections.unmodifiableMap(ofMut(base, c));
  }
  public static <K,V> HashMap<K,V> ofMut(Consumer<HashMap<K,V>> c){
    return ofMut(Map.of(), c);
  }
  public static <K,V> HashMap<K,V> ofMut(Map<K,V> base, Consumer<HashMap<K,V>> c){
    HashMap<K,V> hm=new HashMap<>(base);
    c.accept(hm);
    return hm;
  }
}
