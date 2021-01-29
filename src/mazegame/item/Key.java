package mazegame.item;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.Map;
import java.util.Objects;

public final class Key implements Item {
  private final String name;
  private static final Map<String, Key> pool = new CaseInsensitiveMap<>();
  public static final Key NO_KEY = Key.fromString("");
  public static final Key MASTER_KEY = Key.fromString("Master");

  private Key(String name) {
    this.name = Objects.requireNonNull(name);
  }

  public static synchronized Key fromString(String name) {
    if (isNewKey(name)) {
      pool.put(name, new Key(name));
    }

    return pool.get(name);
  }

  private static boolean isNewKey(String name) {
    return !pool.containsKey(name);
  }

  public String getKeyName() {
    return name;
  }

  @Override
  public void accept(ItemVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String getName() {
    return name + " " + getType();
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
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return "Key{" + "name='" + name + '\'' + '}';
  }

  @Override
  public String toJson() {
    return "{" + "\"name\": \"" + name + "\"," + "\"type\": \"Key\"" + "}";
  }
}
