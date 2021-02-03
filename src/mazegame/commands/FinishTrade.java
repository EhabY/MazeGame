package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.State;
import mazegame.cli.Command;
import mazegame.util.ActionValidityChecker;
import java.util.Objects;

public class FinishTrade implements Command {
    private final PlayerController playerController;

    public FinishTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public String execute() {
        Response response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            playerController.setGameState(State.EXPLORE);
            playerController.setTradeHandler(null);
            return "Exited trade mode";
        } else {
            return response.message;
        }
    }
}
