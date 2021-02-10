package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.trade.TransactionHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class BuyItem implements ItemCommand {
    private final PlayerController playerController;

    public BuyItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute(String itemName) {
        ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            return tryToBuy(itemName);
        } else {
            return response.message;
        }
    }

    private String tryToBuy(String itemName) {
        try {
            TransactionHandler transactionHandler = playerController.getTransactionHandler();
            transactionHandler.buy(itemName);
            return itemName + " bought and acquired";
        } catch (ItemNotFoundException | NotEnoughGoldException exception) {
            return exception.getMessage();
        }
    }
}
