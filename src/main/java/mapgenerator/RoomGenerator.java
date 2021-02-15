package mapgenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.SplittableRandom;
import mapgenerator.mapsitegenerator.WallGenerator;
import mazegame.Direction;
import org.json.JSONArray;
import org.json.JSONObject;

public class RoomGenerator {

  public static final int MINIMUM_DIFFICULTY = 1;
  public static final int MAXIMUM_DIFFICULTY = 10;
  private final int size;
  private final JSONObject[] rooms;
  private final int difficulty;
  private final List<Integer> startingRooms = new ArrayList<>();
  private final Queue<Integer> roomsQueue = new ArrayDeque<>();
  private final SplittableRandom random = new SplittableRandom();

  RoomGenerator(int size, int difficulty) {
    this.size = size;
    this.rooms = new JSONObject[this.size];
    this.difficulty = difficulty;
  }

  public JSONObject getRoom(int id) {
    return rooms[id];
  }

  public JSONObject createRoomIfNull(int id) {
    if (rooms[id] == null) {
      rooms[id] = getBasicRoom(id);
      roomsQueue.add(id);
    }
    return rooms[id];
  }

  private JSONObject getBasicRoom(int id) {
    JSONObject room = new JSONObject();
    room.put("id", id);
    room.put("lightswitch", getLightswitch());
    return room;
  }

  public JSONArray getAllRooms() {
    JSONArray jsonArray = new JSONArray();
    for (int i = 0; i < this.size; i++) {
      if (roomCreated(i)) {
        jsonArray.put(rooms[i]);
      }
    }
    jsonArray.put(getEndRoom());
    return jsonArray;
  }

  private JSONObject getEndRoom() {
    JSONObject endRoom = getBasicRoom(this.size);
    for (Direction direction : Direction.values()) {
      endRoom.put(direction.toString().toLowerCase(), WallGenerator.getWall());
    }
    return endRoom;
  }

  public int generateStartingPosition() {
    int currentPosition = getRandomRoomID();
    JSONObject currentRoom = createRoomIfNull(currentPosition);
    rooms[currentPosition] = currentRoom;
    startingRooms.add(currentPosition);
    return currentPosition;
  }

  private int getRandomRoomID() {
    int position;
    do {
      position = random.nextInt(this.size);
    } while (roomCreated(position));
    return position;
  }

  private boolean roomCreated(int position) {
    return rooms[position] != null;
  }

  private JSONObject getLightswitch() {
    JSONObject lightswitchJson = new JSONObject();
    lightswitchJson.put("hasLights", hasLights());
    lightswitchJson.put("lightsOn", areLightsOn());
    return lightswitchJson;
  }

  private boolean hasLights() {
    int chance = random.nextInt(MAXIMUM_DIFFICULTY);
    return chance < MAXIMUM_DIFFICULTY - difficulty + MINIMUM_DIFFICULTY;
  }

  private boolean areLightsOn() {
    return random.nextBoolean();
  }

  public List<Integer> getStartingRooms() {
    return startingRooms;
  }

  public Queue<Integer> getCreatedRoomsQueue() {
    return roomsQueue;
  }

  public boolean roomHasMapSite(int roomID, Direction direction) {
    return rooms[roomID] != null && rooms[roomID].has(direction.toString().toLowerCase());
  }
}
