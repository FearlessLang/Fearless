package rt.gui;

import javax.swing.JPanel;

import base.MF_2;
import base.gui.HSplitBuilder_0;

public class HSplitBuilder implements HSplitBuilder_0 {

  private JPanel left = new JPanel();
  private JPanel right = new JPanel();
  private final GuiBuilderState state;
  
  public HSplitBuilder(GuiBuilderState state) {
    this.state=state;
  }
  
  @Override
  public HSplitBuilder_0 right$mut(MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder(state);
    gb_m$.$hash$mut(builder);
    this.right = builder.panel(); // Set the configured panel
    return this;
  }

  @Override
  public HSplitBuilder_0 left$mut(MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder(state);
    gb_m$.$hash$mut(builder);
    this.left = builder.panel(); // Set the configured panel
    return this;
  }
  
  public JPanel leftPanel() {
    return left;
  }

  public JPanel rightPanel() {
    return right;
  }

}
