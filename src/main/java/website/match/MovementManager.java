package website.match;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import website.fighting.ConflictResolver;
import website.fighting.FightManager;

public class MovementManager {

  private final Map<Room, PlayerController> roomToPlayerMap = new ConcurrentHashMap<>();
  private final Map<Room, Object> locks = new ConcurrentHashMap<>();
  private final FightManager fightManager;

  MovementManager(Set<PlayerController> players, Collection<Room> rooms,
      ConflictResolver conflictResolver) {
    this.fightManager = new FightManager(players, rooms, conflictResolver);
    createLocksForRooms(rooms);
    addPlayersToRooms(players);
  }

  private void createLocksForRooms(Collection<Room> rooms) {
    for (Room room : rooms) {
      locks.put(room, new Object());
    }
  }

  private void addPlayersToRooms(Collection<PlayerController> players) {
    for (PlayerController player : players) {
      addPlayerToRoom(player);
    }
  }

  public void removePlayerFromRoom(Room previousRoom) {
    synchronized (locks.get(previousRoom)) {
      roomToPlayerMap.remove(previousRoom);
    }
  }

  public boolean addPlayerToRoom(PlayerController playerController) {
    fightManager.notifyPlayerIfWaiting(playerController);
    Room room = playerController.getCurrentRoom();
    synchronized (locks.get(room)) {
      return putPlayerInMap(playerController);
    }
  }

  private boolean putPlayerInMap(PlayerController playerController) {
    Room room = playerController.getCurrentRoom();
    if (roomToPlayerMap.containsKey(room)) {
      PlayerController winner = fightManager
          .startFightBetween(playerController, roomToPlayerMap.get(room));
      roomToPlayerMap.put(room, winner);
      return true;
    } else {
      roomToPlayerMap.put(room, playerController);
      return false;
    }
  }

}
