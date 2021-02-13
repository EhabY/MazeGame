package website.fighting;

import mazegame.PlayerController;

public interface ScoreCalculator {
    long calculateScore(PlayerController playerController);
}
