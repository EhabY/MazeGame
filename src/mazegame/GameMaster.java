package mazegame;

import mazegame.exceptions.InvalidUseOfItem;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.exceptions.NoLightsException;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.trade.TradeHandler;
import mazegame.util.ActionValidityChecker;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameMaster implements JsonSerializable {
  private final Player player;
  private final MazeMap map;
  private final Timer timer;
  private final Instant gameStart;
  private TradeHandler tradeHandler;
  private State state;

  public GameMaster(MazeMap map) {
    this.map = Objects.requireNonNull(map);
    this.player =
        new Player(
            "PLACEHOLDER",
            map.getStartingOrientation(),
            map.getStartRoom(),
            map.getStartingGold(),
            map.getInitialItems());
    this.state = State.EXPLORE;
    this.gameStart = Instant.now();
    this.timer = initializeTimer();
  }

  private Timer initializeTimer() {
    TimerTask setStateLost =
        new TimerTask() {
          public void run() {
            state = State.LOST;
            timer.cancel();
          }
        };
    Timer timer = new Timer("Game timer");
    long timeInMilliseconds = map.getTimeInSeconds() * 1000;
    timer.schedule(setStateLost, timeInMilliseconds);

    return timer;
  }

  public State getGameState() {
    return state;
  }

  public String turnPlayerLeft() {
    player.turnLeft();
    return "Turned left";
  }

  public String turnPlayerRight() {
    player.turnRight();
    return "Turned right";
  }

  public String movePlayerForward() {
    Response response = ActionValidityChecker.canMove(player.getMapSiteAhead(), state);
    if (response.valid) {
      player.moveForward();
      updateWonState();
      return "Moved forward";
    } else {
      return response.message;
    }
  }

  private void updateWonState() {
    Room endRoom = map.getEndRoom();
    if (endRoom.equals(player.getCurrentRoom())) {
      this.state = State.WON;
      timer.cancel();
    }
  }

  public String movePlayerBackward() {
    Response response = ActionValidityChecker.canMove(player.getMapSiteBehind(), state);
    if (response.valid) {
      player.moveBackward();
      updateWonState();
      return "Moved backward";
    } else {
      return response.message;
    }
  }

  public String getPlayerStatus() {
    return player.getStatus();
  }

  public String look() {
    return player.look();
  }

  public String check() {
    Response response = ActionValidityChecker.canCheck(player.getMapSiteAhead(), state);
    if (response.valid) {
      return tryToCheck();
    } else {
      return response.message;
    }
  }

  private String tryToCheck() {
    try {
      return player.checkAhead();
    } catch (MapSiteLockedException mapSiteLockedException) {
      return mapSiteLockedException.getMessage();
    }
  }

  public String openDoor() {
    Response response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), state);
    if (response.valid) {
      return player.openDoor();
    } else {
      return response.message;
    }
  }

  public String initiateTrade() {
    Response response = ActionValidityChecker.canStartTrade(player.getMapSiteAhead(), state);
    if (response.valid) {
      this.state = State.TRADE;
      Seller seller = (Seller) player.getMapSiteAhead();
      tradeHandler = new TradeHandler(player, seller);
      return "\nTrade initiated: \n" + tradeHandler.list();
    } else {
      return response.message;
    }
  }

  public String buyItem(String itemName) {
    Response response = ActionValidityChecker.inTradeMode(state);
    if (response.valid) {
      return tryToBuy(itemName);
    } else {
      return response.message;
    }
  }

  private String tryToBuy(String itemName) {
    try {
      return tradeHandler.buy(itemName);
    } catch (ItemNotFoundException | NotEnoughGoldException exception) {
      return exception.getMessage();
    }
  }

  public String sellItem(String itemName) {
    Response response = ActionValidityChecker.inTradeMode(state);
    if (response.valid) {
      return tryToSell(itemName);
    } else {
      return response.message;
    }
  }

  private String tryToSell(String itemName) {
    try {
      return tradeHandler.sell(itemName);
    } catch (ItemNotFoundException itemNotFoundException) {
      return itemNotFoundException.getMessage();
    }
  }

  public String listSellerItems() {
    Response response = ActionValidityChecker.inTradeMode(state);
    if (response.valid) {
      return tradeHandler.list();
    } else {
      return response.message;
    }
  }

  public String finishTrade() {
    Response response = ActionValidityChecker.inTradeMode(state);
    if (response.valid) {
      this.state = State.EXPLORE;
      tradeHandler = null;
      return "Exited trade mode";
    } else {
      return response.message;
    }
  }

  public String useItem(String name) {
    try {
      player.useItem(name);
      return "used " + name;
    } catch (ItemNotFoundException | InvalidUseOfItem exception) {
      return exception.getMessage();
    }
  }

  public String switchLights() {
    try {
      player.switchLight();
      return "Switched the lights";
    } catch (NoLightsException noLightsException) {
      return noLightsException.getMessage();
    }
  }

  public Room getCurrentRoom() {
    return player.getCurrentRoom();
  }

  @Override
  public String toJson() {
    long timeElapsedInSeconds = Duration.between(gameStart, Instant.now()).toMillis() / 1000;
    return "\"mapConfiguration\": {"
        + "\"startRoomID\": "
        + player.getCurrentRoom().getId()
        + ","
        + "\"endRoomID\": "
        + map.getEndRoom().getId()
        + ","
        + "\"time\": "
        + (map.getTimeInSeconds() - timeElapsedInSeconds)
        + ","
        + player.toJson()
        + "}";
  }

  @Override
  public String toString() {
    return "GameMaster{"
        + "player="
        + player
        + ", map="
        + map
        + ", tradeHandler="
        + tradeHandler
        + ", state="
        + state
        + ", timer="
        + timer
        + ", gameStart="
        + gameStart
        + '}';
  }
}
