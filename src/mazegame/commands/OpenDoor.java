package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class OpenDoor implements Command {
    private final PlayerController playerController;

    public OpenDoor(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        Player player = playerController.getPlayer();
        ValidityResponse response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), playerController.getGameState());
        return response.message.equals("") ? "Nothing happens" : response.message;
    }
}
