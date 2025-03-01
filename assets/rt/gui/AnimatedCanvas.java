package rt.gui;

import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.AnimatedCanvas_0;
import base.gui.Repainter_0;

public class AnimatedCanvas extends JPanel implements AnimatedCanvas_0, Commitable {

  private static final long serialVersionUID = 1L;
  private volatile Repainter_0 painter = (a, b, c, e, f, g) -> a;
  private volatile Repainter_0 desiredPainter = painter;
  volatile int processedPings = 0;
  volatile double interpolatedTime;
  volatile long currentTime;
  
  public JComponent getImpl() {return this;}
  
  @Override
  protected synchronized void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    Graphics sg = new Graphics((Graphics2D)g);
    try {
    this.painter.paint$imm(sg, currentTime, processedPings, interpolatedTime, getWidth(), getHeight());}
    catch(Throwable e) {
      System.err.println("Error occurred while painting the canvas in AnimatedCanvas.");
      System.err.println("Possible causes:");
      System.err.println(" - The painter function threw an exception.");
      System.err.println(" - A null reference was used in the painting logic.");
      System.err.println(" - Graphics object might be in an invalid state.");
      System.err.println("Stack trace:");
      e.printStackTrace();
    }
  }
  
  public void setTime(long currentTime, double interpolatedTime) {
    this.currentTime = currentTime;
    this.interpolatedTime = interpolatedTime;
  }

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    this.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return this.isEnabled()? True_0.$self: False_0.$self;
  }

  @Override
  public Void_0 setPainter$mut(Repainter_0 painter_m$) {
    this.desiredPainter = painter_m$;
    return Void_0.$self;
  }

  @Override
  public void commit() {
    this.painter = desiredPainter;
  }

}
