package mapgenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SplittableRandom;
import mapgenerator.mapsitegenerator.DoorGenerator;
import mapgenerator.mapsitegenerator.KeyHolderGenerator;
import mazegame.Direction;
import org.json.JSONObject;

public class PathGenerator {

  private final int numberOfPlayers;
  private final int levels;
  private final int stepsPerLevel;
  private final RoomGenerator roomGenerator;
  private final PositionManager positionManager;
  private final RandomNameGenerator randomNameGenerator;
  private final SplittableRandom random = new SplittableRandom();

  public PathGenerator(int numberOfPlayers, int levels, int stepsPerLevel,
      RoomGenerator roomGenerator, PositionManager positionManager,
      RandomNameGenerator randomNameGenerator) {
    this.numberOfPlayers = numberOfPlayers;
    this.levels = levels;
    this.stepsPerLevel = stepsPerLevel;
    this.roomGenerator = Objects.requireNonNull(roomGenerator);
    this.positionManager = Objects.requireNonNull(positionManager);
    this.randomNameGenerator = Objects.requireNonNull(randomNameGenerator);
  }

  public void generateStartingPaths() {
    List<List<Direction>> paths = new ArrayList<>();
    for (int i = 0; i < numberOfPlayers; i++) {
      paths.add(generatePathList());
    }

    List<Integer> startingRooms = roomGenerator.getStartingRooms();
    for (int i = 0; i < numberOfPlayers; i++) {
      placeKeysInPath(startingRooms.get(i), paths.get(i));
    }
  }

  private List<Direction> generatePathList() {
    int currentPosition = roomGenerator.generateStartingPosition();
    List<Direction> path = generateRandomPathFrom(currentPosition);
    int endOfPath = createPath(currentPosition, path);
    generateWinningDoor(endOfPath);
    return path;
  }

  private List<Direction> generateRandomPathFrom(int position) {
    int iterations = levels * stepsPerLevel;
    List<Direction> path = new ArrayList<>(iterations);
    Direction previousDirection = null;
    while (path.size() < iterations) {
      Direction direction = getUniqueValidDirection(position, previousDirection);
      path.add(direction);
      position = positionManager.getPositionAfterMoving(position, direction);
      previousDirection = direction.left().left();
    }
    return path;
  }

  private Direction getUniqueValidDirection(int position, Direction previousDirection) {
    Direction direction = getRandomDirection();
    while (isNotUniqueOrValid(position, direction, previousDirection)) {
      direction = getRandomDirection();
    }
    return direction;
  }

  private boolean isNotUniqueOrValid(int position, Direction direction,
      Direction previousDirection) {
    return direction == previousDirection || !positionManager.isDirectionValid(position, direction);
  }

  private Direction getRandomDirection() {
    return Direction.values()[random.nextInt(Direction.values().length)];
  }

  private int createPath(int currentPosition, List<Direction> path) {
    for (Direction stepDirection : path) {
      createDoorIfEmptyDirection(currentPosition, stepDirection);
      currentPosition = positionManager.getPositionAfterMoving(currentPosition, stepDirection);
    }
    return currentPosition;
  }

  private void createDoorIfEmptyDirection(int currentPosition, Direction stepDirection) {
    int nextPosition = positionManager.getPositionAfterMoving(currentPosition, stepDirection);
    roomGenerator.createRoomIfNull(nextPosition);
    if (!roomGenerator.roomHasMapSite(currentPosition, stepDirection)) {
      createDoorBetweenRooms(currentPosition, nextPosition, stepDirection);
    }
  }

  private void createDoorBetweenRooms(int roomID, int otherRoomID, Direction directionFrom) {
    JSONObject doorJson = DoorGenerator
        .getCustomJson(roomID, otherRoomID, randomNameGenerator.getRandomName());
    addMapSiteToRoomInDirection(doorJson, roomID, directionFrom);
    addMapSiteToRoomInDirection(DoorGenerator.getOppositeDoor(doorJson), otherRoomID,
        directionFrom.left().left());
  }

  private void addMapSiteToRoomInDirection(JSONObject mapSite, int roomID, Direction direction) {
    JSONObject roomJson = roomGenerator.createRoomIfNull(roomID);
    roomJson.put(direction.toString().toLowerCase(), mapSite);
  }

  private void generateWinningDoor(int endOfPath) {
    Direction direction = getEmptyRandomDirection(endOfPath);
    int side = positionManager.getSide();
    JSONObject winningDoor = DoorGenerator
        .getCustomJson(endOfPath, side * side, randomNameGenerator.getRandomName());
    addMapSiteToRoomInDirection(winningDoor, endOfPath, direction);
  }

  private Direction getEmptyRandomDirection(int roomID) {
    Direction direction = getRandomDirection();
    while (roomGenerator.roomHasMapSite(roomID, direction)) {
      direction = getRandomDirection();
    }
    return direction;
  }

  private void placeKeysInPath(int currentPosition, List<Direction> path) {
    Queue<String> keyNames = getKeyNamesInPath(currentPosition, path);
    List<MapSiteLocation> potentialPlaces = new ArrayList<>();
    for (int i = 0; i < path.size(); i++) {
      Direction stepDirection = path.get(i);
      potentialPlaces.addAll(getEmptyDirections(currentPosition));
      if (reachedEndOfLevel(i)) {
        boolean canPlaceKey = !potentialPlaces.isEmpty();
        setDoorLock(currentPosition, stepDirection, canPlaceKey);
        tryToPlaceKey(keyNames.remove(), potentialPlaces);
      }
      currentPosition = positionManager.getPositionAfterMoving(currentPosition, stepDirection);
    }
  }

  private Queue<String> getKeyNamesInPath(int currentPosition, List<Direction> path) {
    Queue<String> keyNames = new ArrayDeque<>();
    for (int i = 0; i < path.size(); i++) {
      Direction stepDirection = path.get(i);
      if (reachedEndOfLevel(i)) {
        keyNames.add(getKeyAtEndOfPath(currentPosition, stepDirection));
      }
      currentPosition = positionManager.getPositionAfterMoving(currentPosition, stepDirection);
    }

    return keyNames;
  }

  private String getKeyAtEndOfPath(int currentPosition, Direction direction) {
    JSONObject currentRoom = roomGenerator.getRoom(currentPosition);
    JSONObject doorJson = currentRoom.getJSONObject(direction.toString().toLowerCase());
    return getKeyNameFromJson(doorJson);
  }

  private String getKeyNameFromJson(JSONObject doorJson) {
    return doorJson.getString("key");
  }

  private List<MapSiteLocation> getEmptyDirections(int currentPosition) {
    List<MapSiteLocation> emptyDirections = new ArrayList<>();
    for (Direction direction : Direction.values()) {
      MapSiteLocation mapSiteLocation = new MapSiteLocation(currentPosition, direction);
      addLocationIfEmpty(mapSiteLocation, emptyDirections);
    }
    return emptyDirections;
  }

  private void addLocationIfEmpty(MapSiteLocation location, List<MapSiteLocation> locations) {
    JSONObject currentRoom = roomGenerator.getRoom(location.roomID);
    Direction direction = location.direction;
    if (!currentRoom.has(direction.toString().toLowerCase())) {
      locations.add(location);
    }
  }

  private boolean reachedEndOfLevel(int i) {
    return (i + 1) % stepsPerLevel == 0;
  }

  private void setDoorLock(int currentPosition, Direction direction, boolean locked) {
    int nextPosition = positionManager.getPositionAfterMoving(currentPosition, direction);
    Direction oppositeDirection = direction.left().left();
    JSONObject currentRoom = roomGenerator.getRoom(currentPosition);
    JSONObject nextRoom = roomGenerator.getRoom(nextPosition);
    setDoorJsonLock(currentRoom.getJSONObject(direction.toString().toLowerCase()), locked);
    setDoorJsonLock(nextRoom.getJSONObject(oppositeDirection.toString().toLowerCase()), locked);
  }

  private void setDoorJsonLock(JSONObject doorJson, boolean locked) {
    doorJson.put("locked", locked);
  }

  private void tryToPlaceKey(String keyName, List<MapSiteLocation> potentialPlaces) {
    if (!potentialPlaces.isEmpty()) {
      placeKeyRandomly(keyName, potentialPlaces);
    }
    potentialPlaces.clear();
  }

  private void placeKeyRandomly(String keyName,
      List<MapSiteLocation> potentialPlaces) {
    int randomIndex = random.nextInt(potentialPlaces.size());
    MapSiteLocation mapSiteLocation = potentialPlaces.get(randomIndex);
    JSONObject roomWithKey = roomGenerator.getRoom(mapSiteLocation.roomID);
    roomWithKey.put(mapSiteLocation.direction.toString().toLowerCase(),
        KeyHolderGenerator.getKeyHolder(keyName));
  }

}
