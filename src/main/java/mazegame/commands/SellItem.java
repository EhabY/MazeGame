package mazegame.commands;

import java.util.Objects;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.trade.TradeHandler;

public class SellItem implements ItemCommand {

  private final PlayerController playerController;

  public SellItem(PlayerController playerController) {
    this.playerController = Objects.requireNonNull(playerController);
  }

  @Override
  public Response execute(String itemName) {
    ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
    String message;
    if (response.valid) {
      message = tryToSell(itemName);
    } else {
      message = response.message;
    }
    return new Response(message);
  }

  private String tryToSell(String itemName) {
    TradeHandler tradeHandler = playerController.getTradeHandler();
    if (tradeHandler.sell(itemName)) {
      return itemName + " sold";
    } else {
      return tradeHandler.getReason();
    }
  }
}
