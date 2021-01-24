package mazegame;

import mazegame.item.Item;
import mazegame.room.Room;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MazeMap {
  private final List<Room> rooms;
  private final Room endRoom;
  private final long startingGold;
  private final List<Item> initialItems;
  private final long time;

  public static class Builder {
    private final List<Room> rooms;
    private final Room endRoom;
    private long startingGold = 0;
    private List<Item> initialItems = new ArrayList<>();
    private long time = Long.MAX_VALUE;

    public Builder(Collection<Room> rooms, Room endRoom) {
      this.rooms = getRandomizedStartRooms(rooms, endRoom);
      this.endRoom = endRoom;
    }

    private List<Room> getRandomizedStartRooms(Collection<Room> rooms, Room endRoom) {
      List<Room> startRooms = new ArrayList<>(rooms);
      startRooms.remove(endRoom);
      Collections.shuffle(startRooms);
      return Collections.unmodifiableList(startRooms);
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

  private MazeMap(Builder builder) {
    this.rooms = builder.rooms;
    this.endRoom = builder.endRoom;
    this.startingGold = builder.startingGold;
    this.initialItems = builder.initialItems;
    this.time = builder.time;
  }

  public List<Room> getRooms() {
    return rooms;
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
}
