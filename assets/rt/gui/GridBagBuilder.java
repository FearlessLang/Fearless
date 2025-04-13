package rt.gui;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

import base.gui.GridBagBuilder_0;

public class GridBagBuilder implements GridBagBuilder_0 {

  private List<GridBagConstraints> config = new ArrayList<GridBagConstraints>();
  
  @Override
  public GridBagBuilder_0 config$mut(long x_m$, long y_m$, long width_m$, long height_m$) {
    assert x_m$ >= 0;
    assert y_m$ >= 0;
    assert width_m$ >= 0;
    assert height_m$ >= 0;
         GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = Math.toIntExact(x_m$);
      gbc.gridy = Math.toIntExact(y_m$);
      gbc.gridwidth = Math.toIntExact(width_m$);
      gbc.gridheight = Math.toIntExact(height_m$);
      config.add(gbc);
         return this;
  }

  public List<GridBagConstraints> constriants() {return List.copyOf(config); }
}
