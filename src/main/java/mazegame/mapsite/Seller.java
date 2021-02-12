package mazegame.mapsite;

import mazegame.exceptions.ItemNotFoundException;
import mazegame.item.Item;
import mazegame.item.ItemManager;
import mazegame.util.ItemFormatter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import serialization.Encoder;
import java.util.Collection;
import java.util.Map;

public class Seller implements SerializableMapSite {
  private static final String DESCRIPTION = "Seller";
  private final ItemManager itemManager;
  private final Map<String, Long> priceList;

  public Seller(Collection<? extends Item> items, Map<String, Long> priceList) {
    this.itemManager = new ItemManager(items);
    this.priceList = new CaseInsensitiveMap<>(priceList);
  }

  public void addItem(Item item) {
    itemManager.add(item);
  }

  public long getItemPrice(String name) {
    if (itemExists(name)) {
      return priceList.get(name);
    } else {
      throw new ItemNotFoundException("Item not found");
    }
  }

  public boolean itemExists(String name) {
    return priceList.containsKey(name);
  }

  public Item takeItem(String name) {
    return itemManager.takeFromItems(name);
  }

  public boolean hasItem(String name) {
    return itemManager.hasItem(name);
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Seller{" + "items=" + itemManager.toString() + ", priceList=" + priceList + '}';
  }
}
