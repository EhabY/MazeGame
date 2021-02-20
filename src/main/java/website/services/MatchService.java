package website.services;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mapgenerator.DefaultMapConfiguration;
import mapgenerator.MapConfiguration;
import mazegame.PlayerController;
import mazegame.Response;
import mazegame.events.GameEvent;
import mazegame.events.State;
import mazegame.events.StateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import website.payload.response.EventMessage;
import website.payload.response.StateChangeMessage;
import website.services.fighting.ConflictResolver;
import website.services.fighting.RockPaperScissors;
import website.services.fighting.SimpleScoreCalculator;
import website.services.match.MatchCreator;
import website.services.match.MatchStartListener;
import website.services.player.Interpreter;
import website.services.player.PlayerConfig;

@Service
public class MatchService {

  private final Map<String, PlayerConfig> playerMap = new ConcurrentHashMap<>();
  private final Map<String, Interpreter> playerInterpreterMap = new ConcurrentHashMap<>();
  private final MapConfiguration mapConfiguration;
  private final ConflictResolver conflictResolver;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private MatchCreator matchCreator;
  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  public MatchService() {
    this(new DefaultMapConfiguration.Builder().build(),
        new ConflictResolver(new SimpleScoreCalculator(), new RockPaperScissors()));
  }

  public MatchService(MapConfiguration mapConfiguration, ConflictResolver conflictResolver) {
    this.mapConfiguration = Objects.requireNonNull(mapConfiguration);
    this.conflictResolver = Objects.requireNonNull(conflictResolver);
  }

  public String joinMatch(String username) {
    if (isNewPlayer(username)) {
      addPlayerToMatch(username);
      return "Added " + username + " to the match!";
    } else {
      return "Player is already initialized!";
    }
  }

  private boolean isNewPlayer(String username) {
    return !playerMap.containsKey(username);
  }

  private void addPlayerToMatch(String username) {
    MatchCreator matchCreator = createIfNullOrFull();
    playerMap.put(username, new PlayerConfig(username, matchCreator));
    matchCreator.addPlayer(username);
  }

  private MatchCreator createIfNullOrFull() {
    if (matchCreator == null || matchCreator.hasGameStarted()) {
      matchCreator = new MatchCreator(mapConfiguration, conflictResolver, getMatchStartListener());
    }
    return matchCreator;
  }

  private MatchStartListener getMatchStartListener() {
    return playerControllers -> {
      for (PlayerController playerController : playerControllers) {
        playerInterpreterMap.put(playerController.getUsername(),
            new Interpreter(playerController));
        addStateListeners(playerController);
        sendEventToUser(playerController.getUsername(),
            new EventMessage("The match has started", GameEvent.START_MATCH));
      }
    };
  }

  private void addStateListeners(PlayerController playerController) {
    playerController.addStateListener(new StateListener() {
      @Override
      public void onStateChange(State state, String message) {
        String username = playerController.getUsername();
        sendStateToUser(username, new StateChangeMessage(message, state));
        if (state == State.LOST || state == State.WON) {
          playerMap.remove(username);
          playerInterpreterMap.remove(username);
        }
      }

      @Override
      public void onGameEvent(GameEvent event, String message) {
        sendEventToUser(playerController.getUsername(), new EventMessage(message, event));
      }
    });
  }

  private void sendEventToUser(String user, Object payload) {
    simpMessagingTemplate.convertAndSendToUser(user, "/queue/match/event", payload);
  }

  private void sendStateToUser(String user, Object payload) {
    simpMessagingTemplate.convertAndSendToUser(user, "/queue/match/state", payload);
  }

  public String makePlayerReady(String username) {
    if (isNewPlayer(username)) {
      return "Player has not registered yet!";
    }
    executor.execute(() -> playerMap.get(username).makePlayerReady());
    return username + " is ready to play!";
  }

  public Response executeCommand(String username, String command) {
    if (isUserPlaying(username)) {
      return playerInterpreterMap.get(username).execute(command);
    } else {
      return new Response(getPlayerWaitingStatus(username));
    }
  }

  public String getMazeMapJson(String username) {
    if (isUserPlaying(username)) {
      return playerMap.get(username).getMazeMapJson();
    } else {
      return getPlayerWaitingStatus(username);
    }
  }

  private boolean isUserPlaying(String username) {
    return playerInterpreterMap.containsKey(username);
  }

  private String getPlayerWaitingStatus(String username) {
    if (isNewPlayer(username)) {
      return "Player has not registered yet!";
    } else {
      return "Match has not started yet!";
    }
  }

  public void removePlayer(String username) {
    if (playerMap.containsKey(username)) {
      PlayerConfig playerConfig = playerMap.remove(username);
      playerConfig.removePlayer();
    }
    playerInterpreterMap.remove(username);
  }

}
