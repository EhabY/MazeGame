package mazegame;

import mazegame.events.EventHandler;
import mazegame.events.GameEvent;
import mazegame.events.MoveListener;
import mazegame.events.StateListener;
import mazegame.mapsite.Loot;
import mazegame.player.Player;
import mazegame.room.Room;
import mazegame.trade.TransactionHandler;
import serialization.JsonEncodable;
import serialization.Encoder;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class PlayerController implements JsonEncodable {
  private final String username;
  private final Player player;
  private final MazeMap map;
  private final BlockingDeque<String> fightCommandsQueue = new LinkedBlockingDeque<>();
  private TransactionHandler transactionHandler;
  private State state = State.EXPLORE;
  private final Instant gameStart = Instant.now();
  private final EventHandler eventHandler = new EventHandler();

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

  public void setGameState(State state) {
    this.state = state;
    eventHandler.triggerGameEvent(GameEvent.CHANGED_STATE, this.state.name());
  }

  public State getGameState() {
    return state;
  }

  public void setTransactionHandler(TransactionHandler transactionHandler) {
    this.transactionHandler = transactionHandler;
  }

  public TransactionHandler getTransactionHandler() {
    return transactionHandler;
  }

  public String getNextCommand() throws InterruptedException {
    return fightCommandsQueue.take();
  }

  public void addNextCommand(String command) {
    fightCommandsQueue.add(command);
  }

  public void onMoveFrom(Room previousRoom) {
    eventHandler.triggerMoveEvent(previousRoom);
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

  public void addMoveListener(MoveListener listener) {
    eventHandler.addListener(listener);
  }

  public void addListener(StateListener listener) {
    eventHandler.addListener(listener);
  }

  public void startFight(String message) {
    state = State.FIGHT;
    eventHandler.triggerGameEvent(GameEvent.START_FIGHT, message);
  }

  public void wonFight(String message) {
    state = State.EXPLORE;
    eventHandler.triggerGameEvent(GameEvent.WON_FIGHT, message);
  }

  public void lostFight(String message) {
    state = State.LOST;
    eventHandler.triggerGameEvent(GameEvent.LOST_FIGHT, message);
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
    state = State.WON;
    eventHandler.triggerGameEvent(GameEvent.WON_MATCH, message);
  }

  public void lostMatch(String message) {
    state = State.LOST;
    eventHandler.triggerGameEvent(GameEvent.LOST_MATCH, message);
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
        + ", transactionHandler="
        + transactionHandler
        + ", state="
        + state
        + ", gameStart="
        + gameStart
        + '}';
  }
}
