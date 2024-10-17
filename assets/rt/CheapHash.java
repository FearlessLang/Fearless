package rt;

import java.util.Arrays;

public final class CheapHash implements base.CheapHash_0 {
  private int result = 1;

  @Override public Long compute$mut() {
    return (long) result;
  }
  @Override public CheapHash nat$mut(long x) {
    result = ((result << 5) - result) + Long.hashCode(x);
    return this;
  }
  @Override public CheapHash int$mut(long x) {
    result = ((result << 5) - result) + Long.hashCode(x);
    return this;
  }
  @Override public CheapHash float$mut(double x) {
    result = ((result << 5) - result) + Double.hashCode(x);
    return this;
  }
  @Override public CheapHash byte$mut(byte x) {
    result = ((result << 5) - result) + Byte.hashCode(x);
    return this;
  }
  @Override public CheapHash str$mut(rt.Str x) {
    result = ((result << 5) - result) + x.utf8().hashCode();
    return this;
  }
}
