package mazegame.commands;

import mazegame.PlayerController;
import mazegame.ValidityResponse;
import mazegame.State;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class FinishTrade implements Command {
    private final PlayerController playerController;

    public FinishTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            playerController.setGameState(State.EXPLORE);
            playerController.setTransactionHandler(null);
            return "Exited trade mode";
        } else {
            return response.message;
        }
    }
}
