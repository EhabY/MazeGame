package website.match;

import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.mapsite.Loot;
import mazegame.room.Room;
import website.fighting.ConflictResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Match {
    private static final String WON_MATCH_MESSAGE = "Congratulations, YOU WON!";
    private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
    private final Set<PlayerController> players = new HashSet<>();
    private final Map<Room, PlayerController> roomToPlayerMap = new ConcurrentHashMap<>();
    private final Map<Room, Object> locks = new ConcurrentHashMap<>();
    private final MazeMap mazeMap;
    private final ConflictResolver conflictResolver;

    Match(MazeMap mazeMap, Collection<PlayerController> players, ConflictResolver conflictResolver) {
        this.mazeMap = Objects.requireNonNull(mazeMap);
        this.players.addAll(players);
        for(Room room : mazeMap.getRooms()) {
            locks.put(room, new Object());
        }

        for(PlayerController player : players) {
            addPlayerToRoom(player);
        }

        broadcastPlayerList();
        this.conflictResolver = Objects.requireNonNull(conflictResolver);
        setMatchTimer(this.mazeMap.getTimeInSeconds() * 1000);
    }

    private void broadcastPlayerList() {
        Collection<String> usernames = getUsernameList();
        for(PlayerController playerController : players) {
            playerController.sendingPlayerList(usernames);
        }
    }

    private Collection<String> getUsernameList() {
        List<String> usernames = new ArrayList<>();
        for(PlayerController playerController : players) {
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
        for(PlayerController playerController : players) {
            playerController.lostMatch(message);
        }
        players.clear();
    }

    void moveFrom(PlayerController playerController, Room previousRoom) {
        removePlayerFromRoom(previousRoom);
        addPlayerToRoom(playerController);
    }

    private void removePlayerFromRoom(Room previousRoom) {
        synchronized (locks.get(previousRoom)) {
            roomToPlayerMap.remove(previousRoom);
        }
    }

    private void addPlayerToRoom(PlayerController playerController) {
        Room room = playerController.getCurrentRoom();
        synchronized (locks.get(room)) {
            if(roomToPlayerMap.containsKey(room)) {
                playerController = getWinner(playerController, roomToPlayerMap.get(room));
            }
            roomToPlayerMap.put(room, playerController);
            notifyIfWon(playerController);
        }
    }

    private PlayerController getWinner(PlayerController playerController1, PlayerController playerController2) {
        PlayerController winner = conflictResolver.resolveConflict(playerController1, playerController2);
        if(winner.equals(playerController1)) {
            firstBeatSecond(playerController1, playerController2);
        } else {
            firstBeatSecond(playerController2, playerController1);
        }
        return winner;
    }

    private void firstBeatSecond(PlayerController playerController1, PlayerController playerController2) {
        notifyPlayerLost(playerController2);
        Loot loot = playerController2.getLoot();
        playerController1.addLoot(new Loot(loot.getGold() % players.size(), loot.getItems()));
        long goldPerPlayer = loot.getGold() / players.size();
        for(PlayerController playerController : players) {
            playerController.addGold(goldPerPlayer);
        }
        broadcastPlayerList();
    }

    private void notifyPlayerLost(PlayerController playerController) {
        playerController.lostMatch(LOST_MATCH_MESSAGE);
        players.remove(playerController);
    }

    private void notifyIfWon(PlayerController playerController) {
        if(hasWon(playerController)) {
            notifyPlayerWon(playerController);
        }
    }

    private void notifyPlayerWon(PlayerController playerController) {
        playerController.wonMatch(WON_MATCH_MESSAGE);
        players.remove(playerController);
        notifyAllLost(LOST_MATCH_MESSAGE);
    }

    private boolean hasWon(PlayerController playerController) {
        return playerController.getCurrentRoom().equals(mazeMap.getEndRoom());
    }
}
