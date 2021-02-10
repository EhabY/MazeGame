package mazegame.mapsite;

import mazegame.item.Key;
import serialization.Encoder;

public class Painting extends AbstractHangable {
  private static final String DESCRIPTION = "Painting";

  public Painting(Key hiddenKey) {
    super(DESCRIPTION, hiddenKey);
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }
}
