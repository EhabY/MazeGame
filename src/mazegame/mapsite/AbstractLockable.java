package mazegame.mapsite;

import mazegame.JsonSerializable;
import mazegame.item.Key;

public abstract class AbstractLockable implements Lockable, JsonSerializable {
  private final Lock lock;

  protected AbstractLockable(Key key) {
    this(key, false);
  }

  protected AbstractLockable(Key key, boolean locked) {
    lock = new Lock(key);
    if (shouldToggleLock(locked)) {
      lock.toggleLock(key);
    }
  }

  private boolean shouldToggleLock(boolean locked) {
    return locked && !lock.isLocked() || !locked && lock.isLocked();
  }

  @Override
  public String getKeyName() {
    return lock.getKeyName();
  }

  @Override
  public void toggleLock(Key key) {
    lock.toggleLock(key);
  }

  @Override
  public boolean isLocked() {
    return lock.isLocked();
  }

  @Override
  public String toJson() {
    return "\"key\": \"" + lock.getKeyName() + "\"," + "\"locked\": " + isLocked();
  }
}