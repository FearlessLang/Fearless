package rt.gui;

import javax.swing.JComponent;
import javax.swing.JMenu;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.Menu_0;

public class Menu implements Menu_0 {

  private final JMenu menu;
  
  public Menu(String txt){this.menu = new JMenu(txt);}
  
  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    menu.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return menu.isEnabled()? True_0.$self: False_0.$self;
  }

  public JComponent getImpl() {return this.menu;}

  public void add(MenuItem item) {menu.add(item.getImpl());}
}
