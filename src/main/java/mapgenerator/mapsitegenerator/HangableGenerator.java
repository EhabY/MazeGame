package mapgenerator.mapsitegenerator;

import mapgenerator.RandomNameGenerator;
import org.json.JSONObject;

import java.util.SplittableRandom;

public class HangableGenerator implements MapSiteGenerator {
    private static final int PROBABILITY = 100;
    private static final int HIDDEN_KEY_PROBABILITY = 75;
    private static final int MAPSITE_PROBABILITY = PROBABILITY / 2;
    private static final SplittableRandom random = new SplittableRandom();
    private final RandomNameGenerator randomNameGenerator;

    public HangableGenerator(RandomNameGenerator randomNameGenerator) {
        this.randomNameGenerator = randomNameGenerator;
    }

    @Override
    public JSONObject generate() {
        return getRandomHangable(getHiddenKey());
    }

    public static JSONObject getRandomHangable(String keyName) {
        JSONObject hangableJson = new JSONObject();
        hangableJson.put("mapSite", getMapSiteType());
        hangableJson.put("hiddenKey", keyName);
        return hangableJson;
    }

    private static String getMapSiteType() {
        int chance = random.nextInt(PROBABILITY);
        if(chance < MAPSITE_PROBABILITY) {
            return "Painting";
        } else {
            return "Mirror";
        }
    }

    private String getHiddenKey() {
        int chance = random.nextInt(PROBABILITY);
        if(chance < HIDDEN_KEY_PROBABILITY) {
            return randomNameGenerator.getRandomName();
        } else {
            return "";
        }
    }
}
