package mazegame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mazegame.item.Item;
import mazegame.room.Room;

public class MazeMap {

  private final List<Room> rooms;
  private final List<Room> startingRooms;
  private final Room endRoom;
  private final long startingGold;
  private final List<Item> initialItems;
  private final long time;

  private MazeMap(Builder builder) {
    this.rooms = builder.rooms;
    this.startingRooms = builder.startingRooms;
    this.endRoom = builder.endRoom;
    this.startingGold = builder.startingGold;
    this.initialItems = builder.initialItems;
    this.time = builder.time;
  }

  public List<Room> getRooms() {
    return rooms;
  }

  public List<Room> getStartingRooms() {
    return startingRooms;
  }

  public Room getEndRoom() {
    return endRoom;
  }

  public long getStartingGold() {
    return startingGold;
  }

  public List<Item> getInitialItems() {
    return initialItems;
  }

  public long getTimeInSeconds() {
    return time;
  }

  @Override
  public String toString() {
    return "MazeMap{"
        + "endRoom="
        + endRoom
        + ", startingGold="
        + startingGold
        + ", initialItems="
        + initialItems
        + ", time="
        + time
        + '}';
  }

  public static class Builder {

    private final List<Room> rooms;
    private final List<Room> startingRooms;
    private final Room endRoom;
    private long startingGold = 0;
    private List<Item> initialItems = new ArrayList<>();
    private long time = Long.MAX_VALUE;

    public Builder(Collection<Room> rooms, Collection<Room> startingRooms, Room endRoom) {
      this.rooms = Collections.unmodifiableList(new ArrayList<>(rooms));
      this.startingRooms = Collections.unmodifiableList(new ArrayList<>(startingRooms));
      this.endRoom = endRoom;
    }

    public Builder startingGold(long gold) {
      this.startingGold = gold;
      return this;
    }

    public Builder initialItems(Collection<? extends Item> initialItems) {
      this.initialItems = Collections.unmodifiableList(new ArrayList<>(initialItems));
      return this;
    }

    public Builder time(long seconds) {
      this.time = seconds;
      return this;
    }

    public MazeMap build() {
      return new MazeMap(this);
    }
  }
}
