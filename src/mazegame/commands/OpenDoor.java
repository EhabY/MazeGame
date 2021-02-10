package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;
import java.util.Objects;

public class OpenDoor implements Command {
    private final PlayerController playerController;

    public OpenDoor(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        Player player = playerController.getPlayer();
        ValidityResponse response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), playerController.getGameState());
        String message = response.message.equals("") ? "Nothing happens" : response.message;
        return new Response(message);
    }
}
