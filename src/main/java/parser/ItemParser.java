package parser;

import mazegame.item.Flashlight;
import mazegame.item.Item;
import mazegame.item.Key;
import mazegame.mapsite.Loot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

class ItemParser {

  private ItemParser() {}

  static List<Item> parseItemsArray(JSONArray itemsJson) {
    List<Item> items = new ArrayList<>();
    for (int i = 0; i < itemsJson.length(); i++) {
      JSONObject itemJson = itemsJson.getJSONObject(i);
      items.add(parseItem(itemJson));
    }
    return items;
  }

  static Item parseItem(JSONObject itemJson) {
    String type = itemJson.getString("type");
    if (type.equalsIgnoreCase("Flashlight")) {
      return parseFlashlight(itemJson);
    } else if (type.equalsIgnoreCase("Key")) {
      return parseKey(itemJson);
    }
    throw new JSONException("Unknown item type");
  }

  private static Flashlight parseFlashlight(JSONObject flashlightJson) {
    if (flashlightJson.has("turnedOn")) {
      boolean turnedOn = flashlightJson.getBoolean("turnedOn");
      return new Flashlight(turnedOn);
    } else {
      return new Flashlight();
    }
  }

  private static Key parseKey(JSONObject keyJson) {
    String name = keyJson.getString("name");
    return Key.fromString(name);
  }

  static Loot parseLoot(JSONObject lootJson) {
    JSONArray itemsArray = lootJson.getJSONArray("items");
    long gold = lootJson.getLong("gold");
    List<Item> items = ItemParser.parseItemsArray(itemsArray);
    return new Loot(gold, items);
  }
}
