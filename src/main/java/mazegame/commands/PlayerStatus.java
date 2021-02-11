package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;

public class PlayerStatus implements Command {
    private final Player player;

    public PlayerStatus(PlayerController playerController) {
        this.player = playerController.getPlayer();
    }

    @Override
    public Response execute() {
        return new Response("", player);
    }
}
