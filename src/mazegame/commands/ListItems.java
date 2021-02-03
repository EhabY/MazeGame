package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.Command;
import mazegame.trade.TransactionHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class ListItems implements Command {
    private final PlayerController playerController;
    private final TransactionHandler transactionHandler;

    public ListItems(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.transactionHandler = this.playerController.getTransactionHandler();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            return transactionHandler.listAll();
        } else {
            return response.message;
        }
    }
}
