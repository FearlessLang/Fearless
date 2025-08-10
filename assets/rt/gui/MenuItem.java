package rt.gui;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.MenuItem_0;

public class MenuItem implements MenuItem_0 {

  private final JMenuItem menuItem;
  private GuiBuilderState state;
  
  public MenuItem(String txt, GuiBuilderState state) {
    this.menuItem = new JMenuItem(txt);
    this.state = state;
  }

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    menuItem.addActionListener(e -> state.submitModelTask(listener_m$::$hash$mut));
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    menuItem.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return menuItem.isEnabled()? True_0.$self: False_0.$self;
  }

  public JComponent getImpl() {return this.menuItem;}
}
