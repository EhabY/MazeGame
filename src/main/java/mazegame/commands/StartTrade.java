package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.events.State;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.trade.TradeHandler;

public class StartTrade implements Command {

  private final PlayerController playerController;

  public StartTrade(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    Player player = playerController.getPlayer();
    ValidityResponse response = ActionValidityChecker
        .canStartTrade(player.getMapSiteAhead(), playerController.getGameState());
    if (response.valid) {
      Seller seller = (Seller) player.getMapSiteAhead();
      TradeHandler tradeHandler = initiateTrade(player, seller);
      return new Response("Trade initiated", tradeHandler.getSeller());
    } else {
      return new Response(response.message);
    }
  }

  private TradeHandler initiateTrade(Player player, Seller seller) {
    TradeHandler tradeHandler = TradeHandler.startTransaction(player, seller);
    playerController.setTradeHandler(tradeHandler);
    playerController.setGameState(State.TRADE, "Started trading");
    return tradeHandler;
  }
}
