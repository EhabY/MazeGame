package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.trade.TransactionHandler;
import java.util.Objects;

public class BuyItem implements ItemCommand {
    private final PlayerController playerController;

    public BuyItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute(String itemName) {
        ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        String message;
        if (response.valid) {
            message = tryToBuy(itemName);
        } else {
            message = response.message;
        }
        return new Response(message);
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
