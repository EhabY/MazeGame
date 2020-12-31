package mazegame.mapsite;

public interface Checkable extends SerializableMapSite {
  String accept(CheckableVisitor visitor);
}
