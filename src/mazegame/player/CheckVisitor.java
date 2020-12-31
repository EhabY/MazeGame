package mazegame.player;

import mazegame.item.Key;
import mazegame.mapsite.CheckableVisitor;
import mazegame.mapsite.Chest;
import mazegame.mapsite.Door;
import mazegame.mapsite.Hangable;
import mazegame.mapsite.Loot;
import java.util.Objects;

final class CheckVisitor implements CheckableVisitor {
  private final Inventory inventory;

  public CheckVisitor(Inventory inventory) {
    this.inventory = Objects.requireNonNull(inventory);
  }

  @Override
  public String visit(Hangable hangable) {
    Key foundKey = hangable.takeHiddenKey();

    if (foundKey.equals(Key.NO_KEY)) {
      return "";
    } else {
      inventory.addItem(foundKey);
      return "The " + foundKey.getName() + " was acquired";
    }
  }

  @Override
  public String visit(Chest chest) {
    Loot loot = chest.acquireLoot();
    inventory.addGold(loot.getGold());
    inventory.addItems(loot.getItems());

    return loot.toString();
  }

  @Override
  public String visit(Door door) {
    if (door.isLocked()) {
      return "Door is locked, " + door.getKeyName() + " is needed to unlock";
    } else {
      return "Door is open";
    }
  }
}
