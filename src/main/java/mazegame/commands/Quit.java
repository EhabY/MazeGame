package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;

public class Quit implements Command {

  private final PlayerController playerController;

  public Quit(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    playerController.quitMatch();
    return new Response("You quit the match!");
  }
}
