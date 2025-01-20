package rt.gui;

import javax.swing.JButton;
import javax.swing.JComponent;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.Button_0;

public class Button implements Button_0{

  private final JButton button;
  
  public Button(String text) {this.button = new JButton(text);}
  
  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    button.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return button.isEnabled()? True_0.$self: False_0.$self; 
  }
  
  public Void_0 addActionListener$mut(MF_1 listener) {
    button.addActionListener(listenere -> listener.$hash$mut());
    return Void_0.$self;
  }
  
  public JComponent getImpl() {
    return button;
  }

}
