package mazegame.player;

import mazegame.JsonSerializable;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.item.Flashlight;
import mazegame.item.Item;
import mazegame.item.ItemManager;
import java.util.Collection;
import java.util.Collections;

class Inventory implements JsonSerializable {
  private long gold;
  private final ItemManager itemManager;

  Inventory() {
    this(0);
  }

  Inventory(long gold) {
    this(gold, Collections.emptyList());
  }

  Inventory(long gold, Collection<? extends Item> initialItems) {
    this.gold = gold;
    itemManager = new ItemManager(initialItems);
  }

  long getGold() {
    return gold;
  }

  void addGold(long gold) {
    this.gold += requireNotNegative(gold);
  }

  void subtractGold(long gold) {
    boolean isNotEnough = this.gold < gold;
    if (isNotEnough) {
      throw new NotEnoughGoldException("Not enough gold");
    }

    this.gold -= requireNotNegative(gold);
  }

  private long requireNotNegative(long gold) {
    if (gold < 0) {
      throw new IllegalArgumentException("gold must be positive");
    }

    return gold;
  }

  Flashlight getFlashlight() {
    return (Flashlight) getItem(Flashlight.FLASHLIGHT_NAME);
  }

  boolean hasFlashlight() {
    return itemManager.hasItem(Flashlight.FLASHLIGHT_NAME);
  }

  Item getItem(String name) {
    return itemManager.get(name);
  }

  void addItems(Collection<? extends Item> itemsCollection) {
    itemManager.add(itemsCollection);
  }

  void addItem(Item item) {
    itemManager.add(item);
  }

  Item takeItem(String name) {
    return itemManager.takeFromItems(name);
  }

  @Override
  public String toString() {
    return "Gold = " + getGold() + "\n" + itemManager.toString();
  }

  @Override
  public String toJson() {
    return "\"gold\": " + gold + "," + "\"items\": " + itemManager.toJson();
  }
}
