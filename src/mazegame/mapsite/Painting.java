package mazegame.mapsite;

import mazegame.item.Key;
import serialization.JsonEncoder;

public class Painting extends AbstractHangable {
  private static final String DESCRIPTION = "Painting";

  public Painting(Key hiddenKey) {
    super(DESCRIPTION, hiddenKey);
  }

  @Override
  public String applyEncoder(JsonEncoder encoder) {
    return encoder.visit(this);
  }
}
