package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;
import java.util.Objects;

public class TurnRight implements Command {
    private final PlayerController playerController;

    public TurnRight(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        ValidityResponse response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        String message;
        if(response.valid) {
            Player player = this.playerController.getPlayer();
            player.turnRight();
            message = "Turned right";
        } else {
            message = response.message;
        }
        return new Response(message);
    }
}
