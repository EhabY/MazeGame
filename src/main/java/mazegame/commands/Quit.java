package mazegame.commands;

import mazegame.PlayerController;
import mazegame.Response;
import mazegame.player.Player;
import mazegame.room.Room;
import java.util.Objects;

public class Quit implements Command {
    private final PlayerController playerController;

    public Quit(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
    }

    @Override
    public Response execute() {
        Player player = playerController.getPlayer();
        Room room = player.getCurrentRoom();
        room.addLoot(player.getLoot());
        playerController.quitMatch();
        return new Response("You quit the match!");
    }
}
