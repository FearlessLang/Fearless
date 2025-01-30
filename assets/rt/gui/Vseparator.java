package rt.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSeparator;

import base.gui.Vseparator_0;

public class Vseparator implements Vseparator_0 {

  private final JSeparator separator;

  public Vseparator(int x , int y) {
    this.separator = new JSeparator(JSeparator.VERTICAL);
    this.separator.setPreferredSize(new Dimension(x, y));
  }
  
  public JComponent getImpl() { return separator; }
}
