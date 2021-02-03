package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.Command;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class Check implements Command {
    private final PlayerController playerController;
    private final Player player;

    public Check(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.canCheck(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            return player.checkAhead();
        } else {
            return response.message;
        }
    }
}
