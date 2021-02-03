package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.cli.ItemCommand;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.trade.TradeHandler;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class BuyItem implements ItemCommand {
    private final PlayerController playerController;
    private final TradeHandler tradeHandler;

    public BuyItem(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        this.tradeHandler = this.playerController.getTradeHandler();
    }

    @Override
    public String execute(String itemName) {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            return tryToBuy(itemName);
        } else {
            return response.message;
        }
    }

    private String tryToBuy(String itemName) {
        try {
            return tradeHandler.buy(itemName);
        } catch (ItemNotFoundException | NotEnoughGoldException exception) {
            return exception.getMessage();
        }
    }
}
