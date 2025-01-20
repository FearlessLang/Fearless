package rt.gui;

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import base.MF_1;
import base.MF_2;
import base.Void_0;
import base.gui.GraphicBuilder_0;
import base.gui.GuiBuilder_0;
import base.gui.GuiEvents_0;
import rt.Str;

public class GuiBuilder implements GuiBuilder_0{

  private final JPanel panel;
  private final LayoutManager layout;
  
  public GuiBuilder(LayoutManager layout) {
    Objects.requireNonNull(layout);
    this.panel = new JPanel();
    this.layout = layout;
    this.panel.setLayout(layout);   
//    this.state = state;
  }
  
  public GuiBuilder() {
    this(new GridLayout(1, 0));
  }
  @Override
  public GuiBuilder_0 flex$mut(MF_2 gb_m$) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GuiBuilder_0 canvas$mut(long height_m$, long width_m$, GraphicBuilder_0 gb_m$, MF_2 slot_m$) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GuiEvents_0 build$mut(Str title_m$) {
    JFrame frame = new JFrame(Str.toJavaStr(title_m$.utf8()));
    SwingUtilities.invokeLater(()->_build(frame));
    return new GuiEvents_0() {

      @Override
      public Void_0 stop$mut() {frame.dispose();
        return Void_0.$self;
      }
      
    };
  }
  
  private void _build(JFrame frame) {
    Objects.requireNonNull(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
  }

  @Override
  public GuiBuilder_0 button$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    Button b =new Button(Str.toJavaStr(label_m$.utf8()));
    b.addActionListener(f_m$);
    slot_m$.$hash$mut(b);
    panel.add(b.getImpl());
    return this;
  }

}
