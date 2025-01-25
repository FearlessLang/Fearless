package rt.gui;

import base.MF_1;
import base.MF_2;
import base.gui.MenuBuilder_0;
import rt.Str;

public class MenuBuilder implements MenuBuilder_0 {

  private final MenuBar menuBar;
  private Menu currentMenu;
  
  public MenuBuilder(MenuBar menuBar) {
    this.menuBar = menuBar;
  }
  
  @Override
  public MenuBuilder_0 menu$mut(Str name_m$, MF_2 slot_m$) {
    currentMenu = new Menu(Str.toJavaStr(name_m$.utf8()));
    menuBar.add(currentMenu);
    slot_m$.$hash$mut(currentMenu);
    return this;
  }

  @Override
  public MenuBuilder_0 menuItem$mut(Str name_m$, MF_1 f_m$, MF_2 slot_m$) {
    MenuItem item = new MenuItem(Str.toJavaStr(name_m$.utf8()));
    item.addActionListener$mut(f_m$);
    currentMenu.add(item);
    slot_m$.$hash$mut(item);
    return this;
  }

}
