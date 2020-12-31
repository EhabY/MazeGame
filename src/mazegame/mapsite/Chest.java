package mazegame.mapsite;

import mazegame.exceptions.MapSiteLockedException;
import mazegame.item.Item;
import mazegame.item.Key;
import java.util.List;

public class Chest extends AbstractLockable implements Checkable {
  private static final String DESCRIPTION = "Chest";
  private Loot loot;

  public Chest(long gold, List<Item> items, Key key, boolean locked) {
    super(key, locked);
    this.loot = new Loot(gold, items);
  }

  public Loot acquireLoot() {
    if (isLocked()) {
      throw new MapSiteLockedException(
          "Chest closed, " + getKeyName() + " key is needed to unlock");
    }

    Loot loot = this.loot;
    this.loot = Loot.EMPTY_LOOT;
    return loot;
  }

  @Override
  public String look() {
    return DESCRIPTION;
  }

  @Override
  public String accept(CheckableVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return "Chest{" + "loot=" + loot + '}';
  }

  @Override
  public String toJson() {
    return "{"
        + "\"siteMap\": \"Chest\","
        + "\"loot\": "
        + loot.toJson()
        + ","
        + super.toJson()
        + "}";
  }
}
