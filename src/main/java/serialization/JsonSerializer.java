package serialization;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import mazegame.Direction;
import mazegame.PlayerController;
import mazegame.item.Key;
import mazegame.mapsite.Door;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSerializer {

  private final Encoder encoder = new JsonEncoder();
  private final Set<Integer> visitedRooms;
  private final Queue<Room> roomsQueue;
  private final JSONArray roomsJson;

  private JsonSerializer() {
    this.visitedRooms = new HashSet<>();
    this.roomsQueue = new ArrayDeque<>();
    this.roomsJson = new JSONArray();
  }

  public static String serializeGameState(PlayerController playerController) {
    JsonSerializer jsonSerializer = new JsonSerializer();
    return jsonSerializer.getSerializedGameState(playerController);
  }

  private String getSerializedGameState(PlayerController playerController) {
    JSONObject map = new JSONObject(playerController.encodeUsing(encoder));
    map.put("rooms", serializeRooms(playerController.getCurrentRoom()));
    return map.toString();
  }

  private JSONArray serializeRooms(Room startRoom) {
    visitRoom(startRoom);
    while (!roomsQueue.isEmpty()) {
      Room currentRoom = roomsQueue.remove();
      serializeRoom(currentRoom);
      visitNeighboringRooms(currentRoom);
    }
    return roomsJson;
  }

  private void serializeRoom(Room room) {
    JSONObject roomJson = new JSONObject(room.encodeUsing(encoder));
    roomsJson.put(roomJson);
  }

  private void visitNeighboringRooms(Room room) {
    for (Direction direction : Direction.values()) {
      MapSite mapSite = room.getMapSite(direction);
      tryToVisitNextRoom(mapSite, room);
    }
  }

  private void tryToVisitNextRoom(MapSite mapSite, Room currentRoom) {
    if (isDoor(mapSite)) {
      Door door = (Door) mapSite;
      Room nextRoom = getRoomOnOtherSide(door, currentRoom);
      visitRoom(nextRoom);
    }
  }

  private boolean isDoor(MapSite mapSite) {
    return mapSite instanceof Door;
  }

  private Room getRoomOnOtherSide(Door door, Room room) {
    boolean toggled = toggleIfLocked(door);
    Room otherRoom = door.getNextRoom(room);

    if (toggled) {
      door.toggleLock(Key.MASTER_KEY);
    }

    return otherRoom;
  }

  private boolean toggleIfLocked(Door door) {
    if (door.isLocked()) {
      door.toggleLock(Key.MASTER_KEY);
      return true;
    }

    return false;
  }

  private void visitRoom(Room room) {
    if (hasNotVisitedRoom(room)) {
      markRoomAsVisited(room);
      roomsQueue.add(room);
    }
  }

  private boolean hasNotVisitedRoom(Room room) {
    return !visitedRooms.contains(room.getId());
  }

  private void markRoomAsVisited(Room room) {
    visitedRooms.add(room.getId());
  }

}
