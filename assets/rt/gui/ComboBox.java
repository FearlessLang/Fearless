package rt.gui;

import java.util.Objects;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.ComboBox_0;

public class ComboBox implements ComboBox_0 {

  private final JComboBox<Object> comboBox;
  private GuiBuilderState state;
  
  public ComboBox(Object[] items, GuiBuilderState state) {
    this.comboBox = new JComboBox<>(Objects.requireNonNull(items));
    this.state = state;
  }
  
  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    comboBox.addActionListener(e -> state.submitModelTask(listener_m$::$hash$mut));
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    comboBox.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return comboBox.isEnabled()? True_0.$self: False_0.$self; 
  }
  
  public JComponent getImpl() {
    return comboBox;
  }

  @Override
  public Long selectedItem$read() {
    return Long.valueOf(comboBox.getSelectedIndex());
  }

}
