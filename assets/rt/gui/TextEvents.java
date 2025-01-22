package rt.gui;

import java.util.function.Consumer;

import base.MF_1;
import base.MF_2;
import base.gui.TextEvents_0;

public class TextEvents implements TextEvents_0 {

  MF_2 textChangeHandler;
  MF_2 keyPressHandler;
  MF_1 focusGainedHandler;
  MF_1 focusLostHandler;
  @Override
  public TextEvents_0 onKeyPress$mut(MF_2 handler_m$) {
    this.textChangeHandler=handler_m$;
    return this;
  }

  @Override
  public TextEvents_0 onFocusLost$mut(MF_1 handler_m$) {
    this.focusLostHandler=handler_m$;
    return this;
  }

  @Override
  public TextEvents_0 onFocusGained$mut(MF_1 handler_m$) {
    this.focusGainedHandler=handler_m$;
    return this;
  }

  @Override
  public TextEvents_0 onTextChange$mut(MF_2 handler_m$) {
    this.keyPressHandler=handler_m$;
    return this;
  }

}
