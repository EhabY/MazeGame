package mazegame.commands;

import mazegame.PlayerController;
import mazegame.player.Player;

public class PlayerStatus implements Command {
    private final Player player;

    public PlayerStatus(PlayerController playerController) {
        this.player = playerController.getPlayer();
    }

    @Override
    public String execute() {
        return player.getStatus();
    }
}
