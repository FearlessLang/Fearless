package rt.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
    GuiBuilder flowBuilder = new GuiBuilder(new FlowLayout());
    gb_m$.$hash$mut(flowBuilder);
    panel.add(flowBuilder.panel);
    return this;
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
    b.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(b);
    panel.add(b.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 label$mut(Str label_m$, MF_2 slot_m$) {
    Label l = new Label(Str.toJavaStr(label_m$.utf8()));
    slot_m$.$hash$mut(l);
    panel.add(l.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 checkBox$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    CheckBox c =new CheckBox(Str.toJavaStr(label_m$.utf8()));
    c.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(c);
    panel.add(c.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 comboBox$mut(MF_1 f_m$, MF_2 slot_m$, MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder();
    gb_m$.$hash$mut(builder);
    Component[] components = builder.panel.getComponents();
    assert components.length > 0 : "No items added to the ComboBox";
    Object[] items = Arrays.stream(components)
        .map(component -> component instanceof JLabel ? ((JLabel) component).getText() : component).toArray();
    ComboBox comboBox = new ComboBox(items);
    comboBox.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(comboBox);
    panel.add(comboBox.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 passwordField$mut(Str defaultPw_m$, MF_1 f_m$, MF_2 slot_m$) {
    PasswordField p =new PasswordField(Str.toJavaStr(defaultPw_m$.utf8()));
    p.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(p);
    panel.add(p.getImpl());
    return this;
  }

}
