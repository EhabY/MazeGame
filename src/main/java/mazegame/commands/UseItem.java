package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.InvalidUseOfItem;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.player.Player;
import java.util.Objects;

public class UseItem implements ItemCommand {
    private final PlayerController playerController;

    public UseItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute(String itemName) {
        ValidityResponse response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        String message;
        if(response.valid) {
            message = tryToUseItem(itemName);
        } else {
            message = response.message;
        }
        return new Response(message);
    }

    private String tryToUseItem(String itemName) {
        try {
            Player player = playerController.getPlayer();
            player.useItem(itemName);
            return "Used " + itemName;
        } catch (ItemNotFoundException | InvalidUseOfItem exception) {
            return exception.getMessage();
        }
    }
}
