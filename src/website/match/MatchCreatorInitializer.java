package website.match;

import mazegame.PlayerController;
import mazegame.events.GameEvent;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MatchCreatorInitializer {
    private static final String GAME_STARTED_MESSAGE = "The game has started!";
    private final Map<Session, Interpreter> playersMap;
    private MatchCreator matchCreator;

    private MatchCreatorInitializer(Map<Session, Interpreter> playersMap) {
        this.playersMap = Objects.requireNonNull(playersMap);
        this.matchCreator = initializeMatchCreator();
    }

    public static MatchCreator getMatchCreator(Map<Session, Interpreter> playersMap) {
        MatchCreatorInitializer matchCreatorInitializer = new MatchCreatorInitializer(playersMap);
        return matchCreatorInitializer.matchCreator;
    }

    private MatchCreator initializeMatchCreator() {
        return new MatchCreator(playersMap -> {
            for(Map.Entry<Session, PlayerController> playerEntry : playersMap.entrySet()) {
                Session user = playerEntry.getKey();
                PlayerController playerController = playerEntry.getValue();
                initializePlayer(user, playerController);
            }
            matchCreator = initializeMatchCreator();
        });
    }

    private void initializePlayer(Session user, PlayerController playerController) {
        playersMap.put(user, new Interpreter(playerController));
        addEventListener(user, playerController);
        sendMessageToUser(GAME_STARTED_MESSAGE, user);
    }

    private void addEventListener(Session user, PlayerController playerController) {
        playerController.addListener((event, message) -> {
            Future<Void> sent = sendMessageToUser(event.name() + ": " + message, user);
            if(event == GameEvent.LOST_MATCH || event == GameEvent.WON_MATCH) {
                waitForFuture(sent);
                user.close();
            }
        });
    }

    private void waitForFuture(Future<Void> future) {
        try {
            future.get();
        } catch(ExecutionException executionException) {
            executionException.printStackTrace();
        } catch(InterruptedException interruptedException) {
            // interrupted, proceed normally
        }
    }

    private Future<Void> sendMessageToUser(String message, Session user) {
        return user.getRemote().sendStringByFuture(message);
    }
}
