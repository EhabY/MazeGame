package mazegame.player;

import mazegame.exceptions.NotEnoughGoldException;
import mazegame.item.Flashlight;
import mazegame.item.Item;
import mazegame.item.ItemManager;
import mazegame.mapsite.Loot;
import java.util.Collection;
import java.util.Collections;

class Inventory {
  private long gold;
  private final ItemManager itemManager;

  Inventory(long gold) {
    this(gold, Collections.emptyList());
  }

  Inventory(long gold, Collection<? extends Item> initialItems) {
    this.gold = requireNotNegative(gold);
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

  boolean hasItem(String name) {
    return itemManager.hasItem(name);
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

  Loot getInventoryAsLoot() {
    return new Loot(getGold(), itemManager.getItemList());
  }

  void addLoot(Loot loot) {
    addItems(loot.getItems());
    addGold(loot.getGold());
  }

  @Override
  public String toString() {
    return "Gold = " + getGold() + "\n" + itemManager.toString();
  }
}
