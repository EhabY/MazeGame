package website;

import mazegame.events.GameEvent;
import mazegame.PlayerController;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

@WebSocket
public class MatchWebSocketHandler {
    private final Map<Session, Interpreter> players = new ConcurrentHashMap<>();
    private final MatchCreator matchCreator = new MatchCreator(Main.map);

    @OnWebSocketConnect
    public void onConnect(Session user) {

    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        players.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {
        if(!players.containsKey(user)) {
            initializePlayer(user, message);
            user.getRemote().sendStringByFuture("Added user " + message + " to the match!");
        } else if(!matchCreator.hasGameStarted()) {
            matchCreator.makeReady(players.get(user).getPlayerController());
            user.getRemote().sendStringByFuture("User is ready!");
        } else if(players.get(user).isGameOnGoing()) {
            String response = players.get(user).execute(message);
            user.getRemote().sendStringByFuture(response);
        }
    }

    private void initializePlayer(Session user, String username) {
        PlayerController playerController = matchCreator.addPlayer(username);
        playerController.addListener((event, message) -> {
            user.getRemote().sendStringByFuture(event.name() + ": " + message);
            if(event == GameEvent.LOST_MATCH || event == GameEvent.WON_MATCH) {
                user.close();
            }
        });
        Interpreter interpreter = new Interpreter(playerController);
        players.put(user, interpreter);
    }
}
