package base;

import rt.NativeRuntime;
import rt.Str;

import java.lang.reflect.Field;

public class FearlessMain {
  public static void main(String[] args) {
    if (args.length == 0) {
      fatal("Missing entry-point. Please type in the name of a type that implements base.Main/0 after the command to launch Fearless.");
    }
    var entryPoint = args[0];
    Main_0 myMain = null; try {myMain = getMain(entryPoint);
    } catch (NoSuchFieldException e) {
      fatal("The provided entry-point '%s' was not a singleton.".formatted(entryPoint));
    } catch (ClassNotFoundException e) {
      fatal("The provided entry-point '%s' does not exist.".formatted(entryPoint));
    } catch (ClassCastException e) {
      fatal("The provided entry-point '%s' does not implement base.Main/0.".formatted(entryPoint));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    assert myMain != null;

    var userArgs = buildArgList(args, 1);
    try {
      NativeRuntime.println(myMain.$hash$imm(userArgs).utf8());
    } catch (StackOverflowError e) {
      fatal("Program crashed with: Stack overflowed");
    }
    catch (Throwable t) {
      var msg = t.getMessage() == null ? t.getCause() : t.getMessage();
      fatal("Program crashed with: "+msg);
    }
  }
  public static Main_0 getMain(String mainName) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, ClassCastException {
    Class<?> clazz = Class.forName(mainName+"_0Impl");
    Field f = clazz.getField("$self");
    return (Main_0) f.get(null);
  }
  private static LList_1 buildArgList(String[] args, int offset) {
    var res = LList_1.$self;
    for (int i = args.length - 1; i >= offset; --i) {
      res = res.pushFront$imm(Str.fromJavaStr(args[i]));
    }
    return res;
  }
  private static void fatal(String message) {
    System.err.println(message);
    System.exit(1);
  }
}
