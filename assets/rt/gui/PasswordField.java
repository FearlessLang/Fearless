package rt.gui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.PasswordField_0;

public class PasswordField implements PasswordField_0 {

  private final JPasswordField passwordField;
  private GuiBuilderState state;
  
  public PasswordField(String text, GuiBuilderState state) {
    this.passwordField = new JPasswordField(text);
    this.state = state;
  }

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    passwordField.addActionListener(listener -> state.submitModelTask(listener_m$::$hash$mut));
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    passwordField.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return passwordField.isEnabled()? True_0.$self: False_0.$self; 
  }

  public JComponent getImpl() {
    return passwordField;
  }

  @Override
  public Bool_0 getFocus$read() {
    return passwordField.isFocusOwner()? True_0.$self: False_0.$self;
  }

  @Override
  public Void_0 setFocus$mut(Bool_0 focus_m$) {
    if(focus_m$ ==True_0.$self){passwordField.requestFocusInWindow();}
    else {
      JPanel topPanel =(JPanel)state.topPanel;
      topPanel.requestFocusInWindow();}
    return Void_0.$self;
  }
}
