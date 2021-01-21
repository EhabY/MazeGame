package mazegame.player;

import mazegame.Direction;
import mazegame.JsonSerializable;
import mazegame.item.Item;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Door;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import java.util.Collections;
import java.util.List;

public class Player implements JsonSerializable {
  private final Position position;
  private final Inventory inventory;
  private final UseItemVisitor useItemVisitor;
  private final CheckVisitor checkVisitor;

  public Player(Direction startingDirection, Room startingRoom) {
    this(startingDirection, startingRoom, 0);
  }

  public Player(Direction startingDirection, Room startingRoom, long initialGold) {
    this(startingDirection, startingRoom, initialGold, Collections.emptyList());
  }

  public Player(
      Direction startingDirection,
      Room startingRoom,
      long initialGold,
      List<? extends Item> initialItems) {
    this.position = new Position(startingRoom, startingDirection);
    this.inventory = new Inventory(initialGold, initialItems);
    this.useItemVisitor = new UseItemVisitor(this);
    this.checkVisitor = new CheckVisitor(inventory);
  }

  public String getStatus() {
    return toString();
  }

  public void turnLeft() {
    position.turnLeft();
  }

  public void turnRight() {
    position.turnRight();
  }

  public void moveForward() {
    position.moveForward();
  }

  public void moveBackward() {
    position.moveBackward();
  }

  public Room getCurrentRoom() {
    return position.getCurrentRoom();
  }

  public MapSite getMapSiteAhead() {
    return position.getMapSiteAhead();
  }

  public MapSite getMapSiteBehind() {
    return position.getMapSiteBehind();
  }

  public String look() {
    MapSite mapSite;
    if (inventory.hasFlashlight()) {
      mapSite = position.lookAheadWithFlashlight(inventory.getFlashlight());
    } else {
      mapSite = position.lookAhead();
    }

    return mapSite.look();
  }

  public String checkAhead() {
    Checkable checkable = (Checkable) getMapSiteAhead();
    return checkable.accept(checkVisitor);
  }

  public void addGoldToInventory(long gold) {
    inventory.addGold(gold);
  }

  public void removeGoldFromInventory(long gold) {
    inventory.subtractGold(gold);
  }

  public void addItemToInventory(Item item) {
    inventory.addItem(item);
  }

  public Item takeItemFromInventory(String name) {
    return inventory.takeItem(name);
  }

  public String openDoor() {
    Door door = (Door) getMapSiteAhead();
    if (door.isLocked()) {
      return door.getKeyName() + " key required to unlock";
    } else {
      return "Nothing happens";
    }
  }

  public void useItem(String itemName) {
    Item item = inventory.getItem(itemName);
    item.accept(useItemVisitor);
  }

  public void switchLight() {
    position.switchLight();
  }

  public long getScore() {
    return inventory.getScore();
  }

  @Override
  public String toString() {
    return "Player status:\n" + "Facing " + position + "\n" + inventory.toString();
  }

  @Override
  public String toJson() {
    return "\"orientation\": \""
        + position.getDirection().toString().toLowerCase()
        + "\","
        + inventory.toJson();
  }
}
