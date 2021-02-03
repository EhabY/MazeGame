package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.Command;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class TurnLeft implements Command {
    private final PlayerController playerController;
    private final Player player;

    public TurnLeft(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            player.turnLeft();
            return "Turned left";
        } else {
            return response.message;
        }
    }
}
