package website;

import mapgenerator.MapConfiguration;
import mazegame.Response;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import website.fighting.ConflictResolver;
import website.match.MatchCreator;
import website.match.MatchCreatorInitializer;
import website.match.PlayerConfiguration;
import website.message.InvalidMessage;
import website.message.MapMessage;
import website.message.Message;
import website.message.ResponseMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler {
    private final Map<Session, String> usernames = new ConcurrentHashMap<>();
    private final Map<Session, PlayerConfiguration> players = new ConcurrentHashMap<>();
    private final MatchCreatorInitializer matchCreatorInitializer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MessageHandler(MapConfiguration mapConfiguration, ConflictResolver conflictResolver) {
        this.matchCreatorInitializer = new MatchCreatorInitializer(players, mapConfiguration, conflictResolver);
    }

    public Message getResponseFromMessage(Session user, String messageAsJson) {
        JSONObject messageJson = new JSONObject(messageAsJson);
        String type = messageJson.getString("type");
        String content = messageJson.getString("content");

        Message response;
        if(type.equalsIgnoreCase("username")) {
            response = new ResponseMessage(initializePlayer(user, content), content);
        } else if(type.equalsIgnoreCase("ready")) {
            response = new ResponseMessage(makePlayerReady(user), content);
        } else if(type.equalsIgnoreCase("command")) {
            response = new ResponseMessage(executeCommand(user, content), content);
        } else if(type.equalsIgnoreCase("map")) {
            response = new MapMessage(getMazeMapJson(user));
        } else {
            response = new InvalidMessage("Unknown message type!");
        }

        return response;
    }

    private Response initializePlayer(Session user, String username) {
        String message;
        if(isNewPlayer(user)) {
            MatchCreator matchCreator = matchCreatorInitializer.getMatchCreator();
            matchCreator.addPlayer(username, user);
            usernames.put(user, username);
            message = "Added " + username + " to the match!";
        } else {
            message = "Player is already initialized!";
        }
        return new Response(message);
    }

    private Response makePlayerReady(Session user) {
        if(isNewPlayer(user)) {
            return new Response("Player has not registered yet!");
        }
        String username = usernames.get(user);
        MatchCreator matchCreator = matchCreatorInitializer.getMatchCreator();
        executor.execute(() -> matchCreator.makeReady(username));
        return new Response(username + " is ready!");
    }

    private boolean isNewPlayer(Session user) {
        return !usernames.containsKey(user);
    }

    private Response executeCommand(Session user, String command) {
        if(isUserPlaying(user)) {
            return players.get(user).executeCommand(command);
        } else {
            return new Response(getPlayerWaitingStatus(user));
        }
    }

    private boolean isUserPlaying(Session user) {
        return players.containsKey(user);
    }

    private String getPlayerWaitingStatus(Session user) {
        if(isNewPlayer(user)) {
            return "Player has not registered yet!";
        } else {
            return "Match has not started yet!";
        }
    }

    private String getMazeMapJson(Session user) {
        if(isUserPlaying(user)) {
            return players.get(user).getMazeMapJson();
        } else {
            return getPlayerWaitingStatus(user);
        }
    }

    public void removeUser(Session user) {
        players.remove(user);
        usernames.remove(user);
    }
}
