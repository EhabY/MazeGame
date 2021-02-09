package website.fighting;

import mazegame.PlayerController;
import mazegame.player.ScoreCalculator;
import java.util.Objects;

public class ConflictResolver {
    private static final String START_FIGHT_MESSAGE = "A fight has commenced";
    private static final String WON_FIGHT_MESSAGE = "You have won the fight!";
    private static final String LOST_FIGHT_MESSAGE = "You have lost the fight :(!";
    private final ScoreCalculator scoreCalculator;
    private final TieBreaker tieBreaker;

    public ConflictResolver(ScoreCalculator scoreCalculator, TieBreaker tieBreaker) {
        this.scoreCalculator = Objects.requireNonNull(scoreCalculator);
        this.tieBreaker = Objects.requireNonNull(tieBreaker);
    }

    public PlayerController resolveConflict(PlayerController playerController1, PlayerController playerController2) {
        PlayerController winner = determineWinner(playerController1, playerController2);

        if(winner.equals(playerController1)) {
            firstBeatSecond(playerController1, playerController2);
        } else {
            firstBeatSecond(playerController2, playerController1);
        }

        return winner;
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
        playerController1.wonFight(WON_FIGHT_MESSAGE);
        playerController2.lostFight(LOST_FIGHT_MESSAGE);
    }
}
