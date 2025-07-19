package rt.gui;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import base.MF_2;
import base.gui.TabBuilder_0;
import rt.Str;

public class TabBuilder implements TabBuilder_0 {

  private final List<Tab.TabEntry> tabs = new ArrayList<Tab.TabEntry>();
  private final GuiBuilderState state;
  
  public TabBuilder(GuiBuilderState state) {
    this.state=state;
  }
  @Override
  public TabBuilder_0 tab$mut(Str title_m$, MF_2 gb_m$) {
    tabs.add(new TabEntryImpl(Str.toJavaStr(title_m$.utf8()), gb_m$, state));
    return this; 
  }

  public Tab build(){ 
    return new Tab(){ @Override public List<TabEntry> getTabs() { return tabs; } };
    }
  
  private static class TabEntryImpl implements Tab.TabEntry {
    private final String title;
    private final MF_2 contentBuilder;
    private final GuiBuilderState state;
    
    public TabEntryImpl(String title, MF_2 contentBuilder, GuiBuilderState state) {
      this.title = title;
      this.contentBuilder = contentBuilder;
      this.state = state;
    }

    @Override
    public String title() { return title; }

    @Override
    public GuiBuilder getContent() {
      GuiBuilder builder = new GuiBuilder(new FlowLayout(),state);
      contentBuilder.$hash$mut(builder);
    return builder;
    }
   }
}
