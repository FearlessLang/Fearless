package rt.gui;

import javax.swing.JComponent;
import javax.swing.JRadioButton;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.RadioButton_0;

public class RadioButton implements RadioButton_0 {

  private final JRadioButton radioButton;
  
  public RadioButton(String text) {this.radioButton = new JRadioButton(text);}

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    radioButton.addActionListener(listener -> listener_m$.$hash$mut());
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    radioButton.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return radioButton.isEnabled()? True_0.$self: False_0.$self;
  }

  @Override
  public Bool_0 isSelected$read() {
    return radioButton.isSelected()?True_0.$self: False_0.$self;
  }

  public JComponent getImpl() {
    return radioButton;
  }
}
