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
  private static PlayerController playerController;
  private static final Map<String, Command> generalCommands = new CaseInsensitiveMap<>();
  private static final Map<String, ItemCommand> itemCommands = new CaseInsensitiveMap<>();

  public static void main(String[] args) {
    initializeCommandMaps();
    Scanner in = new Scanner(System.in);

    try {
      System.out.print("Please enter map name: ");
      String mapName = in.nextLine();
      playerController = loadGameState(mapName);
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
          playerController = loadGameState(mapName);
        } else {
          System.out.println("Unknown command: " + command);
        }

      }

      if(playerController.getGameState() == State.WON) {
        System.out.println("Congratulations! YOU WON!");
      } else {
        System.out.println("Too bad, you lost!");
      }
      
    } catch (IOException ioException) {
      System.out.println(ioException.toString());
    }
  }

  private static void initializeCommandMaps() {
    generalCommands.put("left", () -> System.out.println(playerController.turnPlayerLeft()));
    generalCommands.put("right", () -> System.out.println(playerController.turnPlayerRight()));
    generalCommands.put("forward", () -> System.out.println(playerController.movePlayerForward()));
    generalCommands.put("backward", () -> System.out.println(playerController.movePlayerBackward()));
    generalCommands.put("playerstatus", () -> System.out.println(playerController.getPlayerStatus()));
    generalCommands.put("look", () -> System.out.println(playerController.look()));
    generalCommands.put("check", () -> System.out.println(playerController.check()));
    generalCommands.put("check mirror", () -> System.out.println(playerController.check()));
    generalCommands.put("check painting", () -> System.out.println(playerController.check()));
    generalCommands.put("check chest", () -> System.out.println(playerController.check()));
    generalCommands.put("check door", () -> System.out.println(playerController.check()));
    generalCommands.put("open", () -> System.out.println(playerController.openDoor()));
    generalCommands.put("trade", () -> System.out.println(playerController.initiateTrade()));
    generalCommands.put("list", () -> System.out.println(playerController.listSellerItems()));
    generalCommands.put("finish trade", () -> System.out.println(playerController.finishTrade()));
    generalCommands.put("switchlights", () -> System.out.println(playerController.switchLights()));
    itemCommands.put("buy", (itemName) -> System.out.println(playerController.buyItem(itemName)));
    itemCommands.put("sell", (itemName) -> System.out.println(playerController.sellItem(itemName)));
    itemCommands.put("use", (itemName) -> System.out.println(playerController.useItem(itemName)));
  }

  private static boolean gameHasNotEnded() {
    return playerController.getGameState() != State.LOST && playerController.getGameState() != State.WON;
  }

  private static PlayerController loadGameState(String filename) throws IOException {
    return new PlayerController(GameParser.parseJsonFile(filename + ".json"));
  }

  private static void saveGameState(String filename) throws IOException {
    Files.write(Paths.get(filename + ".json"), JsonSerializer.serializeGameState(playerController).getBytes(StandardCharsets.UTF_8));
  }
}
