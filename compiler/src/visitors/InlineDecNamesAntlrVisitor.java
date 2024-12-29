package visitors;

import astFull.T;
import failure.Fail;
import files.Pos;
import generated.FearlessBaseVisitor;
import generated.FearlessParser;
import generated.FearlessParser.TopDecContext;
import id.Id;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class InlineDecNamesAntlrVisitor extends FearlessBaseVisitor<Void> {
  public final Path fileName;
  public final List<T.Alias> inlineDecs = new ArrayList<>();
  private String pkg;
  public InlineDecNamesAntlrVisitor(String pkg, Path fileName) {
    this.fileName = fileName;
    this.pkg = pkg;
  }

  void topDecCollect(TopDecContext decCtx){
    Pos pos=Pos.of(fileName.toUri(),decCtx.getStart().getLine(), decCtx.getStart().getCharPositionInLine()); 
    String cName = decCtx.fullCN().getText();
    if (cName.contains(".")){ throw Fail.crossPackageDeclaration().pos(pos); }
    var longName = pkg + "." +cName;
    var inT=new Id.IT<T>(longName, List.of());
    inlineDecs.add(new T.Alias(inT, cName, Optional.ofNullable(pos)));
  }
  @Override public Void visitTopDec(FearlessParser.TopDecContext ctx) {    
    if (ctx!=null){ topDecCollect(ctx); }
    return super.visitTopDec(ctx);
  }
}
