package mazegame.parser;

import mazegame.item.Item;
import mazegame.item.Key;
import mazegame.mapsite.Chest;
import mazegame.mapsite.Door;
import mazegame.mapsite.Loot;
import mazegame.mapsite.Mirror;
import mazegame.mapsite.Painting;
import mazegame.mapsite.Seller;
import mazegame.mapsite.SerializableMapSite;
import mazegame.mapsite.Wall;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MapSiteParser {
  final Map<ImmutablePair<Integer, Integer>, DoorInfo> doorBetweenRooms = new HashMap<>();
  int roomID;

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
    int otherRoomID = doorJson.getInt("otherRoomID");
    String keyName = doorJson.getString("key");
    boolean locked = isLockableLocked(doorJson);

    return getOrCreateDoor(otherRoomID, keyName, locked);
  }

  private boolean isLockableLocked(JSONObject lockableJson) {
    return lockableJson.getString("key").length() > 0 &&
            (!lockableJson.has("locked") || lockableJson.getBoolean("locked"));
  }

  private Door getOrCreateDoor(int otherRoomID, String keyName, boolean locked) {
    Door.Builder doorBuilder;
    ImmutablePair<Integer, Integer> otherRoomDoor = getDoorOnOtherSide(otherRoomID);
    if(doorAlreadyCreated(otherRoomDoor)) {
      doorBuilder = doorBetweenRooms.get(otherRoomDoor).doorBuilder;
    } else {
      doorBuilder = createDoorBuilder(otherRoomID, keyName, locked);
    }

    return doorBuilder.getDoor();
  }

  private ImmutablePair<Integer, Integer> getDoorOnOtherSide(int otherRoomID) {
    return new ImmutablePair<>(otherRoomID, roomID);
  }

  private boolean doorAlreadyCreated(ImmutablePair<Integer, Integer> otherRoomDoor) {
    return doorBetweenRooms.containsKey(otherRoomDoor);
  }

  private Door.Builder createDoorBuilder(int otherRoomID, String keyName, boolean locked) {
    Door.Builder doorBuilder = new Door.Builder(Key.fromString(keyName), locked);
    ImmutablePair<Integer, Integer> thisRoomDoor = new ImmutablePair<>(roomID, otherRoomID);
    doorBetweenRooms.put(thisRoomDoor, new DoorInfo(roomID, otherRoomID, doorBuilder));
    return doorBuilder;
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
