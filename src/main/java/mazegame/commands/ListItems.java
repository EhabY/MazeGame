package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.trade.TradeHandler;

public class ListItems implements Command {

  private final PlayerController playerController;

  public ListItems(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute() {
    ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
    if (response.valid) {
      TradeHandler tradeHandler = playerController.getTradeHandler();
      return new Response("", tradeHandler.getSeller());
    } else {
      return new Response(response.message);
    }
  }
}
