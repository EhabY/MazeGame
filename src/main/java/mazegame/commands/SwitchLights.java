package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;

public class SwitchLights implements Command {

  private final Player player;

  public SwitchLights(PlayerController playerController) {
    this.player = playerController.getPlayer();
  }

  @Override
  public Response execute() {
    String message;
    if (player.getCurrentRoom().hasLights()) {
      player.switchLight();
      message = "Switched the lights";
    } else {
      message = "No lights to switch";
    }
    return new Response(message);
  }
}
