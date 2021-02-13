package mazegame.mapsite;

import mazegame.item.Key;

public abstract class AbstractLockable implements Lockable {
  private final Lock lock;

  protected AbstractLockable(Key key) {
    this(key, false);
  }

  protected AbstractLockable(Key key, boolean locked) {
    lock = new Lock(key);
    if (shouldToggleLock(key, locked)) {
      lock.toggleLock(key);
    }
  }

  private boolean shouldToggleLock(Key key, boolean locked) {
    return !key.equals(Key.NO_KEY) && (locked && !lock.isLocked() || !locked && lock.isLocked());
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
}
