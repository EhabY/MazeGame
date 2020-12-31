package mazegame.mapsite;

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
  public String toJson() {
    return "{" + "\"siteMap\": \"Wall\"" + "}";
  }
}
