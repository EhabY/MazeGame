package website;

import mazegame.PlayerController;
import mazegame.State;
import mazegame.cli.Command;
import mazegame.cli.ItemCommand;
import mazegame.commands.*;
import mazegame.util.JsonSerializer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Interpreter {
    private final Map<String, Command> generalCommands = new CaseInsensitiveMap<>();
    private final Map<String, ItemCommand> itemCommands = new CaseInsensitiveMap<>();
    private final PlayerController playerController;
    private final AtomicBoolean executingOp = new AtomicBoolean(false);

    Interpreter(PlayerController playerController) {
        this.playerController = Objects.requireNonNull(playerController);
        initializeCommandMaps(this.playerController);
    }

    private void initializeCommandMaps(PlayerController playerController) {
        generalCommands.put("save", () -> JsonSerializer.serializeGameState(playerController));
        generalCommands.put("left", new TurnLeft(playerController));
        generalCommands.put("right", new TurnRight(playerController));
        generalCommands.put("forward", new MoveForward(playerController));
        generalCommands.put("backward", new MoveBackward(playerController));
        generalCommands.put("playerstatus", new PlayerStatus(playerController));
        generalCommands.put("look", new Look(playerController));
        generalCommands.put("check", new Check(playerController));
        generalCommands.put("open", new OpenDoor(playerController));
        generalCommands.put("trade", new StartTrade(playerController));
        generalCommands.put("list", new ListItems(playerController));
        generalCommands.put("finish trade", new FinishTrade(playerController));
        generalCommands.put("switchlights", new SwitchLights(playerController));
        itemCommands.put("buy", new BuyItem(playerController));
        itemCommands.put("sell", new SellItem(playerController));
        itemCommands.put("use", new UseItem(playerController));
    }

    boolean isGameOnGoing() {
        return playerController.getGameState() != State.LOST && playerController.getGameState() != State.WON;
    }

    PlayerController getPlayerController() {
        return playerController;
    }

    String execute(String command) {
        if(executingOp.getAndSet(true)) {
            return "Please wait for the previous operation to finish executing!";
        }
        String response = executeCommand(command);
        executingOp.set(false);
        return response;
    }

    private String executeCommand(String command) {
        String[] words = command.split("\\s+", 2);
        String message;
        if(inFightMode()) {
            playerController.addNextCommand(command);
            message = "Used " + command + " in the tie-breaker!";
        } else if(generalCommands.containsKey(command)) {
            message = generalCommands.get(command).execute();
        } else if(itemCommands.containsKey(words[0])) {
            message = itemCommands.get(words[0]).execute(words[1]);
        } else {
            message = "Unknown command: " + command;
        }

        return message;
    }

    private boolean inFightMode() {
        return playerController.getGameState() == State.FIGHT;
    }

}
