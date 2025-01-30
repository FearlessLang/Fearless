package rt.gui;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.PasswordField_0;

public class PasswordField implements PasswordField_0 {

  private final JPasswordField passwordField;
  
  public PasswordField(String text) {this.passwordField = new JPasswordField(text);}

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    passwordField.addActionListener(listener -> listener_m$.$hash$mut());
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
}
