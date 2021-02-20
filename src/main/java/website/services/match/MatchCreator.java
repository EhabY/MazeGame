package website.services.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mapgenerator.MapConfiguration;
import mapgenerator.MapGenerator;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.events.MatchListener;
import mazegame.room.Room;
import parser.GameParser;
import website.services.fighting.ConflictResolver;

public class MatchCreator {

  private final MapConfiguration mapConfiguration;
  private final ConflictResolver conflictResolver;
  private final MatchStartListener matchStartListener;
  private final Set<String> playersInMatch = new HashSet<>();
  private final Set<String> readyPlayers = new HashSet<>();
  private final Map<String, PlayerController> playerControllers = new ConcurrentHashMap<>();
  private boolean gameStarted = false;
  private Match match;
  private String mazeMapJson;

  public MatchCreator(MapConfiguration mapConfiguration,
      ConflictResolver conflictResolver, MatchStartListener matchStartListener) {
    this.mapConfiguration = Objects.requireNonNull(mapConfiguration);
    this.conflictResolver = Objects.requireNonNull(conflictResolver);
    this.matchStartListener = Objects.requireNonNull(matchStartListener);
  }

  public synchronized void addPlayer(String username) {
    if (gameStarted) {
      throw new IllegalStateException("Match already started!");
    } else if (playersInMatch.contains(username)) {
      throw new IllegalArgumentException("Username already used!");
    }

    playersInMatch.add(username);
  }

  public synchronized void makeReady(String username) {
    if (gameStarted) {
      throw new IllegalStateException("Match already started!");
    } else if (!playersInMatch.contains(username)) {
      throw new IllegalArgumentException("Player is not in match!");
    }

    readyPlayers.add(username);
    if (readyPlayers.size() == playersInMatch.size()) {
      startMatch();
    }
  }

  private void startMatch() {
    MazeMap mazeMap = generateRandomMap();
    Collection<PlayerController> playerControllers = createPlayerControllers(mazeMap);
    this.matchStartListener.onMatchStart(playerControllers);
    this.match = new Match(mazeMap, playerControllers, this.conflictResolver);
    this.gameStarted = true;
    playersInMatch.clear();
    readyPlayers.clear();
  }

  private MazeMap generateRandomMap() {
    this.mapConfiguration.setNumberOfPlayers(playersInMatch.size());
    this.mazeMapJson = MapGenerator.generateMap(this.mapConfiguration);
    return GameParser.parseJson(this.mazeMapJson);
  }

  private Collection<PlayerController> createPlayerControllers(MazeMap map) {
    List<Room> startingRooms = new ArrayList<>(map.getStartingRooms());
    for (String username : playersInMatch) {
      Room startingRoom = startingRooms.remove(startingRooms.size() - 1);
      playerControllers.put(username, new PlayerController(username, map, startingRoom));
    }
    addMatchListeners(playerControllers.values());
    return playerControllers.values();
  }

  private void addMatchListeners(Collection<PlayerController> players) {
    for (PlayerController playerController : players) {
      playerController.addMatchListener(new MatchListener() {
        @Override
        public void onMove(Room fromRoom) {
          match.moveFrom(playerController, fromRoom);
        }

        @Override
        public void onQuit() {
          match.kickPlayer(playerController);
        }
      });
    }
  }

  public void removePlayer(String username) {
    if(hasGameStarted()) {
      PlayerController playerController = playerControllers.remove(username);
      match.kickPlayer(playerController);
    } else {
      readyPlayers.remove(username);
      playersInMatch.remove(username);
    }
  }

  public String getMazeMapJson() {
    if(hasGameStarted()) {
      return mazeMapJson;
    }

    throw new IllegalStateException("Match has not started yet!");
  }

  public synchronized boolean hasGameStarted() {
    return gameStarted;
  }
}
