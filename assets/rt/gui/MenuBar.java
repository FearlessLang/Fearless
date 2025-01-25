package rt.gui;

import javax.swing.JComponent;
import javax.swing.JMenuBar;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.True_0;
import base.Void_0;
import base.gui.MenuBar_0;

public class MenuBar implements MenuBar_0 {

private final JMenuBar menuBar;
  
  public MenuBar() {this.menuBar=new JMenuBar();}
  
  public JComponent getImpl() {return this.menuBar;}

  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    menuBar.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return menuBar.isEnabled()? True_0.$self: False_0.$self;
  }

  public void add(Menu menu) {this.menuBar.add(menu.getImpl());}

  
}
