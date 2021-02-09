package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class TurnRight implements Command {
    private final PlayerController playerController;

    public TurnRight(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            Player player = this.playerController.getPlayer();
            player.turnRight();
            return "Turned right";
        } else {
            return response.message;
        }
    }
}
