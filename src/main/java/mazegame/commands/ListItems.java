package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.trade.TransactionHandler;
import java.util.Objects;

public class ListItems implements Command {
    private final PlayerController playerController;

    public ListItems(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            TransactionHandler transactionHandler = playerController.getTransactionHandler();
            return new Response("", transactionHandler.getSeller());
        } else {
            return new Response(response.message);
        }
    }
}