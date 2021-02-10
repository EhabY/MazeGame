package mazegame.commands;

import mazegame.PlayerController;
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
    public String execute(String itemName) {
        ValidityResponse response = ActionValidityChecker.inExploreMode(playerController.getGameState());
        if(response.valid) {
            return tryToUseItem(itemName);
        } else {
            return response.message;
        }
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
