package mapgenerator;

import java.util.Queue;

import mapgenerator.mapsitegenerator.*;
import mazegame.Direction;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapGenerator {
    public static final int STEPS_PER_LEVEL = 3;
    public static final int SECONDS_PER_ROOM = 90;
    public static final int MINIMUM_DIFFICULTY = 1;
    public static final int MEDIUM_DIFFICULTY = 5;
    public static final long STARTING_GOLD = 10;
    private final int difficulty;
    private final int size;
    private final RoomGenerator roomGenerator;
    private final MovementManager movementManager;
    private final PathGenerator pathGenerator;
    private final WeightedRandomizer<MapSiteGenerator> mapSiteRandomizer;

    static class MapSiteLocation {
        int roomID;
        Direction direction;

        MapSiteLocation(int roomID, Direction direction) {
            this.roomID = roomID;
            this.direction = direction;
        }
    }

    private MapGenerator(int numberOfPlayers, int levels, int difficulty) {
        if(numberOfPlayers < 1) {
            throw new IllegalArgumentException("numberOfPlayers must be greater than or equal to 1");
        }

        if(levels < 1) {
            throw new IllegalArgumentException("levels must be greater than or equal to 1");
        }

        if(difficulty < 1 || difficulty > 10) {
            throw new IllegalArgumentException("difficulty must be greater than or equal to 1 and less than 10");
        }

        int side = STEPS_PER_LEVEL * levels * intSqrt(numberOfPlayers);
        RandomNameGenerator randomNameGenerator = new RandomNameGenerator(side);
        this.difficulty = difficulty;
        this.size = side * side;
        this.roomGenerator = new RoomGenerator(this.size, this.difficulty);
        this.movementManager = new MovementManager(side);
        this.pathGenerator = new PathGenerator(numberOfPlayers, levels, roomGenerator, movementManager, randomNameGenerator);
        this.mapSiteRandomizer = getMapSiteRandomizer(randomNameGenerator);
    }

    private int intSqrt(int number) {
        return (int)(Math.sqrt(number));
    }

    private WeightedRandomizer<MapSiteGenerator> getMapSiteRandomizer(RandomNameGenerator randomNameGenerator) {
        WeightedRandomizer<MapSiteGenerator> mapSiteRandomizer = new WeightedRandomizer<>();
        mapSiteRandomizer.addEvent(new DoorGenerator(randomNameGenerator), 5);
        mapSiteRandomizer.addEvent(new HangableGenerator(randomNameGenerator), 2);
        mapSiteRandomizer.addEvent(new ChestGenerator(randomNameGenerator), 1);
        mapSiteRandomizer.addEvent(new SellerGenerator(randomNameGenerator), 1);
        mapSiteRandomizer.addEvent(new WallGenerator(),1);
        return mapSiteRandomizer;
    }

    public static String generateMap(int numberOfPlayers, int levels) {
        return generateMap(numberOfPlayers, levels, MINIMUM_DIFFICULTY);
    }

    public static String generateMap(int numberOfPlayers, int levels, int difficulty) {
        MapGenerator mapGenerator = new MapGenerator(numberOfPlayers, levels, difficulty);
        return mapGenerator.generateMap();
    }

    private String generateMap() {
        JSONObject map = new JSONObject();
        map.put("rooms", generateRooms());
        map.put("mapConfiguration", getConfiguration());
        return map.toString();
    }

    private JSONObject getConfiguration() {
        JSONObject mapConfig = new JSONObject();
        mapConfig.put("endRoomID", this.size);
        mapConfig.put("time", 2 * intSqrt(this.size) * SECONDS_PER_ROOM / this.difficulty);
        mapConfig.put("startingRooms", roomGenerator.getStartingRooms());
        mapConfig.put("gold", STARTING_GOLD / this.difficulty);
        mapConfig.put("items", getStartingItemList());
        return mapConfig;
    }

    private JSONArray getStartingItemList() {
        JSONArray itemsJson = new JSONArray();
        if(difficulty <= MEDIUM_DIFFICULTY) {
            itemsJson.put(ItemGenerator.getFlashlightJson());
        }
        return itemsJson;
    }

    private JSONArray generateRooms() {
        pathGenerator.generateStartingPaths();
        Queue<Integer> roomsQueue = roomGenerator.getCreatedRoomsQueue();

        while(!roomsQueue.isEmpty()) {
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
        if(!roomGenerator.roomHasMapSite(roomID, direction)) {
            addRandomMapSite(roomID, direction);
        }
    }

    private void addRandomMapSite(int currentPosition, Direction direction) {
        JSONObject mapSiteJson = getValidMapSite(currentPosition, direction);
        if(isMapSiteDoor(mapSiteJson)) {
            mapSiteJson = fixDoorBetweenRooms(mapSiteJson, currentPosition, direction);
        }
        addMapSiteToRoomInDirection(mapSiteJson, currentPosition, direction);
    }

    private JSONObject getValidMapSite(int currentPosition, Direction direction) {
        JSONObject mapSiteJson;
        do {
            mapSiteJson = mapSiteRandomizer.nextElement().generate();
        } while(isInvalidMapSite(mapSiteJson, currentPosition, direction));
        return mapSiteJson;
    }

    private boolean isInvalidMapSite(JSONObject mapSiteJson, int currentPosition, Direction direction) {
        int nextPosition = movementManager.getPositionAfterMoving(currentPosition, direction);
        Direction oppositeDirection = direction.left().left();
        boolean cannotCreateDoor = !movementManager.isDirectionValid(currentPosition, direction);
        return isMapSiteDoor(mapSiteJson) &&
                (cannotCreateDoor || roomGenerator.roomHasMapSite(nextPosition, oppositeDirection));
    }

    private boolean isMapSiteDoor(JSONObject mapSiteJson) {
        return mapSiteJson.getString("mapSite").equalsIgnoreCase("Door");
    }

    private JSONObject fixDoorBetweenRooms(JSONObject doorJson, int currentPosition, Direction direction) {
        int nextPosition = movementManager.getPositionAfterMoving(currentPosition, direction);
        Direction oppositeDirection = direction.left().left();
        doorJson = setRoomIDsInDoor(currentPosition, nextPosition, doorJson);
        addMapSiteToRoomInDirection(DoorGenerator.getOppositeDoor(doorJson), nextPosition, oppositeDirection);
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

}
