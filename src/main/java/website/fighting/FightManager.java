package website.fighting;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mazegame.PlayerController;
import mazegame.mapsite.Loot;
import mazegame.room.Room;

public class FightManager {

  private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
  private static final String WON_FIGHT_MESSAGE = "You have won the fight!";
  private final Map<Room, Boolean> inFight = new ConcurrentHashMap<>();
  private final Set<PlayerController> players;
  private final ConflictResolver conflictResolver;

  public FightManager(Set<PlayerController> players, ConflictResolver conflictResolver) {
    this.players = Objects.requireNonNull(players);
    this.conflictResolver = Objects.requireNonNull(conflictResolver);
  }

  public void notifyPlayerIfWaiting(PlayerController playerController) {
    Room room = playerController.getCurrentRoom();
    if (inFight.get(room)) {
      playerController.startFight();
    }
  }

  public PlayerController startFightBetween(PlayerController controller1,
      PlayerController controller2) {
    Room room = controller1.getCurrentRoom();
    inFight.put(room, true);
    PlayerController winner = getWinnerInFight(controller1, controller2);
    inFight.put(room, false);
    return winner;
  }

  private PlayerController getWinnerInFight(PlayerController controller1,
      PlayerController controller2) {
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
    players.remove(controller2);
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
}
