package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;
import java.util.Objects;

public class Look implements Command {
    private final PlayerController playerController;

    public Look(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        ValidityResponse response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        String message;
        if(response.valid) {
            Player player = this.playerController.getPlayer();
            message = player.look();
        } else {
            message = response.message;
        }
        return new Response(message);
    }
}
