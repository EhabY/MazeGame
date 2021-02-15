package mapgenerator.mapsitegenerator;

import org.json.JSONObject;

public class WallGenerator implements MapSiteGenerator {

  public static JSONObject getWall() {
    JSONObject wallJson = new JSONObject();
    wallJson.put("mapSite", "wall");
    return wallJson;
  }

  @Override
  public JSONObject generate() {
    return getWall();
  }
}
