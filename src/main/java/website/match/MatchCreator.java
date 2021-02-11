package website.match;

import mapgenerator.MapConfiguration;
import mapgenerator.MapGenerator;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.events.MatchListener;
import mazegame.room.Room;
import org.eclipse.jetty.websocket.api.Session;
import parser.GameParser;
import website.fighting.ConflictResolver;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class MatchCreator {
    private final MapConfiguration mapConfiguration;
    private final ConflictResolver conflictResolver;
    private final MatchStartedListener listener;
    private final Map<String, Session> playersInMatch = new ConcurrentHashMap<>();
    private final Set<String> readyPlayers = new HashSet<>();
    private boolean gameStarted = false;
    private Match match;
    private String mazeMapJson;

    public MatchCreator(MapConfiguration mapConfiguration, ConflictResolver conflictResolver, MatchStartedListener listener) {
        this.mapConfiguration = Objects.requireNonNull(mapConfiguration);
        this.conflictResolver = Objects.requireNonNull(conflictResolver);
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
        MazeMap mazeMap = generateRandomMap();
        Map<Session, PlayerController> playerControllerMap = createPlayerControllers(mazeMap);
        this.listener.onMatchStart(playerControllerMap);
        this.match = new Match(mazeMap, playerControllerMap.values(), this.conflictResolver);
        this.gameStarted = true;
    }

    private MazeMap generateRandomMap() {
        this.mapConfiguration.setNumberOfPlayers(playersInMatch.size());
        this.mazeMapJson = MapGenerator.generateMap(this.mapConfiguration);
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
        addMatchListeners(playerControllerMap.values());
        return playerControllerMap;
    }

    private void addMatchListeners(Collection<PlayerController> players) {
        for(PlayerController playerController : players) {
            playerController.addMatchListener(new MatchListener() {
                @Override
                public void onMove(Room fromRoom) {
                    match.moveFrom(playerController, fromRoom);
                }

                @Override
                public void onQuit() {
                    match.removePlayer(playerController);
                }
            });
        }
    }

    public String getMazeMapJson() {
        return mazeMapJson;
    }
}
