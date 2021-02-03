package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.ItemCommand;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.trade.TradeHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class SellItem implements ItemCommand {
    private final PlayerController playerController;
    private final TradeHandler tradeHandler;

    public SellItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.tradeHandler = this.playerController.getTradeHandler();
    }

    @Override
    public String execute(String itemName) {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            return tryToSell(itemName);
        } else {
            return response.message;
        }
    }

    private String tryToSell(String itemName) {
        try {
            return tradeHandler.sell(itemName);
        } catch (ItemNotFoundException itemNotFoundException) {
            return itemNotFoundException.getMessage();
        }
    }
}
