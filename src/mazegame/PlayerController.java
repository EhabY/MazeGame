package mazegame;

import mazegame.events.GameEvent;
import mazegame.events.MoveListener;
import mazegame.events.StateListener;
import mazegame.mapsite.Loot;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.trade.TradeHandler;
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
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
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

  public Player getPlayer() {
    return player;
  }

  public void setGameState(State state) {
    this.state = state;
  }

  public State getGameState() {
    return state;
  }

  public void setTradeHandler(TradeHandler tradeHandler) {
    this.tradeHandler = tradeHandler;
  }

  public TradeHandler getTradeHandler() {
    return tradeHandler;
  }

  public void onMoveFrom(Room previousRoom) {
    for(MoveListener listener : moveListeners) {
      executor.execute(() -> listener.moved(previousRoom));
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
