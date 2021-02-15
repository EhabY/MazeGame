package mapgenerator.mapsitegenerator;

import java.util.Objects;
import java.util.SplittableRandom;
import mapgenerator.ItemGenerator;
import mapgenerator.RandomNameGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChestGenerator implements MapSiteGenerator {

  private static final int PROBABILITY = 100;
  private static final int LOCKED_PROBABILITY = 50;
  private static final int ITEM_PROBABILITY = 40;
  private static final int FLASHLIGHT_PROBABILITY = 20;
  private static final int GOLD_BOUND = 10;
  private static final SplittableRandom random = new SplittableRandom();
  private final RandomNameGenerator randomNameGenerator;

  public ChestGenerator(RandomNameGenerator randomNameGenerator) {
    this.randomNameGenerator = Objects.requireNonNull(randomNameGenerator);
  }

  public static JSONObject getChestWithItem(JSONObject item, String key, boolean locked) {
    JSONArray items = new JSONArray();
    items.put(item);
    return getChestWithItems(items, key, locked);
  }

  public static JSONObject getChestWithItems(JSONArray items, String key, boolean locked) {
    JSONObject chestJson = new JSONObject();
    chestJson.put("mapSite", "Chest");
    chestJson.put("loot", generateLoot(items));
    chestJson.put("key", key);
    chestJson.put("locked", locked);
    return chestJson;
  }

  private static boolean isLocked() {
    int chance = random.nextInt(PROBABILITY);
    return chance < LOCKED_PROBABILITY;
  }

  private static JSONObject generateLoot(JSONArray items) {
    JSONObject lootJson = new JSONObject();
    lootJson.put("gold", random.nextInt(GOLD_BOUND));
    lootJson.put("items", items);
    return lootJson;
  }

  @Override
  public JSONObject generate() {
    return getChestWithItems(getItemsList(), randomNameGenerator.getRandomName(), isLocked());
  }

  private JSONArray getItemsList() {
    JSONArray itemsList = new JSONArray();
    int chance = random.nextInt(PROBABILITY);
    while (chance < ITEM_PROBABILITY) {
      itemsList.put(getItemJson());
      chance = random.nextInt(PROBABILITY);
    }
    return itemsList;
  }

  private JSONObject getItemJson() {
    int chance = random.nextInt(PROBABILITY);
    if (chance < FLASHLIGHT_PROBABILITY) {
      return ItemGenerator.getFlashlightJson();
    } else {
      return ItemGenerator.getKeyJson(randomNameGenerator.getRandomName());
    }
  }

}
