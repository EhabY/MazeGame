package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.Command;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class Look implements Command {
    private final PlayerController playerController;
    private final Player player;

    public Look(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            return player.look();
        } else {
            return response.message;
        }
    }
}
