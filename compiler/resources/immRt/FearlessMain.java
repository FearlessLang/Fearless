package base;

import rt.NativeRuntime;
import rt.Str;

import java.lang.reflect.Field;

public class FearlessMain {
  public static void main(String[] args) {
    Main_0 myMain = null; try {myMain = getMain(args[0]);
    } catch (NoSuchFieldException e) {
      fatal("The provided entry-point '%s' was not a singleton.".formatted(e));
    } catch (ClassNotFoundException e) {
      fatal("The provided entry-point '%s' does not exist.".formatted(e));
    } catch (ClassCastException e) {
      fatal("The provided entry-point '%s' does not implement base.Main/0.".formatted(e));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    assert myMain != null;

    var userArgs = buildArgList(args, 1);
    try {
      NativeRuntime.println(myMain.$hash$imm(userArgs).utf8());
    } catch (StackOverflowError e) {
      fatal("Program crashed with Stack overflow");
    }
    catch (Throwable t) {
      fatal("Program crashed with: "+t.getMessage());
    }
  }
  public static Main_0 getMain(String mainName) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, ClassCastException {
    Class<?> clazz = Class.forName(mainName+"_0Impl");
    Field f = clazz.getField("$self");
    return (Main_0) f.get(null);
  }
  private static LList_1 buildArgList(String[] args, int offset) {
    var res = LList_1.$self;
    for (int i = offset; i < args.length; ++i) {
      res = res.pushFront$imm(Str.fromJavaStr(args[i]));
    }
    return res;
  }
  private static void fatal(String message) {
    System.err.println(message);
    System.exit(1);
  }
}
