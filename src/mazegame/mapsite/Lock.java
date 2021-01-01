package mazegame.mapsite;

import mazegame.exceptions.InvalidUseOfItem;
import mazegame.item.Key;
import java.util.Objects;

public class Lock implements Lockable {
  private final Key key;
  private boolean locked;

  public Lock(Key key) {
    this.key = Objects.requireNonNull(key);
    locked = !key.equals(Key.NO_KEY);
  }

  @Override
  public String getKeyName() {
    return key.getKeyName();
  }

  @Override
  public void toggleLock(Key key) {
    if (canToggleLock(key)) {
      locked = !locked;
    } else {
      throw new InvalidUseOfItem("Key doesn't match the lock");
    }
  }

  private boolean canToggleLock(Key key) {
    return (hasKey() && key.equals(this.key)) || key.equals(Key.MASTER_KEY);
  }

  private boolean hasKey() {
    return !Key.NO_KEY.equals(this.key);
  }

  @Override
  public boolean isLocked() {
    return locked;
  }

  @Override
  public String toString() {
    return "Lock{" + "key=" + key + ", locked=" + locked + '}';
  }
}