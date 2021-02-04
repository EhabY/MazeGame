package mazegame.player;

import mazegame.PlayerController;

public interface ScoreCalculator {
    long calculateScore(PlayerController playerController);
}
