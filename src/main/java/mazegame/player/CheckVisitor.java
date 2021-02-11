package mazegame.player;

import mazegame.Response;
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
  public Response visit(Hangable hangable) {
    Key foundKey = hangable.takeHiddenKey();
    String message;
    if (foundKey.equals(Key.NO_KEY)) {
      message = "Found nothing!";
    } else {
      inventory.addItem(foundKey);
      message = "The " + foundKey.getName() + " key was acquired";
    }
    return new Response(message);
  }

  @Override
  public Response visit(Chest chest) {
    Loot loot = chest.acquireLoot();
    inventory.addLoot(loot);
    return new Response("", loot);
  }

  @Override
  public Response visit(Door door) {
    String message;
    if (door.isLocked()) {
      message = "Door is locked, " + door.getKeyName() + " key is needed to unlock";
    } else {
      message = "Door is open";
    }
    return new Response(message);
  }
}
