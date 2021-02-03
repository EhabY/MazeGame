package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.Command;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class OpenDoor implements Command {
    private final PlayerController playerController;
    private final Player player;

    public OpenDoor(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), playerController.getGameState());
        return response.message.equals("") ? "Nothing happens" : response.message;
    }
}
