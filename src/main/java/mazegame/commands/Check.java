package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;

public class Check implements Command {

  private final PlayerController playerController;

  public Check(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    Player player = this.playerController.getPlayer();
    ValidityResponse response = ActionValidityChecker
        .canCheck(player.getMapSiteAhead(), playerController.getGameState());
    if (response.valid) {
      return player.checkAhead();
    } else {
      return new Response(response.message);
    }
  }
}
