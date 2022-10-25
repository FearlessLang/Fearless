package utils;

@SuppressWarnings("serial")
public class Bug extends RuntimeException{
  public Bug() {super();}
  public Bug(Throwable cause) {super(cause);}
  public Bug(String msg) {super(msg);}
  public Bug(String msg,Throwable cause) {super(msg,cause);}
  public static Bug of(){ return new Bug(); }
  public static Bug of(Throwable cause){ return new Bug(cause); }
  public static Bug of(String msg){ return new Bug(msg); }

  public static <T> T err(){ throw new Bug(); }
  public static <T> T err(String msg){ throw new Bug(msg); }
  
}
