package rt.gui;

public interface Zone {
  GuiBuilder center(GuiBuilder b);
  GuiBuilder north(GuiBuilder b);
  GuiBuilder east(GuiBuilder b);
  GuiBuilder south(GuiBuilder b);
  GuiBuilder west(GuiBuilder b);
}
