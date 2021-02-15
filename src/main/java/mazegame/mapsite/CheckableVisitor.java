package mazegame.mapsite;

import mazegame.Response;

public interface CheckableVisitor {

  Response visit(Hangable hangable);

  Response visit(Chest chest);

  Response visit(Door door);
}
