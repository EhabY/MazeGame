package mapgenerator.mapsitegenerator;

import java.util.SplittableRandom;
import mapgenerator.ItemGenerator;
import org.json.JSONObject;

public class KeyHolderGenerator {

  private static final int PROBABILITY = 100;
  private static final int HANGABLE_PROBABILITY = 60;
  private static final SplittableRandom random = new SplittableRandom();

  public static JSONObject getKeyHolder(String keyName) {
    int chance = random.nextInt(PROBABILITY);
    if (chance < HANGABLE_PROBABILITY) {
      return HangableGenerator.getRandomHangable(keyName);
    } else {
      return ChestGenerator.getChestWithItem(ItemGenerator.getKeyJson(keyName), keyName, false);
    }
  }
}
