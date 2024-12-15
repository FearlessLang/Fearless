package rt;

import base.Void_0;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BlackBox implements base.benchmarking.BlackBox_0 {
  public static final BlackBox $self = new BlackBox();
  @Override public Void_0 consumeCpu$imm(long tokens_m$) {
    var handle = getBlackHoleMethod("consumeCPU", long.class);
    try {
      // static method
      handle.method.invoke(null, tokens_m$);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new RuntimeException("Error running consumeCPU: "+e);
    }
    return Void_0.$self;
  }

  @Override public Void_0 $hash$imm(Object value_m$) {
    var handle = getBlackHoleMethod("consume", Object.class);
    try {
      handle.method.invoke(handle.blackHole, value_m$);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return Void_0.$self;
  }

  record SelectedMethod(Object blackHole, Method method) {}
  private SelectedMethod getBlackHoleMethod(String name, Class<?>... paramTypes) {
    try {
      Class<?> clazz = Class.forName("fearlessFFI.FearlessBenchmarkFFI");
      Field blackHole = clazz.getField("BLACKHOLE");
      var blackHoleScopedValue = blackHole.get(null);
      var blackHoleInstance = blackHoleScopedValue
        .getClass()
        .getMethod("get")
        .invoke(blackHoleScopedValue);
      var method = blackHoleInstance.getClass().getMethod(name, paramTypes);
      return new SelectedMethod(blackHoleInstance, method);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load fearlessFFI.FearlessBenchmarkFFI. Are you in a benchmarking environment?");
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Could not load fearlessFFI.FearlessBenchmarkFFI.BLACKHOLE.");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Could not find method "+name+": "+e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    }
  }
}
