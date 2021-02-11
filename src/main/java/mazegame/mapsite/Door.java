package mazegame.mapsite;

import mazegame.Response;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.item.Key;
import mazegame.room.Room;
import serialization.Encoder;

public class Door extends AbstractLockable implements Checkable {
  private static final String DESCRIPTION = "Door";
  private Room room;
  private Room otherRoom;

  public static class Builder {
    private final Door door;

    public Builder(Key key, boolean locked) {
      door = new Door(key, locked);
    }

    public void setRoom(Room room) {
      door.room = room;
    }

    public void setOtherRoom(Room otherRoom) {
      door.otherRoom = otherRoom;
    }

    public Door getDoor() {
      return door;
    }
  }

  private Door(Key key, boolean locked) {
    super(key, locked);
  }

  public Room getNextRoom(Room roomFrom) {
    if(isLocked()) {
      throw new MapSiteLockedException(
              "Door locked, " + getKeyName() + " key is needed to unlock");
    }

    if (roomFrom.equals(this.room)) {
      return this.otherRoom;
    } else if (roomFrom.equals(this.otherRoom)) {
      return this.room;
    }

    throw new IllegalArgumentException("Invalid room provided");
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }

  @Override
  public Response accept(CheckableVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Door{" + "roomID=" + room.getId() + ", otherRoomID=" + otherRoom.getId() + '}';
  }
}
