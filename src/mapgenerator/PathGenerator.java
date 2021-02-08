package mapgenerator;

import mapgenerator.mapsitegenerator.DoorGenerator;
import mapgenerator.mapsitegenerator.KeyHolderMapSiteGenerator;
import mazegame.Direction;
import org.json.JSONObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SplittableRandom;

public class PathGenerator {
    private static final int STEPS_PER_LEVEL = 3;
    private final int numberOfPlayers;
    private final int levels;
    private final RoomGenerator roomGenerator;
    private final MovementManager movementManager;
    private final RandomNameGenerator randomNameGenerator;
    private final SplittableRandom random = new SplittableRandom();

    public PathGenerator(int numberOfPlayers, int levels, RoomGenerator roomGenerator, MovementManager movementManager, RandomNameGenerator randomNameGenerator) {
        this.numberOfPlayers = numberOfPlayers;
        this.levels = levels;
        this.roomGenerator = Objects.requireNonNull(roomGenerator);
        this.movementManager = Objects.requireNonNull(movementManager);
        this.randomNameGenerator = Objects.requireNonNull(randomNameGenerator);
    }

    public void generateStartingPaths() {
        List<List<Direction>> paths = new ArrayList<>();
        for(int i = 0; i < numberOfPlayers; i++) {
            paths.add(generatePathList());
        }

        List<Integer> startingRooms = roomGenerator.getStartingRooms();
        for(int i = 0; i < numberOfPlayers; i++) {
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
        int iterations = levels * STEPS_PER_LEVEL;
        List<Direction> path = new ArrayList<>(iterations);
        Direction previousDirection = null;
        while(path.size() < iterations) {
            Direction direction = getRandomDirection();
            if(direction != previousDirection && movementManager.isDirectionValid(position, direction)) {
                path.add(direction);
                position = movementManager.getPositionAfterMoving(position, direction);
                previousDirection = direction.left().left();
            }
        }
        return path;
    }

    private Direction getRandomDirection() {
        return Direction.values()[random.nextInt(Direction.values().length)];
    }

    private int createPath(int currentPosition, List<Direction> path) {
        for(Direction stepDirection : path) {
            int nextPosition = movementManager.getPositionAfterMoving(currentPosition, stepDirection);
            roomGenerator.createRoomIfNull(nextPosition);
            if(!roomGenerator.roomHasMapSite(currentPosition, stepDirection)) {
                createDoorBetweenRooms(currentPosition, nextPosition, stepDirection);
            }
            currentPosition = nextPosition;
        }
        return currentPosition;
    }

    private void createDoorBetweenRooms(int roomID, int otherRoomID, Direction directionFrom) {
        JSONObject doorJson = DoorGenerator.getCustomJson(roomID, otherRoomID, randomNameGenerator.getRandomName());
        addMapSiteToRoomInDirection(doorJson, roomID, directionFrom);
        addMapSiteToRoomInDirection(DoorGenerator.getOppositeDoor(doorJson), otherRoomID, directionFrom.left().left());
    }

    private void addMapSiteToRoomInDirection(JSONObject mapSite, int roomID, Direction direction) {
        JSONObject roomJson = roomGenerator.createRoomIfNull(roomID);
        roomJson.put(direction.toString().toLowerCase(), mapSite);
    }

    private void generateWinningDoor(int endOfPath) {
        Direction direction;
        do {
            direction = getRandomDirection();
        } while(roomGenerator.roomHasMapSite(endOfPath, direction));
        int side = movementManager.getSide();
        JSONObject winningDoor = DoorGenerator.getCustomJson(endOfPath, side * side, randomNameGenerator.getRandomName());
        addMapSiteToRoomInDirection(winningDoor, endOfPath, direction);
    }

    private void placeKeysInPath(int currentPosition, List<Direction> path) {
        Queue<String> keyNames = getKeyNamesInPath(currentPosition, path);
        List<MapGenerator.MapSiteLocation> potentialPlaces = new ArrayList<>();
        for(int i = 0; i < path.size(); i++) {
            Direction stepDirection = path.get(i);
            potentialPlaces.addAll(getEmptyDirections(currentPosition));
            if(reachedEndOfLevel(i)) {
                boolean canPlaceKey = !potentialPlaces.isEmpty();
                setDoorLock(currentPosition, stepDirection, canPlaceKey);
                tryToPlaceKey(keyNames.remove(), potentialPlaces);
            }
            currentPosition = movementManager.getPositionAfterMoving(currentPosition, stepDirection);
        }
    }

    private Queue<String> getKeyNamesInPath(int currentPosition, List<Direction> path) {
        Queue<String> keyNames = new ArrayDeque<>();
        for(int i = 0; i < path.size(); i++) {
            Direction stepDirection = path.get(i);
            if(reachedEndOfLevel(i)) {
                JSONObject currentRoom = roomGenerator.getRoom(currentPosition);
                JSONObject doorJson = currentRoom.getJSONObject(stepDirection.toString().toLowerCase());
                keyNames.add(getKeyNameFromJson(doorJson));
            }
            currentPosition = movementManager.getPositionAfterMoving(currentPosition, stepDirection);
        }

        return keyNames;
    }

    private String getKeyNameFromJson(JSONObject doorJson) {
        return doorJson.getString("key");
    }

    private List<MapGenerator.MapSiteLocation> getEmptyDirections(int currentPosition) {
        List<MapGenerator.MapSiteLocation> emptyDirections = new ArrayList<>();
        JSONObject currentRoom = roomGenerator.getRoom(currentPosition);
        for(Direction direction : Direction.values()) {
            String directionName = direction.toString().toLowerCase();
            if(!currentRoom.has(directionName)) {
                emptyDirections.add(new MapGenerator.MapSiteLocation(currentPosition, direction));
            }
        }

        return emptyDirections;
    }

    private boolean reachedEndOfLevel(int i) {
        return (i + 1) % STEPS_PER_LEVEL == 0;
    }

    private void setDoorLock(int currentPosition, Direction direction, boolean locked) {
        int nextPosition = movementManager.getPositionAfterMoving(currentPosition, direction);
        Direction oppositeDirection = direction.left().left();
        JSONObject currentRoom = roomGenerator.getRoom(currentPosition);
        JSONObject nextRoom = roomGenerator.getRoom(nextPosition);
        setDoorJsonLock(currentRoom.getJSONObject(direction.toString().toLowerCase()), locked);
        setDoorJsonLock(nextRoom.getJSONObject(oppositeDirection.toString().toLowerCase()), locked);
    }

    private void setDoorJsonLock(JSONObject doorJson, boolean locked) {
        doorJson.put("locked", locked);
    }

    private void tryToPlaceKey(String keyName, List<MapGenerator.MapSiteLocation> potentialPlaces) {
        if(!potentialPlaces.isEmpty()) {
            placeKeyRandomly(keyName, potentialPlaces);
        }
        potentialPlaces.clear();
    }

    private void placeKeyRandomly(String keyName, List<MapGenerator.MapSiteLocation> potentialPlaces) {
        int randomIndex = random.nextInt(potentialPlaces.size());
        MapGenerator.MapSiteLocation mapSiteLocation = potentialPlaces.get(randomIndex);
        JSONObject roomWithKey = roomGenerator.getRoom(mapSiteLocation.roomID);
        roomWithKey.put(mapSiteLocation.direction.toString().toLowerCase(), KeyHolderMapSiteGenerator.getKeyHolderMapSite(keyName));
    }

}
