package mazegame.item;

import mazegame.JsonSerializable;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.util.ItemFormatter;
import mazegame.util.JsonSerializer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ItemManager implements JsonSerializable {
  private final Map<String, List<Item>> items = new CaseInsensitiveMap<>();

  public ItemManager() {}

  public ItemManager(Collection<? extends Item> itemsList) {
    add(itemsList);
  }

  public void add(Collection<? extends Item> itemsList) {
    for (Item item : itemsList) {
      add(item);
    }
  }

  public void add(Item item) {
    String name = item.getName();
    if (hasItem(name)) {
      items.get(name).add(item);
    } else {
      ArrayList<Item> itemList = new ArrayList<>();
      itemList.add(item);
      items.put(item.getName(), itemList);
    }
  }

  public boolean hasItem(String name) {
    return items.containsKey(name) && items.get(name).size() > 0;
  }

  public Item get(String name) {
    if (hasItem(name)) {
      return items.get(name).get(0);
    } else {
      throw new ItemNotFoundException("No " + name + " in inventory");
    }
  }

  public Item takeFromItems(String name) {
    Item item = get(name);
    removeByName(name);

    return item;
  }

  public void removeByName(String name) {
    if (hasItem(name)) {
      removeOneItem(name);
    } else {
      throw new ItemNotFoundException(name + " does not exist");
    }
  }

  private void removeOneItem(String name) {
    List<Item> itemList = items.get(name);
    int lastKeyIndex = itemList.size() - 1;
    itemList.remove(lastKeyIndex);

    if (lastKeyIndex == 0) {
      items.remove(name);
    }
  }

  @Override
  public String toString() {
    return ItemFormatter.formatItems(items);
  }

  @Override
  public String toJson() {
    StringBuilder itemsJson = new StringBuilder("[");

    for (Map.Entry<String, List<Item>> itemListEntry : items.entrySet()) {
      StringBuilder serializedList = JsonSerializer.listToJson(itemListEntry.getValue());
      itemsJson.append(serializedList);
    }

    return JsonSerializer.removeTrailingChar(itemsJson).append("]").toString();
  }
}
