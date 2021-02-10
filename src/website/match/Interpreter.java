package website.match;

import mazegame.PlayerController;
import mazegame.State;
import mazegame.commands.BuyItem;
import mazegame.commands.Check;
import mazegame.commands.Command;
import mazegame.commands.FinishTrade;
import mazegame.commands.ItemCommand;
import mazegame.commands.ListItems;
import mazegame.commands.Look;
import mazegame.commands.MoveBackward;
import mazegame.commands.MoveForward;
import mazegame.commands.OpenDoor;
import mazegame.commands.PlayerStatus;
import mazegame.commands.SellItem;
import mazegame.commands.StartTrade;
import mazegame.commands.SwitchLights;
import mazegame.commands.TurnLeft;
import mazegame.commands.TurnRight;
import mazegame.commands.UseItem;
import serialization.JsonEncoder;
import serialization.JsonEncoderImpl;
import serialization.JsonSerializer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Interpreter {
    private final Map<String, Command> generalCommands = new CaseInsensitiveMap<>();
    private final Map<String, ItemCommand> itemCommands = new CaseInsensitiveMap<>();
    private final PlayerController playerController;
    private final JsonEncoder jsonEncoder = new JsonEncoderImpl();
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

    public String execute(String command) {
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
