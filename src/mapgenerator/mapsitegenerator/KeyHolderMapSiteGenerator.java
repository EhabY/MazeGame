package mapgenerator.mapsitegenerator;

import mapgenerator.ItemGenerator;
import org.json.JSONObject;
import java.util.SplittableRandom;

public class KeyHolderMapSiteGenerator {
    private static final int PROBABILITY = 100;
    private static final int HANGABLE_PROBABILITY = 60;
    private static final SplittableRandom random = new SplittableRandom();

    public static JSONObject getKeyHolderMapSite(String keyName) {
        int chance = random.nextInt(PROBABILITY);
        if(chance < HANGABLE_PROBABILITY) {
            return HangableGenerator.getRandomHangable(keyName);
        } else {
            return ChestGenerator.getChestWithItem(ItemGenerator.getKeyJson(keyName), keyName, false);
        }
    }
}
