package mazegame.mapsite;

import serialization.JsonEncoder;

public class Wall implements SerializableMapSite {
  private static final String DESCRIPTION = "Wall";
  private static final Wall INSTANCE = new Wall();

  private Wall() {}

  public static Wall getInstance() {
    return INSTANCE;
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }

  @Override
  public String applyEncoder(JsonEncoder encoder) {
    return encoder.visit(this);
  }
}
