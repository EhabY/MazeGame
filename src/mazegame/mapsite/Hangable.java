package mazegame.mapsite;

import mazegame.item.Key;

public interface Hangable extends Checkable {
  String getKeyName();

  Key takeHiddenKey();
}
