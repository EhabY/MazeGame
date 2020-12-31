package mazegame.mapsite;

import mazegame.item.Key;

public interface Lockable {
  String getKeyName();

  void toggleLock(Key key);

  boolean isLocked();
}
