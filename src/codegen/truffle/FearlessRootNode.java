package codegen.truffle;

import codegen.MIR;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import id.Id;
import id.Mdf;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class FearlessRootNode extends RootNode {
  @Child MCallNode entryNode;
  public FearlessRootNode(FearlessLanguage lang, Id.DecId entry) {
    super(lang);
    var entryRecv = new LambdaNode(new MIR.Lambda(
      Mdf.mdf,
      entry,
      "",
      List.of(new Id.IT<>(entry, List.of())),
      List.of(),
      List.of(),
      true
    ));
    this.entryNode = new MCallNode(entryRecv, new FearlessNode[0]);
  }

  @Override public MIR.Lambda execute(VirtualFrame frame) {
    return this.entryNode.executeLambda(frame);
  }
}
