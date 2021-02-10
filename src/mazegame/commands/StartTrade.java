package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.State;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.trade.TransactionHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class StartTrade implements Command {
    private final PlayerController playerController;

    public StartTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        Player player = playerController.getPlayer();
        ValidityResponse response = ActionValidityChecker.canStartTrade(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            Seller seller = (Seller) player.getMapSiteAhead();
            TransactionHandler transactionHandler = initiateTrade(player, seller);
            return "\nTrade initiated: \n" + transactionHandler.listAll();
        } else {
            return response.message;
        }
    }

    private TransactionHandler initiateTrade(Player player, Seller seller) {
        TransactionHandler transactionHandler = TransactionHandler.startTransaction(player, seller);
        playerController.setTransactionHandler(transactionHandler);
        playerController.setGameState(State.TRADE);
        return transactionHandler;
    }
}
