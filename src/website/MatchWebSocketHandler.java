package website;

import mapgenerator.DefaultMapConfiguration;
import mapgenerator.MapConfiguration;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import website.fighting.ConflictResolver;
import website.fighting.RockPaperScissors;
import website.fighting.SimpleScoreCalculator;
import website.message.Message;

@WebSocket
public class MatchWebSocketHandler {
    private final MessageHandler messageHandler;

    public MatchWebSocketHandler() {
        MapConfiguration mapConfiguration = new DefaultMapConfiguration.Builder().build();
        ConflictResolver conflictResolver = new ConflictResolver(new SimpleScoreCalculator(), new RockPaperScissors());
        this.messageHandler = new MessageHandler(mapConfiguration, conflictResolver);
    }

    @OnWebSocketConnect
    public void onConnect(Session user) {}

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        messageHandler.removeUser(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        Message response = messageHandler.getResponseFromMessage(user, message);
        sendMessageToUser(response.getPayload(), user);
    }

    private void sendMessageToUser(String response, Session user) {
        user.getRemote().sendStringByFuture(response);
    }
}
