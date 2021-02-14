package parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mazegame.Direction;
import mazegame.MazeMap;
import mazegame.mapsite.Door;
import mazegame.mapsite.Loot;
import mazegame.mapsite.SerializableMapSite;
import mazegame.room.LightSwitch;
import mazegame.room.NoLightSwitch;
import mazegame.room.Room;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameParser {

  private final Map<Integer, Room> rooms;
  private final MapSiteParser mapSiteParser;

  private GameParser() {
    rooms = new HashMap<>();
    mapSiteParser = new MapSiteParser();
  }

  public static MazeMap parseJsonFile(String pathToFile) throws IOException {
    String jsonString = readWholeFile(pathToFile);
    return parseJson(jsonString);
  }

  public static MazeMap parseJson(String jsonString) {
    GameParser gameParser = new GameParser();
    JSONObject gameJson = new JSONObject(jsonString);
    gameParser.parseAllRooms(gameJson);
    return gameParser.parseMazeMap(gameJson);
  }

  private static String readWholeFile(String pathToFile) throws IOException {
    return new String(Files.readAllBytes(Paths.get(pathToFile)), StandardCharsets.UTF_8);
  }

  private void parseAllRooms(JSONObject gameJson) {
    JSONArray roomsJson = gameJson.getJSONArray("rooms");
    Map<Integer, Room> roomsMap = parseRoomsArray(roomsJson);
    setRoomsInDoors(roomsMap);
  }

  private Map<Integer, Room> parseRoomsArray(JSONArray roomsJson) {
    for (int i = 0; i < roomsJson.length(); i++) {
      Room room = parseRoom(roomsJson.getJSONObject(i));
      rooms.put(room.getId(), room);
    }
    return rooms;
  }

  private Room parseRoom(JSONObject roomJson) {
    int id = roomJson.getInt("id");
    Map<Direction, SerializableMapSite> serializedMapSites = parseMapSitesInRoom(roomJson);
    JSONObject lightswitch = roomJson.getJSONObject("lightswitch");
    return new Room(id, serializedMapSites, parseLightSwitch(lightswitch), getLoot(roomJson));
  }

  private Loot getLoot(JSONObject roomJson) {
    if (roomJson.has("loot")) {
      JSONObject lootJson = roomJson.getJSONObject("loot");
      return ItemParser.parseLoot(lootJson);
    } else {
      return Loot.EMPTY_LOOT;
    }
  }

  private Map<Direction, SerializableMapSite> parseMapSitesInRoom(JSONObject roomJson) {
    Map<Direction, SerializableMapSite> serializedMapSites = new EnumMap<>(Direction.class);
    for (Direction direction : Direction.values()) {
      String directionName = direction.toString().toLowerCase();
      JSONObject mapSiteJson = roomJson.getJSONObject(directionName);
      SerializableMapSite mapSite = mapSiteParser.parseMapSite(mapSiteJson);
      serializedMapSites.put(direction, mapSite);
    }

    return serializedMapSites;
  }

  private LightSwitch parseLightSwitch(JSONObject lightswitchJson) {
    boolean hasLights = lightswitchJson.getBoolean("hasLights");
    if (hasLights) {
      boolean lightsOn = lightswitchJson.getBoolean("lightsOn");
      return new LightSwitch(lightsOn);
    } else {
      return NoLightSwitch.getInstance();
    }
  }

  private void setRoomsInDoors(Map<Integer, Room> rooms) {
    Collection<MapSiteParser.DoorInfo> doors = mapSiteParser.doorBetweenRooms.values();
    for (MapSiteParser.DoorInfo doorInfo : doors) {
      int roomID = doorInfo.roomID;
      int otherRoomID = doorInfo.otherRoomID;
      Door.Builder doorBuilder = doorInfo.doorBuilder;

      doorBuilder.setRoom(rooms.get(roomID)).setOtherRoom(rooms.get(otherRoomID));
    }
  }

  private MazeMap parseMazeMap(JSONObject gameJson) {
    JSONObject mapConfigJson = gameJson.getJSONObject("mapConfiguration");
    List<Room> startingRooms = parseStartingRooms(mapConfigJson.getJSONArray("startRoomsID"));
    int endRoomID = mapConfigJson.getInt("endRoomID");
    long gold = mapConfigJson.getLong("gold");
    long timeInSeconds = mapConfigJson.getLong("time");
    JSONArray itemsJson = mapConfigJson.getJSONArray("items");

    return new MazeMap.Builder(rooms.values(), startingRooms, rooms.get(endRoomID))
        .startingGold(gold)
        .time(timeInSeconds)
        .initialItems(ItemParser.parseItemsArray(itemsJson))
        .build();
  }

  private List<Room> parseStartingRooms(JSONArray startingRoomsJson) {
    List<Room> startingRooms = new ArrayList<>();
    for (int i = 0; i < startingRoomsJson.length(); i++) {
      int roomID = startingRoomsJson.getInt(i);
      startingRooms.add(rooms.get(roomID));
    }
    return startingRooms;
  }

}
