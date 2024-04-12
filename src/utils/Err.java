package utils;

import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Err {
  public static <T> boolean ifMut(List<T> t){
    try{ t.add(null); }
    catch(UnsupportedOperationException uoe){ return true; }
    return false;
  }
  public static String hole="[###]";//not contains \.[]{}()<>*+-=!?^$|
  public static boolean strCmpFormat(String expected,String actual){
    expected=expected.replace("\n","").replace(" ","");
    actual=actual.replace("\n","").replace(" ","");
    return strCmp(expected,actual);
  }
  public static boolean strCmp(String expected,String actual){
    if (expected == null || actual == null) {
      assertEquals(expected,actual);
      throw Bug.of();
    }
    actual = actual.trim();
    expected = expected.trim();
    try{assertTrue(strCmpAux(expected,actual,Err.hole));}
    catch(AssertionFailedError e){
      assertEquals(expected,actual);
      throw Bug.of();
      }
    return true;
    }
  public static String trimExpression(String e){
    if(e.length()<50){return e;}
    String start=e.substring(0,24);
    String end=e.substring(e.length()-24,e.length());
    return start+"[...]"+end;
    }
  public static boolean strCmpAux(String cmp2, String cmp1, String stringHole) {
    if(cmp2.isEmpty()){ return cmp1.isEmpty(); }
    List<String> split = new ArrayList<String>(List.of(cmp2.split(Pattern.quote(stringHole))));
    for(int i = 0; i < split.size(); i++){ if(split.get(i).length() == 0) split.remove(i--); }
    boolean beginswith = cmp2.startsWith(stringHole);
    boolean endswith = cmp2.endsWith(stringHole);    
    int holes = (beginswith ? 1 : 0) + (endswith ? 1 : 0) + split.size() - 1;    
    //Trivial
    if(holes == 0) { return cmp1.equals(cmp2); }    
    //If we didn't start with a hole, compare everything up to the hole
    if(!beginswith) {
      String startCmp2 = split.get(0);      
      //The thing before the hole is bigger than the entire string!
      if(startCmp2.length() > cmp1.length()) { return false; }      
      String startCmp1 = cmp1.substring(0, startCmp2.length());      
      if(!startCmp1.equals(startCmp2)) { return false; }      
      cmp1 = cmp1.substring(startCmp2.length());
      split.remove(0);
    }    
    //If we didn't start with a hole, compare everything up to the hole
    if(!endswith){
      String endCmp2 = split.get(split.size() - 1);      
      //The thing after the hole is bigger than the entire (remaining) string!
      if(endCmp2.length() > cmp1.length()){ return false; }      
      String endCmp1 = cmp1.substring(cmp1.length() - endCmp2.length(), cmp1.length());      
      if(!endCmp1.equals(endCmp2)){ return false; }      
      cmp1 = cmp1.substring(0, cmp1.length() - endCmp2.length());
      split.remove(split.size() - 1);
    }    
    //Everything on both sides of the hole matches, therefore the hole matches everything in the center
    //Or, alternatively, the single hole was at the end, and therefore everything but the whole matched the beginning or the end of cmp1
    if(holes == 1){ return true; }    
    //We have a string in between two holes, and all text outside the holes has been removed from both cmp1 and cmp2
    //Check that the middle text exists within cmp1. If it does, all outer text can be considered part of the holes and we have a match
    if(holes == 2){ return cmp1.contains(split.get(0)); }
    if(holes > 2){
      //Guess how much space the first hole takes up by iteratively searching for the middle text
      int index = 0;
      while((index = cmp1.indexOf(split.get(0))) > -1) {
        cmp1 = cmp1.substring(index);
        if(strCmpAux(reconstituteRight(split,stringHole),cmp1,stringHole)) { return true; }
        cmp1 = cmp1.substring(split.get(0).length());
        }
      }
    return false;
    }
  private static String reconstituteRight(List<String> list,String stringHole){
    StringBuilder builder = new StringBuilder();
    for(int i = 0; i < list.size(); i++){
      builder.append(list.get(i));
      builder.append(stringHole);
    }
    return builder.toString();
  }
}