package website;

import mazegame.MazeMap;
import mazegame.PlayerController;
import mazegame.player.ScoreCalculator;
import mazegame.room.Room;
import website.fighting.TieBreaker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Match {
    private static final String START_FIGHT_MESSAGE = "A fight has commenced";
    private static final String WON_FIGHT_MESSAGE = "You have won the fight!";
    private static final String LOST_FIGHT_MESSAGE = "You have lost the fight :(!";
    private static final String WON_MATCH_MESSAGE = "Congratulations, YOU WON!";
    private static final String LOST_MATCH_MESSAGE = "Sadly, you lost the match :(!";
    private final Set<PlayerController> players = new HashSet<>();
    private final Map<Room, PlayerController> playerInRoom = new ConcurrentHashMap<>();
    private final Map<Room, Object> locks = new ConcurrentHashMap<>();
    private final MazeMap mazeMap;
    private final TieBreaker tieBreaker;
    private final ScoreCalculator scoreCalculator;

    Match(MazeMap mazeMap, Set<PlayerController> players, TieBreaker tieBreaker, ScoreCalculator scoreCalculator) {
        this.mazeMap = Objects.requireNonNull(mazeMap);
        this.players.addAll(players);
        for(Room room : mazeMap.getRooms()) {
            locks.put(room, new Object());
        }

        for(PlayerController player : players) {
            addPlayerToRoom(player);
        }

        this.tieBreaker = Objects.requireNonNull(tieBreaker);
        this.scoreCalculator = Objects.requireNonNull(scoreCalculator);
        setMatchTimer(this.mazeMap.getTimeInSeconds() * 1000);
    }

    private void setMatchTimer(long timeInMilliseconds) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(PlayerController playerController : players) {
                    playerController.lostMatch("Timer ran out! " + LOST_MATCH_MESSAGE);
                }
            }
        }, timeInMilliseconds);
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
                resolveConflict(playerInRoom.get(room), playerController);
            } else {
                playerInRoom.put(room, playerController);
            }

            notifyIfWon(playerController);
        }
    }

    private void resolveConflict(PlayerController playerController1, PlayerController playerController2) {
        PlayerController winner = determineWinner(playerController1, playerController2);
        playerInRoom.put(winner.getCurrentRoom(), winner);

        if(winner.equals(playerController1)) {
            firstBeatSecond(playerController1, playerController2);
        } else {
            firstBeatSecond(playerController2, playerController1);
        }
    }

    private PlayerController determineWinner(PlayerController playerController1, PlayerController playerController2) {
        playerController1.startFight(START_FIGHT_MESSAGE);
        playerController2.startFight(START_FIGHT_MESSAGE);
        long score1 = scoreCalculator.calculateScore(playerController1);
        long score2 = scoreCalculator.calculateScore(playerController2);
        if(score1 > score2) {
            return playerController1;
        } else if(score1 < score2) {
            return playerController2;
        } else {
            return tieBreaker.breakTie(playerController1, playerController2);
        }
    }

    private void firstBeatSecond(PlayerController playerController1, PlayerController playerController2) {
        playerController1.addLoot(playerController2.getLoot());
        playerController1.wonFight(WON_FIGHT_MESSAGE);
        playerController2.lostFight(LOST_FIGHT_MESSAGE);
        playerController2.lostMatch(LOST_MATCH_MESSAGE);
        players.remove(playerController2);
    }

    private void notifyIfWon(PlayerController playerController) {
        if(hasWon(playerController)) {
            playerController.wonMatch(WON_MATCH_MESSAGE);
            for(PlayerController otherPlayerController : players) {
                if(!otherPlayerController.equals(playerController)) {
                    otherPlayerController.lostMatch(LOST_MATCH_MESSAGE);
                }
            }
        }
    }

    private boolean hasWon(PlayerController playerController) {
        return playerController.getCurrentRoom().equals(mazeMap.getEndRoom());
    }
}
