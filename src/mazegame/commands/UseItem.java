package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.InvalidUseOfItem;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.player.Player;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class UseItem implements ItemCommand {
    private final PlayerController playerController;
    private final Player player;

    public UseItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = playerController.getPlayer();
    }

    @Override
    public String execute(String itemName) {
        Response response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            return tryToUseItem(itemName);
        } else {
            return response.message;
        }
    }

    private String tryToUseItem(String itemName) {
        try {
            player.useItem(itemName);
            return "Used " + itemName;
        } catch (ItemNotFoundException | InvalidUseOfItem exception) {
            return exception.getMessage();
        }
    }
}
