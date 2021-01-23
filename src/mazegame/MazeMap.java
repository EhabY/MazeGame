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
  private final Set<Room> rooms;
  private final Set<Room> endRooms;
  private final long startingGold;
  private final List<Item> initialItems;
  private final long time;

  public static class Builder {
    private final Set<Room> rooms;
    private final Set<Room> endRooms;
    private long startingGold = 0;
    private List<Item> initialItems = new ArrayList<>();
    private long time = Long.MAX_VALUE;

    public Builder(Collection<Room> rooms, Collection<Room> endRooms) {
      this.rooms = Collections.unmodifiableSet(new HashSet<>(rooms));
      this.endRooms = Collections.unmodifiableSet(new HashSet<>(endRooms));
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
    this.endRooms = builder.endRooms;
    this.startingGold = builder.startingGold;
    this.initialItems = builder.initialItems;
    this.time = builder.time;
  }

  public Set<Room> getRooms() {
    return rooms;
  }

  public Set<Room> getEndRooms() {
    return endRooms;
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
        + "endRooms="
        + endRooms
        + ", startingGold="
        + startingGold
        + ", initialItems="
        + initialItems
        + ", time="
        + time
        + '}';
  }
}
