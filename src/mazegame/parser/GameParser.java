package mazegame.parser;

import mazegame.Direction;
import mazegame.MazeMap;
import mazegame.item.Item;
import mazegame.mapsite.Door;
import mazegame.mapsite.SerializableMapSite;
import mazegame.room.LightSwitch;
import mazegame.room.NoLightSwitch;
import mazegame.room.Room;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameParser {
  private final Map<Integer, Room> rooms;
  private final MapSiteParser mapSiteParser;

  public static MazeMap parseJsonFile(String pathToFile) throws IOException {
    GameParser gameParser = new GameParser();
    String jsonString = gameParser.readWholeFile(pathToFile);
    JSONObject gameJson = new JSONObject(jsonString);
    gameParser.parseAllRooms(gameJson);
    return gameParser.parseMazeMap(gameJson);
  }

  private GameParser() {
    rooms = new HashMap<>();
    mapSiteParser = new MapSiteParser();
  }

  private String readWholeFile(String pathToFile) throws IOException {
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
    if(roomJson.has("loot")) {
      JSONObject lootJson = roomJson.getJSONObject("loot");
      return new Room(id, serializedMapSites, parseLightSwitch(lightswitch), ItemParser.parseLoot(lootJson));
    } else {
      return new Room(id, serializedMapSites, parseLightSwitch(lightswitch));
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
    List<MapSiteParser.DoorInfo> doors = mapSiteParser.doors;
    for (MapSiteParser.DoorInfo doorInfo : doors) {
      int roomID = doorInfo.roomID;
      int otherRoomID = doorInfo.otherRoomID;
      Door.Builder doorBuilder = doorInfo.doorBuilder;

      doorBuilder.setRoom(rooms.get(roomID));
      doorBuilder.setOtherRoom(rooms.get(otherRoomID));
    }
  }

  private MazeMap parseMazeMap(JSONObject gameJson) {
    JSONObject mazeMapJson = gameJson.getJSONObject("mapConfiguration");
    long gold = mazeMapJson.getLong("gold");
    long timeInSeconds = mazeMapJson.getLong("time");
    List<Item> initialItems = ItemParser.parseItemsArray(mazeMapJson.getJSONArray("items"));
    Room endRoom = rooms.get(mazeMapJson.getInt("endRoomID"));

    return new MazeMap.Builder(rooms.values(), endRoom)
        .startingGold(gold)
        .initialItems(initialItems)
        .time(timeInSeconds)
        .build();
  }

}
