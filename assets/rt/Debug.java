package rt;

import base.Void_0;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Debug implements base.Debug_0 {
  public static final Debug $self = new Debug();

  @Override public Void_0 println$imm(Object x) {
    NativeRuntime.printlnErr(toStr(x).utf8());
    return Void_0.$self;
  }

  @Override public Str identify$imm(Object x) {
    return demangle(x);
  }

  @Override public Object $hash$imm(Object x) {
    NativeRuntime.printlnErr(toStr(x).utf8());
    return x;
  }

  private static Str toStr(Object x) {
    var strMethod = java.util.Arrays.stream(x.getClass().getMethods())
      .filter(meth->
        (meth.getName().equals("str$read") || meth.getName().equals("str$readH"))
          && meth.getReturnType().equals(rt.Str.class)
          && meth.getParameterCount() == 0)
      .findAny();
    if (strMethod.isPresent()) {
      try {
        return (Str)strMethod.get().invoke(x);
      } catch(java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException err) {
        return demangle(x);
      }
    } else {
      return demangle(x);
    }
  }

  public static String demangleStackTrace(StackTraceElement[] stackTrace) {
    return Arrays.stream(stackTrace)
      .map(frame -> {
        var typeName = typeNameFromClassName(frame.getClassName()).orElse("<runtime %s>".formatted(frame.getClassName()));
        // TODO: method names etc.
        return typeName;
      })
      .collect(Collectors.joining("\n"));
  }

  private static final Pattern FEARLESS_TYPE_NAME = Pattern.compile("(.+)_(\\d+)(Impl)?(\\[.*])?$");
  private static Str demangle(Object x) {
    var strValue = x.toString();
    if (!strValue.matches(FEARLESS_TYPE_NAME.pattern())) {
      return Str.fromJavaStr(strValue);
    }

    var className = x.getClass().getName();
    var typeName = typeNameFromClassName(className)
      .orElseThrow(() -> new RuntimeException("Cannot extract type name from "+strValue));
    return Str.fromJavaStr(typeName);
  }
  private static Optional<String> typeNameFromClassName(String className) {
    var matcher = FEARLESS_TYPE_NAME.matcher(className);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    var typeName = matcher.group(1);
    var nGens = Integer.parseInt(matcher.group(2), 10);
    return (typeName+"/"+nGens).describeConstable();
  }
}
