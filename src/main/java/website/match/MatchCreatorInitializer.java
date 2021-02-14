package website.match;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import mapgenerator.MapConfiguration;
import mazegame.PlayerController;
import mazegame.State;
import mazegame.events.GameEvent;
import mazegame.events.StateListener;
import org.eclipse.jetty.websocket.api.Session;
import website.fighting.ConflictResolver;
import website.message.EventMessage;
import website.message.Message;
import website.message.StateChangeMessage;

public class MatchCreatorInitializer {

  private static final String GAME_STARTED_MESSAGE = "The game has started!";
  private final Map<Session, PlayerConfiguration> playersMap;
  private final MapConfiguration mapConfiguration;
  private final ConflictResolver conflictResolver;
  private MatchCreator matchCreator;

  public MatchCreatorInitializer(Map<Session, PlayerConfiguration> playersMap,
      MapConfiguration mapConfiguration, ConflictResolver conflictResolver) {
    this.playersMap = Objects.requireNonNull(playersMap);
    this.mapConfiguration = Objects.requireNonNull(mapConfiguration);
    this.conflictResolver = Objects.requireNonNull(conflictResolver);
    this.matchCreator = initializeMatchCreator();
  }

  public MatchCreator getMatchCreator() {
    return matchCreator;
  }

  private MatchCreator initializeMatchCreator() {
    MatchStartedListener listener = playersMap -> {
      for (Map.Entry<Session, PlayerController> playerEntry : playersMap.entrySet()) {
        Session user = playerEntry.getKey();
        PlayerController playerController = playerEntry.getValue();
        initializePlayer(user, playerController);
      }
      matchCreator = initializeMatchCreator();
    };
    return new MatchCreator(mapConfiguration, conflictResolver, listener);
  }

  private void initializePlayer(Session user, PlayerController playerController) {
    PlayerConfiguration playerConfiguration = new PlayerConfiguration(
        new Interpreter(playerController), matchCreator);
    playersMap.put(user, playerConfiguration);
    addEventListener(user, playerController);
    sendEventMessageToUser(GameEvent.START_MATCH, GAME_STARTED_MESSAGE, user);
  }

  private void addEventListener(Session user, PlayerController playerController) {
    playerController.addStateListener(new StateListener() {
      @Override
      public void onStateChange(State state, String message) {
        Future<Void> sent = sendEventMessageToUser(state, message, user);
        if (state == State.LOST || state == State.WON) {
          waitForFuture(sent);
          user.close();
        }
      }

      @Override
      public void onGameEvent(GameEvent event, String message) {
        sendEventMessageToUser(event, message, user);
      }
    });
  }

  private void waitForFuture(Future<Void> future) {
    try {
      future.get();
    } catch (ExecutionException executionException) {
      executionException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      // interrupted, proceed normally
    }
  }

  private Future<Void> sendEventMessageToUser(GameEvent gameEvent, String message, Session user) {
    Message eventMessage = new EventMessage(message, gameEvent);
    return user.getRemote().sendStringByFuture(eventMessage.getPayload());
  }

  private Future<Void> sendEventMessageToUser(State state, String message, Session user) {
    Message eventMessage = new StateChangeMessage(message, state);
    return user.getRemote().sendStringByFuture(eventMessage.getPayload());
  }
}
