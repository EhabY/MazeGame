package mazegame.util;

import mazegame.item.Item;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.text.WordUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemFormatter {

  private ItemFormatter() {}

  public static String formatItems(List<? extends Item> itemsList) {
    return formatItems(fromListToMap(itemsList));
  }

  private static Map<String, List<Item>> fromListToMap(List<? extends Item> itemsList) {
    Map<String, List<Item>> itemsMap = new CaseInsensitiveMap<>();
    for (Item item : itemsList) {
      addItemToMap(item, itemsMap);
    }

    return itemsMap;
  }

  private static void addItemToMap(Item item, Map<String, List<Item>> itemsMap) {
    String name = item.getName();
    boolean hasItem = itemsMap.containsKey(name);
    if (hasItem) {
      itemsMap.get(name).add(item);
    } else {
      ArrayList<Item> itemList = new ArrayList<>();
      itemList.add(item);
      itemsMap.put(item.getName(), itemList);
    }
  }

  public static String formatItems(Map<String, ? extends List<? extends Item>> items) {
    StringBuilder formattedItems = new StringBuilder("Items: [\n");
    items.forEach((keyName, keyList) -> formattedItems.append(formatItem(keyName, keyList.size())));

    return formattedItems.append("]\n").toString();
  }

  private static String formatItem(String itemName, int quantity) {
    return "\t" + quantity + " x " + WordUtils.capitalize(itemName) + "\n";
  }

  public static String formatPriceList(Map<String, Long> priceList) {
    StringBuilder formattedListing = new StringBuilder("Price list: [\n");
    priceList.forEach((name, price) -> formattedListing.append(formatListing(name, price)));
    formattedListing.append("]\n");
    return formattedListing.toString();
  }

  private static String formatListing(String itemName, long price) {
    return "\t" + WordUtils.capitalize(itemName) + " for $" + price + "\n";
  }
}
