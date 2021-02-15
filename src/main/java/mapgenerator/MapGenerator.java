package mapgenerator;

import java.util.Queue;
import mapgenerator.mapsitegenerator.DoorGenerator;
import mapgenerator.mapsitegenerator.MapSiteGenerator;
import mazegame.Direction;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapGenerator {

  private final MapConfiguration mapConfiguration;
  private final RoomGenerator roomGenerator;
  private final PositionManager positionManager;
  private final PathGenerator pathGenerator;
  private final WeightedRandomizer<MapSiteGenerator> mapSiteRandomizer;

  private MapGenerator(MapConfiguration mapConfiguration) {
    int side = mapConfiguration.getSide();
    int size = side * side;
    RandomNameGenerator randomNameGenerator = new RandomNameGenerator(side);
    this.mapConfiguration = mapConfiguration;
    this.roomGenerator = new RoomGenerator(size, mapConfiguration.getDifficulty());
    this.positionManager = new PositionManager(side);
    this.pathGenerator = new PathGenerator(mapConfiguration.getNumberOfPlayers(),
        mapConfiguration.getLevels(), mapConfiguration.getStepsPerLevel(), roomGenerator,
        positionManager, randomNameGenerator);
    this.mapSiteRandomizer = mapConfiguration.getMapSiteRandomizer(randomNameGenerator);
  }

  public static String generateMap(MapConfiguration mapConfiguration) {
    MapGenerator mapGenerator = new MapGenerator(mapConfiguration);
    return mapGenerator.generateMap();
  }

  private String generateMap() {
    JSONObject map = new JSONObject();
    map.put("rooms", generateRooms());
    JSONObject mapConfigJson = mapConfiguration.getConfiguration();
    mapConfigJson.put("startRoomsID", roomGenerator.getStartingRooms());
    map.put("mapConfiguration", mapConfigJson);
    return map.toString();
  }

  private JSONArray generateRooms() {
    pathGenerator.generateStartingPaths();
    Queue<Integer> roomsQueue = roomGenerator.getCreatedRoomsQueue();

    while (!roomsQueue.isEmpty()) {
      int currentPosition = roomsQueue.remove();
      addMapSitesToRoom(currentPosition);
    }

    return roomGenerator.getAllRooms();
  }

  private void addMapSitesToRoom(int roomID) {
    for (Direction direction : Direction.values()) {
      addRandomMapSiteIfUndefined(roomID, direction);
    }
  }

  private void addRandomMapSiteIfUndefined(int roomID, Direction direction) {
    if (!roomGenerator.roomHasMapSite(roomID, direction)) {
      addRandomMapSite(roomID, direction);
    }
  }

  private void addRandomMapSite(int currentPosition, Direction direction) {
    JSONObject mapSiteJson = getValidMapSite(currentPosition, direction);
    if (isMapSiteDoor(mapSiteJson)) {
      mapSiteJson = fixDoorBetweenRooms(mapSiteJson, currentPosition, direction);
    }
    addMapSiteToRoomInDirection(mapSiteJson, currentPosition, direction);
  }

  private JSONObject getValidMapSite(int currentPosition, Direction direction) {
    JSONObject mapSiteJson;
    do {
      mapSiteJson = mapSiteRandomizer.nextElement().generate();
    } while (isInvalidMapSite(mapSiteJson, currentPosition, direction));
    return mapSiteJson;
  }

  private boolean isInvalidMapSite(JSONObject mapSiteJson, int currentPosition,
      Direction direction) {
    int nextPosition = positionManager.getPositionAfterMoving(currentPosition, direction);
    Direction oppositeDirection = direction.left().left();
    boolean cannotCreateDoor = !positionManager.isDirectionValid(currentPosition, direction);
    return isMapSiteDoor(mapSiteJson) &&
        (cannotCreateDoor || roomGenerator.roomHasMapSite(nextPosition, oppositeDirection));
  }

  private boolean isMapSiteDoor(JSONObject mapSiteJson) {
    return mapSiteJson.getString("mapSite").equalsIgnoreCase("Door");
  }

  private JSONObject fixDoorBetweenRooms(JSONObject doorJson, int currentPosition,
      Direction direction) {
    int nextPosition = positionManager.getPositionAfterMoving(currentPosition, direction);
    Direction oppositeDirection = direction.left().left();
    doorJson = setRoomIDsInDoor(currentPosition, nextPosition, doorJson);
    addMapSiteToRoomInDirection(DoorGenerator.getOppositeDoor(doorJson), nextPosition,
        oppositeDirection);
    return doorJson;
  }

  private JSONObject setRoomIDsInDoor(int from, int to, JSONObject doorJson) {
    doorJson.put("roomID", from);
    doorJson.put("otherRoomID", to);
    return doorJson;
  }

  private void addMapSiteToRoomInDirection(JSONObject mapSite, int roomID, Direction direction) {
    JSONObject roomJson = roomGenerator.createRoomIfNull(roomID);
    roomJson.put(direction.toString().toLowerCase(), mapSite);
  }

  static class MapSiteLocation {

    int roomID;
    Direction direction;

    MapSiteLocation(int roomID, Direction direction) {
      this.roomID = roomID;
      this.direction = direction;
    }
  }

}
