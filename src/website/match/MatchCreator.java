package website.match;

import mapgenerator.MapGenerator;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import org.eclipse.jetty.websocket.api.Session;
import parser.GameParser;
import website.fighting.ConflictResolver;
import website.fighting.RockPaperScissors;
import website.fighting.SimpleScoreCalculator;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class MatchCreator {
    private static final int LEVELS_IN_MAP = 2;
    private final MatchStartedListener listener;
    private final Map<String, Session> playersInMatch = new ConcurrentHashMap<>();
    private final Set<String> readyPlayers = new HashSet<>();
    private boolean gameStarted = false;
    private Match match;
    private String mazeMapJson;

    public MatchCreator(MatchStartedListener listener) {
        this.listener = Objects.requireNonNull(listener);
    }

    public synchronized void addPlayer(String username, Session session) {
        if(gameStarted) {
            throw new IllegalStateException("Match already started!");
        } else if(playersInMatch.containsKey(username)) {
            throw new IllegalArgumentException("Username already used!");
        }

        playersInMatch.put(username, session);
    }

    public synchronized void makeReady(String username) {
        if(!playersInMatch.containsKey(username)) {
            throw new IllegalArgumentException("Player is not in match!");
        }

        readyPlayers.add(username);
        if(readyPlayers.size() == playersInMatch.size()) {
            startMatch();
        }
    }

    private void startMatch() {
        ConflictResolver conflictResolver = new ConflictResolver(new SimpleScoreCalculator(), new RockPaperScissors());
        MazeMap mazeMap = generateRandomMap();
        Map<Session, PlayerController> playerControllerMap = createPlayerControllers(mazeMap);
        this.listener.matchStarted(playerControllerMap);
        this.match = new Match(mazeMap, playerControllerMap.values(), conflictResolver);
        this.gameStarted = true;
    }

    private MazeMap generateRandomMap() {
        this.mazeMapJson = MapGenerator.generateMap(playersInMatch.size(), LEVELS_IN_MAP);
        return GameParser.parseJson(this.mazeMapJson);
    }

    private Map<Session, PlayerController> createPlayerControllers(MazeMap map) {
        Queue<Room> startingRooms = new ArrayDeque<>(map.getStartingRooms());
        Map<Session, PlayerController> playerControllerMap = new ConcurrentHashMap<>();
        for(Map.Entry<String, Session> playerEntry : playersInMatch.entrySet()) {
            Room startingRoom = startingRooms.remove();
            String username = playerEntry.getKey();
            Session session = playerEntry.getValue();
            playerControllerMap.put(session, new PlayerController(username, map, startingRoom));
        }
        addMoveListeners(playerControllerMap.values());
        return playerControllerMap;
    }

    private void addMoveListeners(Collection<PlayerController> players) {
        for(PlayerController player : players) {
            player.addMoveListener(fromRoom -> match.moveFrom(player, fromRoom));
        }
    }

    public String getMazeMapJson() {
        return mazeMapJson;
    }
}
