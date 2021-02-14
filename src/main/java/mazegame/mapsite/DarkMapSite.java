package mazegame.mapsite;

public class DarkMapSite implements MapSite {

  private static final String DESCRIPTION = "Dark";
  private static final DarkMapSite INSTANCE = new DarkMapSite();

  private DarkMapSite() {
  }

  public static DarkMapSite getInstance() {
    return INSTANCE;
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }
}
