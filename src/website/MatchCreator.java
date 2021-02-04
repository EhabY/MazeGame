package website;

import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.room.Room;
import website.tiebreakers.RockPaperScissors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MatchCreator {
    private final MazeMap mazeMap;
    private final List<Room> rooms;
    private final Set<PlayerController> playersInMatch = new HashSet<>();
    private final Set<PlayerController> readyPlayers = new HashSet<>();
    private boolean gameStarted = false;
    private Match match;

    MatchCreator(MazeMap mazeMap) {
        this.mazeMap = Objects.requireNonNull(mazeMap);
        rooms = new ArrayList<>(this.mazeMap.getRooms());
    }

    synchronized PlayerController addPlayer(String username) {
        if(gameStarted) {
            throw new IllegalStateException("Match already started!");
        } else if(rooms.size() == 0) {
            throw new IllegalStateException("Match is full!");
        }

        PlayerController playerController = new PlayerController(username, mazeMap, getStartRoom());
        playersInMatch.add(playerController);

        return playerController;
    }

    synchronized boolean cannotAddPlayers() {
        return gameStarted || rooms.size() == 0;
    }

    synchronized void makeReady(PlayerController playerController) {
        if(!playersInMatch.contains(playerController)) {
            throw new IllegalArgumentException("Player is not in match!");
        }

        readyPlayers.add(playerController);
        if(readyPlayers.size() == playersInMatch.size()) {
            gameStarted = true;
            match = new Match(mazeMap, playersInMatch, new RockPaperScissors());

            for(PlayerController player : readyPlayers) {
                player.addMoveListener(fromRoom -> match.moveFrom(player, fromRoom));
            }
        }
    }

    private Room getStartRoom() {
        return rooms.remove(rooms.size() - 1);
    }

    boolean hasGameStarted() {
        return gameStarted;
    }
}
