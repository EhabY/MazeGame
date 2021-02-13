package mazegame.item;

import serialization.JsonEncodable;

public interface Item extends JsonEncodable {
  void accept(ItemVisitor visitor);

  default String getUniqueName() {
    return (getName() + " " + getType()).trim();
  }

  String getName();

  String getType();
}
