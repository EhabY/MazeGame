package website.services.fighting;

import mazegame.PlayerController;

public interface ScoreCalculator {

  long calculateScore(PlayerController playerController);
}
