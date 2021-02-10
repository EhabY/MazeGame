package mazegame.mapsite;

import mazegame.Response;

public interface Checkable extends SerializableMapSite {
  Response accept(CheckableVisitor visitor);
}
