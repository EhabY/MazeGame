package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.trade.TransactionHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class SellItem implements ItemCommand {
    private final PlayerController playerController;

    public SellItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute(String itemName) {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            return tryToSell(itemName);
        } else {
            return response.message;
        }
    }

    private String tryToSell(String itemName) {
        try {
            TransactionHandler transactionHandler = playerController.getTransactionHandler();
            transactionHandler.sell(itemName);
            return itemName + " sold";
        } catch (ItemNotFoundException itemNotFoundException) {
            return itemNotFoundException.getMessage();
        }
    }
}
