package mazegame.util;

import mazegame.Direction;
import mazegame.PlayerController;
import mazegame.JsonSerializable;
import mazegame.item.Key;
import mazegame.mapsite.Door;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Queue;

public class JsonSerializer {
  private static Set<Integer> visitedRooms;
  private static Queue<Room> roomsQueue;
  private static StringBuilder roomsJson;

  private JsonSerializer() {}

  public static String serializeGameState(PlayerController playerController) {
    visitedRooms = new HashSet<>();
    roomsQueue = new ArrayDeque<>();
    roomsJson = new StringBuilder();

    Room currentRoom = playerController.getCurrentRoom();
    serializeRooms(currentRoom);

    return "{" + playerController.toJson() + ",\"rooms\": [" + removeTrailingChar(roomsJson) + "]" + "}";
  }

  private static void serializeRooms(Room startRoom) {
    visitRoom(startRoom);
    while (!roomsQueue.isEmpty()) {
      Room currentRoom = roomsQueue.remove();
      serializeRoom(currentRoom);
      visitNeighboringRooms(currentRoom);
    }
  }

  private static void serializeRoom(Room room) {
    roomsJson.append(room.toJson()).append(",");
  }

  private static void visitNeighboringRooms(Room room) {
    for (Direction direction : Direction.values()) {
      MapSite mapSite = room.getMapSite(direction);
      tryToVisitNextRoom(mapSite, room);
    }
  }

  private static void tryToVisitNextRoom(MapSite mapSite, Room currentRoom) {
    if (isDoor(mapSite)) {
      Door door = (Door) mapSite;
      Room nextRoom = getRoomOnOtherSide(door, currentRoom);
      visitRoom(nextRoom);
    }
  }

  private static boolean isDoor(MapSite mapSite) {
    return mapSite instanceof Door;
  }

  private static Room getRoomOnOtherSide(Door door, Room room) {
    boolean toggled = toggleIfLocked(door);
    Room otherRoom = door.getNextRoom(room);

    if(toggled) {
      door.toggleLock(Key.MASTER_KEY);
    }

    return otherRoom;
  }

  private static boolean toggleIfLocked(Door door) {
    if(door.isLocked()) {
      door.toggleLock(Key.MASTER_KEY);
      return true;
    }

    return false;
  }

  private static void visitRoom(Room room) {
    if (hasNotVisitedRoom(room)) {
      markRoomAsVisited(room);
      roomsQueue.add(room);
    }
  }

  private static boolean hasNotVisitedRoom(Room room) {
    return !visitedRooms.contains(room.getId());
  }

  private static void markRoomAsVisited(Room room) {
    visitedRooms.add(room.getId());
  }

  public static StringBuilder removeTrailingChar(StringBuilder stringBuilder) {
    if (stringBuilder.length() > 0) {
      stringBuilder.setLength(stringBuilder.length() - 1);
    }

    return stringBuilder;
  }

  public static StringBuilder listToJson(List<? extends JsonSerializable> serializableList) {
    StringBuilder json = new StringBuilder();
    for (JsonSerializable serializable : serializableList) {
      json.append(serializable.toJson()).append(",");
    }

    return json;
  }
}
