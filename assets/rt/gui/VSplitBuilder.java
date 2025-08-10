package rt.gui;

import javax.swing.JPanel;

import base.MF_2;
import base.gui.VSplitBuilder_0;

public class VSplitBuilder implements VSplitBuilder_0 {

  private JPanel top = new JPanel();
  private JPanel bottom = new JPanel();
  private final GuiBuilderState state;
  
  public VSplitBuilder(GuiBuilderState state) {
    this.state=state;
  }
  @Override
  public VSplitBuilder_0 top$mut(MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder(state);
    gb_m$.$hash$mut(builder);
    this.top = builder.panel(); // Set the configured panel
    return this;
  }

  @Override
  public VSplitBuilder_0 bottom$mut(MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder(state);
    gb_m$.$hash$mut(builder);
    this.bottom = builder.panel(); // Set the configured panel
    return this;
  }

  public JPanel topPanel() {
    return top;
  }

  public JPanel bottomPanel() {
    return bottom;
  }
}
