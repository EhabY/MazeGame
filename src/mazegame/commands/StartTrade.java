package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.State;
import mazegame.cli.Command;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.trade.TradeHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class StartTrade implements Command {
    private final PlayerController playerController;
    private final Player player;

    public StartTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.player = this.playerController.getPlayer();
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.canStartTrade(player.getMapSiteAhead(), playerController.getGameState());
        if (response.valid) {
            Seller seller = (Seller) player.getMapSiteAhead();
            TradeHandler tradeHandler = initiateTrade(player, seller);
            return "\nTrade initiated: \n" + tradeHandler.list();
        } else {
            return response.message;
        }
    }

    private TradeHandler initiateTrade(Player player, Seller seller) {
        TradeHandler tradeHandler = new TradeHandler(player, seller);
        playerController.setTradeHandler(tradeHandler);
        playerController.setGameState(State.TRADE);
        return tradeHandler;
    }
}
