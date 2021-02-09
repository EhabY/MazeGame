package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.trade.TransactionHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class ListItems implements Command {
    private final PlayerController playerController;

    public ListItems(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            TransactionHandler transactionHandler = playerController.getTransactionHandler();
            return transactionHandler.listAll();
        } else {
            return response.message;
        }
    }
}
