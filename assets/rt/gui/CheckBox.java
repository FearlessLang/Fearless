package rt.gui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.CheckBox_0;

public class CheckBox implements CheckBox_0 {
  private final JCheckBox checkBox;
  private GuiBuilderState state;
  
  public CheckBox(String text,GuiBuilderState state) {
    this.checkBox = new JCheckBox(text);
    this.state = state;
  }
  
  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    checkBox.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return checkBox.isEnabled()? True_0.$self: False_0.$self; 
  }
  
  public Void_0 addActionListener$mut(MF_1 listener) {
    checkBox.addActionListener(e -> state.submitModelTask(listener::$hash$mut));
    return Void_0.$self;
  }
  
  public JComponent getImpl() {
    return checkBox;
  }

  @Override
  public Bool_0 isSelected$read() {
    return checkBox.isSelected()?True_0.$self: False_0.$self;
  }

}
