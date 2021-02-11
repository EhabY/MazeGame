package mazegame.player;

import mazegame.exceptions.InvalidUseOfItem;
import mazegame.item.Flashlight;
import mazegame.item.ItemVisitor;
import mazegame.item.Key;
import mazegame.mapsite.Lockable;
import java.util.Objects;

final class UseItemVisitor implements ItemVisitor {
  Player player;

  UseItemVisitor(Player player) {
    this.player = Objects.requireNonNull(player);
  }

  @Override
  public void visit(Key key) {
    if (isLockableAhead()) {
      Lockable lockable = (Lockable) player.getMapSiteAhead();
      lockable.toggleLock(key);
    } else {
      throw new InvalidUseOfItem("Not in front of lockable object");
    }
  }

  private boolean isLockableAhead() {
    return player.getMapSiteAhead() instanceof Lockable;
  }

  @Override
  public void visit(Flashlight flashlight) {
    flashlight.toggle();
  }
}
