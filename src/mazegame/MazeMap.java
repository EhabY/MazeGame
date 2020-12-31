package mazegame;

import mazegame.item.Item;
import mazegame.room.Room;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MazeMap {
  private final Direction startingOrientation;
  private final Room startRoom;
  private final Room endRoom;
  private final long startingGold;
  private final List<Item> initialItems;
  private final long time;

  public static class Builder {
    private final Direction startingOrientation;
    private final Room startRoom;
    private final Room endRoom;
    private long startingGold = 0;
    private List<Item> initialItems = new ArrayList<>();
    private long time = Long.MAX_VALUE;

    public Builder(Direction startingOrientation, Room startRoom, Room endRoom) {
      this.startingOrientation = startingOrientation;
      this.startRoom = startRoom;
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

  private MazeMap(Builder builder) {
    this.startingOrientation = builder.startingOrientation;
    this.startRoom = builder.startRoom;
    this.endRoom = builder.endRoom;
    this.startingGold = builder.startingGold;
    this.initialItems = builder.initialItems;
    this.time = builder.time;
  }

  public Direction getStartingOrientation() {
    return startingOrientation;
  }

  public Room getStartRoom() {
    return startRoom;
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
        + "startingOrientation="
        + startingOrientation
        + ", startRoom="
        + startRoom
        + ", endRoom="
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
