package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.State;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.trade.TransactionHandler;
import java.util.Objects;

public class StartTrade implements Command {
    private final PlayerController playerController;

    public StartTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        Player player = playerController.getPlayer();
        ValidityResponse response = ActionValidityChecker.canStartTrade(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            Seller seller = (Seller) player.getMapSiteAhead();
            TransactionHandler transactionHandler = initiateTrade(player, seller);
            return new Response("Trade initiated", transactionHandler.getSeller());
        } else {
            return new Response(response.message);
        }
    }

    private TransactionHandler initiateTrade(Player player, Seller seller) {
        TransactionHandler transactionHandler = TransactionHandler.startTransaction(player, seller);
        playerController.setTransactionHandler(transactionHandler);
        playerController.setGameState(State.TRADE);
        return transactionHandler;
    }
}
