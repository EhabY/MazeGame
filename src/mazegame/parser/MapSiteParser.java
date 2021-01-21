package mazegame.parser;

import mazegame.item.Item;
import mazegame.item.Key;
import mazegame.mapsite.*;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MapSiteParser {
  final List<DoorInfo> doors = new ArrayList<>();

  static class DoorInfo {
    final int roomID;
    final int otherRoomID;
    final Door.Builder doorBuilder;

    DoorInfo(int roomID, int otherRoomID, Door.Builder doorBuilder) {
      this.roomID = roomID;
      this.otherRoomID = otherRoomID;
      this.doorBuilder = doorBuilder;
    }
  }

  SerializableMapSite parseMapSite(JSONObject mapSiteJson) {
    String siteMapType = mapSiteJson.getString("siteMap");
    switch (siteMapType) {
      case "Door":
        return parseDoor(mapSiteJson);
      case "Wall":
        return parseWall(mapSiteJson);
      case "Chest":
        return parseChest(mapSiteJson);
      case "Painting":
        return parsePainting(mapSiteJson);
      case "Mirror":
        return parseMirror(mapSiteJson);
      case "Seller":
        return parseSeller(mapSiteJson);
    }

    throw new JSONException("Cannot parse object " + siteMapType);
  }

  private Door parseDoor(JSONObject doorJson) {
    String keyName = doorJson.getString("key");
    int roomID = doorJson.getInt("roomID");
    int otherRoomID = doorJson.getInt("otherRoomID");
    boolean locked = isLockableLocked(doorJson);

    Door.Builder doorBuilder = new Door.Builder(Key.fromString(keyName), locked);
    doors.add(new DoorInfo(roomID, otherRoomID, doorBuilder));

    return doorBuilder.getDoor();
  }

  private boolean isLockableLocked(JSONObject lockableJson) {
    return lockableJson.getString("key").length() > 0 &&
            (!lockableJson.has("locked") || lockableJson.getBoolean("locked"));
  }

  private Wall parseWall(JSONObject wallJson) {
    return Wall.getInstance();
  }

  private Chest parseChest(JSONObject chestJson) {
    Loot loot = ItemParser.parseLoot(chestJson.getJSONObject("loot"));
    String keyName = chestJson.getString("key");
    boolean locked = isLockableLocked(chestJson);

    return new Chest(loot, Key.fromString(keyName), locked);
  }

  private Painting parsePainting(JSONObject paintingJson) {
    String hiddenKeyName = paintingJson.getString("hiddenKey");

    return new Painting(Key.fromString(hiddenKeyName));
  }

  private Mirror parseMirror(JSONObject mirrorJson) {
    String hiddenKeyName = mirrorJson.getString("hiddenKey");

    return new Mirror(Key.fromString(hiddenKeyName));
  }

  private Seller parseSeller(JSONObject sellerJson) {
    List<Item> itemsList = ItemParser.parseItemsArray(sellerJson.getJSONArray("items"));
    Map<String, Long> priceList = parsePriceList(sellerJson.getJSONArray("priceList"));

    return new Seller(itemsList, priceList);
  }

  private Map<String, Long> parsePriceList(JSONArray priceListJson) {
    Map<String, Long> priceList = new CaseInsensitiveMap<>();

    for (int i = 0; i < priceListJson.length(); i++) {
      JSONObject priceListingJson = priceListJson.getJSONObject(i);
      String itemName = priceListingJson.getString("name");
      Long price = priceListingJson.getLong("price");
      priceList.put(itemName, price);
    }

    return priceList;
  }
}
