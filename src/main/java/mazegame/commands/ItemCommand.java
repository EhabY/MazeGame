package mazegame.commands;

import mazegame.Response;

public interface ItemCommand {

  Response execute(String itemName);
}
