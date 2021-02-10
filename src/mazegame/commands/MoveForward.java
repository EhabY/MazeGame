package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class MoveForward implements Command {
    private final PlayerController playerController;
    private final Player player;

    public MoveForward(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        ValidityResponse response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            return tryToMoveForward();
        } else {
            return response.message;
        }
    }

    private String tryToMoveForward() {
        try {
            Room previousRoom = player.getCurrentRoom();
            String message = "Moved forward\n" + player.moveForward();
            playerController.onMoveFrom(previousRoom);
            return message;
        } catch (MapSiteLockedException mapSiteLockedException) {
            return mapSiteLockedException.getMessage();
        }
    }
}
