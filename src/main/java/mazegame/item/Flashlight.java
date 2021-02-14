package mazegame.item;

import java.util.Objects;
import serialization.Encoder;

public final class Flashlight implements Item {

  public static final String FLASHLIGHT_NAME = "Flashlight";
  private final String name;
  private boolean turnedOn;

  public Flashlight() {
    this(false);
  }

  public Flashlight(boolean turnedOn) {
    this(turnedOn, "");
  }

  public Flashlight(boolean turnedOn, String name) {
    this.turnedOn = turnedOn;
    this.name = name;
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
    return FLASHLIGHT_NAME;
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
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
  }

  @Override
  public String toString() {
    return "Flashlight{" + "turnedOn=" + turnedOn + ", name='" + name + '\'' + '}';
  }
}
