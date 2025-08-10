package rt.gui;

import java.util.List;

public interface Tab {
	
 List<TabEntry> getTabs();

 interface TabEntry {
    String title();
    GuiBuilder getContent();
   }
}
