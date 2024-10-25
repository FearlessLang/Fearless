package errmsg.typeSystem;

import ast.T;
import failure.CompileError;
import id.Id;
import id.Mdf;
import program.CM;
import program.Program;
import program.typesystem.XBs;
import utils.Bug;

import java.util.*;
import java.util.stream.Collectors;

public class TypeSystemMsg {

  /**
   * TODO: Current Limitations
   * Levenshtein distance not the best method.
   * Does not verify whether suggested methods are actually valid to call.
   * Does not take return types into account.
   */
  public static String undefinedMeth(CompileError rawError, Program p) {
    ast.T recvT = switch (rawError.attributes.get("recvT")) {
      case astFull.T t -> t.toAstT();
      case ast.T t -> t;
      default -> throw Bug.unreachable();
    };
    var name = (Id.MethName) rawError.attributes.get("name");
    var astP = (ast.Program) p;
    List<CM> ms = new ArrayList<>();
    for (T.Dec d : astP.ds().values()) {
      ms.addAll(astP.meths(XBs.empty(), Mdf.recMdf, d.toIT(), 0));
    }
    List<CM> sortedMs = sortMethodSimilarity(name, ms, recvT);
    String message = getMessage(rawError, recvT, sortedMs.removeFirst());
    return getMessageHeader(rawError) + message + getOtherSuggestedMethods(sortedMs);
  }

  private static String getMessageHeader(CompileError rawError) {
    assert rawError.pos().isPresent();
    return  "In position %s\n[E%d %s]\n".formatted(rawError.pos().get(), rawError.code(), rawError.name());
  }

  private static String getOtherSuggestedMethods(List<CM> sortedMs) {
    StringBuilder suggestions = new StringBuilder("\n\nOther candidates:");
    for(var meth: sortedMs) {
      String args = "(";
      args += meth.sig().ts().stream().map(T::toString).collect(Collectors.joining(", "));
      args += "): ";
      args += meth.sig().ret().toString();
      suggestions.append("\n");
      suggestions.append(meth.c()).append(meth.name().name()).append(args);
    }
    return suggestions.toString();
  }

  /**
   * TODO: Make message even clearer.
   * Currently displays full name of method. Ex. "test.A[].meth2(imm test.B[], imm test.B[]): imm test.A[]"
   * Maybe something like "A.meth2(B, B)" is better? Should return type also be included?
   */
  private static String getMessage(CompileError rawError, ast.T recvT, CM suggestMeth) {
    Id.MethName name = (Id.MethName) rawError.attributes.get("name");
    // Not sure if there is a way to get arg types from incorrect method.
    return "Method <%s> with %d args does not exist in <%s>".formatted(name.name(), name.num(), recvT)
      + "\nDid you mean <" + suggestMeth.c().name().name() +suggestMeth.name().name()
      + "(" + suggestMeth.sig().ts().stream().map(T::toString).collect(Collectors.joining(", ")) + ")" + ">";
  }

  /**
   * Returns a sorted list of methods based on how similar they are to the user typed method.
   * Methods with same name and recv type will always be prioritised, sorted based on number of arguments.
   * Otherwise sorted based on name similarity.
   * TODO: Better similarity ranking system.
   * Levenshtein distance is not the most effective for ranking name similarity.
   * For example "k" is the exact same similarity as "method2" when given "meh1" as input.
   */
  private static List<CM> sortMethodSimilarity(Id.MethName name, List<CM> ms, ast.T recvT) {
    ArrayList<CM> newList = new ArrayList<>(ms);
    newList.sort(Comparator.comparingDouble((cm)-> {
      double jaro = jaroWinkler(name.name(), cm.name().name());
      // Prioritizes methods from the same receiver type
      // Prioritizes methods with closer number of arguments
      // TODO: This is a hacky way to do this, maybe there is a better method.
      int argsPenalty = Math.abs(cm.name().num() - name.num());
      double typeSimilarity = jaroWinkler(recvT.rt().toString(), cm.c().toString());
      if (jaro + typeSimilarity == 2.0) {
        return argsPenalty*-1 + 10;
      }
      // +50 to prioritize methods with same name and receiver, i*2 to add more weight to name similarity than arg no.
      return jaro + typeSimilarity;
    }));
    return newList.reversed();
  }

  private static int levenshteinDist(String a, String b) {
    int aLen = a.length();
    int bLen = b.length();
    // If either string are empty, min edit cost will be length of other string.
    // This should never happen for method names, but still here if this method needs to be used for other purposes.
    if (aLen == 0) {return bLen;}
    if (bLen == 0) {return aLen;}
    // Swap so b is always the longer string
    if (aLen > bLen) {
      return levenshteinDist(b, a);
    }
    // Unnecessary to store full 2d matrix, only need last array.
    int[] prevRow = new int[aLen + 1];
    for (int i=0; i<=aLen; i++) {
      prevRow[i] = i;
    }
    // Update rows
    for (int j=1; j<=bLen; j++) {
      prevRow = updateRow(a, b, aLen, j, prevRow);
    }
    return prevRow[aLen];
  }

  private static int[] updateRow(String a, String b, int aLen, int bIndex, int[] prevRow) {
    int[] crntRow = new int[aLen + 1];
    crntRow[0] = bIndex;
    int prevVal = prevRow[0];
    for (int i=1; i<=aLen; i++) {
      int cost = (a.charAt(i-1) == b.charAt(bIndex - 1)) ? 0 : 1;
      int newVal = Math.min(Math.min(crntRow[i-1]+1, prevRow[i]+1), prevVal+cost);
      prevVal = prevRow[i];
      crntRow[i] = newVal;
    }
    return crntRow;
  }

  private static double jaroWinkler(String s1, String s2) {
    if (s1.equals(s2)) {return 1.0;}
    if (s1.isBlank() || s2.isBlank()) {return 0.0;}

    int maxDist = Math.max(s1.length(), s2.length())/2 - 1;
    int match = 0;

    boolean[] s1Hash = new boolean [s1.length()];
    boolean[] s2Hash = new boolean [s2.length()];

    for (int i=0; i<s1.length(); i++) {
      int start = Math.max(0, i - maxDist);
      int end = Math.min(s2.length()-1, i + maxDist);
      for (int j=start; j<=end; j++) {
        if(s2Hash[j]) {continue;}
        if(s1.charAt(i) != s2.charAt(j)) {continue;}
        s1Hash[i] = true;
        s2Hash[j] = true;
        match++;
        break;
      }
    }
    if (match == 0) {return 0.0;}

    int k = 0;
    double t = 0;
    for (int i=0; i<s1.length(); i++) {
      if (!s1Hash[i]) {continue;}
      while (!s2Hash[k]) {k++;}
      if (s1.charAt(i) != s2.charAt(k)) {
        t++;
      }
      k++;
    }
    t /= 2;

    double jaro = ((double)match/s1.length() + (double)match/s2.length() + (double)(match-t)/match)/3.0;

    int prefix = 0;
    int maxPrefixLength = 4;
    for (int i=0; i<Math.min(s1.length(), s2.length()); i++) {
      if (s1.charAt(i) == s2.charAt(i)) {
        prefix++;
      } else {
        break;
      }
    }
    prefix = Math.min(prefix, maxPrefixLength);

    double scalingFactor = 0.1;
    return jaro + prefix * scalingFactor * (1-jaro);
  }
}
