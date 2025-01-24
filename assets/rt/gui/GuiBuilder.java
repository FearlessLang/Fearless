package rt.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
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
  public Void_0 build$mut(Str title_m$) {
    // instead of returning GuiEvents_0
    //compleateblefuture
    CompletableFuture<Void> future = new CompletableFuture<Void>();

    JFrame frame = new JFrame(Str.toJavaStr(title_m$.utf8()));
    new GuiEvents_0() {
      @Override
      public Void_0 stop$mut(){
        // when press x this method need to be called
        frame.dispose();
        future.complete(null);
        return Void_0.$self;
      }
      
    };
    SwingUtilities.invokeLater(()->_build(frame));
    try {
      future.get();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return Void_0.$self;
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

  @Override
  public GuiBuilder_0 radioButton$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    RadioButton r =new RadioButton(Str.toJavaStr(label_m$.utf8()));
    r.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(r);
    panel.add(r.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 textField$mut(Str text_m$, MF_2 events_m$, MF_2 slot_m$) {
    TextField tf =new TextField(Str.toJavaStr(text_m$.utf8()));
    tf.addEvents$mut(events_m$);
    slot_m$.$hash$mut(tf);
    panel.add(tf.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 textArea$mut(Str text_m$, MF_2 events_m$, MF_2 slot_m$) {
    TextArea ta =new TextArea(Str.toJavaStr(text_m$.utf8()));
    ta.addEvents$mut(events_m$);
    slot_m$.$hash$mut(ta);
    panel.add(ta.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 vseparator$mut(long x_m$, long y_m$, MF_2 slot_m$) {
    Vseparator v = new Vseparator(Math.toIntExact(x_m$),Math.toIntExact(y_m$));
    slot_m$.$hash$mut(v);
    panel.add(v.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 hseparator$mut(long x_m$, long y_m$, MF_2 slot_m$) {
    Hseparator h = new Hseparator(Math.toIntExact(x_m$),Math.toIntExact(y_m$));
    slot_m$.$hash$mut(h);
    panel.add(h.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 hbox$mut(MF_2 gb_m$) {
    Objects.requireNonNull(gb_m$);
    GuiBuilder boxBuilder = new GuiBuilder(new FlowLayout());
    boxBuilder.panel.setLayout(new BoxLayout(boxBuilder.panel, BoxLayout.X_AXIS));
    gb_m$.$hash$mut(boxBuilder);
    panel.add(boxBuilder.panel);
    return this;
  }

  @Override
  public GuiBuilder_0 vbox$mut(MF_2 gb_m$) {
    Objects.requireNonNull(gb_m$);
    GuiBuilder boxBuilder = new GuiBuilder(new FlowLayout());
    boxBuilder.panel.setLayout(new BoxLayout(boxBuilder.panel, BoxLayout.Y_AXIS));
    gb_m$.$hash$mut(boxBuilder);
    panel.add(boxBuilder.panel);
    return this;
  }

}
