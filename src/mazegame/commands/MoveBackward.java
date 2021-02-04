package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class MoveBackward implements Command {
    private final PlayerController playerController;
    private final Player player;

    public MoveBackward(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.canOpenDoor(player.getMapSiteBehind(), playerController.getGameState());
        if (response.valid) {
            return tryToMoveBackward();
        } else {
            return response.message;
        }
    }

    private String tryToMoveBackward() {
        try {
            Room previousRoom = player.getCurrentRoom();
            String message = "Moved backward\n" + player.moveBackward();
            playerController.onMoveFrom(previousRoom);
            return message;
        } catch (MapSiteLockedException mapSiteLockedException) {
            return mapSiteLockedException.getMessage();
        }
    }

}
