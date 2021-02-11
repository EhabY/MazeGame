package mazegame.item;

import mazegame.exceptions.ItemNotFoundException;
import mazegame.util.ItemFormatter;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ItemManager {
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
    String name = getNameType(item);
    if (hasItem(name)) {
      items.get(name).add(item);
    } else {
      ArrayList<Item> itemList = new ArrayList<>();
      itemList.add(item);
      items.put(name, itemList);
    }
  }

  private String getNameType(Item item) {
    String name = item.getName();
    String type = item.getType();
    return name.equals("") ? type : name + " " + type;
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

  public List<Item> getItemList() {
    List<Item> itemList = new ArrayList<>();
    for(Map.Entry<String, List<Item>> entry : items.entrySet()) {
      itemList.addAll(entry.getValue());
    }

    return itemList;
  }

  @Override
  public String toString() {
    return ItemFormatter.formatItems(items);
  }
}
