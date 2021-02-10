package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class Look implements Command {
    private final PlayerController playerController;

    public Look(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        ValidityResponse response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            Player player = this.playerController.getPlayer();
            return player.look();
        } else {
            return response.message;
        }
    }
}
