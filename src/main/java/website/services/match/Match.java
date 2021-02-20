package website.services.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import website.services.fighting.ConflictResolver;

public class Match {

  private static final String WON_MATCH_MESSAGE = "Congratulations, YOU WON!";
  private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
  private final Set<PlayerController> players = new HashSet<>();
  private final MovementManager movementManager;
  private final MazeMap mazeMap;

  Match(MazeMap mazeMap, Collection<PlayerController> players, ConflictResolver conflictResolver) {
    this.mazeMap = Objects.requireNonNull(mazeMap);
    this.players.addAll(players);
    this.movementManager = new MovementManager(this.players, mazeMap.getRooms(), conflictResolver);
    broadcastPlayerList();
    setMatchTimer(this.mazeMap.getTimeInSeconds() * 1000);
  }

  private void broadcastPlayerList() {
    Collection<String> usernames = getUsernameList();
    for (PlayerController playerController : players) {
      playerController.sendingPlayerList(usernames);
    }
  }

  private Collection<String> getUsernameList() {
    List<String> usernames = new ArrayList<>();
    for (PlayerController playerController : players) {
      usernames.add(playerController.getUsername());
    }
    return usernames;
  }

  private void setMatchTimer(long timeInMilliseconds) {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        notifyAllLost("Timer ran out! " + LOST_MATCH_MESSAGE);
      }
    }, timeInMilliseconds);
  }

  private void notifyAllLost(String message) {
    for (PlayerController playerController : players) {
      playerController.lostMatch(message);
    }
    players.clear();
  }

  void moveFrom(PlayerController playerController, Room previousRoom) {
    movementManager.removePlayerFromRoom(previousRoom);
    boolean refreshPlayerList = movementManager.addPlayerToRoom(playerController);
    if (refreshPlayerList) {
      broadcastPlayerList();
    }
    notifyIfWon(playerController);
  }

  void kickPlayer(PlayerController playerController) {
    dropPlayerLoot(playerController);
    removePlayerFromMatch(playerController);
    broadcastPlayerList();
    notifyPlayerLost(playerController);
  }

  private void dropPlayerLoot(PlayerController playerController) {
    Room room = playerController.getCurrentRoom();
    room.addLoot(playerController.getLoot());
  }

  private void removePlayerFromMatch(PlayerController playerController) {
    players.remove(playerController);
    movementManager.removePlayerFromRoom(playerController.getCurrentRoom());
  }

  private void notifyPlayerLost(PlayerController playerController) {
    playerController.lostMatch(LOST_MATCH_MESSAGE);
  }

  private void notifyIfWon(PlayerController playerController) {
    if (hasWon(playerController)) {
      notifyPlayerWon(playerController);
    }
  }

  private void notifyPlayerWon(PlayerController playerController) {
    playerController.wonMatch(WON_MATCH_MESSAGE);
    removePlayerFromMatch(playerController);
    notifyAllLost(LOST_MATCH_MESSAGE);
  }

  private boolean hasWon(PlayerController playerController) {
    return playerController.getCurrentRoom().equals(mazeMap.getEndRoom());
  }
}
