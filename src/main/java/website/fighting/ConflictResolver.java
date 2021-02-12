package website.fighting;

import mazegame.PlayerController;
import mazegame.player.ScoreCalculator;
import java.util.Objects;

public class ConflictResolver {
    private final ScoreCalculator scoreCalculator;
    private final TieBreaker tieBreaker;

    public ConflictResolver(ScoreCalculator scoreCalculator, TieBreaker tieBreaker) {
        this.scoreCalculator = Objects.requireNonNull(scoreCalculator);
        this.tieBreaker = Objects.requireNonNull(tieBreaker);
    }

    public PlayerController resolveConflict(PlayerController playerController1, PlayerController playerController2) {
        playerController1.startFight();
        playerController2.startFight();
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
}
