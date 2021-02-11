package mazegame.item;

public interface ItemVisitor {
  void visit(Key key);

  void visit(Flashlight flashlight);
}
