package mapgenerator.mapsitegenerator;

import org.json.JSONObject;

public class WallGenerator implements MapSiteGenerator {

    @Override
    public JSONObject generate() {
        return getWall();
    }

    public static JSONObject getWall() {
        JSONObject wallJson = new JSONObject();
        wallJson.put("mapSite", "wall");
        return wallJson;
    }
}
