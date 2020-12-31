package mazegame.item;

import java.util.Objects;

public final class Flashlight implements Item {
  public static final String FLASHLIGHT_NAME = "Flashlight";
  private boolean turnedOn;
  private final String name;

  public Flashlight() {
    this(false);
  }

  public Flashlight(boolean turnedOn) {
    this.turnedOn = turnedOn;
    name = Flashlight.FLASHLIGHT_NAME;
  }

  public void toggle() {
    turnedOn = !turnedOn;
  }

  public boolean isTurnedOn() {
    return turnedOn;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return "Flashlight";
  }

  @Override
  public void accept(ItemVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Flashlight that = (Flashlight) o;
    return name.equals(that.name);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getName());
  }

  @Override
  public String toString() {
    return "Flashlight{" + "turnedOn=" + turnedOn + ", name='" + name + '\'' + '}';
  }

  @Override
  public String toJson() {
    return "{"
        + "\"name\": \""
        + name
        + "\","
        + "\"type\": \"Flashlight\""
        + ","
        + "\"turnedOn\": "
        + turnedOn
        + "}";
  }
}
