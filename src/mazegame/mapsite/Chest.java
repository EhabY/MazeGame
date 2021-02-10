package mazegame.mapsite;

import mazegame.exceptions.MapSiteLockedException;
import mazegame.item.Key;
import serialization.JsonEncoder;

import java.util.Objects;

public class Chest extends AbstractLockable implements Checkable, Lootable {
  private static final String DESCRIPTION = "Chest";
  private Loot loot;

  public Chest(Loot loot, Key key, boolean locked) {
    super(key, locked);
    this.loot = Objects.requireNonNull(loot);
  }

  @Override
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
  public String applyEncoder(JsonEncoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Chest{" + "loot=" + loot + '}';
  }
}
