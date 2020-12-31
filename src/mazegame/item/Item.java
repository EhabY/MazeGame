package mazegame.item;

import mazegame.JsonSerializable;

public interface Item extends JsonSerializable {
  void accept(ItemVisitor visitor);

  String getName();

  String getType();
}
