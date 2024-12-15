package base;

import base.caps._System_0;
import rt.Debug;
import rt.Str;
import rt.vpf.ConfigureVPF;
import rt.vpf.VPF;

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

    FAux.LAUNCH_ARGS = buildArgList(args, 1);
    var shutdownVPF = VPF.start(ConfigureVPF.getHeartbeatInterval());
    try {
      myMain.$hash$imm(_System_0.$self);
    } catch (StackOverflowError e) {
      fatal("Program crashed with: Stack overflowed", Debug.demangleStackTrace(e.getStackTrace()));
    } catch (RuntimeException e) {
      var t = e.getCause();
      if (t == null) { t = e; }
      var msg = t.getMessage() == null ? e.getCause() : t.getMessage();
      if (msg instanceof StackOverflowError) {
        msg = "Stack overflowed";
      }
      fatal("Program crashed with: "+msg, Debug.demangleStackTrace(t.getStackTrace()));
    } catch (Throwable t) {
      var msg = t.getMessage() == null ? t.getCause() : t.getMessage();
      fatal("Program crashed with: "+msg, Debug.demangleStackTrace(t.getStackTrace()));
    } finally {
      shutdownVPF.run();
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
      res = res.pushFront$mut(Str.fromJavaStr(args[i]));
    }
    return res;
  }
  private static void fatal(String message) {
    fatal(message, null);
  }
  private static void fatal(String message, String stackTrace) {
    System.err.println(message);
    if (stackTrace != null) { System.err.println("\nStack trace:\n"+stackTrace); }
    System.exit(1);
  }
  public static class FAux { public static base.LList_1 LAUNCH_ARGS; }
}
