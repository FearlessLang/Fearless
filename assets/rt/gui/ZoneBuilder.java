package rt.gui;

import base.MF_2;
import base.gui.ZoneBuilder_0;

public class ZoneBuilder implements ZoneBuilder_0 {

  private MF_2 center;
  private MF_2 north;
  private MF_2 east;
  private MF_2 south;
  private MF_2 west;
  
  @Override
  public ZoneBuilder_0 center$mut(MF_2 gb_m$) {
    center = gb_m$;
    return this;
  }

  @Override
  public ZoneBuilder_0 east$mut(MF_2 gb_m$) {
    east = gb_m$;
    return this;
  }

  @Override
  public ZoneBuilder_0 south$mut(MF_2 gb_m$) {
    south = gb_m$;
    return this;
  }

  @Override
  public ZoneBuilder_0 west$mut(MF_2 gb_m$) {
    west = gb_m$;
    return this;
  }

  @Override
  public ZoneBuilder_0 north$mut(MF_2 gb_m$) {
    north = gb_m$;
    return this;
  }

  public Zone build() {
    return new Zone() {
      @Override
      public GuiBuilder center(GuiBuilder b) {
        return center == null ? b : (GuiBuilder) center.$hash$mut(b);
      }

      @Override
      public GuiBuilder north(GuiBuilder b) {
        return north == null ? b : (GuiBuilder) north.$hash$mut(b);
      }

      @Override
      public GuiBuilder east(GuiBuilder b) {
        return east == null ? b : (GuiBuilder) east.$hash$mut(b);
      }

      @Override
      public GuiBuilder south(GuiBuilder b) {
        return south == null ? b : (GuiBuilder) south.$hash$mut(b);
      }

      @Override
      public GuiBuilder west(GuiBuilder b) {
        return west == null ? b : (GuiBuilder) west.$hash$mut(b);
      }
    };
  }

}
