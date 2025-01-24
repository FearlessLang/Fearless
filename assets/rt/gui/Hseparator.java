package rt.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSeparator;

import base.gui.Hseparator_0;

public class Hseparator implements Hseparator_0 {

  private final JSeparator separator;

  public Hseparator(int x , int y) {
    this.separator = new JSeparator(JSeparator.HORIZONTAL);
    this.separator.setPreferredSize(new Dimension(x, y));
  }
  
  public JComponent getImpl() { return separator; }
}
