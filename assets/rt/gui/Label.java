package rt.gui;

import javax.swing.JComponent;
import javax.swing.JLabel;

import base.gui.Label_0;

public class Label implements Label_0{

  private final JLabel label;
  
  public Label(String text) {this.label = new JLabel(text);}
  
  public JComponent getImpl() {
    return label;
  }
}
