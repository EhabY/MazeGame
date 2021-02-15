package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.mapsite.Loot;
import mazegame.player.Player;
import mazegame.room.Room;

public class MoveBackward implements Command {

  private final PlayerController playerController;
  private final Player player;

  public MoveBackward(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
    this.player = this.playerController.getPlayer();
  }

  @Override
  public Response execute() {
    ValidityResponse response = ActionValidityChecker
        .canOpenDoor(player.getMapSiteBehind(), playerController.getGameState());
    if (response.valid) {
      return tryToMoveBackward();
    } else {
      return new Response(response.message);
    }
  }

  private Response tryToMoveBackward() {
    try {
      Room previousRoom = player.getCurrentRoom();
      Loot loot = player.moveBackward();
      playerController.moveFrom(previousRoom);
      return new Response("Moved backward", loot);
    } catch (MapSiteLockedException mapSiteLockedException) {
      return new Response(mapSiteLockedException.getMessage());
    }
  }

}
