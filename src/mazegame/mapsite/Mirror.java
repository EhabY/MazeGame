package mazegame.mapsite;

import mazegame.item.Key;
import serialization.JsonEncoder;

public class Mirror extends AbstractHangable {
  private static final String DESCRIPTION = "You See a silhouette of you";

  public Mirror(Key hiddenKey) {
    super(DESCRIPTION, hiddenKey);
  }

  @Override
  public String applyEncoder(JsonEncoder encoder) {
    return encoder.visit(this);
  }
}
