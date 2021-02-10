package mazegame.item;

import serialization.JsonEncoder;
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
    name = "";
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
    Flashlight flashlight = (Flashlight) o;
    return name.equals(flashlight.name);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(name + " " + getType());
  }

  @Override
  public String applyEncoder(JsonEncoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Flashlight{" + "turnedOn=" + turnedOn + ", name='" + name + '\'' + '}';
  }
}
