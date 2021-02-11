package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.mapsite.Loot;
import mazegame.player.Player;
import mazegame.room.Room;
import java.util.Objects;

public class MoveForward implements Command {
    private final PlayerController playerController;
    private final Player player;

    public MoveForward(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public Response execute() {
        ValidityResponse response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            return tryToMoveForward();
        } else {
            return new Response(response.message);
        }
    }

    private Response tryToMoveForward() {
        try {
            Room previousRoom = player.getCurrentRoom();
            Loot loot = player.moveForward();
            playerController.moveFrom(previousRoom);
            return new Response("Moved forward", loot);
        } catch (MapSiteLockedException mapSiteLockedException) {
            return new Response(mapSiteLockedException.getMessage());
        }
    }
}
