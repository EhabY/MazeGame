package mazegame;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import mazegame.events.EventHandler;
import mazegame.events.GameEvent;
import mazegame.events.MatchListener;
import mazegame.events.State;
import mazegame.events.StateListener;
import mazegame.mapsite.Loot;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.trade.TradeHandler;
import serialization.Encoder;
import serialization.JsonEncodable;

public class PlayerController implements JsonEncodable {

  private final String username;
  private final Player player;
  private final MazeMap map;
  private final BlockingDeque<String> fightCommandsQueue = new LinkedBlockingDeque<>();
  private final EventHandler eventHandler = new EventHandler();
  private final Instant gameStart = Instant.now();
  private TradeHandler tradeHandler;
  private State state = State.EXPLORE;

  public PlayerController(String username, MazeMap map, Room startRoom) {
    this.username = username;
    this.map = Objects.requireNonNull(map);
    this.player =
        new Player(
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

  public void setGameState(State state, String message) {
    this.state = state;
    eventHandler.triggerStateChangeEvent(state, message);

    if(hasMatchEnded()) {
      eventHandler.clearAllListeners();
    }
  }

  private boolean hasMatchEnded() {
    return state == State.WON || state == State.LOST;
  }

  public State getGameState() {
    return state;
  }

  public TradeHandler getTradeHandler() {
    return tradeHandler;
  }

  public void setTradeHandler(TradeHandler tradeHandler) {
    this.tradeHandler = tradeHandler;
  }

  public String getNextCommand() throws InterruptedException {
    return fightCommandsQueue.take();
  }

  public void addNextCommand(String command) {
    fightCommandsQueue.add(command);
  }

  public void moveFrom(Room previousRoom) {
    eventHandler.triggerMoveEvent(previousRoom);
  }

  public void quitMatch() {
    eventHandler.triggerQuitEvent();
  }

  public Room getCurrentRoom() {
    return player.getCurrentRoom();
  }

  public String getUsername() {
    return username;
  }

  public void addGold(long gold) {
    player.addGoldToInventory(gold);
  }

  public void addLoot(Loot loot) {
    player.addLoot(loot);
  }

  public Loot getLoot() {
    return player.getLoot();
  }

  public void addMatchListener(MatchListener listener) {
    eventHandler.addListener(listener);
  }

  public void addStateListener(StateListener listener) {
    eventHandler.addListener(listener);
  }

  public void startFight() {
    setGameState(State.FIGHT, "Fight has commenced!");
  }

  public void wonFight(String message) {
    setGameState(State.EXPLORE, message);
  }

  public void tieFight(String message) {
    eventHandler.triggerGameEvent(GameEvent.TIE_FIGHT, message);
  }

  public void requestingInput(String message) {
    eventHandler.triggerGameEvent(GameEvent.REQUESTING_INPUT, message);
  }

  public void sendingPlayerList(Collection<String> usernames) {
    eventHandler.triggerGameEvent(GameEvent.SENDING_PLAYER_LIST, usernames.toString());
  }

  public void wonMatch(String message) {
    setGameState(State.WON, message);
  }

  public void lostMatch(String message) {
    setGameState(State.LOST, message);
  }

  @Override
  public String encodeUsing(Encoder encoder) {
    return encoder.visit(this);
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
