package mazegame.mapsite;

public interface CheckableVisitor {
  String visit(Hangable hangable);

  String visit(Chest chest);

  String visit(Door door);
}
