package mazegame.player;

import mazegame.Direction;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.item.Flashlight;
import mazegame.mapsite.DarkMapSite;
import mazegame.mapsite.Door;
import mazegame.mapsite.Loot;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import java.util.Objects;

class Position {
  private Direction direction;
  private Room currentRoom;

  Position(Room startingRoom, Direction startingDirection) {
    this.currentRoom = Objects.requireNonNull(startingRoom);
    this.direction = Objects.requireNonNull(startingDirection);
  }

  void turnLeft() {
    direction = direction.left();
  }

  void turnRight() {
    direction = direction.right();
  }

  Loot moveForward() {
    Door door = (Door) getMapSiteAhead();
    return goToNextRoom(door);
  }

  MapSite getMapSiteAhead() {
    return currentRoom.getMapSite(direction);
  }

  Loot moveBackward() {
    Door door = (Door) getMapSiteBehind();
    return goToNextRoom(door);
  }

  MapSite getMapSiteBehind() {
    Direction oppositeDirection = direction.left().left();
    return currentRoom.getMapSite(oppositeDirection);
  }

  private Loot goToNextRoom(Door door) {
    if (door.isLocked()) {
      throw new MapSiteLockedException("Door is locked");
    } else {
      currentRoom = door.getNextRoom(currentRoom);
      return currentRoom.acquireLoot();
    }
  }

  MapSite lookAheadWithFlashlight(Flashlight flashlight) {
    if (flashlight.isTurnedOn()) {
      return getMapSiteAhead();
    } else {
      return lookAhead();
    }
  }

  MapSite lookAhead() {
    if (currentRoom.isLit()) {
      return getMapSiteAhead();
    } else {
      return DarkMapSite.getInstance();
    }
  }

  void switchLight() {
    currentRoom.toggleLights();
  }

  public Room getCurrentRoom() {
    return currentRoom;
  }

  public Direction getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return direction.toString();
  }
}
