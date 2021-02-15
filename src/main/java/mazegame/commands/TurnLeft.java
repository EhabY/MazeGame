package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;

public class TurnLeft implements Command {

  private final PlayerController playerController;

  public TurnLeft(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    ValidityResponse response = ActionValidityChecker
        .inExploreMode(playerController.getGameState());
    String message;
    if (response.valid) {
      Player player = this.playerController.getPlayer();
      player.turnLeft();
      message = "Turned left";
    } else {
      message = response.message;
    }
    return new Response(message);
  }
}
