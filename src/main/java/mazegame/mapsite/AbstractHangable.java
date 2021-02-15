package mazegame.mapsite;

import java.util.Objects;
import mazegame.Response;
import mazegame.item.Key;

public abstract class AbstractHangable implements Hangable {

  private final String description;
  private Key hiddenKey;

  public AbstractHangable(String description, Key hiddenKey) {
    this.description = Objects.requireNonNull(description);
    this.hiddenKey = Objects.requireNonNull(hiddenKey);
  }

  @Override
  public String look() {
    return description;
  }

  @Override
  public String getKeyName() {
    return hiddenKey.getName();
  }

  @Override
  public Key takeHiddenKey() {
    Key hiddenKey = this.hiddenKey;
    this.hiddenKey = Key.NO_KEY;

    return hiddenKey;
  }

  @Override
  public Response accept(CheckableVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return "AbstractHangable{"
        + "description=\""
        + description
        + "\""
        + ", hiddenKey=\""
        + getKeyName()
        + "\""
        + "}";
  }
}
