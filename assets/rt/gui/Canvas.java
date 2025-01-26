package rt.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;

import base.MF_2;
import base.Void_0;
import base.gui.Canvas_0;

public class Canvas extends JLabel implements Canvas_0 {

  private static final long serialVersionUID = 1L;
  private MF_2 painter = g -> null;
  
  public Canvas(int width, int height) {
    setPreferredSize(new Dimension(width, height));
  }
  
  @Override
  protected void paintComponent(java.awt.Graphics g){
   super.paintComponent(g);
   Graphics sg = new Graphics((Graphics2D)g);
   painter.$hash$mut(sg);
  }
  
  
  public JComponent getImpl() {return this;}

  @Override
  public Void_0 paint$mut(MF_2 gb_m$) {
    this.painter = Objects.requireNonNull(gb_m$);
    return Void_0.$self;
  }
}
