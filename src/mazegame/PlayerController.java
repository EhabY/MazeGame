package mazegame;

import mazegame.events.GameEvent;
import mazegame.events.MoveListener;
import mazegame.events.StateListener;
import mazegame.exceptions.InvalidUseOfItem;
import mazegame.exceptions.ItemNotFoundException;
import mazegame.exceptions.MapSiteLockedException;
import mazegame.exceptions.NoLightsException;
import mazegame.exceptions.NotEnoughGoldException;
import mazegame.mapsite.Door;
import mazegame.mapsite.Loot;
import mazegame.mapsite.Seller;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.trade.TradeHandler;
import mazegame.util.ActionValidityChecker;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerController implements JsonSerializable {
  private final Player player;
  private final MazeMap map;
  private TradeHandler tradeHandler;
  private State state = State.EXPLORE;
  private final Instant gameStart = Instant.now();
  ExecutorService executor = Executors.newSingleThreadExecutor();
  private final List<MoveListener> moveListeners = new ArrayList<>();
  private final List<StateListener> stateListeners = new ArrayList<>();

  public PlayerController(String username, MazeMap map, Room startRoom) {
    this.map = Objects.requireNonNull(map);
    this.player =
        new Player(
                username,
                getRandomDirection(),
                startRoom,
            map.getStartingGold(),
            map.getInitialItems());
  }

  private Direction getRandomDirection() {
    return Direction.values()[new Random().nextInt(Direction.values().length)];
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
    Response response = ActionValidityChecker.canOpenDoor(player.getMapSiteAhead(), state);
    if (response.valid) {
      return tryToMoveForward();
    } else {
      return response.message;
    }
  }

  private String tryToMoveForward() {
    try {
      Room previousRoom = player.getCurrentRoom();
      String movedResult = "Moved forward\n" + player.moveForward();
      onMoveFrom(previousRoom);
      return movedResult;
    } catch (MapSiteLockedException mapSiteLockedException) {
      return mapSiteLockedException.getMessage();
    }
  }

  private void onMoveFrom(Room previousRoom) {
    for(MoveListener listener : moveListeners) {
      executor.execute(() -> listener.moved(previousRoom));
    }
  }

  public String movePlayerBackward() {
    Response response = ActionValidityChecker.canOpenDoor(player.getMapSiteBehind(), state);
    if (response.valid) {
      return tryToMoveBackward();
    } else {
      return response.message;
    }
  }

  private String tryToMoveBackward() {
    try {
      Room previousRoom = player.getCurrentRoom();
      String movedResult = "Moved backward\n" + player.moveBackward();
      onMoveFrom(previousRoom);
      return movedResult;
    } catch (MapSiteLockedException mapSiteLockedException) {
      return mapSiteLockedException.getMessage();
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
      return tryToOpenDoor();
    } else {
      return response.message;
    }
  }

  private String tryToOpenDoor() {
    Door door = (Door) player.getMapSiteAhead();
    if (door.isLocked()) {
      return door.getKeyName() + " key required to unlock";
    } else {
      return "Nothing happens";
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

  public long getScore() {
    return player.getScore();
  }

  public void addLoot(Loot loot) {
    player.addLoot(loot);
  }

  public Loot getLoot() {
    return player.getLoot();
  }

  public void addMoveListener(MoveListener listener) {
    moveListeners.add(listener);
  }

  public void addStateListener(StateListener listener) {
    stateListeners.add(listener);
  }

  public void startFight(String message) {
    state = State.FIGHT;
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.START_FIGHT, message);
    }
  }

  public void wonFight(String message) {
    state = State.EXPLORE;
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.WON_FIGHT, message);
    }
  }

  public void lostFight(String message) {
    state = State.LOST;
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.LOST_FIGHT, message);
    }
  }

  public void tieFight(String message) {
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.TIE_FIGHT, message);
    }
  }

  public void requestingInput(String message) {
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.REQUESTING_INPUT, message);
    }
  }

  public void lostMatch(String message) {
    state = State.LOST;
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.LOST_MATCH, message);
    }
  }

  public void wonMatch(String message) {
    state = State.WON;
    for(StateListener listener : stateListeners) {
      listener.stateChanged(GameEvent.WON_MATCH, message);
    }
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
    return "PlayerController{"
        + "player="
        + player
        + ", map="
        + map
        + ", tradeHandler="
        + tradeHandler
        + ", state="
        + state
        + ", gameStart="
        + gameStart
        + '}';
  }
}
