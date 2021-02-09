package website;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import website.match.Interpreter;
import website.match.MatchCreator;
import website.match.MatchCreatorInitializer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBroker {
    private final Map<Session, String> usernames = new ConcurrentHashMap<>();
    private final Map<Session, Interpreter> players = new ConcurrentHashMap<>();
    MatchCreator matchCreator = MatchCreatorInitializer.getMatchCreator(players);

    public String handleJsonMessage(Session user, String messageAsJson) {
        JSONObject messageJson = new JSONObject(messageAsJson);
        String type = messageJson.getString("type");
        String message = messageJson.getString("message");

        String response;
        if(type.equalsIgnoreCase("username")) {
            response = initializePlayer(user, message);
        } else if(type.equalsIgnoreCase("ready")) {
            response = makePlayerReady(user);
        } else if(type.equalsIgnoreCase("command")) {
            response = executeCommand(user, message);
        } else {
            response = "Unknown message type!";
        }

        return response;
    }

    private String initializePlayer(Session user, String username) {
        if(isNewPlayer(user)) {
            matchCreator.addPlayer(username, user);
            usernames.put(user, username);
            return "Added " + username + " to the match!";
        } else {
            return "Player is already initialized!";
        }
    }

    private String makePlayerReady(Session user) {
        if(isNewPlayer(user)) {
            return "Player has not registered yet!";
        }
        String username = usernames.get(user);
        matchCreator.makeReady(username);
        return username + " is ready!";
    }

    private boolean isNewPlayer(Session user) {
        return !usernames.containsKey(user);
    }

    private String executeCommand(Session user, String command) {
        if(isUserInGame(user)) {
            return players.get(user).execute(command);
        } else {
            return "Player is not in the match!";
        }
    }

    private boolean isUserInGame(Session user) {
        return players.containsKey(user);
    }

    public void removeUser(Session user) {
        players.remove(user);
        usernames.remove(user);
    }
}
