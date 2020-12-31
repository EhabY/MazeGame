package mazegame.mapsite;

import mazegame.JsonSerializable;
import mazegame.item.Item;
import mazegame.util.ItemFormatter;
import mazegame.util.JsonSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Loot implements JsonSerializable {
  public static final Loot EMPTY_LOOT = new Loot(0);
  private final long gold;
  private final List<Item> itemsList;

  public Loot(long gold) {
    this(gold, Collections.emptyList());
  }

  public Loot(long gold, Collection<? extends Item> items) {
    this.gold = gold;
    this.itemsList = Collections.unmodifiableList(new ArrayList<>(items));
  }

  public long getGold() {
    return gold;
  }

  public List<Item> getItems() {
    return itemsList;
  }

  @Override
  public String toString() {
    return "Gold = " + getGold() + "\n" + ItemFormatter.formatItems(itemsList);
  }

  @Override
  public String toJson() {
    return "{"
        + "\"gold\": "
        + gold
        + ","
        + "\"items\": ["
        + JsonSerializer.removeTrailingChar(JsonSerializer.listToJson(itemsList))
        + "]"
        + "}";
  }
}
