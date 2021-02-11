package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.State;
import java.util.Objects;

public class FinishTrade implements Command {
    private final PlayerController playerController;

    public FinishTrade(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        ValidityResponse response = ActionValidityChecker.inTradeMode(playerController.getGameState());
        if (response.valid) {
            playerController.setGameState(State.EXPLORE);
            playerController.setTransactionHandler(null);
            return new Response("Exited trade mode");
        } else {
            return new Response(response.message);
        }
    }
}
