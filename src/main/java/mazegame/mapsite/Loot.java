package mazegame.mapsite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mazegame.item.Item;
import serialization.Encoder;
import serialization.JsonEncodable;

public final class Loot implements JsonEncodable {

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
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Gold = " + getGold() + "\n" + itemsList.toString();
  }
}
