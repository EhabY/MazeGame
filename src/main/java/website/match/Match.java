package website.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.mapsite.Loot;
import mazegame.room.Room;
import website.fighting.ConflictResolver;

public class Match {

  private static final String WON_MATCH_MESSAGE = "Congratulations, YOU WON!";
  private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
  private static final String WON_FIGHT_MESSAGE = "You have won the fight!";
  private final Set<PlayerController> players = new HashSet<>();
  private final Map<Room, PlayerController> roomToPlayerMap = new ConcurrentHashMap<>();
  private final Map<Room, Object> locks = new ConcurrentHashMap<>();
  private final Map<Room, Boolean> inFight = new ConcurrentHashMap<>();
  private final MazeMap mazeMap;
  private final ConflictResolver conflictResolver;

  Match(MazeMap mazeMap, Collection<PlayerController> players, ConflictResolver conflictResolver) {
    this.mazeMap = Objects.requireNonNull(mazeMap);
    this.players.addAll(players);
    for (Room room : mazeMap.getRooms()) {
      locks.put(room, new Object());
      inFight.put(room, false);
    }

    for (PlayerController player : players) {
      addPlayerToRoom(player);
    }

    broadcastPlayerList();
    this.conflictResolver = Objects.requireNonNull(conflictResolver);
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
    roomToPlayerMap.clear();
  }

  void moveFrom(PlayerController playerController, Room previousRoom) {
    removePlayerFromRoom(previousRoom);
    addPlayerToRoom(playerController);
  }

  void kickPlayer(PlayerController playerController) {
    notifyPlayerLost(playerController);
    removePlayerFromMatch(playerController);
    broadcastPlayerList();
  }

  private void removePlayerFromMatch(PlayerController playerController) {
    players.remove(playerController);
    removePlayerFromRoom(playerController.getCurrentRoom());
  }

  private void removePlayerFromRoom(Room previousRoom) {
    synchronized (locks.get(previousRoom)) {
      roomToPlayerMap.remove(previousRoom);
    }
  }

  private void addPlayerToRoom(PlayerController playerController) {
    notifyPlayerIfWaiting(playerController);
    Room room = playerController.getCurrentRoom();
    synchronized (locks.get(room)) {
      if (roomToPlayerMap.containsKey(room)) {
        PlayerController winner = startFight(playerController);
        roomToPlayerMap.put(room, winner);
        broadcastPlayerList();
      } else {
        roomToPlayerMap.put(room, playerController);
      }
      notifyIfWon(playerController);
    }
  }

  private void notifyPlayerIfWaiting(PlayerController playerController) {
    Room room = playerController.getCurrentRoom();
    if(inFight.get(room)) {
      playerController.startFight();
    }
  }

  private PlayerController startFight(PlayerController playerController) {
    Room room = playerController.getCurrentRoom();
    inFight.put(room, true);
    playerController = getWinnerInFight(playerController, roomToPlayerMap.get(room));
    inFight.put(room, false);
    return playerController;
  }

  private PlayerController getWinnerInFight(PlayerController controller1, PlayerController controller2) {
    PlayerController winner = conflictResolver.resolveConflict(controller1, controller2);
    if (winner.equals(controller1)) {
      firstBeatSecond(controller1, controller2);
    } else {
      firstBeatSecond(controller2, controller1);
    }
    return winner;
  }

  private void firstBeatSecond(PlayerController controller1, PlayerController controller2) {
    Loot loot = controller2.getLoot();
    giveLootTo(loot, controller1);
    distributeGold(loot.getGold());
    notifyPlayerWonFight(controller1);
    notifyPlayerLost(controller2);
  }

  private void giveLootTo(Loot loot, PlayerController playerController) {
    int numberOfPlayers = players.size() - 1;
    long remainingGold = getRemainingGold(loot.getGold(), numberOfPlayers);
    Loot winnerShare = new Loot(remainingGold, loot.getItems());
    playerController.addLoot(winnerShare);
  }

  private long getRemainingGold(long gold, int numberOfPlayers) {
    return gold % numberOfPlayers;
  }

  private void distributeGold(long gold) {
    int numberOfPlayers = players.size() - 1;
    long goldPerPlayer = gold / numberOfPlayers;
    for (PlayerController playerController : players) {
      playerController.addGold(goldPerPlayer);
    }
  }

  private void notifyPlayerWonFight(PlayerController playerController) {
    playerController.wonFight(WON_FIGHT_MESSAGE);
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
