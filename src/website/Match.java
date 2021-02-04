package website;

import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import website.fighting.ConflictResolver;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Match {
    private static final String WON_MATCH_MESSAGE = "Congratulations, YOU WON!";
    private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
    private final Set<PlayerController> players = new HashSet<>();
    private final Map<Room, PlayerController> playerInRoom = new ConcurrentHashMap<>();
    private final Map<Room, Object> locks = new ConcurrentHashMap<>();
    private final MazeMap mazeMap;
    private final ConflictResolver conflictResolver;

    Match(MazeMap mazeMap, Set<PlayerController> players, ConflictResolver conflictResolver) {
        this.mazeMap = Objects.requireNonNull(mazeMap);
        this.players.addAll(players);
        for(Room room : mazeMap.getRooms()) {
            locks.put(room, new Object());
        }

        for(PlayerController player : players) {
            addPlayerToRoom(player);
        }

        this.conflictResolver = Objects.requireNonNull(conflictResolver);
        setMatchTimer(this.mazeMap.getTimeInSeconds() * 1000);
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
            playerInRoom.remove(previousRoom);
        }
    }

    private void addPlayerToRoom(PlayerController playerController) {
        Room room = playerController.getCurrentRoom();
        synchronized (locks.get(room)) {
            if(playerInRoom.containsKey(room)) {
                playerController = getWinner(playerController, playerInRoom.get(room));
            }
            playerInRoom.put(room, playerController);
            notifyIfWon(playerController);
        }
    }

    private PlayerController getWinner(PlayerController playerController1, PlayerController playerController2) {
        PlayerController winner = conflictResolver.resolveConflict(playerController1, playerController2);
        if(winner.equals(playerController1)) {
            notifyPlayerLost(playerController2);
        } else {
            notifyPlayerLost(playerController1);
        }
        return winner;
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

    private void notifyPlayerLost(PlayerController playerController) {
        playerController.lostMatch(LOST_MATCH_MESSAGE);
        players.remove(playerController);
    }

    private boolean hasWon(PlayerController playerController) {
        return playerController.getCurrentRoom().equals(mazeMap.getEndRoom());
    }
}
