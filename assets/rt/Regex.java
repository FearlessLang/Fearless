package rt;

public final class Regex implements base.Regex_0 {
  private final NativeRuntime.Regex inner;
  private final rt.Str pattenStr;
  public Regex(rt.Str patternStr) {
    this.pattenStr = patternStr;
    this.inner = new NativeRuntime.Regex(patternStr.utf8());
  }

  @Override public rt.Str str$read() {
    return pattenStr;
  }

  @Override public base.Bool_0 isMatch$imm(rt.Str str) {
    return this.inner.doesRegexMatch(str.utf8()) ? base.True_0.$self : base.False_0.$self;
  }
}