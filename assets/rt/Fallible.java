package rt;

import base.Action_1;
import base.MF_2;

public interface Fallible extends Action_1 {
  @Override default Object $exclamation$mut() {
    return Action_1.$exclamation$mut$fun(this);
  }
  @Override default Action_1 mapInfo$mut(MF_2 f_m$) {
    return Action_1.mapInfo$mut$fun(f_m$, this);
  }
  @Override default Action_1 map$mut(MF_2 f_m$) {
    return Action_1.map$mut$fun(f_m$, this);
  }
  @Override default Action_1 andThen$mut(MF_2 f_m$) {
    return Action_1.andThen$mut$fun(f_m$, this);
  }
  @Override default base.Opt_1 ok$mut() { return Action_1.ok$mut$fun(this); }
  @Override default base.Opt_1 info$mut() { return Action_1.info$mut$fun(this); }
}
