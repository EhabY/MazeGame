package mazegame.mapsite;

import mazegame.item.Key;

public class Painting extends AbstractHangable {
  private static final String DESCRIPTION = "Painting";

  public Painting(Key hiddenKey) {
    super(DESCRIPTION, hiddenKey);
  }

  @Override
  public String toJson() {
    return "{" + "\"siteMap\": \"Painting\"," + "\"hiddenKey\": \"" + getKeyName() + "\"" + "}";
  }
}
