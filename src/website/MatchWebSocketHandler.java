package website;

import mazegame.PlayerController;
import mazegame.events.GameEvent;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import website.match.MatchCreator;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@WebSocket
public class MatchWebSocketHandler {
    private final Map<Session, String> usernames = new ConcurrentHashMap<>();
    private final Map<Session, Interpreter> players = new ConcurrentHashMap<>();
    private MatchCreator matchCreator = initializeMatchCreator();

    @OnWebSocketConnect
    public void onConnect(Session user) {}

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
        players.put(user, new Interpreter(playerController));
        addEventListener(user, playerController);
        sendMessageToUser("The game has started!", user);
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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        players.remove(user);
        usernames.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {
        String response;
        if(isNewPlayer(user)) {
            initializePlayer(message, user);
            response = "Added user " + message + " to the match!";
        } else if(isUserInGame(user)) {
            response = players.get(user).execute(message);
        } else {
            matchCreator.makeReady(usernames.get(user));
            response = "User is ready!";
        }
        sendMessageToUser(response, user);
    }

    private boolean isNewPlayer(Session user) {
        return !usernames.containsKey(user);
    }

    private void initializePlayer(String username, Session session) {
        matchCreator.addPlayer(username, session);
        usernames.put(session, username);
    }

    private boolean isUserInGame(Session user) {
        return players.containsKey(user);
    }

    private Future<Void> sendMessageToUser(String message, Session user) {
        return user.getRemote().sendStringByFuture(message);
    }
}
