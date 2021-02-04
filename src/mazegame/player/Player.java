package mazegame.player;

import mazegame.Direction;
import mazegame.JsonSerializable;
import mazegame.item.Item;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Loot;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import java.util.Collections;
import java.util.List;

public class Player implements JsonSerializable {
  private final String name;
  private final Position position;
  private final Inventory inventory;
  private final UseItemVisitor useItemVisitor;
  private final CheckVisitor checkVisitor;

  public Player(String name, Direction startingDirection, Room startingRoom) {
    this(name, startingDirection, startingRoom, 0);
  }

  public Player(String name, Direction startingDirection, Room startingRoom, long initialGold) {
    this(name, startingDirection, startingRoom, initialGold, Collections.emptyList());
  }

  public Player(
      String name,
      Direction startingDirection,
      Room startingRoom,
      long initialGold,
      List<? extends Item> initialItems) {
    this.name = name;
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

  public String moveForward() {
    Loot loot = position.moveForward();
    inventory.addLoot(loot);
    return loot.equals(Loot.EMPTY_LOOT) ? "" : loot.toString();
  }

  public String moveBackward() {
    Loot loot = position.moveBackward();
    inventory.addLoot(loot);
    return loot.equals(Loot.EMPTY_LOOT) ? "" : loot.toString();
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

  public void useItem(String itemName) {
    Item item = inventory.getItem(itemName);
    item.accept(useItemVisitor);
  }

  public void switchLight() {
    position.switchLight();
  }

  public void addLoot(Loot loot) {
    inventory.addLoot(loot);
  }

  public Loot getLoot() {
    return inventory.getInventoryAsLoot();
  }

  public String getName() {
    return name;
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
