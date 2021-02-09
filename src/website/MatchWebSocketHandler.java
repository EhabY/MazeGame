package website;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import java.io.IOException;

@WebSocket
public class MatchWebSocketHandler {
    MessageBroker messageBroker = new MessageBroker();

    @OnWebSocketConnect
    public void onConnect(Session user) {}

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        messageBroker.removeUser(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        String response = messageBroker.handleJsonMessage(user, message);
        sendMessageToUser(response, user);
    }

    private void sendMessageToUser(String message, Session user) {
        user.getRemote().sendStringByFuture(message);
    }
}
