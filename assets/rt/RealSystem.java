package rt;

import base.gui.GuiBuilder_0;
import rt.gui.GuiBuilder;

public final class RealSystem implements base.caps._System_0 {
  @Override public RealSystem iso$mut() { return this; }
  @Override public RealSystem self$mut() { return this; }
  @Override public rt.IO io$mut() {
    return rt.IO.$self;
  }
  @Override public rt.CapTry try$mut() {
    return new rt.CapTry();
  }
  @Override public base.caps.RandomSeed_0 rng$mut() {
    return rt.Random.SeedGenerator.$self;
  }
  @Override
  public GuiBuilder_0 gui$mut() {
    return new GuiBuilder();
  }
}
