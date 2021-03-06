package mazegame.player;

import java.util.Collections;
import java.util.List;
import mazegame.Direction;
import mazegame.Response;
import mazegame.item.Item;
import mazegame.mapsite.Checkable;
import mazegame.mapsite.Loot;
import mazegame.mapsite.MapSite;
import mazegame.room.Room;
import serialization.Encoder;
import serialization.JsonEncodable;

public class Player implements JsonEncodable {

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

  public void turnLeft() {
    position.turnLeft();
  }

  public void turnRight() {
    position.turnRight();
  }

  public Loot moveForward() {
    Loot loot = position.moveForward();
    inventory.addLoot(loot);
    return loot;
  }

  public Loot moveBackward() {
    Loot loot = position.moveBackward();
    inventory.addLoot(loot);
    return loot;
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

  public Response checkAhead() {
    Checkable checkable = (Checkable) getMapSiteAhead();
    return checkable.accept(checkVisitor);
  }

  public void switchLight() {
    position.switchLight();
  }

  public Direction getDirection() {
    return position.getDirection();
  }

  public long getGold() {
    return inventory.getGold();
  }

  public void addGoldToInventory(long gold) {
    inventory.addGold(gold);
  }

  public void removeGoldFromInventory(long gold) {
    inventory.subtractGold(gold);
  }

  public boolean hasItem(String name) {
    return inventory.hasItem(name);
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

  public void addLoot(Loot loot) {
    inventory.addLoot(loot);
  }

  public Loot getLoot() {
    return inventory.getInventoryAsLoot();
  }

  @Override
  public String toString() {
    return "Player status:\n" + "Facing " + position + "\n" + inventory.toString();
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }
}
