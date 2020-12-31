package mazegame;

import mazegame.cli.Command;
import mazegame.cli.ItemCommand;
import mazegame.parser.GameParser;
import mazegame.util.JsonSerializer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class Interpreter {
  private static GameMaster gameMaster;
  private static final Map<String, Command> generalCommands = new CaseInsensitiveMap<>();
  private static final Map<String, ItemCommand> itemCommands = new CaseInsensitiveMap<>();

  public static void main(String[] args) {
    initializeCommandMaps();
    Scanner in = new Scanner(System.in);

    try {
      System.out.print("Please enter map name: ");
      String mapName = in.nextLine();
      gameMaster = loadGameState(mapName);
      while(gameHasNotEnded()) {
        System.out.print("> ");
        String command = in.nextLine();
        String[] words = command.split("\\s+", 2);
        if(generalCommands.containsKey(command)) {
          generalCommands.get(command).execute();
        } else if(itemCommands.containsKey(words[0])) {
          itemCommands.get(words[0]).execute(words[1]);
        } else if(words[0].equalsIgnoreCase("save")) {
          saveGameState(words[1]);
        } else if(words[0].equalsIgnoreCase("quit")) {
          break;
        } else if(words[0].equalsIgnoreCase("restart")) {
          gameMaster = loadGameState(mapName);
        } else {
          System.out.println("Unknown command: " + command);
        }

      }

      if(gameMaster.getGameState() == State.WON) {
        System.out.println("Congratulations! YOU WON!");
      } else {
        System.out.println("Too bad, you lost!");
      }
      
    } catch (IOException ioException) {
      System.out.println(ioException.toString());
    }
  }

  private static void initializeCommandMaps() {
    generalCommands.put("left", () -> System.out.println(gameMaster.turnPlayerLeft()));
    generalCommands.put("right", () -> System.out.println(gameMaster.turnPlayerRight()));
    generalCommands.put("forward", () -> System.out.println(gameMaster.movePlayerForward()));
    generalCommands.put("backward", () -> System.out.println(gameMaster.movePlayerBackward()));
    generalCommands.put("playerstatus", () -> System.out.println(gameMaster.getPlayerStatus()));
    generalCommands.put("look", () -> System.out.println(gameMaster.look()));
    generalCommands.put("check", () -> System.out.println(gameMaster.check()));
    generalCommands.put("check mirror", () -> System.out.println(gameMaster.check()));
    generalCommands.put("check painting", () -> System.out.println(gameMaster.check()));
    generalCommands.put("check chest", () -> System.out.println(gameMaster.check()));
    generalCommands.put("check door", () -> System.out.println(gameMaster.check()));
    generalCommands.put("open", () -> System.out.println(gameMaster.openDoor()));
    generalCommands.put("trade", () -> System.out.println(gameMaster.initiateTrade()));
    generalCommands.put("list", () -> System.out.println(gameMaster.listSellerItems()));
    generalCommands.put("finish trade", () -> System.out.println(gameMaster.finishTrade()));
    generalCommands.put("switchlights", () -> System.out.println(gameMaster.switchLights()));
    itemCommands.put("buy", (itemName) -> System.out.println(gameMaster.buyItem(itemName)));
    itemCommands.put("sell", (itemName) -> System.out.println(gameMaster.sellItem(itemName)));
    itemCommands.put("use", (itemName) -> System.out.println(gameMaster.useItem(itemName)));
  }

  private static boolean gameHasNotEnded() {
    return gameMaster.getGameState() != State.LOST && gameMaster.getGameState() != State.WON;
  }

  private static GameMaster loadGameState(String filename) throws IOException {
    return new GameMaster(GameParser.parseJsonFile(filename + ".json"));
  }

  private static void saveGameState(String filename) throws IOException {
    Files.write(Paths.get(filename + ".json"), JsonSerializer.serializeGameState(gameMaster).getBytes(StandardCharsets.UTF_8));
  }
}
