package mazegame.commands;

import mazegame.PlayerController;
import mazegame.player.Player;

public class SwitchLights implements Command {
    private final Player player;

    public SwitchLights(PlayerController playerController) {
        this.player = playerController.getPlayer();
    }

    @Override
    public String execute() {
        if(player.getCurrentRoom().hasLights()) {
            player.switchLight();
            return "Switched the lights";
        } else {
            return "No lights to switch";
        }
    }
}
