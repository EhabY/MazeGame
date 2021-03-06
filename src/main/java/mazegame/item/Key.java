package mazegame.item;

import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import serialization.Encoder;

public final class Key implements Item {

  private static final Map<String, Key> pool = new CaseInsensitiveMap<>();
  public static final Key NO_KEY = Key.fromString("");
  public static final Key MASTER_KEY = Key.fromString("Master");
  private final String name;

  private Key(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public static Key fromString(String name) {
    pool.putIfAbsent(name, new Key(name));
    return pool.get(name);
  }

  @Override
  public void accept(ItemVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return "Key";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Key key = (Key) o;
    return name.equals(key.name);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(name + " " + getType());
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Key{" + "name='" + name + '\'' + '}';
  }
}
