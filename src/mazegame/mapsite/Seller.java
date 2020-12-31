package mazegame.mapsite;

import mazegame.exceptions.ItemNotFoundException;
import mazegame.item.Item;
import mazegame.item.ItemManager;
import mazegame.util.ItemFormatter;
import mazegame.util.JsonSerializer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.Collection;
import java.util.Map;

public class Seller implements SerializableMapSite {
  private static final String DESCRIPTION = "Seller";
  private final ItemManager items;
  private final Map<String, Long> priceList;

  public Seller(Collection<? extends Item> items, Map<String, Long> priceList) {
    this.items = new ItemManager(items);
    this.priceList = new CaseInsensitiveMap<>(priceList);
  }

  public void addItem(Item item) {
    items.add(item);
  }

  public long getItemPrice(String name) {
    if (priceList.containsKey(name)) {
      return priceList.get(name);
    } else {
      throw new ItemNotFoundException("Item not found");
    }
  }

  public Item takeItem(String name) {
    return items.takeFromItems(name);
  }

  public String getItemList() {
    return items.toString();
  }

  public String getPriceList() {
    return ItemFormatter.formatPriceList(priceList);
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }

  @Override
  public String toString() {
    return "Seller{" + "items=" + getItemList() + ", priceList=" + getPriceList() + '}';
  }

  @Override
  public String toJson() {
    return "{"
        + "\"siteMap\": \"Seller\","
        + "\"items\": "
        + items.toJson()
        + ","
        + "\"priceList\": ["
        + priceListToJson()
        + "]"
        + "}";
  }

  private String priceListToJson() {
    StringBuilder pricesJson = new StringBuilder();
    for (Map.Entry<String, Long> listing : priceList.entrySet()) {
      String itemName = listing.getKey();
      long itemPrice = listing.getValue();
      pricesJson.append("{\"name\": \"").append(itemName);
      pricesJson.append("\",\"price\": ").append(itemPrice).append("},");
    }

    return JsonSerializer.removeTrailingChar(pricesJson).toString();
  }
}
