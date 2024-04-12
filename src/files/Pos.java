package files;

import java.net.URI;
import java.util.Objects;

//Note: we plan to soft connect positions with AST node by having a static external hash map
public record Pos(URI fileName, int line, int column) implements java.io.Serializable {
  public static Pos UNKNOWN = Pos.of(URI.create("unknown"), 0, 0);
  public static Pos of(URI fileName, int line, int column){
    return new Pos(fileName,line,column);
  }

  @Override
  public String toString() {
    return fileName + ":" + line() + ":" + column();
  }
  public Pos withFileName(URI fileName) {
    return this.fileName == fileName ? this : new Pos(fileName, this.line, this.column);
  }
  public Pos withLine(int line) {
    return this.line == line ? this : new Pos(this.fileName, line, this.column);
  }
  public Pos withColumn(int column) {
    return this.column == column ? this : new Pos(this.fileName, this.line, column);
  }

  /**
   * We do not consider Pos in any equality or hashing. This will always return true.
   */
  @Override public boolean equals(Object o) {
    return true;
  }
  /**
   * We do not consider Pos in any equality or hashing. This will always return 0.
   */
  @Override public int hashCode() {
    return 0;
  }

  public int realHashCode() {
    return Objects.hash(fileName, line, column);
  }
}
