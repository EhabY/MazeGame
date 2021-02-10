package mazegame.item;

import serialization.JsonEncodable;

public interface Item extends JsonEncodable {
  void accept(ItemVisitor visitor);

  String getName();

  String getType();
}
